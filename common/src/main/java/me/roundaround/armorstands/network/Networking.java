package me.roundaround.armorstands.network;

import me.roundaround.armorstands.client.network.ClientNetworking;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.mixin.ArmorStandAccessor;
import me.roundaround.armorstands.server.network.ServerNetworking;
import me.roundaround.armorstands.util.MoveMode;
import me.roundaround.armorstands.util.MoveUnits;
import me.roundaround.armorstands.util.Pose;
import me.roundaround.armorstands.util.PosePreset;
import me.roundaround.trove.network.TroveNetworking;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class Networking {
  public static final Identifier CLIENT_UPDATE_S2C = Identifier.fromNamespaceAndPath(
      Constants.MOD_ID,
      "client_update_s2c"
  );
  public static final Identifier MANNEQUIN_SETTINGS_S2C = Identifier.fromNamespaceAndPath(
      Constants.MOD_ID,
      "mannequin_settings_s2c"
  );
  public static final Identifier MESSAGE_S2C = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "message_s2c");
  public static final Identifier OPEN_SCREEN_S2C = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "open_screen_s2c");
  public static final Identifier PONG_S2C = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pong_s2c");

  public static final Identifier ADJUST_POSE_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "adjust_pose_c2s");
  public static final Identifier ADJUST_POS_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "adjust_pos_c2s");
  public static final Identifier ADJUST_YAW_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "adjust_yaw_c2s");
  public static final Identifier PING_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "ping_c2s");
  public static final Identifier REQUEST_SCREEN_C2S = Identifier.fromNamespaceAndPath(
      Constants.MOD_ID,
      "request_screen_c2s"
  );
  public static final Identifier SET_FLAG_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "set_flag_c2s");
  public static final Identifier SET_MANNEQUIN_FLAG_C2S = Identifier.fromNamespaceAndPath(
      Constants.MOD_ID,
      "set_mannequin_flag_c2s"
  );
  public static final Identifier SET_POSE_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "set_pose_c2s");
  public static final Identifier SET_POSE_PRESET_C2S = Identifier.fromNamespaceAndPath(
      Constants.MOD_ID,
      "set_pose_preset_c2s"
  );
  public static final Identifier SET_SCALE_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "set_scale_c2s");
  public static final Identifier SET_YAW_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "set_yaw_c2s");
  public static final Identifier UNDO_C2S = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "undo_c2s");
  public static final Identifier UTILITY_ACTION_C2S = Identifier.fromNamespaceAndPath(
      Constants.MOD_ID,
      "utility_action_c2s"
  );

  public static void register() {
    TroveNetworking.registerC2S(AdjustPoseC2S.ID, AdjustPoseC2S.CODEC, ServerNetworking::handleAdjustPose);
    TroveNetworking.registerC2S(AdjustPosC2S.ID, AdjustPosC2S.CODEC, ServerNetworking::handleAdjustPos);
    TroveNetworking.registerC2S(AdjustYawC2S.ID, AdjustYawC2S.CODEC, ServerNetworking::handleAdjustYaw);
    TroveNetworking.registerC2S(PingC2S.ID, PingC2S.CODEC, ServerNetworking::handlePing);
    TroveNetworking.registerC2S(RequestScreenC2S.ID, RequestScreenC2S.CODEC, ServerNetworking::handleRequestScreen);
    TroveNetworking.registerC2S(SetFlagC2S.ID, SetFlagC2S.CODEC, ServerNetworking::handleSetFlag);
    TroveNetworking.registerC2S(
        SetMannequinFlagC2S.ID,
        SetMannequinFlagC2S.CODEC,
        ServerNetworking::handleSetMannequinFlag
    );
    TroveNetworking.registerC2S(SetPoseC2S.ID, SetPoseC2S.CODEC, ServerNetworking::handleSetPose);
    TroveNetworking.registerC2S(SetPosePresetC2S.ID, SetPosePresetC2S.CODEC, ServerNetworking::handleSetPosePreset);
    TroveNetworking.registerC2S(SetScaleC2S.ID, SetScaleC2S.CODEC, ServerNetworking::handleSetScale);
    TroveNetworking.registerC2S(SetYawC2S.ID, SetYawC2S.CODEC, ServerNetworking::handleSetYaw);
    TroveNetworking.registerC2S(UndoC2S.ID, UndoC2S.CODEC, ServerNetworking::handleUndo);
    TroveNetworking.registerC2S(UtilityActionC2S.ID, UtilityActionC2S.CODEC, ServerNetworking::handleUtilityAction);

    // Explicit lambdas, NOT method refs: register() runs on the dedicated server
    // too (the codecs must be registered to send), and a ClientNetworking::method
    // ref links the metafactory directly to ClientNetworking, loading that
    // client-only class — and the screen classes it constructs — on the server.
    // An explicit lambda defers that load to invocation (client-only).
    TroveNetworking.registerS2C(ClientUpdateS2C.ID, ClientUpdateS2C.CODEC,
        payload -> ClientNetworking.onClientUpdate(payload));
    TroveNetworking.registerS2C(
        MannequinSettingsS2C.ID,
        MannequinSettingsS2C.CODEC,
        payload -> ClientNetworking.onMannequinSettings(payload)
    );
    TroveNetworking.registerS2C(MessageS2C.ID, MessageS2C.CODEC,
        payload -> ClientNetworking.onMessage(payload));
    TroveNetworking.registerS2C(OpenScreenS2C.ID, OpenScreenS2C.CODEC,
        payload -> ClientNetworking.onOpenScreen(payload));
    TroveNetworking.registerS2C(PongS2C.ID, PongS2C.CODEC,
        payload -> ClientNetworking.onPong(payload));
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
        StreamCodec.ofMember(ClientUpdateS2C::write,
        ClientUpdateS2C::new
    );

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
          buf.readDouble(),
          buf.readDouble(),
          buf.readDouble(),
          buf.readFloat(),
          buf.readFloat(),
          buf.readBoolean(),
          buf.readInt()
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

  public record MannequinSettingsS2C(int entityId, int bits) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MannequinSettingsS2C> ID = new CustomPacketPayload.Type<>(
        MANNEQUIN_SETTINGS_S2C);
    public static final StreamCodec<RegistryFriendlyByteBuf, MannequinSettingsS2C> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        MannequinSettingsS2C::entityId,
        ByteBufCodecs.INT,
        MannequinSettingsS2C::bits,
        MannequinSettingsS2C::new
    );

    @Override
    @NotNull
    public Type<MannequinSettingsS2C> type() {
      return ID;
    }
  }

  public record MessageS2C(boolean translatable, String message, boolean styled, int color) implements
      CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageS2C> ID = new CustomPacketPayload.Type<>(MESSAGE_S2C);
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageS2C> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        MessageS2C::translatable,
        ByteBufCodecs.STRING_UTF8,
        MessageS2C::message,
        ByteBufCodecs.BOOL,
        MessageS2C::styled,
        ByteBufCodecs.INT,
        MessageS2C::color,
        MessageS2C::new
    );

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
        ByteBufCodecs.INT,
        OpenScreenS2C::syncId,
        ByteBufCodecs.INT,
        OpenScreenS2C::armorStandId,
        ScreenType.PACKET_CODEC,
        OpenScreenS2C::screenType,
        OpenScreenS2C::new
    );

    @Override
    @NotNull
    public Type<OpenScreenS2C> type() {
      return ID;
    }
  }

  public record PongS2C(UUID playerUuid) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PongS2C> ID = new CustomPacketPayload.Type<>(PONG_S2C);
    public static final StreamCodec<RegistryFriendlyByteBuf, PongS2C> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        PongS2C::playerUuid,
        PongS2C::new
    );

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
        PosePart.PACKET_CODEC,
        AdjustPoseC2S::part,
        EulerAngleParameter.PACKET_CODEC,
        AdjustPoseC2S::parameter,
        ByteBufCodecs.FLOAT,
        AdjustPoseC2S::amount,
        AdjustPoseC2S::new
    );

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
        Direction.STREAM_CODEC,
        AdjustPosC2S::direction,
        ByteBufCodecs.INT,
        AdjustPosC2S::amount,
        MoveMode.PACKET_CODEC,
        AdjustPosC2S::mode,
        MoveUnits.PACKET_CODEC,
        AdjustPosC2S::units,
        AdjustPosC2S::new
    );

    @Override
    @NotNull
    public Type<AdjustPosC2S> type() {
      return ID;
    }
  }

  public record AdjustYawC2S(int amount) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AdjustYawC2S> ID = new CustomPacketPayload.Type<>(ADJUST_YAW_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, AdjustYawC2S> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        AdjustYawC2S::amount,
        AdjustYawC2S::new
    );

    @Override
    @NotNull
    public Type<AdjustYawC2S> type() {
      return ID;
    }
  }

  public record PingC2S(UUID playerUuid) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PingC2S> ID = new CustomPacketPayload.Type<>(PING_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, PingC2S> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        PingC2S::playerUuid,
        PingC2S::new
    );

    @Override
    @NotNull
    public Type<PingC2S> type() {
      return ID;
    }
  }

  public record RequestScreenC2S(int armorStandId, ScreenType screenType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestScreenC2S> ID = new CustomPacketPayload.Type<>(
        REQUEST_SCREEN_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestScreenC2S> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        RequestScreenC2S::armorStandId,
        ScreenType.PACKET_CODEC,
        RequestScreenC2S::screenType,
        RequestScreenC2S::new
    );

    @Override
    @NotNull
    public Type<RequestScreenC2S> type() {
      return ID;
    }
  }

  public record SetFlagC2S(ArmorStandFlag flag, boolean value) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetFlagC2S> ID = new CustomPacketPayload.Type<>(SET_FLAG_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, SetFlagC2S> CODEC = StreamCodec.composite(
        ArmorStandFlag.PACKET_CODEC,
        SetFlagC2S::flag,
        ByteBufCodecs.BOOL,
        SetFlagC2S::value,
        SetFlagC2S::new
    );

    @Override
    @NotNull
    public Type<SetFlagC2S> type() {
      return ID;
    }
  }

  public record SetMannequinFlagC2S(MannequinFlag flag, boolean value) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetMannequinFlagC2S> ID = new CustomPacketPayload.Type<>(
        SET_MANNEQUIN_FLAG_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, SetMannequinFlagC2S> CODEC = StreamCodec.composite(
        MannequinFlag.PACKET_CODEC,
        SetMannequinFlagC2S::flag,
        ByteBufCodecs.BOOL,
        SetMannequinFlagC2S::value,
        SetMannequinFlagC2S::new
    );

    @Override
    @NotNull
    public Type<SetMannequinFlagC2S> type() {
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
        SetPoseC2S::write,
        SetPoseC2S::new
    );

    public SetPoseC2S(Pose pose) {
      this(
          pose.getHead(),
          pose.getBody(),
          pose.getRightArm(),
          pose.getLeftArm(),
          pose.getRightLeg(),
          pose.getLeftLeg()
      );
    }

    private SetPoseC2S(FriendlyByteBuf buf) {
      this(
          NetworkHelpers.readEulerAngle(buf),
          NetworkHelpers.readEulerAngle(buf),
          NetworkHelpers.readEulerAngle(buf),
          NetworkHelpers.readEulerAngle(buf),
          NetworkHelpers.readEulerAngle(buf),
          NetworkHelpers.readEulerAngle(buf)
      );
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
    public static final CustomPacketPayload.Type<SetPosePresetC2S> ID = new CustomPacketPayload.Type<>(
        SET_POSE_PRESET_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, SetPosePresetC2S> CODEC = StreamCodec.composite(
        PosePreset.PACKET_CODEC,
        SetPosePresetC2S::pose,
        SetPosePresetC2S::new
    );

    @Override
    @NotNull
    public Type<SetPosePresetC2S> type() {
      return ID;
    }
  }

  public record SetScaleC2S(float scale) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetScaleC2S> ID = new CustomPacketPayload.Type<>(SET_SCALE_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, SetScaleC2S> CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        SetScaleC2S::scale,
        SetScaleC2S::new
    );

    @Override
    @NotNull
    public Type<SetScaleC2S> type() {
      return ID;
    }
  }

  public record SetYawC2S(float angle) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetYawC2S> ID = new CustomPacketPayload.Type<>(SET_YAW_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, SetYawC2S> CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        SetYawC2S::angle,
        SetYawC2S::new
    );

    @Override
    @NotNull
    public Type<SetYawC2S> type() {
      return ID;
    }
  }

  public record UndoC2S(boolean redo) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UndoC2S> ID = new CustomPacketPayload.Type<>(UNDO_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, UndoC2S> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        UndoC2S::redo,
        UndoC2S::new
    );

    @Override
    @NotNull
    public Type<UndoC2S> type() {
      return ID;
    }
  }

  public record UtilityActionC2S(UtilityAction action) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UtilityActionC2S> ID = new CustomPacketPayload.Type<>(
        UTILITY_ACTION_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, UtilityActionC2S> CODEC =
        StreamCodec.composite(UtilityAction.PACKET_CODEC,
        UtilityActionC2S::action,
        UtilityActionC2S::new
    );

    @Override
    @NotNull
    public Type<UtilityActionC2S> type() {
      return ID;
    }
  }

  private Networking() {
  }
}
