package me.roundaround.armorstands.util.actions;

import me.roundaround.armorstands.util.ArmorStandApplyable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

public interface ArmorStandAction extends ArmorStandApplyable {
  public abstract Component getName(ArmorStand armorStand);

  public abstract void undo(Player player, ArmorStand armorStand);

  public static ArmorStandAction noop() {
    return new ArmorStandAction() {
      @Override
      public Component getName(ArmorStand armorStand) {
        return null;
      }

      @Override
      public void apply(Player player, ArmorStand armorStand) {
      }

      @Override
      public void undo(Player player, ArmorStand armorStand) {
      }
    };
  }
}
