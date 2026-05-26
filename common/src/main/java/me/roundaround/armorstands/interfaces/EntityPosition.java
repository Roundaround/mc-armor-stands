package me.roundaround.armorstands.interfaces;

import me.roundaround.allay.api.InjectedInterface;
import net.minecraft.world.phys.Vec3;

@InjectedInterface
public interface EntityPosition {
  default Vec3 armorstands$getPos() {
    return Vec3.ZERO;
  }
}
