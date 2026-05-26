package me.roundaround.armorstands.util;

import java.util.HashMap;
import java.util.UUID;

import me.roundaround.armorstands.network.ScreenType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;

public class LastUsedScreen {
  private static final HashMap<UUID, Entry> lastUsedScreens = new HashMap<>();

  public static void set(ServerPlayer player, ArmorStand armorStand, ScreenType screenType) {
    lastUsedScreens.put(player.getUUID(), new Entry(armorStand, screenType));
  }

  public static ScreenType get(ServerPlayer player, ArmorStand armorStand) {
    if (!lastUsedScreens.containsKey(player.getUUID())) {
      return null;
    }

    Entry entry = lastUsedScreens.get(player.getUUID());
    return entry.matches(armorStand, entry.screenType) ? entry.screenType : null;
  }

  public static ScreenType getOrDefault(
      ServerPlayer player,
      ArmorStand armorStand,
      ScreenType defaultScreenType) {
    ScreenType screenType = get(player, armorStand);
    return screenType == null ? defaultScreenType : screenType;
  }

  public static void remove(ServerPlayer player) {
    lastUsedScreens.remove(player.getUUID());
  }

  private static class Entry {
    private final UUID armorStandUuid;
    private final ScreenType screenType;

    private Entry(ArmorStand armorStand, ScreenType screenType) {
      this.armorStandUuid = armorStand.getUUID();
      this.screenType = screenType;
    }

    public boolean matches(ArmorStand armorStand, ScreenType screenType) {
      return this.armorStandUuid.equals(armorStand.getUUID()) && this.screenType == screenType;
    }
  }
}
