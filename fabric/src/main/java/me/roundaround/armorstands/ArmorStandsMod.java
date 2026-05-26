package me.roundaround.armorstands;

import me.roundaround.allay.api.Entrypoint;
import me.roundaround.armorstands.client.ClientSideConfig;
import me.roundaround.armorstands.network.Networking;
import me.roundaround.armorstands.server.command.ArmorStandsCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

@Entrypoint(Entrypoint.MAIN)
public final class ArmorStandsMod implements ModInitializer {
  @Override
  public void onInitialize() {
    ClientSideConfig.getInstance().init();
    Networking.register();

    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
      ArmorStandsCommand.register(dispatcher);
    });
  }
}
