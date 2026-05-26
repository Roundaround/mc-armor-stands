package me.roundaround.armorstands.util;

import me.roundaround.armorstands.network.ArmorStandFlag;
import me.roundaround.armorstands.network.EulerAngleParameter;
import me.roundaround.armorstands.network.PosePart;
import me.roundaround.armorstands.util.actions.*;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Stack;
import java.util.UUID;

public class ArmorStandEditor {
  private static final HashMap<UUID, Tuple<UUID, ArmorStandEditor>> editors = new HashMap<>();

  private final ServerPlayer player;
  private final ArmorStand armorStand;
  private final SizeLimitedStack<ArmorStandAction> actions = new SizeLimitedStack<>(30);
  private final SizeLimitedStack<ArmorStandAction> undos = new SizeLimitedStack<>(30);

  public static ArmorStandEditor get(ServerPlayer player, ArmorStand armorStand) {
    UUID uuid = player.getUUID();

    if (!editors.containsKey(uuid)) {
      editors.put(uuid, new Tuple<>(armorStand.getUUID(), new ArmorStandEditor(player, armorStand)));
    }

    Tuple<UUID, ArmorStandEditor> pair = editors.get(uuid);
    if (!pair.getA().equals(armorStand.getUUID())) {
      editors.put(uuid, new Tuple<>(armorStand.getUUID(), new ArmorStandEditor(player, armorStand)));
    }

    return editors.get(uuid).getB();
  }

  public static void remove(ServerPlayer player) {
    editors.remove(player.getUUID());
  }

  private ArmorStandEditor(ServerPlayer player, ArmorStand armorStand) {
    this.player = player;
    this.armorStand = armorStand;
  }

  public ArmorStand getArmorStand() {
    return this.armorStand;
  }

  public void applyAction(ArmorStandAction action) {
    if (action == null) {
      return;
    }
    action.apply(this.player, this.armorStand);
    this.actions.push(action);
    this.undos.clear();
  }

  public boolean undo() {
    if (this.actions.isEmpty()) {
      return false;
    }

    ArmorStandAction action = this.actions.pop();
    if (action == null) {
      return false;
    }

    action.undo(this.player, this.armorStand);
    this.undos.push(action);
    return true;
  }

  public boolean redo() {
    if (this.undos.isEmpty()) {
      return false;
    }

    ArmorStandAction action = this.undos.pop();
    if (action == null) {
      return false;
    }

    action.apply(this.player, this.armorStand);
    this.actions.push(action);
    return true;
  }

  public void movePos(Direction direction, int pixels) {
    movePos(new Vec3(direction.step()).scale(pixels * 0.0625), true);
  }

  public void movePos(Vec3 amount) {
    movePos(amount, false);
  }

  public void movePos(Vec3 amount, boolean roundToPixel) {
    applyAction(MoveAction.relative(amount, roundToPixel));
  }

  public void setPos(double x, double y, double z) {
    setPos(new Vec3(x, y, z));
  }

  public void setPos(Vec3 position) {
    applyAction(MoveAction.absolute(position));
  }

  public void rotate(float rotation) {
    applyAction(RotateAction.relative(rotation));
  }

  public void setRotation(float rotation) {
    applyAction(RotateAction.absolute(rotation));
  }

  public void scale(float scale) {
    applyAction(ScaleAction.relative(scale));
  }

  public void setScale(float scale) {
    applyAction(ScaleAction.absolute(scale));
  }

  public void toggleFlag(ArmorStandFlag flag) {
    applyAction(FlagAction.toggle(flag));
  }

  public void setFlag(ArmorStandFlag flag, boolean value) {
    applyAction(FlagAction.set(flag, value));
  }

  public void setPose(Pose pose) {
    applyAction(PoseAction.fromPose(pose));
  }

  public void setPose(
      Rotations head, Rotations body, Rotations rightArm, Rotations leftArm, Rotations rightLeg, Rotations leftLeg
  ) {
    setPose(new Pose(head, body, rightArm, leftArm, rightLeg, leftLeg));
  }

  public void adjustPose(PosePart part, EulerAngleParameter parameter, float amount) {
    applyAction(AdjustPoseAction.absolute(part, parameter, amount));
  }

  private static class SizeLimitedStack<T> extends Stack<T> {
    private final int limit;

    public SizeLimitedStack(int limit) {
      this.limit = limit;
    }

    @Override
    public T push(T obj) {
      while (size() >= limit) {
        remove(0);
      }
      return super.push(obj);
    }
  }
}
