package me.roundaround.armorstands.util.actions;

import me.roundaround.armorstands.network.ArmorStandFlag;
import me.roundaround.armorstands.util.Pose;
import net.minecraft.core.Rotations;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class HoldingAction extends ComboAction {
  private static final Rotations ROT_EMPTY = new Rotations(0f, 0f, 0f);
  private static final Vec3 POS_UPRIGHT_ITEM = new Vec3(0.36, -1.41, -0.5625);
  private static final Rotations ROT_UPRIGHT_ITEM = new Rotations(-90f, 0f, 0f);
  private static final Vec3 POS_FLAT_ITEM = new Vec3(0.385, -0.78, -0.295);
  private static final Rotations ROT_FLAT_ITEM = new Rotations(0f, 0f, 0f);
  private static final Vec3 POS_BLOCK = new Vec3(0.5725, -0.655, 0.352);
  private static final Rotations ROT_BLOCK = new Rotations(-15f, 135f, 0f);
  private static final Vec3 POS_TOOL = new Vec3(-0.17, -1.285, -0.44);
  private static final Rotations ROT_TOOL = new Rotations(-10f, 0f, -90f);

  private HoldingAction(ArmorStand armorStand, boolean small, Vec3 pos, Rotations rot) {
    super(Component.translatable("armorstands.action.holding"), List.of(
        FlagAction.set(ArmorStandFlag.INVISIBLE, true),
        FlagAction.set(ArmorStandFlag.HIDE_BASE_PLATE, true),
        FlagAction.set(ArmorStandFlag.NAME, false),
        FlagAction.set(ArmorStandFlag.SMALL, small),
        SnapToGroundAction.standing(),
        MoveAction.local(small ? pos.scale(0.5) : pos),
        PoseAction.fromPose(new Pose(
            ROT_EMPTY,
            ROT_EMPTY,
            rot,
            ROT_EMPTY,
            ROT_EMPTY,
            ROT_EMPTY))));
  }

  public static HoldingAction uprightItem(ArmorStand armorStand) {
    return uprightItem(armorStand, false);
  }

  public static HoldingAction uprightItem(ArmorStand armorStand, boolean small) {
    return new HoldingAction(armorStand, small, POS_UPRIGHT_ITEM, ROT_UPRIGHT_ITEM);
  }

  public static HoldingAction flatItem(ArmorStand armorStand) {
    return flatItem(armorStand, false);
  }

  public static HoldingAction flatItem(ArmorStand armorStand, boolean small) {
    return new HoldingAction(armorStand, small, POS_FLAT_ITEM, ROT_FLAT_ITEM);
  }

  public static HoldingAction block(ArmorStand armorStand) {
    return block(armorStand, false);
  }

  public static HoldingAction block(ArmorStand armorStand, boolean small) {
    return new HoldingAction(armorStand, small, POS_BLOCK, ROT_BLOCK);
  }

  public static HoldingAction tool(ArmorStand armorStand) {
    return tool(armorStand, false);
  }

  public static HoldingAction tool(ArmorStand armorStand, boolean small) {
    return new HoldingAction(armorStand, small, POS_TOOL, ROT_TOOL);
  }
}
