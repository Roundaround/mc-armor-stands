package me.roundaround.armorstands.util.actions;

import me.roundaround.armorstands.interfaces.EntityPosition;
import me.roundaround.armorstands.util.ArmorStandHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;

public class MoveToGroundAction implements ArmorStandAction {
  private Optional<Vec3> originalPosition = Optional.empty();
  private final boolean sitting;

  private MoveToGroundAction(boolean sitting) {
    this.sitting = sitting;
  }

  public static MoveToGroundAction standing() {
    return new MoveToGroundAction(false);
  }

  public static MoveToGroundAction sitting() {
    return new MoveToGroundAction(true);
  }

  public static MoveToGroundAction create(boolean sitting) {
    return new MoveToGroundAction(sitting);
  }

  @Override
  public Component getName(ArmorStand armorStand) {
    return Component.translatable("armorstands.action.moveToGround");
  }

  @Override
  public void apply(Player player, ArmorStand armorStand) {
    Optional<Vec3> maybeGround = ArmorStandHelper.getGroundPos(armorStand, this.sitting);

    if (maybeGround.isPresent()) {
      this.originalPosition = Optional.of(((EntityPosition) armorStand).armorstands$getPos());
      MoveAction.setPosition(armorStand, maybeGround.get());
    }
  }

  @Override
  public void undo(Player player, ArmorStand armorStand) {
    if (this.originalPosition.isEmpty()) {
      return;
    }

    MoveAction.setPosition(armorStand, this.originalPosition.get());
  }
}
