package me.roundaround.armorstands.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import me.roundaround.armorstands.client.gui.screen.AbstractArmorStandScreen;
import me.roundaround.armorstands.client.gui.screen.PassesEventsThrough;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
  @Shadow
  public Screen screen;

  @Inject(method = "shouldEntityAppearGlowing", at = @At(value = "HEAD"), cancellable = true)
  private void hasOutline(Entity entity, CallbackInfoReturnable<Boolean> info) {
    if (!(this.screen instanceof AbstractArmorStandScreen standScreen)) {
      return;
    }

    info.setReturnValue(standScreen.shouldHighlight(entity));
  }

  @ModifyExpressionValue(
      method = "tick", at = @At(
      value = "FIELD",
      opcode = Opcodes.GETFIELD,
      target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;"
  ), slice = @Slice(
      from = @At(
          value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;clearCache()V"
      ), to = @At(
      value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;handleKeybinds()V"
  )
  )
  )
  private Screen modifyCurrentScreen(Screen screen) {
    if (screen instanceof PassesEventsThrough passScreen && passScreen.shouldPassEvents()) {
      // Forge/NeoForge set movement keys to IN_GAME conflict context,
      // which makes KeyMapping.set() skip them when a screen is open.
      // Sync key state directly from GLFW before the tick processes input.
      KeyMapping.setAll();
      return null;
    }
    return screen;
  }

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
