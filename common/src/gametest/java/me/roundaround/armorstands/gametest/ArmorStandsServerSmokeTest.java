package me.roundaround.armorstands.gametest;

import me.roundaround.allay.api.gametest.ServerGameTest;
import me.roundaround.trove.gametest.ServerSmokeTest;

/**
 * Asserts Armor Stands boots a dedicated server cleanly and loads no client code.
 * The body lives in {@link ServerSmokeTest}; this opt-in subclass just carries the
 * {@code @ServerGameTest} marker the build-time scan discovers.
 */
@ServerGameTest
public class ArmorStandsServerSmokeTest extends ServerSmokeTest {
}
