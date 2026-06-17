package me.roundaround.armorstands.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import me.roundaround.armorstands.client.gui.screen.PassesEventsThrough;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public abstract class GuiMixin {
  // 26.2 moved KeyMapping.releaseAll() out of Minecraft.setScreen into Gui.setScreen; the wrap that
  // keeps movement keys held for pass-through screens follows it here.
  // (HUD suppression for armor-stand screens moved to HudMixin — Gui.extractRenderState now also drives
  // the screen render, so cancelling it there blanked the screen.)
  @WrapWithCondition(
      method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;releaseAll()V")
  )
  private boolean shouldUnpressAll(@Local(argsOnly = true) Screen screen) {
    if (!(screen instanceof PassesEventsThrough passScreen)) {
      return true;
    }
    return !passScreen.shouldPassEvents();
  }
}
