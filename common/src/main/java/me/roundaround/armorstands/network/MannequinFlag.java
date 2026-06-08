package me.roundaround.armorstands.network;

import io.netty.buffer.ByteBuf;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import me.roundaround.armorstands.client.render.MannequinSettings;
import me.roundaround.armorstands.interfaces.MannequinSettingsAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.decoration.ArmorStand;

/**
 * One boolean toggle of an armor stand's {@link MannequinSettings}. Mirrors {@link ArmorStandFlag}:
 * each value reads/writes its flag on the stand's synched mannequin settings, and the enum is
 * packet-serialized by index.
 */
public enum MannequinFlag {
  ENABLED(0, "enabled", MannequinSettings::enabled, MannequinSettings::withEnabled),
  ANIMATIONS(1, "animations", MannequinSettings::animations, MannequinSettings::withAnimations),
  CAPE(2, "cape", MannequinSettings::showCape, MannequinSettings::withShowCape),
  HAT(3, "hat", MannequinSettings::showHat, MannequinSettings::withShowHat),
  JACKET(4, "jacket", MannequinSettings::showJacket, MannequinSettings::withShowJacket),
  LEFT_SLEEVE(5, "leftSleeve", MannequinSettings::showLeftSleeve, MannequinSettings::withShowLeftSleeve),
  RIGHT_SLEEVE(6, "rightSleeve", MannequinSettings::showRightSleeve, MannequinSettings::withShowRightSleeve),
  LEFT_PANTS(7, "leftPants", MannequinSettings::showLeftPants, MannequinSettings::withShowLeftPants),
  RIGHT_PANTS(8, "rightPants", MannequinSettings::showRightPants, MannequinSettings::withShowRightPants),
  SLIM(9, "slim", MannequinSettings::slim, MannequinSettings::withSlim),
  AUTO_REFRESH(10, "autoRefresh", MannequinSettings::autoRefresh, MannequinSettings::withAutoRefresh);

  public static final IntFunction<MannequinFlag> ID_TO_VALUE_FUNCTION = ByIdMap.continuous(
      MannequinFlag::getIndex,
      values(),
      ByIdMap.OutOfBoundsStrategy.ZERO
  );
  public static final StreamCodec<ByteBuf, MannequinFlag> PACKET_CODEC = ByteBufCodecs.idMapper(
      ID_TO_VALUE_FUNCTION,
      MannequinFlag::getIndex
  );

  private final int index;
  private final String id;
  private final Predicate<MannequinSettings> getter;
  private final BiFunction<MannequinSettings, Boolean, MannequinSettings> setter;

  MannequinFlag(
      int index,
      String id,
      Predicate<MannequinSettings> getter,
      BiFunction<MannequinSettings, Boolean, MannequinSettings> setter
  ) {
    this.index = index;
    this.id = id;
    this.getter = getter;
    this.setter = setter;
  }

  public int getIndex() {
    return this.index;
  }

  public String getId() {
    return this.id;
  }

  public Component getDisplayName() {
    return Component.translatable("armorstands.mannequin." + this.id);
  }

  /** Reads this flag from a settings record. */
  public boolean get(MannequinSettings settings) {
    return this.getter.test(settings);
  }

  /** Returns a copy of the settings record with this flag set to the given value. */
  public MannequinSettings with(MannequinSettings settings, boolean value) {
    return this.setter.apply(settings, value);
  }

  /** Reads this flag from the stand's synched mannequin settings. */
  public boolean getValue(ArmorStand armorStand) {
    return this.get(((MannequinSettingsAccess) armorStand).armorstands$getMannequinSettings());
  }

  /** Writes this flag onto the stand's synched mannequin settings (replicates to clients server-side). */
  public void setValue(ArmorStand armorStand, boolean value) {
    MannequinSettingsAccess access = (MannequinSettingsAccess) armorStand;
    access.armorstands$setMannequinSettings(this.with(access.armorstands$getMannequinSettings(), value));
  }
}
