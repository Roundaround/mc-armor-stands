package me.roundaround.armorstands.client.util;

import com.google.common.io.Files;
import com.google.gson.*;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.util.Pose;
import me.roundaround.armorstands.util.SavedPose;
import me.roundaround.trove.util.PathAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.decoration.ArmorStand;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PoseStorage {
  private static final Logger LOGGER = LogManager.getLogger(Constants.MOD_ID);
  private static final File FILE = PathAccessor.get().getGameDir().resolve("armorstandsposes.json").toFile();
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final HashMap<UUID, SavedPose> map = new HashMap<>();

  public static void add(String name, ArmorStand armorStand) {
    add(name, new Pose(armorStand));
  }

  public static void add(String name, Pose pose) {
    UUID uuid = UUID.randomUUID();
    try {
      map.put(uuid, new SavedPose(name, pose));
      save();
    } catch (IOException e) {
      map.remove(uuid);
      LOGGER.error(e);
    }
  }

  public static void rename(UUID uuid, String name) {
    try {
      SavedPose savedPose = map.get(uuid);
      if (savedPose == null) {
        return;
      }
      map.put(uuid, new SavedPose(name, savedPose.toPose()));
      save();
    } catch (IOException e) {
      LOGGER.error(e);
    }
  }

  public static void remove(UUID uuid) {
    try {
      map.remove(uuid);
      save();
    } catch (IOException e) {
      LOGGER.error(e);
    }
  }

  public static void reload() {
    try {
      load();
    } catch (IOException e) {
      LOGGER.error(e);
    }
  }

  public static Map<UUID, SavedPose> getPoses() {
    return Map.copyOf(map);
  }

  private static void save() throws IOException {
    JsonArray jsonArray = new JsonArray();
    map.values().stream().map(SavedPose::toJson).forEach(jsonArray::add);
    try (BufferedWriter bufferedWriter = Files.newWriter(FILE, StandardCharsets.UTF_8);) {
      GSON.toJson(jsonArray, bufferedWriter);
    }
  }

  private static void load() throws IOException {
    if (!FILE.exists()) {
      return;
    }
    try (BufferedReader bufferedReader = Files.newReader(FILE, StandardCharsets.UTF_8)) {
      map.clear();

      JsonArray jsonArray = GSON.fromJson(bufferedReader, JsonArray.class);
      for (JsonElement jsonElement : jsonArray) {
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "entry");
        map.put(UUID.randomUUID(), SavedPose.fromJson(jsonObject));
      }
    }
  }
}
