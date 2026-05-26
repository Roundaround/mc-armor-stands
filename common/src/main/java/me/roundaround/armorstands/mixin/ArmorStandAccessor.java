package me.roundaround.armorstands.mixin;

import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArmorStand.class)
public interface ArmorStandAccessor {
  @Invoker("setSmall")
  void invokeSetSmall(boolean small);

  @Invoker("setShowArms")
  void invokeSetShowArms(boolean showArms);

  @Invoker("setNoBasePlate")
  void invokeSetHideBasePlate(boolean hideBasePlate);

  @Accessor("disabledSlots")
  int getDisabledSlots();

  @Accessor("disabledSlots")
  void setDisabledSlots(int disabledSlots);
}
