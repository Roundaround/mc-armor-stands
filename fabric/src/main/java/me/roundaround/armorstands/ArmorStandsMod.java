package me.roundaround.armorstands;

import me.roundaround.allay.api.Entrypoint;
import me.roundaround.armorstands.client.ClientSideConfig;
import me.roundaround.armorstands.network.Networking;
import me.roundaround.armorstands.server.command.ArmorStandsCommand;
import me.roundaround.armorstands.server.network.ServerNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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

    EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
      if (!(trackedEntity instanceof ArmorStand armorStand)) return;
      ServerNetworking.sendMannequinSettings(player, armorStand);
    });
  }
}
