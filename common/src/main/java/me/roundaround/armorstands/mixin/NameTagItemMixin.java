package me.roundaround.armorstands.mixin;

import me.roundaround.armorstands.server.ArmorStandUsers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NameTagItem.class)
public abstract class NameTagItemMixin {
  @Inject(
      method = "interactLivingEntity", at = @At(
      value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setCustomName(Lnet/minecraft/network/chat/Component;)V"
  )
  )
  public void useOnEntity(
      ItemStack stack,
      Player playerEntity,
      LivingEntity entity,
      InteractionHand hand,
      CallbackInfoReturnable<InteractionResult> info
  ) {
    if (!(playerEntity instanceof ServerPlayer player) || !ArmorStandUsers.canEditArmorStands(player)) {
      return;
    }

    if (entity instanceof ArmorStand) {
      entity.setCustomNameVisible(true);
    }
  }
}
