package me.roundaround.armorstands.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import me.roundaround.armorstands.client.ClientSideConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.Rotations;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;

/**
 * Client-only "mannequin mode" prototype renderer. Draws an armor stand as a player skin instead of
 * the vanilla stick model, but only when the stand has a custom name (e.g. from a nametag) — the
 * custom name is used as the skin username. Unnamed stands render as the normal stick model.
 *
 * <p>Simplified prototype: no synched data, no server profile resolution, no editor or packets. The
 * skin is resolved + downloaded entirely client-side, keyed by the stand's custom name.
 *
 * <p>Because cancelling the vanilla submit drops {@code ArmorStandRenderer}'s render layers, this
 * helper re-creates them on the player model: body skin + cape + the mannequin idle arm-bob, plus
 * armor and elytra (real {@link HumanoidArmorLayer}/{@link WingsLayer}) and held + head items
 * (replicated from {@code ItemInHandLayer}/{@code CustomHeadLayer}, since their resolved
 * {@link ItemStackRenderState}s live on the incoming render state and cannot be copied).
 */
public final class MannequinRenderer {
  private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

  /** Skin re-resolution cadence (wall-clock millis): the auto-refresh interval. */
  private static final long SKIN_REFRESH_MS = 15L * 60L * 1000L; // 15 minutes

  // Cached name -> unresolved profile, so the per-frame skin lookup reuses a stable profile object.
  // Replaceable (not pure computeIfAbsent): the auto-refresh periodically swaps in a fresh profile
  // object to force a vanilla-cache miss and re-resolution (see resolveSkin).
  private static final Map<String, ResolvableProfile> profilesByName = new HashMap<>();

  // Last successfully-loaded downloaded skin per username. Returned whenever the live resolve is
  // unavailable (still downloading after a scheduled or forced refresh, or a transient outage) so
  // neither the in-world stand nor the edit-screen preview blinks to the stick model. Keyed by name
  // because the skin is a function of the name, not the entity, so two stands sharing a name share it.
  private static final Map<String, PlayerSkin> lastGoodSkinsByName = new HashMap<>();

  // Per-UUID wall-clock timestamps of last skin re-fetch, drives the 15-minute refresh cadence.
  private static final Map<UUID, Long> lastRefreshMillis = new HashMap<>();

  // Usernames whose profilesByName entry is a fully-resolved profile already persisted to the disk
  // cache. Lets us pin + persist a resolved skin exactly once per (initial | post-refresh) resolution
  // rather than every frame; a refresh swap drops the name so the next resolve re-pins the new skin.
  private static final Set<String> resolvedNames = new HashSet<>();
  // Whether profilesByName has been seeded from the persistent skin cache yet (done once, lazily).
  private static boolean skinStoreLoaded = false;

  // Captured from ArmorStandRenderer's constructor (see ArmorStandMannequinRendererMixin); it is the
  // only layer dependency not reachable from Minecraft.getInstance(). Null until first construction.
  private static EquipmentLayerRenderer equipmentRenderer = null;

  // Slim-dependent models/layers, cached per arm width: index 0 = wide, index 1 = slim. Both arm
  // widths are kept resident simultaneously so a scene mixing slim and wide mannequins never rebakes
  // per frame (each variant is built once, on first use, and reused thereafter).
  private static final SlimVariant[] variants = new SlimVariant[2];
  // Slim-independent, shared singletons: cape geometry is identical for both arm widths, and the
  // skull models have nothing to do with arm width.
  private static MannequinCapeModel capeModel = null;
  private static Function<SkullBlock.Type, SkullModelBase> skullModels = null;

  private MannequinRenderer() {
  }

  /** Cached slim-dependent bakes for one arm width (wide or slim). See {@link #ensureModels}. */
  private record SlimVariant(
      MannequinPlayerModel bodyModel,
      HumanoidArmorLayer<AvatarRenderState, PlayerModel, PlayerModel> armorLayer,
      WingsLayer<AvatarRenderState, PlayerModel> wingsLayer
  ) {
  }

  /** Captures the equipment-layer renderer from the armor stand renderer's constructor. */
  public static void setEquipmentRenderer(EquipmentLayerRenderer renderer) {
    equipmentRenderer = renderer;
  }

  /**
   * Attempts to draw the armor stand as the hardcoded player skin (with cape, idle bob, armor,
   * elytra, and held/head items). Returns {@code true} if the mannequin was submitted (and the
   * caller should cancel the vanilla submit); returns {@code false} if the skin is not yet
   * available or the lookup failed (caller should let vanilla draw the stick model).
   */
  public static boolean tryRenderMannequin(
      ArmorStandRenderState source,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector
  ) {
    PlayerSkin skin = mannequinSkin(source);
    if (skin == null) {
      return false;
    }

    MannequinSettings settings = ((MannequinSettingsHolder) source).armorstands$getMannequinSettings();

    // Arm-width override: when slim is forced, substitute a synthetic skin reporting SLIM so both
    // the baked model choice (below) and PlayerModel#setupAnim's slim hand offset agree. When not
    // overridden, follow the skin's natural model type.
    if (settings.slim() && skin.model() != PlayerModelType.SLIM) {
      skin = new PlayerSkin(skin.body(), skin.cape(), skin.elytra(), PlayerModelType.SLIM, skin.secure());
    }

    boolean slim = skin.model() == PlayerModelType.SLIM;
    SlimVariant variant = ensureModels(slim);
    MannequinPlayerModel model = variant.bodyModel();

    // Per-stand render state: submitModel DEFERS setupAnim to render time, so a single shared state
    // instance would make every stand render with the last-submitted stand's pose + age. Allocate a
    // fresh one each call so each stand keeps its own pose, idle phase, and equipment.
    MannequinRenderState state = new MannequinRenderState();
    state.skin = skin;
    // ageInTicks (inherited from the stand's render state as tickCount + partialTick) drives the
    // mannequin-style idle arm bob in MannequinPlayerModel#setupAnim.
    state.ageInTicks = source.ageInTicks;
    state.outlineColor = source.outlineColor;
    state.headPose = source.headPose;
    state.bodyPose = source.bodyPose;
    state.leftArmPose = source.leftArmPose;
    state.rightArmPose = source.rightArmPose;
    state.leftLegPose = source.leftLegPose;
    state.rightLegPose = source.rightLegPose;
    // Armor/elytra read these copyable ItemStack fields (held/head item ItemStackRenderStates are
    // final and uncopyable, so those layers read the incoming `source` directly instead).
    state.headEquipment = source.headEquipment;
    state.chestEquipment = source.chestEquipment;
    state.legsEquipment = source.legsEquipment;
    state.feetEquipment = source.feetEquipment;
    // Tag as an armor stand so HumanoidArmorLayer uses the HUMANOID equipment layer. A small stand
    // reports isBaby() == isSmall(); without this the layer would pick HUMANOID_BABY, which player
    // armor assets don't define, hiding all armor. We handle small via a uniform scale below, so
    // isBaby is deliberately left false.
    state.entityType = EntityType.ARMOR_STAND;

    // Skin-layer toggles: read by PlayerModel#setupAnim (hat/jacket/sleeves/pants) at render time,
    // and by the inline cape submit below (showCape). animationsEnabled gates the idle arm bob.
    state.showHat = settings.showHat();
    state.showJacket = settings.showJacket();
    state.showLeftSleeve = settings.showLeftSleeve();
    state.showRightSleeve = settings.showRightSleeve();
    state.showLeftPants = settings.showLeftPants();
    state.showRightPants = settings.showRightPants();
    state.showCape = settings.showCape();
    // The per-stand animations flag plays the idle bob only when the client global kill switch is
    // also off, so disableMannequinAnimations skips the bob on every stand regardless of its setting.
    state.animationsEnabled =
        settings.animations() && !ClientSideConfig.getInstance().disableMannequinAnimations.getPendingValue();

    poseStack.pushPose();

    // Replicate LivingEntityRenderer#submit's transform chain so the mannequin sits where the
    // armor stand would: entity scale, body-yaw rotation (180 - bodyRot), the model flip, the
    // standard player 0.9375 down-scale, then the -1.501 vertical offset.
    float scale = source.scale;
    poseStack.scale(scale, scale, scale);
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - source.bodyRot));
    if (source.wiggle < 5.0F) {
      poseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(source.wiggle / 1.5F * (float) Math.PI) * 3.0F));
    }
    poseStack.scale(-1.0F, -1.0F, 1.0F);
    // 0.9375 is the standard avatar down-scale. A small armor stand renders at half size — vanilla
    // swaps to a smaller model, but the player model has no small variant, so scale uniformly here
    // (same point as the avatar scale, so the feet stay grounded). Armor + items inherit it.
    float bodyScale = source.isSmall ? 0.9375F * 0.5F : 0.9375F;
    poseStack.scale(bodyScale, bodyScale, bodyScale);
    poseStack.translate(0.0F, -1.501F, 0.0F);

    // Body + cape are skipped for an invisible armor stand (matching vanilla, which still renders
    // the layers below — the classic "floating armor" look).
    if (!source.isInvisible) {
      Identifier bodyTexture = skin.body().texturePath();
      submitNodeCollector.submitModel(model, state, poseStack, RenderTypes.entityTranslucent(bodyTexture),
          source.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, source.outlineColor, null);

      // The cape is a child of the body part (and we re-apply the body pose in the cape model) so it
      // follows the stand's body rotation and hangs straight at rest. Only when the skin has one and
      // the showCape toggle is on (MannequinRenderer submits the cape inline rather than via
      // CapeLayer, so the showCape gate must be applied explicitly here).
      if (skin.cape() != null && state.showCape) {
        Identifier capeTexture = skin.cape().texturePath();
        submitNodeCollector.submitModel(capeModel, state, poseStack, RenderTypes.entitySolid(capeTexture),
            source.lightCoords, OverlayTexture.NO_OVERLAY, source.outlineColor, null);
      }
    }

    // The body model's parts must be posed NOW (submitModel only defers setupAnim to render time)
    // so the manual held/head item placement below reads the correct hand/head transforms. Mirrors
    // LivingEntityRenderer#submit, which calls setupAnim before iterating its layers.
    model.setupAnim(state);

    // Armor + elytra use the real vanilla layers (their equipment is copyable ItemStacks). The
    // armor models are posed MannequinPlayerModels so the armor follows the stand's pose. The layers
    // are null only on the very first frame before the equipment renderer was captured.
    if (variant.armorLayer() != null) {
      variant.armorLayer().submit(poseStack, submitNodeCollector, source.lightCoords, state, state.yRot, state.xRot);
    }
    if (variant.wingsLayer() != null) {
      variant.wingsLayer().submit(poseStack, submitNodeCollector, source.lightCoords, state, state.yRot, state.xRot);
    }

    // Held + head items: replicated from ItemInHandLayer / CustomHeadLayer, reading the already
    // resolved ItemStackRenderStates off the incoming `source` (they cannot be copied onto `state`).
    submitHeldItem(source, source.rightHandItemState, state, model, HumanoidArm.RIGHT, poseStack, submitNodeCollector);
    submitHeldItem(source, source.leftHandItemState, state, model, HumanoidArm.LEFT, poseStack, submitNodeCollector);
    submitHeadItem(source, model, poseStack, submitNodeCollector);

    poseStack.popPose();
    return true;
  }

  /**
   * Replicates {@code ItemInHandLayer#submitArmWithItem} for one hand: position at the (posed) hand,
   * apply the fixed item orientation, then submit the incoming render state's resolved hand item.
   * The animated attack/use-item branches are omitted — a static armor stand never uses them.
   */
  private static void submitHeldItem(
      ArmorStandRenderState source,
      ItemStackRenderState item,
      MannequinRenderState state,
      PlayerModel model,
      HumanoidArm arm,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector
  ) {
    if (item.isEmpty()) {
      return;
    }

    poseStack.pushPose();
    model.translateToHand(state, arm, poseStack);
    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
    // Armor stands never use the baby offset (entityType is ARMOR_STAND), so the offsets are fixed.
    boolean left = arm == HumanoidArm.LEFT;
    poseStack.translate((left ? -1.0F : 1.0F) / 16.0F, 2.0F / 16.0F, -10.0F / 16.0F);
    item.submit(poseStack, submitNodeCollector, source.lightCoords, OverlayTexture.NO_OVERLAY, source.outlineColor);
    poseStack.popPose();
  }

  /**
   * Replicates {@code CustomHeadLayer#submit}: position at the (posed) head, then submit either the
   * worn skull/player-head or an arbitrary head item. Supports this mod's "any item in head slot".
   */
  private static void submitHeadItem(
      ArmorStandRenderState source,
      PlayerModel model,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector
  ) {
    if (source.headItem.isEmpty() && source.wornHeadType == null) {
      return;
    }

    poseStack.pushPose();
    model.root().translateAndRotate(poseStack);
    model.translateToHead(poseStack);
    if (source.wornHeadType != null) {
      poseStack.scale(1.1875F, 1.1875F, 1.1875F);
      SkullBlock.Type type = source.wornHeadType;
      RenderType renderType = skullRenderType(type, source.wornHeadProfile);
      SkullBlockRenderer.submitSkull(source.wornHeadAnimationPos, poseStack, submitNodeCollector,
          source.lightCoords, skullModel(type), renderType, source.outlineColor, null);
    } else {
      CustomHeadLayer.translateToHead(poseStack, CustomHeadLayer.Transforms.DEFAULT);
      source.headItem.submit(poseStack, submitNodeCollector, source.lightCoords, OverlayTexture.NO_OVERLAY,
          source.outlineColor);
    }
    poseStack.popPose();
  }

  private static RenderType skullRenderType(SkullBlock.Type type, ResolvableProfile profile) {
    if (type == SkullBlock.Types.PLAYER && profile != null) {
      return Minecraft.getInstance().playerSkinRenderCache().getOrDefault(profile).renderType();
    }
    return SkullBlockRenderer.getSkullRenderType(type, null);
  }

  private static SkullModelBase skullModel(SkullBlock.Type type) {
    if (skullModels == null) {
      EntityModelSet models = Minecraft.getInstance().getEntityModels();
      skullModels = Util.memoize(t -> SkullBlockRenderer.createModel(models, t));
    }
    return skullModels.apply(type);
  }

  /**
   * Seeds {@link #profilesByName} from the persistent {@link MannequinSkinStore} on first use, so a
   * previously-seen mannequin renders its saved skin on join without a fresh name&rarr;UUID web
   * request: each saved entry is a fully-resolved profile, so its lookup resolves the profile
   * instantly and only the (usually disk-cached) texture has to load. Idempotent; runs at most once.
   */
  private static void ensureSkinStoreLoaded() {
    if (skinStoreLoaded) {
      return;
    }
    skinStoreLoaded = true;
    for (Map.Entry<String, ResolvableProfile> entry : MannequinSkinStore.load().entrySet()) {
      profilesByName.putIfAbsent(entry.getKey(), entry.getValue());
      resolvedNames.add(entry.getKey());
    }
  }

  /**
   * Resolves the downloaded skin for the given username via the vanilla render cache, or {@code null}
   * if the name does not resolve to a real player or the lookup is still in flight. Using
   * {@code lookup} (rather than {@code getOrDefault}) means unresolvable names yield no skin, so the
   * caller falls back to the vanilla stick model instead of showing a default-skin mannequin.
   *
   * <p>On the first frame a name resolves to a real downloaded skin, the now-resolved profile is
   * pinned into the cache and persisted (see the inline note), so later lookups skip the web request.
   */
  private static PlayerSkin resolveSkin(String username) {
    ResolvableProfile profile = profilesByName.computeIfAbsent(username, ResolvableProfile::createUnresolved);
    Optional<PlayerSkinRenderCache.RenderInfo> infoOpt =
        Minecraft.getInstance().playerSkinRenderCache().lookup(profile).getNow(Optional.empty());
    if (infoOpt.isEmpty()) {
      return null;
    }
    PlayerSkinRenderCache.RenderInfo info = infoOpt.get();
    PlayerSkin skin = info.playerSkin();
    // A real player's downloaded skin is the ONLY positive "this name is a real account" signal: a
    // network failure and a nonexistent name both come back as a default ResourceTexture, so they are
    // indistinguishable here. On that positive signal, pin the now-RESOLVED profile (it carries the
    // texture properties) into the cache and persist it, so later lookups skip the name->UUID step (an
    // outage then keeps the skin) and it survives a restart. Done once per resolution (resolvedNames
    // gate); a refresh swap clears the gate so the next resolve re-pins the possibly-updated skin.
    if (skin.body() instanceof ClientAsset.DownloadedTexture && !resolvedNames.contains(username)) {
      ResolvableProfile resolved = ResolvableProfile.createResolved(info.gameProfile());
      profilesByName.put(username, resolved);
      resolvedNames.add(username);
      MannequinSkinStore.remember(username, resolved);
    }
    return skin;
  }

  /**
   * Auto-refresh: every {@link #SKIN_REFRESH_MS} (wall clock), replace the cached
   * {@link ResolvableProfile} for {@code username} with a freshly-created unresolved one so the
   * mannequin tracks the named player's current skin.
   *
   * <p>This is the best feasible approximation of a forced refresh given the vanilla API surface.
   * {@code PlayerSkinRenderCache} exposes no public invalidate/evict/refresh — its backing Guava
   * {@code LoadingCache} (keyed by {@code ResolvableProfile}) is private, with a 5-minute
   * expire-after-access that only re-fetches a skin once the stand has gone unrendered that long.
   * Reaching the private field by reflection to call {@code invalidate} would be fragile and
   * version-sensitive, so instead we hand the cache a NEW profile object: since the cache keys on
   * {@code ResolvableProfile} equality and a fresh {@code createUnresolved} is not equal to the
   * resolved entry, the lookup misses and the resolve + skin-download chain runs again, picking up
   * the player's current skin. The stale entry expires from vanilla's 5-minute window on its own.
   * LIMITATION: this re-resolves through the public pipeline rather than truly invalidating the
   * vanilla cache, so the cadence floor is the 15 minutes we enforce here (not instantaneous), and a
   * brand-new profile briefly re-enters the "still resolving" state until the async fetch completes.
   */
  private static void maybeRefreshSkin(UUID uuid, String username, boolean perStandAutoRefresh) {
    if (!perStandAutoRefresh) {
      // Auto-refresh is off for this stand; the manual Refresh skin button still works.
      return;
    }
    if (ClientSideConfig.getInstance().disableAutoSkinRefresh.getPendingValue()) {
      // Auto-refresh disabled globally via config; the manual Refresh skin button still works.
      return;
    }
    long now = System.currentTimeMillis();
    long last = lastRefreshMillis.getOrDefault(uuid, 0L);
    if (last == 0L) {
      // First sighting of this stand: start the clock without forcing an immediate re-fetch
      // (the initial resolveSkin below already triggers the first lookup).
      lastRefreshMillis.put(uuid, now);
      return;
    }
    if (now - last >= SKIN_REFRESH_MS) {
      profilesByName.put(username, ResolvableProfile.createUnresolved(username));
      resolvedNames.remove(username);
      lastRefreshMillis.put(uuid, now);
    }
  }

  /**
   * The downloaded player skin to render the stand as a mannequin, or {@code null} when it should
   * render as the vanilla stick model: no custom name, the name is still resolving, or it resolves
   * only to a bundled default skin (an unresolvable name — rendered as a stick, not a Steve clone).
   * A real player's skin is a {@link ClientAsset.DownloadedTexture}; bundled defaults are
   * {@link ClientAsset.ResourceTexture}.
   *
   * <p>Auto-refresh is opt-in per stand (the {@code autoRefresh} flag, gated further by the global
   * {@code disableAutoSkinRefresh} config); see {@link #maybeRefreshSkin}. To avoid blinking to the
   * stick model while a (scheduled or forced) re-resolve is still in flight, {@link #resolveMannequinSkin}
   * keeps returning the last successfully resolved skin for the name (see {@link #lastGoodSkinsByName}).
   * Resolved skins are also persisted per name (see {@link MannequinSkinStore}), so a saved skin renders
   * on join and is kept through a network outage.
   */
  private static PlayerSkin mannequinSkin(ArmorStandRenderState source) {
    MannequinSettings settings = ((MannequinSettingsHolder) source).armorstands$getMannequinSettings();
    // Client-side global kill switch: when set, every stand renders as the vanilla stick model on
    // this client regardless of its per-stand settings, with zero skin-lookup cost. Checked first so
    // no name read or skin resolution happens for any stand while the option is on.
    if (ClientSideConfig.getInstance().disableMannequins.getPendingValue()) {
      return null;
    }
    // The main override MUST be checked before anything else: when disabled the stand renders as
    // the vanilla stick model and we never touch the name or call resolveMannequinSkin, so no skin
    // lookup/download is ever attempted for a stand the player hasn't opted in (this is the
    // performance guard — keep this check first).
    if (!settings.enabled()) {
      return null;
    }

    // Seed the cache from disk before the first lookup so a saved skin resolves without a web request.
    ensureSkinStoreLoaded();
    String name = ((MannequinNameHolder) source).armorstands$getMannequinName();
    UUID uuid = ((MannequinNameHolder) source).armorstands$getMannequinUuid();

    if (uuid != null) {
      maybeRefreshSkin(uuid, name == null ? "" : name, settings.autoRefresh());
    }
    // resolveMannequinSkin keeps returning the last good skin for this name while a refresh re-fetches,
    // so the stand never blinks to the stick model during the fetch window.
    return resolveMannequinSkin(name);
  }

  /**
   * Forces an immediate skin re-fetch for the given armor stand. Swaps a fresh unresolved profile
   * into {@link #profilesByName} for the stand's custom name (triggering a vanilla-cache miss and
   * re-resolution) and resets that UUID's refresh timer. The last-good skin is preserved so there
   * is no blink to the stick model during the fetch window. No-op when the stand has no custom name.
   */
  public static void forceRefresh(ArmorStand armorStand) {
    Component customName = armorStand.getCustomName();
    if (customName == null) {
      return;
    }
    String name = customName.getString();
    if (name.isBlank()) {
      return;
    }
    UUID uuid = armorStand.getUUID();
    // Swap in a fresh unresolved profile so the next lookup misses the vanilla cache and re-fetches;
    // drop the resolved/persisted flag so the re-fetch re-pins (and re-saves) the refreshed skin.
    profilesByName.put(name, ResolvableProfile.createUnresolved(name));
    resolvedNames.remove(name);
    // Reset the refresh timer so maybeRefreshSkin doesn't immediately re-swap before the fetch completes.
    lastRefreshMillis.put(uuid, System.currentTimeMillis());
    // lastGoodSkinsByName is intentionally NOT cleared: the stand and the preview keep the last known
    // skin visible during the async re-fetch window (no blink to the stick model).
  }

  /**
   * The downloaded player skin for the given username, or {@code null} when the name is blank, does
   * not resolve to a real player, or is still downloading (a bundled default skin counts as "not a
   * mannequin"). Public so the edit screen can show whose skin, if anyone's, the stand displays.
   */
  public static PlayerSkin resolveMannequinSkin(String username) {
    if (username == null || username.isBlank()) {
      return null;
    }
    ensureSkinStoreLoaded();
    PlayerSkin skin = resolveSkin(username);
    if (skin != null && skin.body() instanceof ClientAsset.DownloadedTexture) {
      lastGoodSkinsByName.put(username, skin);
      return skin;
    }
    // Live resolve unavailable (still downloading after a forced/scheduled refresh, or a transient
    // outage): keep the last skin we loaded for this name so nothing blinks to the stick model. Null
    // only when this name has never resolved (initial load or a genuinely unresolvable name).
    return lastGoodSkinsByName.get(username);
  }

  /**
   * Whether the stand should render as a mannequin — used by the mixin to suppress the vanilla stick
   * model and its layers while leaving the rest of the vanilla submit (name label, leash) intact.
   */
  public static boolean shouldRenderAsMannequin(ArmorStandRenderState source) {
    return mannequinSkin(source) != null;
  }

  /**
   * Returns the cached {@link SlimVariant} (body model + armor/elytra layers) for the given arm
   * width, baking it once on first use. Both arm widths are cached in separate slots so a scene that
   * mixes slim and wide mannequins never rebakes per frame: each variant is built once and reused.
   * The slim-independent cape and skull models are shared singletons, baked once regardless of width.
   *
   * <p>The {@code armorLayer}/{@code wingsLayer} entries may be {@code null} on a variant baked before
   * {@code equipmentRenderer} first arrived (the renderer can be constructed after the first bake);
   * such a variant is rebuilt once on the next call once the equipment renderer is available.
   */
  private static SlimVariant ensureModels(boolean slim) {
    EntityModelSet models = Minecraft.getInstance().getEntityModels();

    if (capeModel == null) {
      capeModel = new MannequinCapeModel(models.bakeLayer(ModelLayers.PLAYER_CAPE));
    }

    int slot = slim ? 1 : 0;
    SlimVariant variant = variants[slot];
    // (Re)build only when this slot is empty, or when it was baked before the equipment renderer
    // arrived (so its armor/wings layers are still null). The OTHER slot is never touched here.
    if (variant == null || (variant.armorLayer() == null && equipmentRenderer != null)) {
      variant = buildVariant(models, slim);
      variants[slot] = variant;
    }
    return variant;
  }

  /** Bakes one arm-width variant's body model and (if the equipment renderer is ready) its layers. */
  private static SlimVariant buildVariant(EntityModelSet models, boolean slim) {
    ModelPart root = models.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER);
    MannequinPlayerModel bodyModel = new MannequinPlayerModel(root, slim);

    if (equipmentRenderer == null) {
      // Equipment renderer not captured yet; bake the body now and fill the layers on a later call.
      return new SlimVariant(bodyModel, null, null);
    }

    // The layers' parent only needs to return the body model (the armor/wings layers never call it;
    // held/head positioning uses the model directly). Bind it to this variant's body model.
    RenderLayerParent<AvatarRenderState, PlayerModel> parent = () -> bodyModel;

    // Armor models are posed MannequinPlayerModels so armor tracks the stand's pose. 26.1's
    // HumanoidArmorLayer no longer toggles part visibility (the equipment textures cover only the
    // relevant cubes), so the posed subclass is safe to reuse here.
    ArmorModelSet<PlayerModel> armorModels = ArmorModelSet.<PlayerModel>bake(
        slim ? ModelLayers.PLAYER_SLIM_ARMOR : ModelLayers.PLAYER_ARMOR,
        models,
        part -> new MannequinPlayerModel(part, slim));

    HumanoidArmorLayer<AvatarRenderState, PlayerModel, PlayerModel> armorLayer =
        new HumanoidArmorLayer<>(parent, armorModels, equipmentRenderer);
    WingsLayer<AvatarRenderState, PlayerModel> wingsLayer =
        new WingsLayer<>(parent, models, equipmentRenderer);
    return new SlimVariant(bodyModel, armorLayer, wingsLayer);
  }

  /** Applies a degrees-based armor stand {@link Rotations} pose onto a model part. */
  private static void applyPose(ModelPart part, Rotations pose) {
    part.xRot = DEG_TO_RAD * pose.x();
    part.yRot = DEG_TO_RAD * pose.y();
    part.zRot = DEG_TO_RAD * pose.z();
  }

  /**
   * Reusable render state that carries the armor stand's six pose Rotations to the model's
   * setupAnim. A fresh one is allocated per stand per frame (see tryRenderMannequin).
   */
  private static final class MannequinRenderState extends AvatarRenderState {
    private static final Rotations ZERO = new Rotations(0.0F, 0.0F, 0.0F);

    // Whether the mannequin idle arm bob plays (the show* skin-layer flags live on AvatarRenderState).
    boolean animationsEnabled = true;

    Rotations headPose = ZERO;
    Rotations bodyPose = ZERO;
    Rotations leftArmPose = ZERO;
    Rotations rightArmPose = ZERO;
    Rotations leftLegPose = ZERO;
    Rotations rightLegPose = ZERO;
  }

  /**
   * Player model that applies the armor stand's six pose Rotations onto the corresponding humanoid
   * parts, mirroring vanilla ArmorStandArmorModel#setupAnim. setupAnim is invoked at render time on
   * the state passed to submitModel, so applying the poses here (after super.setupAnim) keeps the
   * armor stand's posing working. The vanilla idle arm-bob (added by super.setupAnim, then clobbered
   * by our arm pose) is re-applied on top so the mannequin sways subtly like a real one.
   */
  private static final class MannequinPlayerModel extends PlayerModel {
    MannequinPlayerModel(ModelPart root, boolean slim) {
      super(root, slim);
    }

    @Override
    public void setupAnim(AvatarRenderState state) {
      super.setupAnim(state);
      if (!(state instanceof MannequinRenderState mannequin)) {
        return;
      }

      applyPose(this.head, mannequin.headPose);
      applyPose(this.body, mannequin.bodyPose);
      applyPose(this.leftLeg, mannequin.leftLegPose);
      applyPose(this.rightLeg, mannequin.rightLegPose);

      // Arms: apply the static pose, then layer the mannequin idle bob back on top (super.setupAnim
      // added it before our overwrite wiped it). bobModelPart is additive and reads ageInTicks.
      // Skip the bob entirely when animations are toggled off.
      applyPose(this.leftArm, mannequin.leftArmPose);
      applyPose(this.rightArm, mannequin.rightArmPose);
      if (mannequin.animationsEnabled) {
        AnimationUtils.bobModelPart(this.rightArm, state.ageInTicks, 1.0F);
        AnimationUtils.bobModelPart(this.leftArm, state.ageInTicks, -1.0F);
      }
    }
  }

  /**
   * Cape model that re-applies the armor stand's body pose so the cape stays attached to a posed
   * body. The cape itself hangs at its natural rest pose (capeFlap/capeLean default to 0).
   */
  private static final class MannequinCapeModel extends PlayerCapeModel {
    MannequinCapeModel(ModelPart root) {
      super(root);
    }

    @Override
    public void setupAnim(AvatarRenderState state) {
      super.setupAnim(state);
      if (state instanceof MannequinRenderState mannequin) {
        applyPose(this.body, mannequin.bodyPose);
      }
    }
  }
}
