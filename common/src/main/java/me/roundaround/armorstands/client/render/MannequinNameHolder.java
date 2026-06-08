package me.roundaround.armorstands.client.render;

import java.util.UUID;

/**
 * Duck interface implemented on {@code ArmorStandRenderState} (via a mixin) to carry the armor
 * stand's custom name from extraction time to render time. The render-state's own {@code nameTag}
 * field is only populated when the name label is actually being drawn (in range, visible, not
 * suppressed by config), so it cannot be used to decide whether a stand has a custom name.
 *
 * <p>Mannequin mode renders only when this name is present, and uses it as the skin username.
 *
 * <p>The stand's entity UUID is carried alongside the name (captured in the same extraction step)
 * so the renderer can key per-stand state — chiefly the LOCKED-skin freeze store — by a stable
 * identity rather than the shared, name-derived skin pipeline. The UUID is the only per-stand
 * identity available at render time, where only the {@code ArmorStandRenderState} is in hand.
 */
public interface MannequinNameHolder {
  String armorstands$getMannequinName();

  void armorstands$setMannequinName(String name);

  UUID armorstands$getMannequinUuid();

  void armorstands$setMannequinUuid(UUID uuid);
}
