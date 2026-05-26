package me.roundaround.armorstands.util;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ArmorStandHelper {
  public static Vec3 getCornerPos(ArmorStand armorStand) {
    return new Vec3(Math.floor(armorStand.getX()), armorStand.getY(), Math.floor(armorStand.getZ()));
  }

  public static Vec3 getCenterPos(ArmorStand armorStand) {
    return new Vec3(
        Math.round(armorStand.getX() + 0.5) - 0.5,
        armorStand.getY(),
        Math.round(armorStand.getZ() + 0.5) - 0.5
    );
  }

  public static Optional<Vec3> getStandingPos(ArmorStand armorStand) {
    return getGroundPos(armorStand, false);
  }

  public static Optional<Vec3> getSittingPos(ArmorStand armorStand) {
    return getGroundPos(armorStand, true);
  }

  public static Optional<Vec3> getGroundPos(ArmorStand armorStand, boolean sitting) {
    Level world = armorStand.level();
    BlockPos blockPos = armorStand.blockPosition().above(2);
    boolean failed = false;

    while (!world.loadedAndEntityCanStandOn(blockPos.below(), armorStand)) {
      blockPos = blockPos.below();

      if (world.isOutsideBuildHeight(blockPos)) {
        failed = true;
        break;
      }
    }

    if (failed) {
      return Optional.empty();
    }

    Vec3 newPosition = new Vec3(armorStand.getX(), blockPos.getY(), armorStand.getZ());

    if (sitting) {
      newPosition = newPosition.subtract(0, 11 * 0.0625, 0);
    }

    return Optional.of(newPosition);
  }

  public static Vec3 localToRelative(Entity entity, Vec3 amount) {
    float pitch = entity.getXRot();
    float yaw = entity.getYRot();

    float pi = (float) Math.PI;

    float f = Mth.cos((yaw + 90f) * pi / 180f);
    float g = Mth.sin((yaw + 90f) * pi / 180f);
    float h = Mth.cos(-pitch * pi / 180f);
    float i = Mth.sin(-pitch * pi / 180f);
    float j = Mth.cos((-pitch + 90f) * pi / 180f);
    float k = Mth.sin((-pitch + 90f) * pi / 180f);

    Vec3 vec3d2 = new Vec3(f * h, i, g * h);
    Vec3 vec3d3 = new Vec3(f * j, k, g * j);

    Vec3 vec3d4 = vec3d2.cross(vec3d3).scale(-1);

    double x = vec3d2.x * amount.z + vec3d3.x * amount.y + vec3d4.x * amount.x;
    double y = vec3d2.y * amount.z + vec3d3.y * amount.y + vec3d4.y * amount.x;
    double z = vec3d2.z * amount.z + vec3d3.z * amount.y + vec3d4.z * amount.x;

    return new Vec3(x, y, z);
  }

  public static float getLookYaw(ArmorStand armorStand, Vec3 point) {
    Vec3 pos = armorStand.getPositionCodec().getBase();
    double dX = point.x - pos.x;
    double dZ = point.z - pos.z;
    return Mth.wrapDegrees((float) Math.toDegrees(Mth.atan2(dZ, dX)) - 90.0f);
  }
}
