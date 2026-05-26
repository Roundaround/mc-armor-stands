package me.roundaround.armorstands.util.actions;

import me.roundaround.armorstands.interfaces.EntityPosition;
import me.roundaround.armorstands.util.ArmorStandHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;

public class MoveAction implements ArmorStandAction {
  private final Vec3 argument;
  private final MoveType moveType;
  private final boolean roundToPixel;
  private final boolean localToPlayer;
  private Optional<Vec3> originalPosition = Optional.empty();

  private MoveAction(Vec3 argument, MoveType moveType, boolean roundToPixel, boolean localToPlayer) {
    this.argument = argument;
    this.moveType = moveType;
    this.roundToPixel = roundToPixel;
    this.localToPlayer = localToPlayer;
  }

  public static MoveAction absolute(Vec3 position) {
    return new MoveAction(position, MoveType.ABSOLUTE, false, false);
  }

  public static MoveAction absolute(double x, double y, double z) {
    return absolute(new Vec3(x, y, z));
  }

  public static MoveAction relative(Vec3 position, boolean roundToPixel) {
    return new MoveAction(position, MoveType.RELATIVE, roundToPixel, false);
  }

  public static MoveAction relative(double x, double y, double z, boolean roundToPixel) {
    return relative(new Vec3(x, y, z), roundToPixel);
  }

  public static MoveAction relative(Vec3 position) {
    return relative(position, false);
  }

  public static MoveAction relative(double x, double y, double z) {
    return relative(x, y, z, false);
  }

  public static MoveAction local(Vec3 position) {
    return new MoveAction(position, MoveType.LOCAL, false, false);
  }

  public static MoveAction local(double x, double y, double z) {
    return local(new Vec3(x, y, z));
  }

  public static MoveAction local(Vec3 position, boolean localToPlayer) {
    return new MoveAction(position, MoveType.LOCAL, false, localToPlayer);
  }

  public static MoveAction local(double x, double y, double z, boolean localToPlayer) {
    return local(new Vec3(x, y, z), localToPlayer);
  }

  @Override
  public Component getName(ArmorStand armorStand) {
    return Component.translatable("armorstands.action.move");
  }

  @Override
  public void apply(Player player, ArmorStand armorStand) {
    this.originalPosition = Optional.of(((EntityPosition) armorStand).armorstands$getPos());

    Vec3 position = switch (this.moveType) {
      case ABSOLUTE -> this.argument;
      case RELATIVE -> this.originalPosition.get().add(this.argument);
      case LOCAL -> this.originalPosition.get()
          .add(ArmorStandHelper.localToRelative(this.localToPlayer ? player : armorStand, this.argument));
    };

    if (this.roundToPixel) {
      double x = Math.round(position.x * 16) / 16.0;
      double y = Math.round(position.y * 16) / 16.0;
      double z = Math.round(position.z * 16) / 16.0;
      position = new Vec3(x, y, z);
    }

    setPosition(armorStand, position);
  }

  @Override
  public void undo(Player player, ArmorStand armorStand) {
    if (this.originalPosition.isEmpty()) {
      return;
    }

    setPosition(armorStand, this.originalPosition.get());
  }

  public static void setPosition(ArmorStand armorStand, Vec3 position) {
    setPosition(armorStand, position.x, position.y, position.z);
  }

  public static void setPosition(ArmorStand armorStand, double x, double y, double z) {
    // Adjust y pos up just the tiniest bit if gravity is enabled to prevent the
    // stand from falling through the floor when rounding errors occur.
    boolean hasGravity = !armorStand.isNoGravity();
    double currY = armorStand.getY();
    if (hasGravity && Math.abs(y - currY) > Mth.EPSILON) {
      y += 0.001;
    }

    armorStand.syncPacketPositionCodec(x, y, z);
    armorStand.setPos(new Vec3(x, y, z));
  }

  public enum MoveType {
    ABSOLUTE("absolute"), RELATIVE("relative"), LOCAL("local");

    private final String id;

    MoveType(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }

    public static MoveType fromId(String id) {
      for (MoveType type : values()) {
        if (type.id.equals(id)) {
          return type;
        }
      }

      return ABSOLUTE;
    }
  }
}
