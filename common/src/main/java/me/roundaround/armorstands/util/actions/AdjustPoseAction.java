package me.roundaround.armorstands.util.actions;

import java.util.Optional;

import me.roundaround.armorstands.network.EulerAngleParameter;
import me.roundaround.armorstands.network.PosePart;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

public class AdjustPoseAction implements ArmorStandAction {
  private final PosePart part;
  private final EulerAngleParameter parameter;
  private final float argument;
  private final boolean absolute;
  private final boolean round;
  private Optional<Float> originalValue = Optional.empty();

  private AdjustPoseAction(
      PosePart part,
      EulerAngleParameter parameter,
      float argument,
      boolean absolute,
      boolean round) {
    this.part = part;
    this.parameter = parameter;
    this.argument = argument;
    this.absolute = absolute;
    this.round = round;
  }

  public static AdjustPoseAction absolute(
      PosePart part,
      EulerAngleParameter parameter,
      float argument) {
    return new AdjustPoseAction(part, parameter, argument, true, false);
  }

  @Override
  public Component getName(ArmorStand armorStand) {
    return Component.translatable("armorstands.action.adjustPose");
  }

  @Override
  public void apply(Player player, ArmorStand armorStand) {
    this.originalValue = Optional.of(this.parameter.get(this.part.get(armorStand)));

    float value = this.argument;

    if (!this.absolute) {
      value += this.originalValue.get();
    }

    if (this.round) {
      value = Math.round(value);
    }

    this.part.set(armorStand, this.parameter, value);
  }

  @Override
  public void undo(Player player, ArmorStand armorStand) {
    if (this.originalValue.isEmpty()) {
      return;
    }
    this.part.set(armorStand, this.parameter, this.originalValue.get());
  }
}
