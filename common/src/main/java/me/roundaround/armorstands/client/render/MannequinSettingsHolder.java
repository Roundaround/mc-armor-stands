package me.roundaround.armorstands.client.render;

/**
 * Duck interface implemented on {@code ArmorStandRenderState} (via a mixin) to carry the per-stand
 * {@link MannequinSettings} from extraction time to render time, alongside the mannequin name (see
 * {@link MannequinNameHolder}).
 *
 * <p>The settings must ride on the render state (rather than being looked up live by UUID at render
 * time) because {@code shouldRenderAsMannequin} — which gates suppression of the vanilla stick model
 * and its layers — only receives the {@code ArmorStandRenderState}, not the entity or its UUID.
 *
 * <p>Never returns {@code null}: a freshly-extracted state that was not populated defaults to
 * {@link MannequinSettings#DEFAULT}.
 */
public interface MannequinSettingsHolder {
  MannequinSettings armorstands$getMannequinSettings();

  void armorstands$setMannequinSettings(MannequinSettings settings);
}
