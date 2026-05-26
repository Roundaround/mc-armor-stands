package me.roundaround.armorstands.util.actions;

import me.roundaround.armorstands.network.ArmorStandFlag;
import me.roundaround.armorstands.util.Pose;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.Collection;

public class ToolRackAction extends ComboAction {
  private ToolRackAction(Collection<ArmorStandAction> actions) {
    super(Component.translatable("armorstands.action.toolRack"), actions);
  }

  public static ToolRackAction create(ArmorStand armorStand) {
    BlockPos hookPos = findTripwireHook(armorStand);

    if (hookPos == null) {
      return null;
    }

    ArrayList<ArmorStandAction> actions = new ArrayList<>();

    actions.add(FlagAction.set(ArmorStandFlag.INVISIBLE, true));
    actions.add(FlagAction.set(ArmorStandFlag.HIDE_BASE_PLATE, true));
    actions.add(FlagAction.set(ArmorStandFlag.NO_GRAVITY, true));
    actions.add(FlagAction.set(ArmorStandFlag.NAME, false));
    actions.add(FlagAction.set(ArmorStandFlag.SMALL, false));
    actions.add(RotateAction.absolute(armorStand.level()
        .getBlockState(hookPos)
        .getValue(TripWireHookBlock.FACING)
        .toYRot()));
    actions.add(ScaleAction.absolute(1f));
    actions.add(MoveAction.absolute(hookPos.getX() + 0.5, hookPos.getY() - 1, hookPos.getZ() + 0.5));
    actions.add(MoveAction.local(-0.17, 0.24, -0.05));
    actions.add(PoseAction.fromPose(new Pose(
        new Rotations(0f, 0.001f, 0f),
        new Rotations(0f, 0.001f, 0f),
        new Rotations(-100f, 90f, 180f),
        new Rotations(0f, 0f, 0f),
        new Rotations(0f, 0f, 0f),
        new Rotations(0f, 0f, 0f)
    )));

    return new ToolRackAction(actions);
  }

  private static BlockPos findTripwireHook(ArmorStand armorStand) {
    BlockPos pos = armorStand.blockPosition();
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

    for (int i = 1; i <= 3; i++) {
      mutable.set(pos.getX(), pos.getY() + i, pos.getZ());
      BlockState state = armorStand.level().getBlockState(mutable);
      if (state.is(Blocks.TRIPWIRE_HOOK)) {
        return mutable.immutable();
      }
    }

    return null;
  }
}
