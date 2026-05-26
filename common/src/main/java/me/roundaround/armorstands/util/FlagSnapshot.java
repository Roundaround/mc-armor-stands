package me.roundaround.armorstands.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import me.roundaround.armorstands.network.ArmorStandFlag;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

public class FlagSnapshot implements ArmorStandApplyable {
  private HashMap<ArmorStandFlag, Boolean> values = new HashMap<>();

  private FlagSnapshot(ArmorStand armorStand, Predicate<ArmorStandFlag> predicate) {
    Arrays.stream(ArmorStandFlag.values())
        .filter(predicate)
        .filter((flag) -> flag != ArmorStandFlag.UNKNOWN)
        .forEach((flag) -> {
          values.put(flag, flag.getValue(armorStand));
        });
  }

  public static FlagSnapshot all(ArmorStand armorStand) {
    return new FlagSnapshot(armorStand, (flag) -> true);
  }

  public static FlagSnapshot none(ArmorStand armorStand) {
    return new FlagSnapshot(armorStand, (flag) -> false);
  }

  public static FlagSnapshot some(ArmorStand armorStand, Predicate<ArmorStandFlag> predicate) {
    return new FlagSnapshot(armorStand, predicate);
  }

  public static FlagSnapshot of(ArmorStand armorStand, ArmorStandFlag... flags) {
    return new FlagSnapshot(armorStand, (flag) -> List.of(flags).contains(flag));
  }

  @Override
  public void apply(Player player, ArmorStand armorStand) {
    values.entrySet().forEach((entry) -> {
      entry.getKey().setValue(armorStand, entry.getValue());
    });
  }
}
