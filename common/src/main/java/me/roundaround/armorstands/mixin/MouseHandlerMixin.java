package me.roundaround.armorstands.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.roundaround.armorstands.client.gui.screen.AbstractArmorStandScreen;
import me.roundaround.armorstands.client.gui.screen.PassesEventsThrough;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
  @Shadow
  @Final
  private Minecraft minecraft;

  @Inject(method = "isMouseGrabbed", at = @At(value = "HEAD"), cancellable = true)
  public void isCursorLocked(CallbackInfoReturnable<Boolean> info) {
    if (this.minecraft.gui.screen() instanceof AbstractArmorStandScreen standScreen) {
      info.setReturnValue(standScreen.isCursorLocked());
    }
  }

  // 26.2 moved the screen off Minecraft onto Gui; onButton reads gui.screen() four times. Null ONLY the
  // last (ordinal 3) — the in-world-click guard `if (gui.screen() == null && overlay == null)` — so a
  // pass-through click still routes to Screen.mouseClicked (buttons work) AND falls through to the world.
  // (26.1 targeted the equivalent field-read ordinal 3; nulling all reads starved that routing → no clicks.)
  @ModifyExpressionValue(
      method = "onButton", at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/gui/Gui;screen()Lnet/minecraft/client/gui/screens/Screen;",
      ordinal = 3
  )
  )
  private Screen modifyCurrentScreen(Screen screen) {
    return screen instanceof PassesEventsThrough passScreen && passScreen.shouldPassEvents() ? null : screen;
  }
}
