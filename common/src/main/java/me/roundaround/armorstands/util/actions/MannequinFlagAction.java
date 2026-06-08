package me.roundaround.armorstands.util.actions;

import me.roundaround.armorstands.network.MannequinFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

/**
 * Editor action that sets a single {@link MannequinFlag} on a stand's mannequin settings, caching
 * the previous value so it can be undone. Mirrors {@link FlagAction}.
 */
public class MannequinFlagAction implements ArmorStandAction {
  private final MannequinFlag flag;
  private final boolean value;
  private boolean previousValue;

  private MannequinFlagAction(MannequinFlag flag, boolean value) {
    this.flag = flag;
    this.value = value;
  }

  public static MannequinFlagAction set(MannequinFlag flag, boolean value) {
    return new MannequinFlagAction(flag, value);
  }

  @Override
  public Component getName(ArmorStand armorStand) {
    return Component.translatable("armorstands.action.mannequin", this.flag.getDisplayName());
  }

  @Override
  public void apply(Player player, ArmorStand armorStand) {
    this.previousValue = this.flag.getValue(armorStand);
    this.flag.setValue(armorStand, this.value);
  }

  @Override
  public void undo(Player player, ArmorStand armorStand) {
    this.flag.setValue(armorStand, this.previousValue);
  }
}
