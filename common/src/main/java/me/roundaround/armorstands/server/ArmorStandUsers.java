package me.roundaround.armorstands.server;

import me.roundaround.armorstands.network.Networking;
import me.roundaround.trove.config.option.StringListConfigOption;
import me.roundaround.armorstands.server.config.ServerSideConfig;
import me.roundaround.trove.network.TroveNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ArmorStandUsers {
  private ArmorStandUsers() {
  }

  public static final int PERMISSION_LEVEL = 2;

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public static boolean canEditArmorStands(ServerPlayer player) {
    MinecraftServer server = player.level().getServer();
    // Enforce on dedicated servers and on integrated servers opened to friends/LAN; private single
    // player stays unrestricted so the host can always edit their own world.
    if (!ServerSideConfig.appliesTo(server)) {
      return true;
    }

    if (!TroveNetworking.canSend(player, Networking.PongS2C.ID)) {
      return false;
    }

    ServerSideConfig config = ServerSideConfig.getInstance();
    if (!config.enforcePermissions.getValue()) {
      return true;
    }

    if (config.opsHavePermissions.getValue() && player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
      return true;
    }

    return contains(player.nameAndId());
  }

  public static boolean contains(NameAndId player) {
    return ServerSideConfig.getInstance().allowedUsers.getValue().contains(player.id().toString());
  }

  public static void add(NameAndId player) {
    StringListConfigOption allowedUsers = ServerSideConfig.getInstance().allowedUsers;
    String toAdd = player.id().toString();
    if (allowedUsers.getValue().stream().noneMatch((uuid) -> uuid.equalsIgnoreCase(toAdd))) {
      allowedUsers.add(toAdd);
      if (allowedUsers.isDirty()) {
        ServerSideConfig.getInstance().writeToStore();
      }
    }
  }

  public static void remove(NameAndId player) {
    StringListConfigOption allowedUsers = ServerSideConfig.getInstance().allowedUsers;
    allowedUsers.remove(player.id().toString());
    if (allowedUsers.isDirty()) {
      ServerSideConfig.getInstance().writeToStore();
    }
  }

  public static void reload() {
    ServerSideConfig.getInstance().syncWithStore();
  }

  public static Collection<String> listNames(MinecraftServer server) {
    UserNameToIdResolver cache = server.services().nameToIdCache();
    return ServerSideConfig.getInstance().allowedUsers.getValue().stream().map((rawUuid) -> {
      UUID uuid;
      try {
        uuid = UUID.fromString(rawUuid);
      } catch (IllegalArgumentException unused) {
        return null;
      }

      Optional<NameAndId> opt = cache.get(uuid);
      return opt.map(NameAndId::name).orElse(null);
    }).filter(Objects::nonNull).collect(Collectors.toSet());
  }
}
