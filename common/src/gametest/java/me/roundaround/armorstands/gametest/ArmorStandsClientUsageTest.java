package me.roundaround.armorstands.gametest;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.armorstands.client.gui.screen.AbstractArmorStandScreen;
import me.roundaround.armorstands.client.network.ClientNetworking;
import me.roundaround.armorstands.network.ArmorStandFlag;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.ArmorStand;

import static me.roundaround.armorstands.gametest.ArmorStandsServerTestSupport.check;

/**
 * Full client + integrated-server "basic mod usage" through the real GUI: spawn a
 * world, place an armor stand, right-click it to open the editor, drive an edit
 * over the mod's networking, and assert the change replicates back to the client.
 *
 * <p>Single-player, so editing needs no permission — the integrated server isn't a
 * dedicated server, so {@code canEditArmorStands} is unconditionally true. The
 * dedicated-server permission path is the integration test's job.
 */
@ClientGameTest
public class ArmorStandsClientUsageTest implements ClientTest {
  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      // A small platform so the player and the stand share solid ground in range.
      world.fill(new BlockPos(0, 64, 0), new BlockPos(3, 64, 3), "minecraft:smooth_stone");
      world.teleport(0.5, 65.0, 0.5);
      context.waitTicks(2);

      ArmorStand stand = world.summon(EntityTypes.ARMOR_STAND, new BlockPos(2, 65, 0));
      context.waitTicks(2);

      // Right-click the stand: the mod's interact mixin opens the editor screen.
      world.lookAt(stand);
      world.useItemOn(stand);
      context.waitForScreen(AbstractArmorStandScreen.class);

      // Drive an edit the way the screen's "Show Arms" toggle does, then confirm it
      // round-tripped to the server and replicated back onto the client stand.
      check(!stand.showArms(), "arms hidden before the edit");
      context.runOnClient((mc) -> ClientNetworking.sendSetFlagPacket(ArmorStandFlag.SHOW_ARMS, true));
      world.settle();
      check(stand.showArms(), "Show Arms replicated to the client stand");

      context.assertScreen(AbstractArmorStandScreen.class);
    }
  }
}
