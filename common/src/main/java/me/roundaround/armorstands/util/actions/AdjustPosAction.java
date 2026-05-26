package me.roundaround.armorstands.util.actions;

import me.roundaround.armorstands.util.MoveUnits;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class AdjustPosAction implements ArmorStandAction {
  private final MoveAction moveAction;

  private AdjustPosAction(MoveAction moveAction) {
    this.moveAction = moveAction;
  }

  public static AdjustPosAction relative(Direction direction, int amount, MoveUnits units) {
    return new AdjustPosAction(
        MoveAction.relative(
            new Vec3(direction.step()).scale(units.getAmount(amount)),
            true));
  }

  public static AdjustPosAction local(Direction direction, int amount, MoveUnits units, boolean localToPlayer) {
    return new AdjustPosAction(
        MoveAction.local(
            new Vec3(direction.step()).scale(units.getAmount(amount)),
            localToPlayer));
  }

  @Override
  public Component getName(ArmorStand armorStand) {
    return this.moveAction.getName(armorStand);
  }

  @Override
  public void apply(Player player, ArmorStand armorStand) {
    this.moveAction.apply(player, armorStand);
  }

  @Override
  public void undo(Player player, ArmorStand armorStand) {
    this.moveAction.undo(player, armorStand);
  }
}
