package me.roundaround.armorstands.mixin;

import me.roundaround.armorstands.client.gui.screen.PassesEventsThrough;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {
  @Shadow
  boolean isDown;

  @Inject(method = "isDown()Z", at = @At("HEAD"), cancellable = true)
  private void bypassConflictContextForPassthrough(CallbackInfoReturnable<Boolean> info) {
    if (this.isDown
        && Minecraft.getInstance().gui.screen() instanceof PassesEventsThrough pst
        && pst.shouldPassEvents()) {
      info.setReturnValue(true);
    }
  }
}
