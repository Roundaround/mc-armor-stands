package me.roundaround.armorstands.mixin;

import me.roundaround.armorstands.client.gui.screen.PassesEventsThrough;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
  protected AbstractContainerScreenMixin() {
    super(null);
  }

  @Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
  public void keyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> info) {
    if (this instanceof PassesEventsThrough passScreen && passScreen.shouldPassEvents()) {
      info.setReturnValue(super.keyPressed(input));
    }
  }
}
