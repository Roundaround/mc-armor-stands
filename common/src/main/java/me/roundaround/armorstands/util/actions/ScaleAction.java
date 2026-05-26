package me.roundaround.armorstands.util.actions;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

public class ScaleAction implements ArmorStandAction {
  private final float argument;
  private final boolean absolute;
  private final boolean round;
  private Float originalScale = null;

  private ScaleAction(float argument, boolean absolute, boolean round) {
    this.argument = argument;
    this.absolute = absolute;
    this.round = round;
  }

  public static ScaleAction absolute(float scale) {
    return absolute(scale, false);
  }

  public static ScaleAction absolute(float scale, boolean round) {
    return new ScaleAction(scale, true, round);
  }

  public static ScaleAction relative(float scale) {
    return relative(scale, true);
  }

  public static ScaleAction relative(float scale, boolean round) {
    return new ScaleAction(scale, false, round);
  }

  @Override
  public Component getName(ArmorStand armorStand) {
    return Component.translatable("armorstands.action.scale");
  }

  @Override
  public void apply(Player player, ArmorStand armorStand) {
    this.originalScale = armorStand.getScale();

    float scale = this.argument;
    if (!this.absolute) {
      scale += this.originalScale;
    }

    setScale(armorStand, scale, this.round);
  }

  @Override
  public void undo(Player player, ArmorStand armorStand) {
    if (this.originalScale == null) {
      return;
    }
    setScale(armorStand, this.originalScale);
  }

  public static void setScale(ArmorStand armorStand, float scale) {
    setScale(armorStand, scale, false);
  }

  public static void setScale(ArmorStand armorStand, float scale, boolean round) {
    float target = Mth.clamp(scale, 0.01f, 10f);
    if (round) {
      if (target < 1) {
        target = Math.round(target * 4) / 4f;
      } else {
        target = Math.round(target * 2) / 2f;
      }
    }

    AttributeInstance attribute = armorStand.getAttributes().getInstance(Attributes.SCALE);
    if (attribute != null) {
      attribute.setBaseValue(target);
    }
  }
}
