package me.roundaround.armorstands.gametest;

import me.roundaround.allay.api.gametest.ServerGameTest;
import me.roundaround.armorstands.network.Networking;
import me.roundaround.armorstands.server.ArmorStandUsers;
import me.roundaround.armorstands.server.config.ServerSideConfig;
import me.roundaround.armorstands.server.network.ServerNetworking;
import me.roundaround.trove.gametest.ServerTest;
import me.roundaround.trove.gametest.ServerTestContext;
import me.roundaround.trove.network.TroveNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;

import static me.roundaround.armorstands.gametest.ArmorStandsServerTestSupport.check;
import static me.roundaround.armorstands.gametest.ArmorStandsServerTestSupport.setConfig;
import static me.roundaround.armorstands.gametest.ArmorStandsServerTestSupport.spawnStand;

/**
 * The "vanilla client" angle, server-side: an unmodded client must be denied
 * editing and must not be able to crash the server.
 *
 * <p>A connected player who is neither op nor allow-listed is denied under the
 * default config on every loader. A <em>genuinely</em> mod-less client is denied
 * one step earlier, by the {@code canSend} gate, before any permission check — a
 * mock player models that faithfully only where its embedded connection reports
 * {@code canSend == false} (Fabric). On NeoForge/Forge the mock connection still
 * advertises the channel, so the canSend-specific facts can't be exercised
 * headlessly and are asserted only when the proxy is faithful.
 */
@ServerGameTest
public class ArmorStandsModlessClientServerTest implements ServerTest {
  @Override
  public void runTest(ServerTestContext context) {
    context.waitTicks(2);

    ServerPlayer player = context.spawnPlayer();
    ArmorStand stand = spawnStand(context);
    boolean faithfulModless =
        context.computeOnServer((server) -> !TroveNetworking.canSend(player, Networking.PongS2C.ID));

    context.runOnServer((server) -> {
      check(!ArmorStandUsers.canEditArmorStands(player), "an unpermitted player is denied editing");

      if (faithfulModless) {
        // canSend is checked before permissions, so a mod-less client stays denied
        // even with permission enforcement turned off.
        ServerSideConfig config = ServerSideConfig.getInstance();
        boolean original = config.enforcePermissions.getValue();
        setConfig(config.enforcePermissions, false);
        check(!ArmorStandUsers.canEditArmorStands(player),
            "mod-less client stays denied even with enforcePermissions=false (canSend gate first)");
        setConfig(config.enforcePermissions, original);

        // And the server quietly skips pushing mannequin state toward it — no
        // custom S2C is sent to a client that can't receive one.
        ServerNetworking.sendMannequinSettings(player, stand);
        ServerNetworking.broadcastMannequinSettings(stand);
      }
    });
    context.waitTicks(1);

    context.assertNoClientClassesLoaded();
  }
}
