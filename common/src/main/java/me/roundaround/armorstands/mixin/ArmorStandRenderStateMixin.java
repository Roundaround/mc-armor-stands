package me.roundaround.armorstands.mixin;

import java.util.UUID;
import me.roundaround.allay.api.MixinEnv;
import me.roundaround.armorstands.client.render.MannequinNameHolder;
import me.roundaround.armorstands.client.render.MannequinSettings;
import me.roundaround.armorstands.client.render.MannequinSettingsHolder;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ArmorStandRenderState.class)
@MixinEnv(MixinEnv.Env.CLIENT)
public class ArmorStandRenderStateMixin implements MannequinNameHolder, MannequinSettingsHolder {
  @Unique
  private String armorstands$mannequinName;

  @Unique
  private UUID armorstands$mannequinUuid;

  @Unique
  private MannequinSettings armorstands$mannequinSettings = MannequinSettings.DEFAULT;

  @Override
  public String armorstands$getMannequinName() {
    return this.armorstands$mannequinName;
  }

  @Override
  public void armorstands$setMannequinName(String name) {
    this.armorstands$mannequinName = name;
  }

  @Override
  public UUID armorstands$getMannequinUuid() {
    return this.armorstands$mannequinUuid;
  }

  @Override
  public void armorstands$setMannequinUuid(UUID uuid) {
    this.armorstands$mannequinUuid = uuid;
  }

  @Override
  public MannequinSettings armorstands$getMannequinSettings() {
    return this.armorstands$mannequinSettings;
  }

  @Override
  public void armorstands$setMannequinSettings(MannequinSettings settings) {
    this.armorstands$mannequinSettings = settings;
  }
}
