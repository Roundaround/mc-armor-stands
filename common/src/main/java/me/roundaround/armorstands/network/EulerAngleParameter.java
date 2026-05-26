package me.roundaround.armorstands.network;

import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.function.IntFunction;
import net.minecraft.core.Rotations;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public enum EulerAngleParameter {
  PITCH(0, "pitch"), YAW(1, "yaw"), ROLL(2, "roll");

  public static final IntFunction<EulerAngleParameter> ID_TO_VALUE_FUNCTION =
      ByIdMap.continuous(EulerAngleParameter::getIndex,
      values(),
      ByIdMap.OutOfBoundsStrategy.CLAMP
  );
  public static final StreamCodec<ByteBuf, EulerAngleParameter> PACKET_CODEC = ByteBufCodecs.idMapper(
      ID_TO_VALUE_FUNCTION,
      EulerAngleParameter::getIndex
  );

  private final int index;
  private final String id;

  EulerAngleParameter(int index, String id) {
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
    return Component.translatable("armorstands.parameter." + id);
  }

  public float get(Rotations angle) {
    return switch (this) {
      case PITCH -> angle.x();
      case YAW -> angle.y();
      case ROLL -> angle.z();
    };
  }

  public Rotations set(Rotations angle, float value) {
    return switch (this) {
      case PITCH -> new Rotations(value, angle.y(), angle.z());
      case YAW -> new Rotations(angle.x(), value, angle.z());
      case ROLL -> new Rotations(angle.x(), angle.y(), value);
    };
  }

  public static EulerAngleParameter fromString(String string) {
    return Arrays.stream(values())
        .filter(parameter -> parameter.id.equals(string))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown parameter: " + string));
  }
}
