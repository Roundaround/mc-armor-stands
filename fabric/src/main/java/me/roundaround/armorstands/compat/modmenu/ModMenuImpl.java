package me.roundaround.armorstands.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.roundaround.allay.api.Entrypoint;
import me.roundaround.armorstands.client.ClientSideConfig;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.trove.client.gui.screen.ConfigScreen;

@Entrypoint(Entrypoint.MOD_MENU)
public class ModMenuImpl implements ModMenuApi {
  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return (parent) -> {
      ClientSideConfig config = ClientSideConfig.getInstance();
      return config.isApplicable() ? new ConfigScreen(parent, Constants.MOD_ID, config) : null;
    };
  }
}
