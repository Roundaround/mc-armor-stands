package me.roundaround.armorstands.util;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public enum MoveMode {
  RELATIVE(0, "relative", false, false),
  LOCAL_TO_STAND(1, "stand", true, false),
  LOCAL_TO_PLAYER(2, "player", true, true);

  public static final IntFunction<MoveMode> ID_TO_VALUE_FUNCTION = ByIdMap.continuous(
      MoveMode::getIndex,
      values(),
      ByIdMap.OutOfBoundsStrategy.CLAMP
  );
  public static final StreamCodec<ByteBuf, MoveMode> PACKET_CODEC = ByteBufCodecs.idMapper(
      ID_TO_VALUE_FUNCTION,
      MoveMode::getIndex
  );

  private final int index;
  private final String id;
  private final boolean local;
  private final boolean player;

  MoveMode(int index, String id, boolean local, boolean player) {
    this.index = index;
    this.id = id;
    this.local = local;
    this.player = player;
  }

  public int getIndex() {
    return this.index;
  }

  public String getId() {
    return this.id;
  }

  public boolean isLocal() {
    return this.local;
  }

  public boolean isLocalToPlayer() {
    return this.player;
  }

  public MoveUnits getDefaultUnits() {
    return switch (this) {
      case RELATIVE -> MoveUnits.PIXELS;
      case LOCAL_TO_STAND, LOCAL_TO_PLAYER -> MoveUnits.BLOCKS;
    };

  }

  public Component getOptionValueText() {
    return Component.translatable("armorstands.move.mode." + this.id);
  }

  public Component getDirectionText(Direction direction) {
    if (this.equals(RELATIVE)) {
      return Component.translatable("armorstands.move." + direction.getName());
    } else {
      return Component.translatable("armorstands.move.local." + direction.getName());
    }
  }

  public static Component getOptionLabelText() {
    return Component.translatable("armorstands.move.mode");
  }

  public static MoveMode fromId(String id) {
    for (MoveMode mode : MoveMode.values()) {
      if (mode.getId().equals(id)) {
        return mode;
      }
    }

    return RELATIVE;
  }
}
