package me.roundaround.armorstands.mixin;

import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import me.roundaround.armorstands.interfaces.ArmorStandScreenHandlerAccess;
import me.roundaround.armorstands.server.network.ServerNetworking;
import me.roundaround.armorstands.util.ArmorStandEditor;
import me.roundaround.armorstands.util.Clipboard;
import me.roundaround.armorstands.util.LastUsedScreen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements ArmorStandScreenHandlerAccess {
  @Shadow
  private int containerCounter;

  @Shadow
  protected abstract void nextContainerCounter();

  @Shadow
  protected abstract void initMenu(AbstractContainerMenu screenHandler);

  @Inject(method = "disconnect", at = @At(value = "HEAD"))
  public void onDisconnect(CallbackInfo info) {
    ServerPlayer self = (ServerPlayer) (Object) this;

    Clipboard.remove(self);
    ArmorStandEditor.remove(self);
    LastUsedScreen.remove(self);
  }

  @Override
  public void armorstands$openScreen(ArmorStand armorStand, ScreenType screenType) {
    ServerPlayer self = (ServerPlayer) (Object) this;

    if (self.containerMenu instanceof ArmorStandScreenHandler) {
      // Skip sending the close packet to the client so that it doesn't try to exit the screen.
      self.doCloseContainer();
    } else if (self.containerMenu != self.inventoryMenu) {
      self.closeContainer();
    }

    LastUsedScreen.set(self, armorStand, screenType);

    this.nextContainerCounter();
    ServerNetworking.sendOpenScreenPacket(self, this.containerCounter, armorStand, screenType);
    self.containerMenu = new ArmorStandScreenHandler(
        this.containerCounter, self.getInventory(), armorStand, screenType);
    this.initMenu(self.containerMenu);
  }
}
