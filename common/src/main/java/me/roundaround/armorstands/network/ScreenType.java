package me.roundaround.armorstands.network;

import io.netty.buffer.ByteBuf;
import me.roundaround.trove.client.gui.icon.BuiltinIcon;
import me.roundaround.trove.client.gui.icon.CustomIcon;
import me.roundaround.trove.client.gui.icon.Icon;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import java.util.Arrays;
import java.util.function.IntFunction;

public enum ScreenType {
  UTILITIES(0, "utilities", new CustomIcon("flag", 20)),
  MOVE(1, "move", BuiltinIcon.MOVE_18),
  ROTATE(2, "rotate", BuiltinIcon.ROTATE_18),
  POSE(3, "pose", BuiltinIcon.SLIDERS_18),
  PRESETS(4, "presets", new CustomIcon("pose", 20)),
  INVENTORY(5, "inventory", new CustomIcon("inventory", 20));

  public static final IntFunction<ScreenType> ID_TO_VALUE_FUNCTION = ByIdMap.continuous(
      ScreenType::getIndex,
      values(),
      ByIdMap.OutOfBoundsStrategy.ZERO
  );
  public static final StreamCodec<ByteBuf, ScreenType> PACKET_CODEC = ByteBufCodecs.idMapper(
      ID_TO_VALUE_FUNCTION,
      ScreenType::getIndex
  );

  private final int index;
  private final String id;
  private final Icon icon;

  ScreenType(int index, String id, Icon icon) {
    this.index = index;
    this.id = id;
    this.icon = icon;
  }

  public int getIndex() {
    return this.index;
  }

  public String getId() {
    return this.id;
  }

  public Icon getIcon() {
    return this.icon;
  }

  public Component getDisplayName() {
    return Component.translatable("armorstands.screen." + id);
  }

  public boolean usesInventory() {
    return this == INVENTORY;
  }

  public ScreenType next() {
    int index = ordinal() + 1;
    if (index >= values().length) {
      index = 0;
    }
    return values()[index];
  }

  public ScreenType previous() {
    int index = ordinal() - 1;
    if (index < 0) {
      index = values().length - 1;
    }
    return values()[index];
  }

  public static ScreenType fromId(String id) {
    return Arrays.stream(values()).filter(type -> type.getId().equals(id)).findFirst().orElse(null);
  }
}
