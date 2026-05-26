package me.roundaround.armorstands.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Gui.class)
public interface GuiAccessor {
  @Invoker("updateVignetteBrightness")
  void invokeUpdateVignetteDarkness(Entity entity);

  @Invoker("extractVignette")
  void invokeExtractVignette(GuiGraphicsExtractor context, Entity entity);
}
