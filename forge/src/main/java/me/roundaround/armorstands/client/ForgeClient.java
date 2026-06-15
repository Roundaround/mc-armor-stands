package me.roundaround.armorstands.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.trove.client.KeyBindings;
import me.roundaround.trove.client.gui.screen.ConfigScreen;
import me.roundaround.trove.resource.BuiltinResourcePack;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class ForgeClient {
  public static void init(FMLJavaModLoadingContext context) {
    FMLClientSetupEvent.getBus(context.getModBusGroup()).addListener(event -> {
      ClientSideConfig.getInstance().init();

      ArmorStandsKeyMappings.highlightArmorStand = KeyBindings.register(new KeyMapping(
          "armorstands.key.highlight_armor_stand",
          InputConstants.Type.KEYSYM,
          InputConstants.UNKNOWN.getValue(),
          KeyMapping.Category.MISC
      ));

      BuiltinResourcePack.register(
          Constants.MOD_ID,
          "armorstands-dark-ui",
          Component.translatable("armorstands.resource.darkui")
      );
    });

    context.getContainer().registerExtensionPoint(
        ConfigScreenHandler.ConfigScreenFactory.class,
        () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> {
          ClientSideConfig config = ClientSideConfig.getInstance();
          return config.isApplicable() ? new ConfigScreen(parent, Constants.MOD_ID, config) : null;
        })
    );
  }

  private ForgeClient() {
  }
}
