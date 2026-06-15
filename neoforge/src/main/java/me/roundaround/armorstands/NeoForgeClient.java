package me.roundaround.armorstands;

import com.mojang.blaze3d.platform.InputConstants;
import me.roundaround.armorstands.client.ArmorStandsKeyMappings;
import me.roundaround.armorstands.client.ClientSideConfig;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.trove.client.KeyBindings;
import me.roundaround.trove.client.gui.screen.ConfigScreen;
import me.roundaround.trove.resource.BuiltinResourcePack;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class NeoForgeClient {
  public static void init(IEventBus modBus, ModContainer container) {
    modBus.addListener(
        FMLClientSetupEvent.class, event -> {
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
        }
    );

    container.registerExtensionPoint(
        IConfigScreenFactory.class, (modContainer, parent) -> {
          ClientSideConfig config = ClientSideConfig.getInstance();
          return config.isApplicable() ? new ConfigScreen(parent, Constants.MOD_ID, config) : null;
        }
    );
  }

  private NeoForgeClient() {
  }
}
