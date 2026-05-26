package me.roundaround.armorstands.util.actions;

import java.util.Collection;
import java.util.List;

import me.roundaround.armorstands.network.ArmorStandFlag;
import me.roundaround.armorstands.util.PosePreset;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;

public class PrepareAction extends ComboAction {
  private PrepareAction(Collection<ArmorStandAction> actions) {
    super(Component.translatable("armorstands.action.prepare"), actions);
  }

  public static PrepareAction create(ArmorStand armorStand) {
    return new PrepareAction(List.of(
        FlagAction.set(ArmorStandFlag.SHOW_ARMS, true),
        FlagAction.set(ArmorStandFlag.HIDE_BASE_PLATE, true),
        FlagAction.set(ArmorStandFlag.INVISIBLE, false),
        FlagAction.set(ArmorStandFlag.NO_GRAVITY, true),
        PoseAction.fromPose(PosePreset.DEFAULT.toPose())));
  }
}
