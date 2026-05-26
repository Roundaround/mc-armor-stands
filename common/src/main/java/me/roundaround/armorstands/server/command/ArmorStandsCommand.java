package me.roundaround.armorstands.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.server.ArmorStandUsers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import java.util.Collection;
import java.util.stream.Stream;

public class ArmorStandsCommand {
  private static final Logger LOGGER = LogManager.getLogger(Constants.MOD_ID);

  private static final SimpleCommandExceptionType ADD_FAILED_EXCEPTION =
      new SimpleCommandExceptionType(Component.translatable(
      "armorstands.commands.add.failed"));
  private static final SimpleCommandExceptionType REMOVE_FAILED_EXCEPTION =
      new SimpleCommandExceptionType(Component.translatable(
      "armorstands.commands.remove.failed"));
  private static final SimpleCommandExceptionType RELOAD_FAILED_EXCEPTION =
      new SimpleCommandExceptionType(Component.translatable(
      "armorstands.commands.reload.failed"));

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    LiteralArgumentBuilder<CommandSourceStack> baseCommand = Commands.literal(Constants.MOD_ID)
        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));

    LiteralArgumentBuilder<CommandSourceStack> addSub = Commands.literal("add")
        .then(Commands.argument("targets", GameProfileArgument.gameProfile())
            .suggests((context, builder) -> {
              PlayerList playerManager = context.getSource().getServer().getPlayerList();
              Stream<String> playerNames = playerManager.getPlayers()
                  .stream()
                  .filter((player) -> !ArmorStandUsers.contains(player.nameAndId()))
                  .map((player) -> player.getGameProfile().name());
              return SharedSuggestionProvider.suggest(playerNames, builder);
            })
            .executes((context) -> executeAdd(
                context.getSource(),
                GameProfileArgument.getGameProfiles(context, "targets")
            )));

    LiteralArgumentBuilder<CommandSourceStack> removeSub = Commands.literal("remove")
        .then(Commands.argument("targets", GameProfileArgument.gameProfile())
            .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                ArmorStandUsers.listNames(context.getSource()
                    .getServer()), builder
            ))
            .executes((context) -> executeRemove(
                context.getSource(),
                GameProfileArgument.getGameProfiles(context, "targets")
            )));

    LiteralArgumentBuilder<CommandSourceStack> reloadSub = Commands.literal("reload")
        .executes((context) -> executeReload(context.getSource()));

    LiteralArgumentBuilder<CommandSourceStack> finalCommand = baseCommand.then(addSub).then(removeSub).then(reloadSub);

    dispatcher.register(finalCommand);
  }

  private static int executeAdd(CommandSourceStack source, Collection<NameAndId> targets)
      throws CommandSyntaxException {
    int added = 0;

    for (NameAndId target : targets) {
      if (ArmorStandUsers.contains(target)) {
        continue;
      }

      ArmorStandUsers.add(target);
      source.sendSuccess(() -> Component.translatable("armorstands.commands.add.success", target.name()), true);
      added++;
    }

    if (added == 0) {
      throw ADD_FAILED_EXCEPTION.create();
    }

    return added;
  }

  private static int executeRemove(CommandSourceStack source, Collection<NameAndId> targets)
      throws CommandSyntaxException {
    int removed = 0;

    for (NameAndId target : targets) {
      if (!ArmorStandUsers.contains(target)) {
        continue;
      }

      ArmorStandUsers.remove(target);
      source.sendSuccess(() -> Component.translatable("armorstands.commands.remove.success", target.name()), true);
      removed++;
    }

    if (removed == 0) {
      throw REMOVE_FAILED_EXCEPTION.create();
    }

    return removed;
  }

  private static int executeReload(CommandSourceStack source) throws CommandSyntaxException {
    try {
      ArmorStandUsers.reload();
    } catch (Exception exception) {
      LOGGER.warn("Failed to reload armor stand users: ", exception);
      throw RELOAD_FAILED_EXCEPTION.create();
    }

    source.sendSuccess(() -> Component.translatable("armorstands.commands.reload.success"), true);
    return 1;
  }
}
