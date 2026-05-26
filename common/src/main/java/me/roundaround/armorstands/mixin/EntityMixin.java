package me.roundaround.armorstands.mixin;

import me.roundaround.armorstands.interfaces.EntityPosition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public class EntityMixin implements EntityPosition {
  @Shadow
  private Vec3 position;

  @Override
  public Vec3 armorstands$getPos() {
    // Copy so we don't accidentally modify the coords individually inside the pos object
    return new Vec3(this.position.x, this.position.y, this.position.z);
  }
}
