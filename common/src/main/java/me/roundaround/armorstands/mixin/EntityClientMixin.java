package me.roundaround.armorstands.mixin;

import me.roundaround.armorstands.interfaces.EntityPosition;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import me.roundaround.allay.api.MixinEnv;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
@MixinEnv(MixinEnv.Env.CLIENT)
public abstract class EntityClientMixin implements EntityPosition {
  // 26.3: every moveOrInterpolateTo overload funnels into this 4-arg one.
  @Inject(
      method = "moveOrInterpolateTo(Lnet/minecraft/world/entity/PositionPath;FFZ)V",
      at = @At(value = "HEAD"),
      cancellable = true
  )
  public void updateTrackedPositionAndAngles(
      PositionPath pos,
      float yaw,
      float pitch,
      boolean hasRotation,
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
