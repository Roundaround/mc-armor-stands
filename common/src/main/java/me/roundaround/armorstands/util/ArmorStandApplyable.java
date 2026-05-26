package me.roundaround.armorstands.util;

import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface ArmorStandApplyable {
  void apply(Player player, ArmorStand armorStand);
}
