package me.roundaround.armorstands.mixin;

import me.roundaround.armorstands.client.gui.screen.AbstractArmorStandScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 26.2 split the HUD render out of Gui into Hud.extractRenderState. Gui.extractRenderState now also
// drives the SCREEN render, so cancelling THAT (the old approach) blanked the editor screen. Suppress
// only the HUD draw when an armor-stand editor is open; the screen still renders.
@Mixin(Hud.class)
public abstract class HudMixin {
  @Shadow
  @Final
  private Minecraft minecraft;

  @Inject(method = "extractRenderState", at = @At(value = "HEAD"), cancellable = true)
  private void suppressHudForArmorStandScreen(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
    if (this.minecraft.gui.screen() instanceof AbstractArmorStandScreen) {
      ci.cancel();
    }
  }
}
