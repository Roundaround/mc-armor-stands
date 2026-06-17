package me.roundaround.armorstands.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.roundaround.armorstands.client.gui.screen.AbstractArmorStandScreen;
import me.roundaround.armorstands.client.gui.screen.PassesEventsThrough;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
  // 26.2 moved the screen off Minecraft onto its Gui field; shadow gui and read screen() from it.
  @Shadow
  @Final
  public Gui gui;

  @Inject(method = "shouldEntityAppearGlowing", at = @At(value = "HEAD"), cancellable = true)
  private void hasOutline(Entity entity, CallbackInfoReturnable<Boolean> info) {
    if (!(this.gui.screen() instanceof AbstractArmorStandScreen standScreen)) {
      return;
    }

    info.setReturnValue(standScreen.shouldHighlight(entity));
  }

  // 26.2: the keybind gate now reads the screen via Gui.screen(); target the second such call in the
  // Gui.tick()..handleKeybinds() window (the one whose null-check decides if keybinds run).
  @ModifyExpressionValue(
      method = "tick", at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/gui/Gui;screen()Lnet/minecraft/client/gui/screens/Screen;",
      ordinal = 1
  ), slice = @Slice(
      from = @At(
          value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;tick()V"
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
}
