package me.roundaround.armorstands.client.render;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.trove.util.PathAccessor;
import net.minecraft.world.item.component.ResolvableProfile;
import org.slf4j.Logger;

/**
 * Persistent, client-side cache of resolved mannequin profiles, keyed by username, written as JSON to
 * {@code config/<modid>/mannequin-skins.json}. Each value is a fully-resolved {@link ResolvableProfile}
 * — it carries the player's id and signed texture properties — so a saved skin reloads straight from
 * those properties with no name&rarr;UUID web request.
 *
 * <p>That buys three things the in-memory cache could not: a saved skin renders (near-)immediately on
 * join, it sticks around forever when auto-refresh is off and the refresh button is never pressed, and
 * a transient network outage keeps the saved skin instead of collapsing to the default/stick model.
 * Pure client cache; never synced and never authoritative.
 */
public final class MannequinSkinStore {
  private static final Logger LOGGER = LogUtils.getLogger();
  private static final String FILE_NAME = "mannequin-skins.json";
  private static final Codec<Map<String, ResolvableProfile>> CODEC =
      Codec.unboundedMap(Codec.STRING, ResolvableProfile.CODEC);
  private static final Gson GSON = new Gson();

  private static final Map<String, ResolvableProfile> entries = new HashMap<>();
  private static boolean loaded = false;

  private MannequinSkinStore() {
  }

  /** Loads the saved profiles once (later calls return the cached map). Never throws. */
  public static Map<String, ResolvableProfile> load() {
    if (loaded) {
      return entries;
    }
    loaded = true;
    Path file = file();
    if (file == null || !Files.exists(file)) {
      return entries;
    }
    try {
      JsonElement json = GSON.fromJson(Files.readString(file), JsonElement.class);
      CODEC.parse(JsonOps.INSTANCE, json).result().ifPresent(entries::putAll);
    } catch (Exception e) {
      // A corrupt or unreadable cache must never break rendering; just start empty.
      LOGGER.warn("[armorstands] Failed to read mannequin skin cache; starting empty", e);
    }
    return entries;
  }

  /** Records a freshly-resolved profile for a username and rewrites the file. Never throws. */
  public static void remember(String username, ResolvableProfile resolved) {
    load();
    entries.put(username, resolved);
    save();
  }

  private static void save() {
    Path file = file();
    if (file == null) {
      return;
    }
    try {
      Files.createDirectories(file.getParent());
      JsonElement json = CODEC.encodeStart(JsonOps.INSTANCE, entries).getOrThrow();
      Files.writeString(file, GSON.toJson(json));
    } catch (Exception e) {
      LOGGER.warn("[armorstands] Failed to write mannequin skin cache", e);
    }
  }

  private static Path file() {
    try {
      return PathAccessor.get().getModConfigDir(Constants.MOD_ID).resolve(FILE_NAME);
    } catch (Exception e) {
      return null;
    }
  }
}
