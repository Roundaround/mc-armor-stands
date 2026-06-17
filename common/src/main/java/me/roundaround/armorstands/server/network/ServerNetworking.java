package me.roundaround.armorstands.server.network;

import me.roundaround.armorstands.interfaces.ArmorStandScreenHandlerAccess;
import me.roundaround.armorstands.interfaces.MannequinSettingsAccess;
import me.roundaround.armorstands.network.Networking;
import me.roundaround.armorstands.server.config.ServerSideConfig;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import me.roundaround.armorstands.util.MoveMode;
import me.roundaround.armorstands.util.actions.AdjustPosAction;
import me.roundaround.trove.network.TroveNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;

import java.util.function.Supplier;

public final class ServerNetworking {
  private ServerNetworking() {
  }

  public static void sendClientUpdatePacket(ServerPlayer player, ArmorStand armorStand) {
    TroveNetworking.sendToClient(player, new Networking.ClientUpdateS2C(armorStand));
  }

  /** Sends the stand's mannequin settings to one player (start-tracking sync; skips mod-less clients). */
  public static void sendMannequinSettings(ServerPlayer player, ArmorStand armorStand) {
    if (!TroveNetworking.canSend(player, Networking.MannequinSettingsS2C.ID)) return;
    if (player.level().getServer().isDedicatedServer()
        && !ServerSideConfig.getInstance().enableMannequins.getValue()) return;
    int bits = ((MannequinSettingsAccess) armorStand).armorstands$getMannequinSettings().toBits();
    TroveNetworking.sendToClient(player, new Networking.MannequinSettingsS2C(armorStand.getId(), bits));
  }

  /** Broadcasts the stand's mannequin settings to every tracking player on a server-side change. */
  public static void broadcastMannequinSettings(ArmorStand armorStand) {
    if (!(armorStand.level() instanceof ServerLevel serverLevel)) return;
    if (serverLevel.getServer().isDedicatedServer()
        && !ServerSideConfig.getInstance().enableMannequins.getValue()) return;
    int bits = ((MannequinSettingsAccess) armorStand).armorstands$getMannequinSettings().toBits();
    Networking.MannequinSettingsS2C payload = new Networking.MannequinSettingsS2C(armorStand.getId(), bits);
    // Trove's transport, not a raw payload packet: Forge routes payloads through its own channel.
    for (ServerPlayer player : serverLevel.players()) {
      if (TroveNetworking.canSend(player, Networking.MannequinSettingsS2C.ID)) {
        TroveNetworking.sendToClient(player, payload);
      }
    }
  }

  public static void sendMessagePacket(ServerPlayer player, String message) {
    TroveNetworking.sendToClient(player, new Networking.MessageS2C(message));
  }

  public static void sendMessagePacket(ServerPlayer player, String message, int color) {
    TroveNetworking.sendToClient(player, new Networking.MessageS2C(message, color));
  }

  public static void sendOpenScreenPacket(
      ServerPlayer player,
      int syncId,
      ArmorStand armorStand,
      ScreenType screenType
  ) {
    TroveNetworking.sendToClient(player, new Networking.OpenScreenS2C(syncId, armorStand.getId(), screenType));
  }

  public static void sendPongPacket(ServerPlayer player) {
    TroveNetworking.sendToClient(player, new Networking.PongS2C(player.getUUID()));
  }

  // C2S receivers — dispatched on the server thread by the loader networking bridge. Bodies live here
  // (not in Networking) so register() stays a thin both-sided registration table. All server-safe, so
  // Networking references them via plain ServerNetworking::handle… method refs.

  public static void handleAdjustPose(Networking.AdjustPoseC2S payload, ServerPlayer player) {
    if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
    sh.getEditor().adjustPose(payload.part(), payload.parameter(), payload.amount());
  }

  public static void handleAdjustPos(Networking.AdjustPosC2S payload, ServerPlayer player) {
    if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
    MoveMode mode = payload.mode();
    sh.getEditor().applyAction(mode.isLocal()
        ? AdjustPosAction.local(payload.direction(), payload.amount(), payload.units(), mode.isLocalToPlayer())
        : AdjustPosAction.relative(payload.direction(), payload.amount(), payload.units()));
  }

  public static void handleAdjustYaw(Networking.AdjustYawC2S payload, ServerPlayer player) {
    if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
    sh.getEditor().rotate(payload.amount());
  }

  public static void handlePing(Networking.PingC2S payload, ServerPlayer player) {
    sendPongPacket(player);
  }

  public static void handleRequestScreen(Networking.RequestScreenC2S payload, ServerPlayer player) {
    if (!(player.level().getEntity(payload.armorStandId()) instanceof ArmorStand armorStand)) return;
    ((ArmorStandScreenHandlerAccess) player).armorstands$openScreen(armorStand, payload.screenType());
  }

  public static void handleSetFlag(Networking.SetFlagC2S payload, ServerPlayer player) {
    if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
    sh.getEditor().setFlag(payload.flag(), payload.value());
    sendClientUpdatePacket(player, sh.getEditor().getArmorStand());
  }

  public static void handleSetMannequinFlag(Networking.SetMannequinFlagC2S payload, ServerPlayer player) {
    if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
    if (player.level().getServer().isDedicatedServer()
        && !ServerSideConfig.getInstance().enableMannequins.getValue()) return;
    sh.getEditor().setMannequinFlag(payload.flag(), payload.value());
  }

  public static void handleSetPose(Networking.SetPoseC2S payload, ServerPlayer player) {
    if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
    sh.getEditor().setPose(
        payload.head(), payload.body(), payload.rightArm(),
        payload.leftArm(), payload.rightLeg(), payload.leftLeg());
  }

  public static void handleSetPosePreset(Networking.SetPosePresetC2S payload, ServerPlayer player) {
    if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
    sh.getEditor().setPose(payload.pose().toPose());
  }

  public static void handleSetScale(Networking.SetScaleC2S payload, ServerPlayer player) {
    if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
    sh.getEditor().setScale(payload.scale());
  }

  public static void handleSetYaw(Networking.SetYawC2S payload, ServerPlayer player) {
    if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
    sh.getEditor().setRotation(payload.angle());
  }

  public static void handleUndo(Networking.UndoC2S payload, ServerPlayer player) {
    if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
    Supplier<Boolean> action = payload.redo() ? sh.getEditor()::redo : sh.getEditor()::undo;
    String successMessage = payload.redo() ? "armorstands.message.redo" : "armorstands.message.undo";
    String failureMessage = payload.redo() ? "armorstands.message.redo.fail" : "armorstands.message.undo.fail";
    if (action.get()) {
      sendMessagePacket(player, successMessage);
    } else {
      sendMessagePacket(player, failureMessage);
    }
  }

  public static void handleUtilityAction(Networking.UtilityActionC2S payload, ServerPlayer player) {
    if (!(player.containerMenu instanceof ArmorStandScreenHandler sh)) return;
    payload.action().apply(sh.getEditor(), player);
  }
}
