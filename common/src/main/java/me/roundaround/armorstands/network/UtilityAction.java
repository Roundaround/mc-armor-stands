package me.roundaround.armorstands.network;

import io.netty.buffer.ByteBuf;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.interfaces.EntityPosition;
import me.roundaround.armorstands.server.network.ServerNetworking;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import me.roundaround.armorstands.util.ArmorStandEditor;
import me.roundaround.armorstands.util.ArmorStandHelper;
import me.roundaround.armorstands.util.Clipboard;
import me.roundaround.armorstands.util.actions.*;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import java.util.Arrays;

public enum UtilityAction {
  COPY("copy"),
  PASTE("paste"),
  PREPARE("prepare"),
  TOOL_RACK("tool_rack"),
  UPRIGHT_ITEM("upright_item"),
  UPRIGHT_ITEM_SMALL("upright_item_small"),
  FLAT_ITEM("flat_item"),
  FLAT_ITEM_SMALL("flat_item_small"),
  BLOCK("block"),
  BLOCK_SMALL("block_small"),
  TOOL("tool"),
  TOOL_SMALL("tool_small"),
  SNAP_CORNER("snap_corner"),
  SNAP_CENTER("snap_center"),
  SNAP_STANDING("snap_standing"),
  SNAP_SITTING("snap_sitting"),
  SNAP_PLAYER("snap_player"),
  FACE_TOWARD("face_toward"),
  FACE_AWAY("face_away"),
  FACE_WITH("face_with"),
  UNKNOWN("unknown");

  private static final Logger LOGGER = LogManager.getLogger(Constants.MOD_ID);

  public static final StreamCodec<ByteBuf, UtilityAction> PACKET_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8, UtilityAction::getId, UtilityAction::fromString);

  private final String id;

  UtilityAction(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return id;
  }

  public String getId() {
    return this.id;
  }

  public void apply(ArmorStandEditor editor, ServerPlayer player) {
    ArmorStand armorStand = editor.getArmorStand();

    switch (this) {
      case COPY:
        Clipboard.copy(player, armorStand);
        break;
      case PASTE:
        Clipboard.paste(player, editor);
        break;
      case PREPARE:
        editor.applyAction(PrepareAction.create(armorStand));
        break;
      case TOOL_RACK:
        ToolRackAction action = ToolRackAction.create(armorStand);
        if (action == null) {
          ServerNetworking.sendMessagePacket(player, "armorstands.utility.toolRack.noHook", 0xFF0000);
        } else {
          editor.applyAction(action);
        }
        break;
      case UPRIGHT_ITEM:
      case UPRIGHT_ITEM_SMALL:
        editor.applyAction(HoldingAction.uprightItem(armorStand, this == UPRIGHT_ITEM_SMALL));
        break;
      case FLAT_ITEM:
      case FLAT_ITEM_SMALL:
        editor.applyAction(HoldingAction.flatItem(armorStand, this == FLAT_ITEM_SMALL));
        break;
      case BLOCK:
      case BLOCK_SMALL:
        editor.applyAction(HoldingAction.block(armorStand, this == BLOCK_SMALL));
        break;
      case TOOL:
      case TOOL_SMALL:
        editor.applyAction(HoldingAction.tool(armorStand, this == TOOL_SMALL));
        break;
      case SNAP_CORNER:
        editor.setPos(ArmorStandHelper.getCornerPos(armorStand));
        break;
      case SNAP_CENTER:
        editor.setPos(ArmorStandHelper.getCenterPos(armorStand));
        break;
      case SNAP_STANDING:
      case SNAP_SITTING:
        editor.applyAction(SnapToGroundAction.create(this == SNAP_SITTING));
        break;
      case SNAP_PLAYER:
        editor.setPos(((EntityPosition) player).armorstands$getPos());
        break;
      case FACE_TOWARD:
        editor.setRotation(ArmorStandHelper.getLookYaw(armorStand, player.getEyePosition()));
        break;
      case FACE_AWAY:
        editor.setRotation(180 + ArmorStandHelper.getLookYaw(armorStand, player.getEyePosition()));
        break;
      case FACE_WITH:
        editor.setRotation(player.getYRot());
        break;
      case UNKNOWN:
      default:
        editor.applyAction(ArmorStandAction.noop());
    }
  }

  public UtilityAction forSmall(boolean small) {
    return switch (this) {
      case UPRIGHT_ITEM, UPRIGHT_ITEM_SMALL -> small ? UPRIGHT_ITEM_SMALL : UPRIGHT_ITEM;
      case FLAT_ITEM, FLAT_ITEM_SMALL -> small ? FLAT_ITEM_SMALL : FLAT_ITEM;
      case BLOCK, BLOCK_SMALL -> small ? BLOCK_SMALL : BLOCK;
      case TOOL, TOOL_SMALL -> small ? TOOL_SMALL : TOOL;
      default -> this;
    };
  }

  public static UtilityAction fromString(String value) {
    return Arrays.stream(UtilityAction.values())
        .filter((action) -> action.id.equals(value))
        .findFirst()
        .orElseGet(() -> {
          LOGGER.warn("Unknown id '{}'. Defaulting to UNKNOWN.", value);
          return UNKNOWN;
        });
  }
}
