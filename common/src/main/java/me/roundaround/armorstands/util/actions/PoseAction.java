package me.roundaround.armorstands.util.actions;

import me.roundaround.armorstands.util.Pose;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;

public class PoseAction extends SimpleArmorStandAction<Pose> {
  private PoseAction(Pose pose) {
    super(pose);
  }

  public static PoseAction fromPose(Pose pose) {
    return new PoseAction(pose);
  }

  @Override
  public Component getName(ArmorStand armorStand) {
    return Component.translatable("armorstands.action.pose");
  }

  @Override
  protected Pose get(ArmorStand armorStand) {
    return new Pose(armorStand);
  }
}
