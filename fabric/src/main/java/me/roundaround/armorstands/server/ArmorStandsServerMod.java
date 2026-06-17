package me.roundaround.armorstands.server;

import me.roundaround.allay.api.Entrypoint;
import net.fabricmc.api.DedicatedServerModInitializer;

@Entrypoint(Entrypoint.SERVER)
public class ArmorStandsServerMod implements DedicatedServerModInitializer {
  @Override
  public void onInitializeServer() {
    // ServerSideConfig creation moved to ArmorStandsMod's SERVER_STARTED listener (runs on both sides)
    // so integrated/friends-hosted servers get it too. Nothing dedicated-only remains here.
  }
}
