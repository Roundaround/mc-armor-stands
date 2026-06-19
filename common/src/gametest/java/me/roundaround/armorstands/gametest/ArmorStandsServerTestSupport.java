package me.roundaround.armorstands.gametest;

import me.roundaround.trove.config.option.BooleanConfigOption;
import me.roundaround.trove.gametest.GameTestAssertionException;
import me.roundaround.trove.gametest.ServerTestContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ArmorStand;

/** Shared helpers for the Armor Stands {@code @ServerGameTest} suite. */
final class ArmorStandsServerTestSupport {
  private ArmorStandsServerTestSupport() {
  }

  /**
   * Spawn a fresh armor stand in the overworld and return it. Created on the
   * server thread and discarded automatically at test teardown.
   */
  static ArmorStand spawnStand(ServerTestContext context) {
    ArmorStand stand = context.computeOnServer((server) -> {
      ServerLevel level = server.overworld();
      ArmorStand entity = new ArmorStand(level, 0.5, 64.0, 0.5);
      if (!level.addFreshEntity(entity)) {
        throw new GameTestAssertionException("could not add the test armor stand to the overworld");
      }
      return entity;
    });
    context.onCleanup(() -> context.runOnServer((server) -> stand.discard()));
    return stand;
  }

  /** Set and commit a server config option in memory (no disk write). */
  static void setConfig(BooleanConfigOption option, boolean value) {
    option.setValue(value);
    option.commit();
  }

  static void check(boolean condition, String message) {
    if (!condition) {
      throw new GameTestAssertionException(message);
    }
  }

  static void checkClose(float expected, float actual, float epsilon, String message) {
    if (Math.abs(expected - actual) > epsilon) {
      throw new GameTestAssertionException(message + " (expected " + expected + " but was " + actual + ")");
    }
  }
}
