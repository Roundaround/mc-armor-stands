package me.roundaround.armorstands.mixin;

import me.roundaround.armorstands.client.gui.screen.ArmorStandPoseScreen;
import me.roundaround.armorstands.network.PosePart;
import me.roundaround.allay.api.MixinEnv;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Rotations;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStand.class)
@MixinEnv(MixinEnv.Env.CLIENT)
public class ArmorStandClientMixin {
  @Inject(method = "setHeadPose", at = @At("RETURN"))
  public void setHeadRotation(Rotations headRotation, CallbackInfo info) {
    Minecraft client = Minecraft.getInstance();
    if (client.gui.screen() instanceof ArmorStandPoseScreen screen) {
      screen.onArmorStandPoseChanged(this.self(), PosePart.HEAD);
    }
  }

  @Inject(method = "setBodyPose", at = @At("RETURN"))
  public void setBodyRotation(Rotations bodyRotation, CallbackInfo info) {
    Minecraft client = Minecraft.getInstance();
    if (client.gui.screen() instanceof ArmorStandPoseScreen screen) {
      screen.onArmorStandPoseChanged(this.self(), PosePart.BODY);
    }
  }

  @Inject(method = "setLeftArmPose", at = @At("RETURN"))
  public void setLeftArmRotation(Rotations leftArmRotation, CallbackInfo info) {
    Minecraft client = Minecraft.getInstance();
    if (client.gui.screen() instanceof ArmorStandPoseScreen screen) {
      screen.onArmorStandPoseChanged(this.self(), PosePart.LEFT_ARM);
    }
  }

  @Inject(method = "setRightArmPose", at = @At("RETURN"))
  public void setRightArmRotation(Rotations rightArmRotation, CallbackInfo info) {
    Minecraft client = Minecraft.getInstance();
    if (client.gui.screen() instanceof ArmorStandPoseScreen screen) {
      screen.onArmorStandPoseChanged(this.self(), PosePart.RIGHT_ARM);
    }
  }

  @Inject(method = "setLeftLegPose", at = @At("RETURN"))
  public void setLeftLegRotation(Rotations leftLegRotation, CallbackInfo info) {
    Minecraft client = Minecraft.getInstance();
    if (client.gui.screen() instanceof ArmorStandPoseScreen screen) {
      screen.onArmorStandPoseChanged(this.self(), PosePart.LEFT_LEG);
    }
  }

  @Inject(method = "setRightLegPose", at = @At("RETURN"))
  public void setRightLegRotation(Rotations rightLegRotation, CallbackInfo info) {
    Minecraft client = Minecraft.getInstance();
    if (client.gui.screen() instanceof ArmorStandPoseScreen screen) {
      screen.onArmorStandPoseChanged(this.self(), PosePart.RIGHT_LEG);
    }
  }

  @Unique
  private ArmorStand self() {
    return (ArmorStand) (Object) this;
  }
}
