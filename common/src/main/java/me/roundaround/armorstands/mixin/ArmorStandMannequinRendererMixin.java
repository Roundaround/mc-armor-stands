package me.roundaround.armorstands.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.PoseStack;
import me.roundaround.armorstands.client.render.MannequinNameHolder;
import me.roundaround.armorstands.client.render.MannequinRenderer;
import me.roundaround.armorstands.client.render.MannequinSettingsHolder;
import me.roundaround.armorstands.interfaces.MannequinSettingsAccess;
import me.roundaround.allay.api.MixinEnv;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandRenderer.class)
@MixinEnv(MixinEnv.Env.CLIENT)
public class ArmorStandMannequinRendererMixin {
  @Inject(method = "<init>", at = @At("TAIL"))
  private void captureEquipmentRenderer(EntityRendererProvider.Context context, CallbackInfo info) {
    MannequinRenderer.setEquipmentRenderer(context.getEquipmentRenderer());
  }

  @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/decoration/ArmorStand;Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;F)V", at = @At("TAIL"))
  private void captureMannequinName(ArmorStand entity, ArmorStandRenderState state, float partialTicks, CallbackInfo info) {
    // Capture the custom name for mannequin-mode gating. The floating name label is left to the
    // existing custom-name-visibility toggle (shouldShowName), so it works the same as a normal stand.
    Component customName = entity.getCustomName();
    MannequinNameHolder nameHolder = (MannequinNameHolder) state;
    nameHolder.armorstands$setMannequinName(customName == null ? null : customName.getString());

    // Capture the stand's UUID so the renderer can key per-stand state (the LOCKED-skin freeze
    // store) by a stable identity. Only the render state reaches the renderer, so the UUID must be
    // carried over here rather than looked up live.
    nameHolder.armorstands$setMannequinUuid(entity.getUUID());

    // Capture the per-stand mannequin settings (synched entity data) so the renderer + suppression
    // gates can read them off the render state (shouldRenderAsMannequin only gets the state).
    ((MannequinSettingsHolder) state).armorstands$setMannequinSettings(
        ((MannequinSettingsAccess) entity).armorstands$getMannequinSettings());
  }

  // Render the mannequin (a no-op for unnamed / unresolvable stands). We deliberately do NOT cancel
  // the vanilla submit — that would also drop the name label and leash drawn at the end of the chain.
  // Instead the stick model and vanilla layers are suppressed below for mannequins.
  @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"))
  private void renderAsMannequin(
      ArmorStandRenderState state,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      CameraRenderState camera,
      CallbackInfo info
  ) {
    MannequinRenderer.tryRenderMannequin(state, poseStack, submitNodeCollector);
  }

  // Suppress the vanilla stick model for a mannequin (a null render type skips the model submit).
  @ModifyReturnValue(method = "getRenderType(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;", at = @At("RETURN"))
  private RenderType suppressMannequinModel(
      RenderType original,
      ArmorStandRenderState state,
      boolean isBodyVisible,
      boolean forceTransparent,
      boolean appearGlowing
  ) {
    return MannequinRenderer.shouldRenderAsMannequin(state) ? null : original;
  }

  // Suppress the vanilla armor-stand layers (armor/held/head items on the stick model) for a
  // mannequin — they are re-rendered on the player model instead. Overrides the inherited
  // LivingEntityRenderer#shouldRenderLayers (erased to LivingEntityRenderState) via a merged method.
  protected boolean shouldRenderLayers(LivingEntityRenderState state) {
    return !(state instanceof ArmorStandRenderState armorStandState
        && MannequinRenderer.shouldRenderAsMannequin(armorStandState));
  }
}
