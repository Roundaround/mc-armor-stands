package me.roundaround.armorstands.client.network;

import me.roundaround.armorstands.client.gui.MessageRenderer;
import me.roundaround.armorstands.client.gui.screen.*;
import me.roundaround.armorstands.client.render.MannequinSettings;
import me.roundaround.armorstands.interfaces.MannequinSettingsAccess;
import me.roundaround.armorstands.network.*;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import me.roundaround.armorstands.util.*;
import me.roundaround.trove.network.TroveNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;

public final class ClientNetworking {
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

  public static void onClientUpdate(Networking.ClientUpdateS2C payload) {
    if (!(Minecraft.getInstance().gui.screen() instanceof AbstractArmorStandScreen screen)) {
      return;
    }
    screen.updatePosOnClient(payload.x(), payload.y(), payload.z());
    screen.updateYawOnClient(Mth.wrapDegrees(payload.yaw()));
    screen.updatePitchOnClient(Mth.wrapDegrees(payload.pitch()));
    screen.updateInvulnerableOnClient(payload.invulnerable());
    screen.updateDisabledSlotsOnClient(payload.disabledSlots());
  }

  public static void onMannequinSettings(Networking.MannequinSettingsS2C payload) {
    Minecraft mc = Minecraft.getInstance();
    if (mc.level == null) {
      return;
    }
    if (!(mc.level.getEntity(payload.entityId()) instanceof ArmorStand armorStand)) {
      return;
    }
    // Write the local copy via the duck interface. The level here is a ClientLevel, so the setter
    // skips the broadcast and just updates the field the renderer/screen read each tick.
    ((MannequinSettingsAccess) armorStand).armorstands$setMannequinSettings(MannequinSettings.fromBits(payload.bits()));
  }

  public static void onMessage(Networking.MessageS2C payload) {
    if (!(Minecraft.getInstance().gui.screen() instanceof AbstractArmorStandScreen screen)) {
      return;
    }
    screen.getMessageRenderer().addMessage(
        payload.translatable() ? Component.translatable(payload.message()) : Component.literal(payload.message()),
        payload.styled() ? payload.color() : MessageRenderer.BASE_COLOR
    );
  }

  public static void onOpenScreen(Networking.OpenScreenS2C payload) {
    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = mc.player;
    if (player == null) {
      return;
    }
    if (!(player.level().getEntity(payload.armorStandId()) instanceof ArmorStand armorStand)) {
      return;
    }

    ArmorStandScreenHandler screenHandler = new ArmorStandScreenHandler(
        payload.syncId(),
        player.getInventory(),
        armorStand,
        payload.screenType()
    );
    player.containerMenu = screenHandler;
    mc.gui.setScreen(switch (screenHandler.getScreenType()) {
      case UTILITIES -> new ArmorStandUtilitiesScreen(screenHandler);
      case MOVE -> new ArmorStandMoveScreen(screenHandler);
      case ROTATE -> new ArmorStandRotateScreen(screenHandler);
      case POSE -> new ArmorStandPoseScreen(screenHandler);
      case PRESETS -> new ArmorStandPresetsScreen(screenHandler);
      case INVENTORY -> new ArmorStandInventoryScreen(screenHandler);
      case MANNEQUIN -> new ArmorStandMannequinScreen(screenHandler);
    });
  }

  public static void onPong(Networking.PongS2C payload) {
    if (!(Minecraft.getInstance().gui.screen() instanceof AbstractArmorStandScreen screen)) {
      return;
    }
    screen.onPong();
  }

  private ClientNetworking() {
  }
}
