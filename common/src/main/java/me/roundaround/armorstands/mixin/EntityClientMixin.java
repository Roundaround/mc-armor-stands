package me.roundaround.armorstands.mixin;

import me.roundaround.armorstands.interfaces.EntityPosition;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import me.roundaround.allay.api.MixinEnv;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Entity.class)
@MixinEnv(MixinEnv.Env.CLIENT)
public abstract class EntityClientMixin implements EntityPosition {
  @Inject(
      method = "moveOrInterpolateTo(Ljava/util/Optional;Ljava/util/Optional;Ljava/util/Optional;)V",
      at = @At(value = "HEAD"),
      cancellable = true
  )
  public void updateTrackedPositionAndAngles(
      Optional<Vec3> pos,
      Optional<Float> yaw,
      Optional<Float> pitch,
      CallbackInfo info
  ) {
    if (!(this.self() instanceof ArmorStand self)) {
      return;
    }

    Level world = self.level();
    if (!world.isClientSide()) {
      return;
    }

    Minecraft client = Minecraft.getInstance();
    if (client.player == null) {
      return;
    }

    AbstractContainerMenu rawScreenHandler = client.player.containerMenu;
    if (!(rawScreenHandler instanceof ArmorStandScreenHandler screenHandler)) {
      return;
    }

    if (screenHandler.getArmorStand() == self) {
      info.cancel();
    }
  }

  @Unique
  private Entity self() {
    return (Entity) (Object) this;
  }
}
