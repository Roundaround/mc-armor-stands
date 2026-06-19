package me.roundaround.armorstands.gametest;

import me.roundaround.allay.api.gametest.ServerGameTest;
import me.roundaround.armorstands.network.ArmorStandFlag;
import me.roundaround.armorstands.util.ArmorStandEditor;
import me.roundaround.trove.gametest.ServerTest;
import me.roundaround.trove.gametest.ServerTestContext;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;

import static me.roundaround.armorstands.gametest.ArmorStandsServerTestSupport.check;
import static me.roundaround.armorstands.gametest.ArmorStandsServerTestSupport.checkClose;
import static me.roundaround.armorstands.gametest.ArmorStandsServerTestSupport.spawnStand;

/**
 * Drives the server-side edit pipeline ({@link ArmorStandEditor}) end to end —
 * flag, rotation, scale, pose, and undo/redo — against a live armor stand. This
 * is the headless core of "basic mod usage": every edit a player makes routes
 * through these actions.
 */
@ServerGameTest
public class ArmorStandsEditServerTest implements ServerTest {
  @Override
  public void runTest(ServerTestContext context) {
    context.waitTicks(2);

    ServerPlayer player = context.spawnPlayer();
    ArmorStand stand = spawnStand(context);

    context.runOnServer((server) -> {
      ArmorStandEditor editor = ArmorStandEditor.get(player, stand);

      // Flag: real FlagAction pipeline, read back off the stand's vanilla state.
      check(!stand.showArms(), "arms are hidden on a fresh stand");
      editor.setFlag(ArmorStandFlag.SHOW_ARMS, true);
      check(stand.showArms(), "setFlag(SHOW_ARMS, true) shows arms");
      check(ArmorStandFlag.SHOW_ARMS.getValue(stand), "flag getValue agrees with the entity");

      // Rotation.
      editor.setRotation(90.0f);
      checkClose(90.0f, stand.getYRot(), 0.01f, "setRotation(90)");

      // Scale (the SCALE attribute).
      editor.setScale(2.0f);
      checkClose(2.0f, stand.getScale(), 0.01f, "setScale(2)");

      // Pose, then undo/redo round-trips it.
      Rotations zero = new Rotations(0.0f, 0.0f, 0.0f);
      checkClose(0.0f, stand.getHeadPose().x(), 0.01f, "head pose starts at zero");
      editor.setPose(new Rotations(15.0f, 0.0f, 0.0f), zero, zero, zero, zero, zero);
      checkClose(15.0f, stand.getHeadPose().x(), 0.01f, "setPose tilts the head");

      check(editor.undo(), "undo pops the pose action");
      checkClose(0.0f, stand.getHeadPose().x(), 0.01f, "undo restores the head pose");
      check(editor.redo(), "redo re-applies the pose action");
      checkClose(15.0f, stand.getHeadPose().x(), 0.01f, "redo re-tilts the head");
    });
  }
}
