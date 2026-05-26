package me.roundaround.armorstands.server;

import me.roundaround.allay.api.Entrypoint;
import me.roundaround.armorstands.server.config.ServerSideConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.dedicated.DedicatedServer;

@Entrypoint(Entrypoint.SERVER)
public class ArmorStandsServerMod implements DedicatedServerModInitializer {
  @Override
  public void onInitializeServer() {
    ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
      if (!(server instanceof DedicatedServer dedicatedServer)) {
        return;
      }
      ServerSideConfig.create(dedicatedServer).init();
    });
  }
}
