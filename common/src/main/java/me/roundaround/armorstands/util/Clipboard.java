package me.roundaround.armorstands.util;

import me.roundaround.armorstands.server.network.ServerNetworking;
import me.roundaround.armorstands.util.actions.ClipboardPasteAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class Clipboard {
  private static final HashMap<UUID, Entry> entries = new HashMap<>();

  public static void copy(ServerPlayer player, ArmorStand armorStand) {
    entries.put(player.getUUID(), Entry.everything(armorStand));
    ServerNetworking.sendMessagePacket(player, "armorstands.message.copy");
  }

  public static boolean paste(ServerPlayer player, ArmorStandEditor editor) {
    if (!entries.containsKey(player.getUUID())) {
      return false;
    }

    editor.applyAction(ClipboardPasteAction.create(entries.get(player.getUUID())));
    ServerNetworking.sendMessagePacket(player, "armorstands.message.paste");
    return true;
  }

  public static void remove(ServerPlayer player) {
    entries.remove(player.getUUID());
  }

  // Use all Optionals so that we can do partial copies if we want
  public static class Entry implements ArmorStandApplyable {
    private Optional<Pose> pose;
    private Optional<FlagSnapshot> flags;

    private Entry(Optional<Pose> pose, Optional<FlagSnapshot> flags) {
      this.pose = pose;
      this.flags = flags;
    }

    public static Entry everything(ArmorStand armorStand) {
      return new Entry(Optional.of(new Pose(armorStand)), Optional.of(FlagSnapshot.all(armorStand)));
    }

    public static Entry poseOnly(ArmorStand armorStand) {
      return new Entry(Optional.of(new Pose(armorStand)), Optional.empty());
    }

    public static Entry flagsOnly(ArmorStand armorStand) {
      return new Entry(Optional.empty(), Optional.of(FlagSnapshot.all(armorStand)));
    }

    @Override
    public void apply(Player player, ArmorStand armorStand) {
      pose.ifPresent((pose) -> pose.apply(player, armorStand));
      flags.ifPresent((flags) -> flags.apply(player, armorStand));
    }
  }
}
