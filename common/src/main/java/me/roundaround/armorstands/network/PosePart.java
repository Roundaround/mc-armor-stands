package me.roundaround.armorstands.network;

import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.core.Rotations;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.decoration.ArmorStand;

public enum PosePart {
  HEAD(0, "head"),
  BODY(1, "body"),
  RIGHT_ARM(2, "rightArm"),
  LEFT_ARM(3, "leftArm"),
  RIGHT_LEG(4, "rightLeg"),
  LEFT_LEG(5, "leftLeg");

  public static final IntFunction<PosePart> ID_TO_VALUE_FUNCTION = ByIdMap.continuous(
      PosePart::getIndex,
      values(),
      ByIdMap.OutOfBoundsStrategy.CLAMP
  );
  public static final StreamCodec<ByteBuf, PosePart> PACKET_CODEC = ByteBufCodecs.idMapper(
      ID_TO_VALUE_FUNCTION,
      PosePart::getIndex
  );

  private final int index;
  private final String id;

  PosePart(int index, String id) {
    this.index = index;
    this.id = id;
  }

  @Override
  public String toString() {
    return id;
  }

  public int getIndex() {
    return this.index;
  }

  public Component getDisplayName() {
    return Component.translatable("armorstands.part." + id);
  }

  public Rotations get(ArmorStand armorStand) {
    return switch (this) {
      case HEAD -> armorStand.getHeadPose();
      case BODY -> armorStand.getBodyPose();
      case RIGHT_ARM -> armorStand.getRightArmPose();
      case LEFT_ARM -> armorStand.getLeftArmPose();
      case RIGHT_LEG -> armorStand.getRightLegPose();
      case LEFT_LEG -> armorStand.getLeftLegPose();
    };
  }

  public void set(ArmorStand armorStand, Rotations angle) {
    Consumer<Rotations> consumer = switch (this) {
      case HEAD -> armorStand::setHeadPose;
      case BODY -> armorStand::setBodyPose;
      case RIGHT_ARM -> armorStand::setRightArmPose;
      case LEFT_ARM -> armorStand::setLeftArmPose;
      case RIGHT_LEG -> armorStand::setRightLegPose;
      case LEFT_LEG -> armorStand::setLeftLegPose;
    };
    consumer.accept(angle);
  }

  public void set(ArmorStand armorStand, EulerAngleParameter parameter, float value) {
    set(armorStand, parameter.set(get(armorStand), value));
  }

  public static PosePart fromString(String value) {
    return Arrays.stream(PosePart.values())
        .filter((flag) -> flag.id.equals(value))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown part: " + value));
  }
}
