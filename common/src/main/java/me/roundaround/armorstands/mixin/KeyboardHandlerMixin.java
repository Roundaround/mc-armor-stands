package me.roundaround.armorstands.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.roundaround.armorstands.client.gui.screen.PassesEventsThrough;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
  // 26.2 reads the screen via Gui.screen() instead of the Minecraft.screen field. Null ONLY the
  // `boolean handlesGameInput = gui.screen() == null` read (ordinal 2) so a pass-through key still reaches
  // Screen.keyPressed (Alt/Esc/Ctrl-shortcuts work) yet also drives KeyMapping for game movement.
  // (26.1 targeted field-read ordinal 4; 26.2 dropped two earlier reads, shifting this one to ordinal 2.)
  @ModifyExpressionValue(
      method = "keyPress", at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/gui/Gui;screen()Lnet/minecraft/client/gui/screens/Screen;",
      ordinal = 2
  )
  )
  private Screen modifyCurrentScreen(Screen screen) {
    return screen instanceof PassesEventsThrough passScreen && passScreen.shouldPassEvents() ? null : screen;
  }
}
