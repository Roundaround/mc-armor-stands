package me.roundaround.armorstands.gametest;

import me.roundaround.allay.api.gametest.ServerGameTest;
import me.roundaround.armorstands.network.MannequinFlag;
import me.roundaround.armorstands.network.Networking;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import me.roundaround.armorstands.server.config.ServerSideConfig;
import me.roundaround.armorstands.server.network.ServerNetworking;
import me.roundaround.trove.gametest.ServerTest;
import me.roundaround.trove.gametest.ServerTestContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;

import static me.roundaround.armorstands.gametest.ArmorStandsServerTestSupport.check;
import static me.roundaround.armorstands.gametest.ArmorStandsServerTestSupport.setConfig;
import static me.roundaround.armorstands.gametest.ArmorStandsServerTestSupport.spawnStand;

/**
 * Verifies the server-side config: registered defaults, in-memory mutation, and
 * one config-driven behavior gate — {@code enableMannequins} short-circuiting the
 * mannequin-flag handler.
 */
@ServerGameTest
public class ArmorStandsServerConfigTest implements ServerTest {
  @Override
  public void runTest(ServerTestContext context) {
    context.waitTicks(2);

    context.runOnServer((server) -> {
      ServerSideConfig config = ServerSideConfig.getInstance();
      check(config.enforcePermissions.getValue(), "enforcePermissions defaults true");
      check(config.opsHavePermissions.getValue(), "opsHavePermissions defaults true");
      check(!config.requireSneakingToEdit.getValue(), "requireSneakingToEdit defaults false");
      check(config.allowAnyItemInHeadSlot.getValue(), "allowAnyItemInHeadSlot defaults true");
      check(config.enableMannequins.getValue(), "enableMannequins defaults true");
      check(config.allowedUsers.getValue().isEmpty(), "allowedUsers defaults empty");
    });

    // A committed mutation is visible through getValue; restore it afterward.
    context.runOnServer((server) -> {
      ServerSideConfig config = ServerSideConfig.getInstance();
      boolean original = config.requireSneakingToEdit.getValue();
      setConfig(config.requireSneakingToEdit, !original);
      check(config.requireSneakingToEdit.getValue() == !original, "committed mutation is visible");
      setConfig(config.requireSneakingToEdit, original);
    });

    ServerPlayer player = context.spawnPlayer();
    ArmorStand stand = spawnStand(context);

    context.runOnServer((server) -> {
      ServerSideConfig config = ServerSideConfig.getInstance();
      boolean originalEnable = config.enableMannequins.getValue();

      // A real armor-stand menu so the handler's containerMenu check passes.
      ArmorStandScreenHandler handler =
          new ArmorStandScreenHandler(99, player.getInventory(), stand, ScreenType.MANNEQUIN);
      player.containerMenu = handler;

      // Detach the mock player from the level so the server-side mannequin
      // replicate has no audience: a mock player has no real client connection,
      // and NeoForge/Forge reject a custom S2C send toward it (Fabric skips it).
      // We're asserting the handler's config gate, not its network broadcast.
      server.getPlayerList().remove(player);
      player.containerMenu = handler;

      boolean baseline = MannequinFlag.ENABLED.getValue(stand);
      Networking.SetMannequinFlagC2S payload = new Networking.SetMannequinFlagC2S(MannequinFlag.ENABLED, true);

      setConfig(config.enableMannequins, false);
      ServerNetworking.handleSetMannequinFlag(payload, player);
      check(MannequinFlag.ENABLED.getValue(stand) == baseline, "enableMannequins=false blocks the handler");

      setConfig(config.enableMannequins, true);
      ServerNetworking.handleSetMannequinFlag(payload, player);
      check(MannequinFlag.ENABLED.getValue(stand), "enableMannequins=true lets the handler apply");

      setConfig(config.enableMannequins, originalEnable);
    });
  }
}
