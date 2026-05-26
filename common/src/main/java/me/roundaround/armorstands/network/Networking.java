package me.roundaround.armorstands.network;

import me.roundaround.armorstands.client.gui.MessageRenderer;
import me.roundaround.armorstands.client.gui.screen.AbstractArmorStandScreen;
import me.roundaround.armorstands.client.gui.screen.ArmorStandInventoryScreen;
import me.roundaround.armorstands.client.gui.screen.ArmorStandMoveScreen;
import me.roundaround.armorstands.client.gui.screen.ArmorStandPoseScreen;
import me.roundaround.armorstands.client.gui.screen.ArmorStandPresetsScreen;
import me.roundaround.armorstands.client.gui.screen.ArmorStandRotateScreen;
import me.roundaround.armorstands.client.gui.screen.ArmorStandUtilitiesScreen;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.mixin.ArmorStandAccessor;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import me.roundaround.armorstands.interfaces.ArmorStandScreenHandlerAccess;
import me.roundaround.armorstands.server.network.ServerNetworking;
import me.roundaround.armorstands.util.MoveMode;
import me.roundaround.armorstands.util.MoveUnits;
import me.roundaround.armorstands.util.Pose;
import me.roundaround.armorstands.util.PosePreset;
import me.roundaround.armorstands.util.actions.AdjustPosAction;
import me.roundaround.trove.network.TroveNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Supplier;

public final class Networking {
  private Networking() {
  }

  public static final Identifier CLIENT_UPDATE_S2C = Identifier.fromNamespaceAndPath(
      Constants.MOD_ID, "client_update_s2c");
  public static final Identifier MESSAGE_S2C = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "message_s2c");
  public static final Identifier OPEN_SCREEN_S2C = Identifier.fromNamespaceAndPath(
      Constants.MOD_ID, "open_screen_s2c");
  public static final Identifier PONG_S2C = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pong_s2c");

  public static final Identifier ADJUST_POSE_C2S = Identifier.fromNamespaceAndPath(
      Constants.MOD_ID, "adjust_pose_c2s");
  public static final Identifier ADJUST_POS_C2S = Identifier.fromNamespaceAndPath(
      Constants.MOD_ID, "adjust_pos_c2s");
  public static final Identifier ADJUST_YAW_C2S = Identifier.fromNamespaceAndPath(
      Constants.MOD_ID, "adjust_yaw_c2s");
  public static final Identifier PING_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "ping_c2s");
  public static final Identifier REQUEST_SCREEN_C2S = Identifier.fromNamespaceAndPath(
      Constants.MOD_ID, "request_screen_c2s");
  public static final Identifier SET_FLAG_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "set_flag_c2s");
  public static final Identifier SET_POSE_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "set_pose_c2s");
  public static final Identifier SET_POSE_PRESET_C2S = Identifier.fromNamespaceAndPath(
      Constants.MOD_ID, "set_pose_preset_c2s");
  public static final Identifier SET_SCALE_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "set_scale_c2s");
  public static final Identifier SET_YAW_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "set_yaw_c2s");
  public static final Identifier UNDO_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "undo_c2s");
  public static final Identifier UTILITY_ACTION_C2S = Identifier.fromNamespaceAndPath(
      Constants.MOD_ID, "utility_action_c2s");

  public static void register() {
    TroveNetworking.registerC2S(AdjustPoseC2S.ID, AdjustPoseC2S.CODEC, (payload, player) -> {
      if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
      sh.getEditor().adjustPose(payload.part(), payload.parameter(), payload.amount());
    });

    TroveNetworking.registerC2S(AdjustPosC2S.ID, AdjustPosC2S.CODEC, (payload, player) -> {
      if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
      MoveMode mode = payload.mode();
      sh.getEditor().applyAction(mode.isLocal()
          ? AdjustPosAction.local(payload.direction(), payload.amount(), payload.units(), mode.isLocalToPlayer())
          : AdjustPosAction.relative(payload.direction(), payload.amount(), payload.units()));
    });

    TroveNetworking.registerC2S(AdjustYawC2S.ID, AdjustYawC2S.CODEC, (payload, player) -> {
      if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
      sh.getEditor().rotate(payload.amount());
    });

    TroveNetworking.registerC2S(PingC2S.ID, PingC2S.CODEC, (payload, player) -> {
      ServerNetworking.sendPongPacket(player);
    });

    TroveNetworking.registerC2S(RequestScreenC2S.ID, RequestScreenC2S.CODEC, (payload, player) -> {
      if (!(player.level().getEntity(payload.armorStandId()) instanceof ArmorStand armorStand)) return;
      ((ArmorStandScreenHandlerAccess) player).armorstands$openScreen(armorStand, payload.screenType());
    });

    TroveNetworking.registerC2S(SetFlagC2S.ID, SetFlagC2S.CODEC, (payload, player) -> {
      if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
      sh.getEditor().setFlag(payload.flag(), payload.value());
      ServerNetworking.sendClientUpdatePacket(player, sh.getEditor().getArmorStand());
    });

    TroveNetworking.registerC2S(SetPoseC2S.ID, SetPoseC2S.CODEC, (payload, player) -> {
      if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
      sh.getEditor().setPose(
          payload.head(), payload.body(), payload.rightArm(),
          payload.leftArm(), payload.rightLeg(), payload.leftLeg());
    });

    TroveNetworking.registerC2S(SetPosePresetC2S.ID, SetPosePresetC2S.CODEC, (payload, player) -> {
      if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
      sh.getEditor().setPose(payload.pose().toPose());
    });

    TroveNetworking.registerC2S(SetScaleC2S.ID, SetScaleC2S.CODEC, (payload, player) -> {
      if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
      sh.getEditor().setScale(payload.scale());
    });

    TroveNetworking.registerC2S(SetYawC2S.ID, SetYawC2S.CODEC, (payload, player) -> {
      if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
      sh.getEditor().setRotation(payload.angle());
    });

    TroveNetworking.registerC2S(UndoC2S.ID, UndoC2S.CODEC, (payload, player) -> {
      if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
      Supplier<Boolean> action = payload.redo() ? sh.getEditor()::redo : sh.getEditor()::undo;
      String successMessage = payload.redo() ? "armorstands.message.redo" : "armorstands.message.undo";
      String failureMessage = payload.redo() ? "armorstands.message.redo.fail" : "armorstands.message.undo.fail";
      if (action.get()) {
        ServerNetworking.sendMessagePacket(player, successMessage);
      } else {
        ServerNetworking.sendMessagePacket(player, failureMessage);
      }
    });

    TroveNetworking.registerC2S(UtilityActionC2S.ID, UtilityActionC2S.CODEC, (payload, player) -> {
      if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
      payload.action().apply(sh.getEditor(), player);
    });

    TroveNetworking.registerS2C(ClientUpdateS2C.ID, ClientUpdateS2C.CODEC, (payload) -> {
      if (!(Minecraft.getInstance().screen instanceof AbstractArmorStandScreen screen)) return;
      screen.updatePosOnClient(payload.x(), payload.y(), payload.z());
      screen.updateYawOnClient(Mth.wrapDegrees(payload.yaw()));
      screen.updatePitchOnClient(Mth.wrapDegrees(payload.pitch()));
      screen.updateInvulnerableOnClient(payload.invulnerable());
      screen.updateDisabledSlotsOnClient(payload.disabledSlots());
    });

    TroveNetworking.registerS2C(MessageS2C.ID, MessageS2C.CODEC, (payload) -> {
      if (!(Minecraft.getInstance().screen instanceof AbstractArmorStandScreen screen)) return;
      screen.getMessageRenderer().addMessage(
          payload.translatable() ? Component.translatable(payload.message()) : Component.literal(payload.message()),
          payload.styled() ? payload.color() : MessageRenderer.BASE_COLOR);
    });

    TroveNetworking.registerS2C(OpenScreenS2C.ID, OpenScreenS2C.CODEC, (payload) -> {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player == null) return;
      if (!(player.level().getEntity(payload.armorStandId()) instanceof ArmorStand armorStand)) return;

      ArmorStandScreenHandler screenHandler = new ArmorStandScreenHandler(
          payload.syncId(), player.getInventory(), armorStand, payload.screenType());
      player.containerMenu = screenHandler;
      mc.setScreen(switch (screenHandler.getScreenType()) {
        case UTILITIES -> new ArmorStandUtilitiesScreen(screenHandler);
        case MOVE -> new ArmorStandMoveScreen(screenHandler);
        case ROTATE -> new ArmorStandRotateScreen(screenHandler);
        case POSE -> new ArmorStandPoseScreen(screenHandler);
        case PRESETS -> new ArmorStandPresetsScreen(screenHandler);
        case INVENTORY -> new ArmorStandInventoryScreen(screenHandler);
      });
    });

    TroveNetworking.registerS2C(PongS2C.ID, PongS2C.CODEC, (payload) -> {
      if (!(Minecraft.getInstance().screen instanceof AbstractArmorStandScreen screen)) return;
      screen.onPong();
    });
  }

  public record ClientUpdateS2C(double x,
                                double y,
                                double z,
                                float yaw,
                                float pitch,
                                boolean invulnerable,
                                int disabledSlots) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientUpdateS2C> ID =
        new CustomPacketPayload.Type<>(CLIENT_UPDATE_S2C);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientUpdateS2C> CODEC =
        StreamCodec.ofMember(ClientUpdateS2C::write, ClientUpdateS2C::new);

    public ClientUpdateS2C(ArmorStand armorStand) {
      this(
          armorStand.getX(),
          armorStand.getY(),
          armorStand.getZ(),
          armorStand.getYRot(),
          armorStand.getXRot(),
          armorStand.isInvulnerable(),
          ((ArmorStandAccessor) armorStand).getDisabledSlots()
      );
    }

    private ClientUpdateS2C(FriendlyByteBuf buf) {
      this(
          buf.readDouble(), buf.readDouble(), buf.readDouble(),
          buf.readFloat(), buf.readFloat(),
          buf.readBoolean(), buf.readInt()
      );
    }

    private void write(FriendlyByteBuf buf) {
      buf.writeDouble(this.x);
      buf.writeDouble(this.y);
      buf.writeDouble(this.z);
      buf.writeFloat(this.yaw);
      buf.writeFloat(this.pitch);
      buf.writeBoolean(this.invulnerable);
      buf.writeInt(this.disabledSlots);
    }

    @Override
    @NotNull
    public Type<ClientUpdateS2C> type() {
      return ID;
    }
  }

  public record MessageS2C(boolean translatable, String message, boolean styled, int color) implements
      CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageS2C> ID = new CustomPacketPayload.Type<>(MESSAGE_S2C);
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageS2C> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, MessageS2C::translatable,
        ByteBufCodecs.STRING_UTF8, MessageS2C::message,
        ByteBufCodecs.BOOL, MessageS2C::styled,
        ByteBufCodecs.INT, MessageS2C::color,
        MessageS2C::new);

    public MessageS2C(String message) {
      this(true, message);
    }

    public MessageS2C(String message, int color) {
      this(true, message, color);
    }

    public MessageS2C(boolean translatable, String message) {
      this(translatable, message, false, -1);
    }

    public MessageS2C(boolean translatable, String message, int color) {
      this(translatable, message, true, color);
    }

    @Override
    @NotNull
    public Type<MessageS2C> type() {
      return ID;
    }
  }

  public record OpenScreenS2C(int syncId, int armorStandId, ScreenType screenType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenScreenS2C> ID = new CustomPacketPayload.Type<>(OPEN_SCREEN_S2C);
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenScreenS2C> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, OpenScreenS2C::syncId,
        ByteBufCodecs.INT, OpenScreenS2C::armorStandId,
        ScreenType.PACKET_CODEC, OpenScreenS2C::screenType,
        OpenScreenS2C::new);

    @Override
    @NotNull
    public Type<OpenScreenS2C> type() {
      return ID;
    }
  }

  public record PongS2C(UUID playerUuid) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PongS2C> ID = new CustomPacketPayload.Type<>(PONG_S2C);
    public static final StreamCodec<RegistryFriendlyByteBuf, PongS2C> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, PongS2C::playerUuid, PongS2C::new);

    @Override
    @NotNull
    public Type<PongS2C> type() {
      return ID;
    }
  }

  public record AdjustPoseC2S(PosePart part,
                              EulerAngleParameter parameter,
                              float amount) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AdjustPoseC2S> ID = new CustomPacketPayload.Type<>(ADJUST_POSE_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, AdjustPoseC2S> CODEC = StreamCodec.composite(
        PosePart.PACKET_CODEC, AdjustPoseC2S::part,
        EulerAngleParameter.PACKET_CODEC, AdjustPoseC2S::parameter,
        ByteBufCodecs.FLOAT, AdjustPoseC2S::amount,
        AdjustPoseC2S::new);

    @Override
    @NotNull
    public Type<AdjustPoseC2S> type() {
      return ID;
    }
  }

  public record AdjustPosC2S(Direction direction, int amount, MoveMode mode, MoveUnits units) implements
      CustomPacketPayload {
    public static final CustomPacketPayload.Type<AdjustPosC2S> ID = new CustomPacketPayload.Type<>(ADJUST_POS_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, AdjustPosC2S> CODEC = StreamCodec.composite(
        Direction.STREAM_CODEC, AdjustPosC2S::direction,
        ByteBufCodecs.INT, AdjustPosC2S::amount,
        MoveMode.PACKET_CODEC, AdjustPosC2S::mode,
        MoveUnits.PACKET_CODEC, AdjustPosC2S::units,
        AdjustPosC2S::new);

    @Override
    @NotNull
    public Type<AdjustPosC2S> type() {
      return ID;
    }
  }

  public record AdjustYawC2S(int amount) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AdjustYawC2S> ID = new CustomPacketPayload.Type<>(ADJUST_YAW_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, AdjustYawC2S> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, AdjustYawC2S::amount, AdjustYawC2S::new);

    @Override
    @NotNull
    public Type<AdjustYawC2S> type() {
      return ID;
    }
  }

  public record PingC2S(UUID playerUuid) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PingC2S> ID = new CustomPacketPayload.Type<>(PING_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, PingC2S> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, PingC2S::playerUuid, PingC2S::new);

    @Override
    @NotNull
    public Type<PingC2S> type() {
      return ID;
    }
  }

  public record RequestScreenC2S(int armorStandId, ScreenType screenType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestScreenC2S> ID =
        new CustomPacketPayload.Type<>(REQUEST_SCREEN_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestScreenC2S> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, RequestScreenC2S::armorStandId,
        ScreenType.PACKET_CODEC, RequestScreenC2S::screenType,
        RequestScreenC2S::new);

    @Override
    @NotNull
    public Type<RequestScreenC2S> type() {
      return ID;
    }
  }

  public record SetFlagC2S(ArmorStandFlag flag, boolean value) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetFlagC2S> ID = new CustomPacketPayload.Type<>(SET_FLAG_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, SetFlagC2S> CODEC = StreamCodec.composite(
        ArmorStandFlag.PACKET_CODEC, SetFlagC2S::flag,
        ByteBufCodecs.BOOL, SetFlagC2S::value,
        SetFlagC2S::new);

    @Override
    @NotNull
    public Type<SetFlagC2S> type() {
      return ID;
    }
  }

  public record SetPoseC2S(Rotations head,
                           Rotations body,
                           Rotations rightArm,
                           Rotations leftArm,
                           Rotations rightLeg,
                           Rotations leftLeg) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetPoseC2S> ID = new CustomPacketPayload.Type<>(SET_POSE_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, SetPoseC2S> CODEC = StreamCodec.ofMember(
        SetPoseC2S::write, SetPoseC2S::new);

    public SetPoseC2S(Pose pose) {
      this(pose.getHead(), pose.getBody(), pose.getRightArm(),
          pose.getLeftArm(), pose.getRightLeg(), pose.getLeftLeg());
    }

    private SetPoseC2S(FriendlyByteBuf buf) {
      this(
          NetworkHelpers.readEulerAngle(buf), NetworkHelpers.readEulerAngle(buf),
          NetworkHelpers.readEulerAngle(buf), NetworkHelpers.readEulerAngle(buf),
          NetworkHelpers.readEulerAngle(buf), NetworkHelpers.readEulerAngle(buf));
    }

    private void write(FriendlyByteBuf buf) {
      NetworkHelpers.writeEulerAngle(buf, this.head);
      NetworkHelpers.writeEulerAngle(buf, this.body);
      NetworkHelpers.writeEulerAngle(buf, this.rightArm);
      NetworkHelpers.writeEulerAngle(buf, this.leftArm);
      NetworkHelpers.writeEulerAngle(buf, this.rightLeg);
      NetworkHelpers.writeEulerAngle(buf, this.leftLeg);
    }

    @Override
    @NotNull
    public Type<SetPoseC2S> type() {
      return ID;
    }
  }

  public record SetPosePresetC2S(PosePreset pose) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetPosePresetC2S> ID =
        new CustomPacketPayload.Type<>(SET_POSE_PRESET_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, SetPosePresetC2S> CODEC = StreamCodec.composite(
        PosePreset.PACKET_CODEC, SetPosePresetC2S::pose, SetPosePresetC2S::new);

    @Override
    @NotNull
    public Type<SetPosePresetC2S> type() {
      return ID;
    }
  }

  public record SetScaleC2S(float scale) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetScaleC2S> ID = new CustomPacketPayload.Type<>(SET_SCALE_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, SetScaleC2S> CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, SetScaleC2S::scale, SetScaleC2S::new);

    @Override
    @NotNull
    public Type<SetScaleC2S> type() {
      return ID;
    }
  }

  public record SetYawC2S(float angle) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetYawC2S> ID = new CustomPacketPayload.Type<>(SET_YAW_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, SetYawC2S> CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, SetYawC2S::angle, SetYawC2S::new);

    @Override
    @NotNull
    public Type<SetYawC2S> type() {
      return ID;
    }
  }

  public record UndoC2S(boolean redo) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UndoC2S> ID = new CustomPacketPayload.Type<>(UNDO_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, UndoC2S> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, UndoC2S::redo, UndoC2S::new);

    @Override
    @NotNull
    public Type<UndoC2S> type() {
      return ID;
    }
  }

  public record UtilityActionC2S(UtilityAction action) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UtilityActionC2S> ID =
        new CustomPacketPayload.Type<>(UTILITY_ACTION_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, UtilityActionC2S> CODEC = StreamCodec.composite(
        UtilityAction.PACKET_CODEC, UtilityActionC2S::action, UtilityActionC2S::new);

    @Override
    @NotNull
    public Type<UtilityActionC2S> type() {
      return ID;
    }
  }
}
