package me.roundaround.armorstands.forge;

import com.mojang.blaze3d.platform.InputConstants;
import me.roundaround.armorstands.client.ArmorStandsKeyMappings;
import me.roundaround.armorstands.client.ClientSideConfig;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.network.Networking;
import me.roundaround.armorstands.server.command.ArmorStandsCommand;
import me.roundaround.armorstands.server.config.ServerSideConfig;
import me.roundaround.trove.client.KeyBindings;
import me.roundaround.trove.client.gui.screen.ConfigScreen;
import me.roundaround.trove.forge.TroveForge;
import me.roundaround.trove.resource.BuiltinResourcePack;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("armorstands")
public final class ArmorStandsForgeMod {
  public ArmorStandsForgeMod(FMLJavaModLoadingContext context) {
    TroveForge.bootstrap(context);
    Networking.register();

    RegisterCommandsEvent.BUS.addListener(event -> {
      ArmorStandsCommand.register(event.getDispatcher());
    });

    ServerStartedEvent.BUS.addListener(event -> {
      if (!(event.getServer() instanceof DedicatedServer dedicatedServer)) return;
      ServerSideConfig.create(dedicatedServer).init();
    });

    FMLClientSetupEvent.getBus(context.getModBusGroup()).addListener(event -> {
      ClientSideConfig.getInstance().init();

      ArmorStandsKeyMappings.highlightArmorStand = KeyBindings.register(new KeyMapping(
          "armorstands.key.highlight_armor_stand",
          InputConstants.Type.KEYSYM,
          InputConstants.UNKNOWN.getValue(),
          KeyMapping.Category.MISC
      ));

      BuiltinResourcePack.register(
          Constants.MOD_ID, "armorstands-dark-ui",
          Component.translatable("armorstands.resource.darkui"));
    });

    context.getContainer().registerExtensionPoint(
        ConfigScreenHandler.ConfigScreenFactory.class,
        () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> {
          ClientSideConfig config = ClientSideConfig.getInstance();
          return config.isApplicable() ? new ConfigScreen(parent, Constants.MOD_ID, config) : null;
        }));
  }
}
