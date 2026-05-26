package me.roundaround.armorstands.server.network;

import me.roundaround.armorstands.network.Networking;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.trove.network.TroveNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;

public final class ServerNetworking {
  private ServerNetworking() {
  }

  public static void sendClientUpdatePacket(ServerPlayer player, ArmorStand armorStand) {
    TroveNetworking.sendToClient(player, new Networking.ClientUpdateS2C(armorStand));
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
}
