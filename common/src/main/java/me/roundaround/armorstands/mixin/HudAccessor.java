package me.roundaround.armorstands.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// MC 26.2 split the HUD out of Gui into Hud; the vignette helpers moved with it
// (Gui.hud holds the instance).
@Mixin(Hud.class)
public interface HudAccessor {
  @Invoker("updateVignetteBrightness")
  void invokeUpdateVignetteDarkness(Entity entity);

  @Invoker("extractVignette")
  void invokeExtractVignette(GuiGraphicsExtractor context, Entity entity);
}
