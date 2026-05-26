package me.roundaround.armorstands.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.roundaround.armorstands.client.ClientSideConfig;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorStandRenderer.class)
public abstract class ArmorStandRendererMixin extends LivingEntityRenderer<ArmorStand,
    ArmorStandRenderState, ArmorStandArmorModel> {

  @ModifyReturnValue(method = "shouldShowName(Lnet/minecraft/world/entity/decoration/ArmorStand;D)Z", at = @At("RETURN"))
  private boolean adjustHasLabel(boolean original, ArmorStand entity, double squaredDistanceToCamera) {
    if (!original) {
      return false;
    }

    ClientSideConfig config = ClientSideConfig.getInstance();

    if (config.directOnlyNameRender.getPendingValue() && this.entityRenderDispatcher.crosshairPickEntity != entity) {
      return false;
    }

    int nameRenderDistance = config.nameRenderDistance.getPendingValue();
    int maxSquaredDistance = nameRenderDistance * nameRenderDistance;
    if (maxSquaredDistance > 0) {
      return squaredDistanceToCamera < maxSquaredDistance;
    }

    return true;
  }

  private ArmorStandRendererMixin(
      EntityRendererProvider.Context ctx,
      ArmorStandArmorModel model,
      float shadowRadius
  ) {
    super(ctx, model, shadowRadius);
  }
}
