package me.roundaround.armorstands.util.actions;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

public class RotateAction implements ArmorStandAction {
  private final float argument;
  private final boolean absolute;
  private final boolean round;
  private Optional<Float> originalRotation = Optional.empty();

  private RotateAction(float argument, boolean absolute, boolean round) {
    this.argument = argument;
    this.absolute = absolute;
    this.round = round;
  }

  public static RotateAction absolute(float rotation) {
    return absolute(rotation, false);
  }

  public static RotateAction absolute(float rotation, boolean round) {
    return new RotateAction(rotation, true, round);
  }

  public static RotateAction relative(float rotation) {
    return relative(rotation, true);
  }

  public static RotateAction relative(float rotation, boolean round) {
    return new RotateAction(rotation, false, round);
  }

  @Override
  public Component getName(ArmorStand armorStand) {
    return Component.translatable("armorstands.action.rotate");
  }

  @Override
  public void apply(Player player, ArmorStand armorStand) {
    originalRotation = Optional.of(armorStand.getYRot());

    float rotation = argument;

    if (!absolute) {
      rotation += originalRotation.get();
    }

    setRotation(armorStand, rotation, round);
  }

  @Override
  public void undo(Player player, ArmorStand armorStand) {
    if (originalRotation.isEmpty()) {
      return;
    }
    setRotation(armorStand, originalRotation.get());
  }

  public static void setRotation(ArmorStand armorStand, float rotation) {
    setRotation(armorStand, rotation, false);
  }

  public static void setRotation(ArmorStand armorStand, float rotation, boolean round) {
    float target = rotation % 360f;
    if (round) {
      target = Math.round(target);
    }

    armorStand.setYRot(target);
  }
}
