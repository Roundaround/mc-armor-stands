package me.roundaround.armorstands.mixin;

import me.roundaround.armorstands.client.gui.screen.AbstractArmorStandScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
  @Shadow
  @Final
  private Minecraft minecraft;

  @Inject(method = "extractRenderState", at = @At(value = "HEAD"), cancellable = true)
  private void extractRenderState(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
    if (this.minecraft.screen instanceof AbstractArmorStandScreen) {
      ci.cancel();
    }
  }
}
