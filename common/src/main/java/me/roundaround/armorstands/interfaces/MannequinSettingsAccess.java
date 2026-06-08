package me.roundaround.armorstands.interfaces;

import me.roundaround.armorstands.client.render.MannequinSettings;

/**
 * Duck interface implemented on {@code ArmorStand} (via {@code ArmorStandMannequinDataMixin}) to
 * read and write the stand's mannequin settings, which are backed by a plain int bitmask field (kept
 * off the vanilla synched-entity-data stream so mod-less clients don't crash). Writing on the server
 * replicates to every tracking client via a custom S2C payload; writing on the client only updates
 * the local copy and does not re-broadcast.
 */
public interface MannequinSettingsAccess {
  MannequinSettings armorstands$getMannequinSettings();

  void armorstands$setMannequinSettings(MannequinSettings settings);
}
