package me.roundaround.armorstands;

import me.roundaround.armorstands.client.ForgeClient;
import me.roundaround.armorstands.network.Networking;
import me.roundaround.armorstands.server.command.ArmorStandsCommand;
import me.roundaround.armorstands.server.config.ServerSideConfig;
import me.roundaround.armorstands.server.network.ServerNetworking;
import me.roundaround.trove.forge.TroveForge;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("armorstands")
public final class ArmorStandsForgeMod {
  public ArmorStandsForgeMod(FMLJavaModLoadingContext context) {
    TroveForge.bootstrap(context);
    Networking.register();

    RegisterCommandsEvent.BUS.addListener(event -> {
      ArmorStandsCommand.register(event.getDispatcher());
    });

    ServerStartedEvent.BUS.addListener(event -> {
      if (!(event.getServer() instanceof DedicatedServer dedicatedServer)) {
        return;
      }
      ServerSideConfig.create(dedicatedServer).init();
    });

    PlayerEvent.StartTracking.BUS.addListener(event -> {
      if (!(event.getTarget() instanceof ArmorStand armorStand)) {
        return;
      }
      if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
        return;
      }
      ServerNetworking.sendMannequinSettings(serverPlayer, armorStand);
    });

    if (FMLEnvironment.dist.isClient()) {
      ForgeClient.init(context);
    }
  }
}
