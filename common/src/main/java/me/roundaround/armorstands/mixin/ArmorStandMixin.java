package me.roundaround.armorstands.mixin;

import me.roundaround.armorstands.interfaces.ArmorStandScreenHandlerAccess;
import me.roundaround.armorstands.client.ClientSideConfig;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.trove.config.option.BooleanConfigOption;
import me.roundaround.armorstands.server.ArmorStandUsers;
import me.roundaround.armorstands.server.config.ServerSideConfig;
import me.roundaround.armorstands.util.LastUsedScreen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin {
  @Inject(
      method = "interact", at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/world/entity/decoration/ArmorStand;getEquipmentSlotForItem(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/EquipmentSlot;"
  ), cancellable = true
  )
  public void interact(
      Player player,
      InteractionHand hand,
      Vec3 location,
      CallbackInfoReturnable<InteractionResult> cir
  ) {
    if (!(player instanceof ServerPlayer serverPlayer) || !ArmorStandUsers.canEditArmorStands(serverPlayer)) {
      return;
    }

    if (serverPlayer.isShiftKeyDown() != doesConfigRequireSneaking(serverPlayer)) {
      return;
    }

    ArmorStand self = (ArmorStand) (Object) this;
    ScreenType screenType = LastUsedScreen.getOrDefault(serverPlayer, self, ScreenType.UTILITIES);
    ((ArmorStandScreenHandlerAccess) serverPlayer).armorstands$openScreen(self, screenType);
    cir.setReturnValue(InteractionResult.PASS);
  }

  @Unique
  private static boolean doesConfigRequireSneaking(ServerPlayer player) {
    MinecraftServer server = player.level().getServer();
    BooleanConfigOption configOption = server.isDedicatedServer() ?
        ServerSideConfig.getInstance().requireSneakingToEdit :
        ClientSideConfig.getInstance().requireSneakingToEdit;
    return configOption.getValue();
  }
}
