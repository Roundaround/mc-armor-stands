package me.roundaround.armorstands.gametest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import me.roundaround.allay.api.gametest.ServerGameTest;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.server.ArmorStandUsers;
import me.roundaround.trove.gametest.ServerTest;
import me.roundaround.trove.gametest.ServerTestContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import static me.roundaround.armorstands.gametest.ArmorStandsServerTestSupport.check;

/**
 * Covers the server-side permission surface that doesn't need a live client
 * connection: the allow-list store ({@link ArmorStandUsers}) and the
 * {@code /armorstands} command's gamemaster gate. The full
 * {@code canEditArmorStands} op/allow-list path is only reachable by a connected
 * modded client (it short-circuits on {@code canSend} for everyone else), so the
 * integration test exercises that end.
 */
@ServerGameTest
public class ArmorStandsPermissionsServerTest implements ServerTest {
  @Override
  public void runTest(ServerTestContext context) {
    context.waitTicks(2);

    ServerPlayer player = context.spawnPlayer();
    NameAndId id = player.nameAndId();
    // Leave the store as we found it even if an assertion below throws.
    context.onCleanup(() -> context.runOnServer((server) -> ArmorStandUsers.remove(id)));

    context.runOnServer((server) -> {
      check(!ArmorStandUsers.contains(id), "player is not allow-listed initially");
      ArmorStandUsers.add(id);
      check(ArmorStandUsers.contains(id), "add() allow-lists the player");
      ArmorStandUsers.remove(id);
      check(!ArmorStandUsers.contains(id), "remove() clears the player");
    });

    context.runOnServer((server) -> {
      CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
      CommandNode<CommandSourceStack> node = dispatcher.getRoot().getChild(Constants.MOD_ID);
      check(node != null, "/" + Constants.MOD_ID + " command is registered");
      check(node.canUse(server.createCommandSourceStack()), "the console (op) may use the command");
      check(!node.canUse(player.createCommandSourceStack()), "a non-op player may not use the command");
    });

    // The console is permitted, so reload runs cleanly end to end.
    context.runCommand(Constants.MOD_ID + " reload");
    context.waitTicks(1);
  }
}
