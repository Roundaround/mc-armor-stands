package me.roundaround.armorstands.client.network;

import me.roundaround.armorstands.network.ArmorStandFlag;
import me.roundaround.armorstands.network.MannequinFlag;
import me.roundaround.armorstands.network.Networking;
import me.roundaround.armorstands.network.PosePart;
import me.roundaround.armorstands.network.EulerAngleParameter;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.armorstands.network.UtilityAction;
import me.roundaround.armorstands.util.MoveMode;
import me.roundaround.armorstands.util.MoveUnits;
import me.roundaround.armorstands.util.Pose;
import me.roundaround.armorstands.util.PosePreset;
import me.roundaround.armorstands.util.SavedPose;
import me.roundaround.trove.network.TroveNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.ArmorStand;

public final class ClientNetworking {
  private ClientNetworking() {
  }

  public static void sendAdjustPosePacket(PosePart part, EulerAngleParameter parameter, float amount) {
    TroveNetworking.sendToServer(new Networking.AdjustPoseC2S(part, parameter, amount));
  }

  public static void sendAdjustPosPacket(Direction direction, int amount, MoveMode mode, MoveUnits units) {
    TroveNetworking.sendToServer(new Networking.AdjustPosC2S(direction, amount, mode, units));
  }

  public static void sendAdjustYawPacket(int amount) {
    TroveNetworking.sendToServer(new Networking.AdjustYawC2S(amount));
  }

  public static void sendPingPacket(LocalPlayer player) {
    TroveNetworking.sendToServer(new Networking.PingC2S(player.getUUID()));
  }

  public static void sendRequestScreenPacket(ArmorStand armorStand, ScreenType screenType) {
    TroveNetworking.sendToServer(new Networking.RequestScreenC2S(armorStand.getId(), screenType));
  }

  public static void sendSetFlagPacket(ArmorStandFlag flag, boolean value) {
    TroveNetworking.sendToServer(new Networking.SetFlagC2S(flag, value));
  }

  public static void sendSetMannequinFlagPacket(MannequinFlag flag, boolean value) {
    TroveNetworking.sendToServer(new Networking.SetMannequinFlagC2S(flag, value));
  }

  public static void sendSetPosePacket(SavedPose pose) {
    sendSetPosePacket(pose.toPose());
  }

  public static void sendSetPosePacket(Pose pose) {
    TroveNetworking.sendToServer(new Networking.SetPoseC2S(pose));
  }

  public static void sendSetPosePresetPacket(PosePreset pose) {
    TroveNetworking.sendToServer(new Networking.SetPosePresetC2S(pose));
  }

  public static void sendSetScalePacket(float scale) {
    TroveNetworking.sendToServer(new Networking.SetScaleC2S(scale));
  }

  public static void sendSetYawPacket(float angle) {
    TroveNetworking.sendToServer(new Networking.SetYawC2S(angle));
  }

  public static void sendUndoPacket(boolean redo) {
    TroveNetworking.sendToServer(new Networking.UndoC2S(redo));
  }

  public static void sendUtilityActionPacket(UtilityAction action) {
    TroveNetworking.sendToServer(new Networking.UtilityActionC2S(action));
  }
}
