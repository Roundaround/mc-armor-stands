package me.roundaround.armorstands.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.roundaround.allay.api.Entrypoint;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.trove.client.KeyBindings;
import me.roundaround.trove.resource.BuiltinResourcePack;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

@Entrypoint(Entrypoint.CLIENT)
public class ArmorStandsClientMod implements ClientModInitializer {
  public static final String RESOURCE_PACK_ID = "armorstands-dark-ui";

  @Override
  public void onInitializeClient() {
    ArmorStandsKeyMappings.highlightArmorStand = KeyBindings.register(new KeyMapping(
        "armorstands.key.highlight_armor_stand",
        InputConstants.Type.KEYSYM,
        InputConstants.UNKNOWN.getValue(),
        KeyMapping.Category.MISC
    ));

    BuiltinResourcePack.register(
        Constants.MOD_ID, RESOURCE_PACK_ID, Component.translatable("armorstands.resource.darkui"));
  }
}
