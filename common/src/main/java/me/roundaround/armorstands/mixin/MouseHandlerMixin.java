package me.roundaround.armorstands.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.roundaround.armorstands.client.gui.screen.AbstractArmorStandScreen;
import me.roundaround.armorstands.client.gui.screen.PassesEventsThrough;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import org.objectweb.asm.Opcodes;
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
    if (this.minecraft.screen instanceof AbstractArmorStandScreen standScreen) {
      info.setReturnValue(standScreen.isCursorLocked());
    }
  }

  @ModifyExpressionValue(
      method = "onButton", at = @At(
      value = "FIELD",
      opcode = Opcodes.GETFIELD,
      target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;",
      ordinal = 3
  )
  )
  private Screen modifyCurrentScreen(Screen screen) {
    return screen instanceof PassesEventsThrough passScreen && passScreen.shouldPassEvents() ? null : screen;
  }
}
