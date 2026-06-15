package me.roundaround.armorstands;

import me.roundaround.armorstands.network.Networking;
import me.roundaround.armorstands.server.command.ArmorStandsCommand;
import me.roundaround.armorstands.server.config.ServerSideConfig;
import me.roundaround.armorstands.server.network.ServerNetworking;
import me.roundaround.trove.neoforge.TroveNeoForge;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("armorstands")
public final class ArmorStandsNeoForgeMod {
  public ArmorStandsNeoForgeMod(IEventBus modBus, ModContainer container) {
    TroveNeoForge.bootstrap(modBus, container);
    Networking.register();

    NeoForge.EVENT_BUS.addListener(
        RegisterCommandsEvent.class, event -> {
          ArmorStandsCommand.register(event.getDispatcher());
        }
    );

    NeoForge.EVENT_BUS.addListener(
        ServerStartedEvent.class, event -> {
          if (!(event.getServer() instanceof DedicatedServer dedicatedServer)) {
            return;
          }
          ServerSideConfig.create(dedicatedServer).init();
        }
    );

    NeoForge.EVENT_BUS.addListener(
        PlayerEvent.StartTracking.class, event -> {
          if (!(event.getTarget() instanceof ArmorStand armorStand)) {
            return;
          }
          if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
          }
          ServerNetworking.sendMannequinSettings(serverPlayer, armorStand);
        }
    );

    if (FMLEnvironment.getDist() == Dist.CLIENT) {
      NeoForgeClient.init(modBus, container);
    }
  }
}
