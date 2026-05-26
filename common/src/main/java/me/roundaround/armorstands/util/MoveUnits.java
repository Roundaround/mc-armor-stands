package me.roundaround.armorstands.util;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public enum MoveUnits {
  PIXELS(0, "pixels"), BLOCKS(1, "blocks");

  public static final IntFunction<MoveUnits> ID_TO_VALUE_FUNCTION = ByIdMap.continuous(
      MoveUnits::getIndex,
      values(),
      ByIdMap.OutOfBoundsStrategy.CLAMP
  );
  public static final StreamCodec<ByteBuf, MoveUnits> PACKET_CODEC = ByteBufCodecs.idMapper(
      ID_TO_VALUE_FUNCTION,
      MoveUnits::getIndex
  );

  private final int index;
  private final String id;

  MoveUnits(int index, String id) {
    this.index = index;
    this.id = id;
  }

  public int getIndex() {
    return this.index;
  }

  public String getId() {
    return this.id;
  }

  public Component getOptionValueText() {
    return Component.translatable("armorstands.move.units." + this.id);
  }

  public Component getButtonText(int amount) {
    return Component.translatable("armorstands.move." + this.id + "." + amount);
  }

  public double getAmount(int amount) {
    return switch (this) {
      case PIXELS -> getPixelAmount(amount);
      case BLOCKS -> getBlockAmount(amount);
    };
  }

  private static double getPixelAmount(int amount) {
    // 1, 3, and 8 pixels
    return switch (amount) {
      case 2 -> 0.1875;
      case 3 -> 0.5;
      default -> 0.0625;
    };
  }

  private static double getBlockAmount(int amount) {
    // 1/4, 1/3, and 1 block
    return switch (amount) {
      case 2 -> 1d / 3d;
      case 3 -> 1;
      default -> 0.25;
    };
  }

  public static Component getOptionLabelText() {
    return Component.translatable("armorstands.move.units");
  }

  public static MoveUnits fromId(String id) {
    for (MoveUnits units : MoveUnits.values()) {
      if (units.getId().equals(id)) {
        return units;
      }
    }

    return PIXELS;
  }
}
