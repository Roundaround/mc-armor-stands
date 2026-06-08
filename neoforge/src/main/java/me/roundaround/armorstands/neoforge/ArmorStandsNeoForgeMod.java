package me.roundaround.armorstands.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import me.roundaround.armorstands.client.ArmorStandsKeyMappings;
import me.roundaround.armorstands.client.ClientSideConfig;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.network.Networking;
import me.roundaround.armorstands.server.command.ArmorStandsCommand;
import me.roundaround.armorstands.server.config.ServerSideConfig;
import me.roundaround.armorstands.server.network.ServerNetworking;
import me.roundaround.trove.client.KeyBindings;
import me.roundaround.trove.client.gui.screen.ConfigScreen;
import me.roundaround.trove.neoforge.TroveNeoForge;
import me.roundaround.trove.resource.BuiltinResourcePack;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("armorstands")
public final class ArmorStandsNeoForgeMod {
  public ArmorStandsNeoForgeMod(IEventBus modBus, ModContainer container) {
    TroveNeoForge.bootstrap(modBus, container);
    Networking.register();

    NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> {
      ArmorStandsCommand.register(event.getDispatcher());
    });

    NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, event -> {
      if (!(event.getServer() instanceof DedicatedServer dedicatedServer)) return;
      ServerSideConfig.create(dedicatedServer).init();
    });

    NeoForge.EVENT_BUS.addListener(PlayerEvent.StartTracking.class, event -> {
      if (!(event.getTarget() instanceof ArmorStand armorStand)) return;
      if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
      ServerNetworking.sendMannequinSettings(serverPlayer, armorStand);
    });

    modBus.addListener(FMLClientSetupEvent.class, event -> {
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

    container.registerExtensionPoint(IConfigScreenFactory.class,
        (modContainer, parent) -> {
          ClientSideConfig config = ClientSideConfig.getInstance();
          return config.isApplicable() ? new ConfigScreen(parent, Constants.MOD_ID, config) : null;
        });
  }
}
