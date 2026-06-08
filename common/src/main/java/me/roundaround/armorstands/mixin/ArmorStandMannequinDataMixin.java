package me.roundaround.armorstands.mixin;

import me.roundaround.armorstands.client.render.MannequinSettings;
import me.roundaround.armorstands.interfaces.MannequinSettingsAccess;
import me.roundaround.armorstands.server.network.ServerNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the per-stand mannequin settings to the vanilla {@code ArmorStand} as a plain int bitmask
 * field. The field is kept off the vanilla synched-entity-data stream so mod-less clients never trip
 * over an extra data slot; replication to tracking clients is done manually via a custom S2C payload
 * (see {@code ServerNetworking#broadcastMannequinSettings}). Settings persist through save/load and
 * can be mutated by the server-side editor (gaining undo/redo). Common env: runs on both server and
 * client.
 */
@Mixin(ArmorStand.class)
public abstract class ArmorStandMannequinDataMixin implements MannequinSettingsAccess {
  @Unique
  private int armorstands$mannequinBits = MannequinSettings.DEFAULT.toBits();

  @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
  private void armorstands$saveMannequinSettings(ValueOutput output, CallbackInfo info) {
    output.putInt("MannequinSettings", this.armorstands$mannequinBits);
  }

  @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
  private void armorstands$loadMannequinSettings(ValueInput input, CallbackInfo info) {
    this.armorstands$mannequinBits = input.getIntOr("MannequinSettings", MannequinSettings.DEFAULT.toBits());
  }

  @Override
  public MannequinSettings armorstands$getMannequinSettings() {
    return MannequinSettings.fromBits(this.armorstands$mannequinBits);
  }

  @Override
  public void armorstands$setMannequinSettings(MannequinSettings settings) {
    this.armorstands$mannequinBits = settings.toBits();
    ArmorStand armorStand = (ArmorStand) (Object) this;
    if (armorStand.level() instanceof ServerLevel) {
      // The vanilla synched-data stream no longer carries the settings, so replicate the new value
      // to every tracking client ourselves. Client-side writes (level is a ClientLevel) skip this.
      ServerNetworking.broadcastMannequinSettings(armorStand);
    }
  }
}
