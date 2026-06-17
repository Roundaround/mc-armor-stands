package me.roundaround.armorstands;

import me.roundaround.allay.api.Entrypoint;
import me.roundaround.armorstands.client.ClientSideConfig;
import me.roundaround.armorstands.network.Networking;
import me.roundaround.armorstands.server.command.ArmorStandsCommand;
import me.roundaround.armorstands.server.config.ServerSideConfig;
import me.roundaround.armorstands.server.network.ServerNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.world.entity.decoration.ArmorStand;

@Entrypoint(Entrypoint.MAIN)
public final class ArmorStandsMod implements ModInitializer {
  @Override
  public void onInitialize() {
    ClientSideConfig.getInstance().init();
    Networking.register();

    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
      ArmorStandsCommand.register(dispatcher);
    });

    // Create the server-side config for every server (dedicated AND integrated) so a world opened to
    // friends has permission enforcement available; ServerSideConfig.appliesTo() decides when it governs.
    ServerLifecycleEvents.SERVER_STARTED.register((server) -> ServerSideConfig.create(server).init());

    EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
      if (!(trackedEntity instanceof ArmorStand armorStand)) return;
      ServerNetworking.sendMannequinSettings(player, armorStand);
    });
  }
}
