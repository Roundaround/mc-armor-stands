package me.roundaround.armorstands.server.network;

import me.roundaround.armorstands.interfaces.MannequinSettingsAccess;
import me.roundaround.armorstands.network.Networking;
import me.roundaround.armorstands.server.config.ServerSideConfig;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.trove.network.TroveNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;

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
}
