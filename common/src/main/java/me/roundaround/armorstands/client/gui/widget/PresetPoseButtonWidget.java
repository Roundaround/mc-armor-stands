package me.roundaround.armorstands.client.gui.widget;

import me.roundaround.armorstands.client.network.ClientNetworking;
import me.roundaround.armorstands.util.PosePreset;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class PresetPoseButtonWidget extends Button.Plain {
  private PosePreset pose = PosePreset.DEFAULT;

  public PresetPoseButtonWidget(int width, int height) {
    super(
        0,
        0,
        width,
        height,
        Component.empty(),
        (button) -> ClientNetworking.sendSetPosePresetPacket(((PresetPoseButtonWidget) button).getPose()),
        Button.DEFAULT_NARRATION
    );

    this.updateMessage();
  }

  public PosePreset getPose() {
    return this.pose;
  }

  public void setPose(PosePreset pose) {
    this.pose = pose;
    this.updateMessage();
  }

  private void updateMessage() {
    MutableComponent category = Component.translatable(
        "armorstands.presets.category",
        this.pose.getCategory().getDisplayName()
    );
    MutableComponent source = Component.translatable(
        "armorstands.presets.source",
        this.pose.getSource().getColoredName()
    );
    this.setTooltip(Tooltip.create(category.append(Component.literal("\n")).append(source)));
    this.setMessage(this.pose.getDisplayName());
  }
}
