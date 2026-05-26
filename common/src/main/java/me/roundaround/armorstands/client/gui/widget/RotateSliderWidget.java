package me.roundaround.armorstands.client.gui.widget;

import me.roundaround.armorstands.client.gui.screen.AbstractArmorStandScreen;
import me.roundaround.armorstands.client.network.ClientNetworking;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
import java.util.Optional;

public class RotateSliderWidget extends AbstractSliderButton {
  private final AbstractArmorStandScreen parent;
  private final ArmorStand armorStand;

  private Optional<Float> lastAngle = Optional.empty();
  private int min = -180;
  private int max = 180;
  private Optional<Long> lastScroll = Optional.empty();
  private boolean isDragging = false;
  private boolean pendingDragPing = false;

  public RotateSliderWidget(AbstractArmorStandScreen parent, int width, int height, ArmorStand armorStand) {
    super(0, 0, width, height, Component.empty(), 0);

    this.parent = parent;
    this.armorStand = armorStand;

    refresh();
  }

  public boolean isPending(Screen parent) {
    return isDragging() || this.lastScroll.isPresent();
  }

  public void setRange(int min, int max) {
    this.min = min;
    this.max = max;
    refresh();
  }

  public void refresh() {
    float armorStandAngle = this.armorStand.getYRot();
    if (this.lastAngle.isPresent() && Math.abs(armorStandAngle - this.lastAngle.get()) < Mth.EPSILON) {
      return;
    }

    this.lastAngle = Optional.of(armorStandAngle);
    setAngle(armorStandAngle);
  }

  public void zero() {
    setAngle(0);
    persistValue();
  }

  public void increment() {
    double up = Math.ceil(getAngle());
    if (up - getAngle() < Mth.EPSILON) {
      up += 1;
    }
    setAngle((float) up);
    persistValue();
  }

  public void decrement() {
    double down = Math.floor(getAngle());
    if (getAngle() - down < Mth.EPSILON) {
      down -= 1;
    }
    setAngle((float) down);
    persistValue();
  }

  public void tick() {
    this.lastScroll.ifPresent(lastScroll -> {
      if (System.currentTimeMillis() - lastScroll > 500) {
        this.lastScroll = Optional.empty();
        persistValue();
      }
    });
  }

  public boolean isDragging() {
    return this.isDragging || this.pendingDragPing;
  }

  public void onPong() {
    this.pendingDragPing = false;
  }

  @Override
  protected void updateMessage() {
    setMessage(Component.translatable("armorstands.angle", String.format("%.2f", getAngle())));
  }

  @Override
  protected void applyValue() {
    this.armorStand.setYRot(getAngle());
  }

  @Override
  public void onClick(MouseButtonEvent click, boolean doubled) {
    super.onClick(click, doubled);

    this.isDragging = true;
  }

  @Override
  public void onRelease(MouseButtonEvent click) {
    super.onRelease(click);

    this.isDragging = false;
    this.pendingDragPing = true;
    this.parent.sendPing();

    persistValue();
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    if (isMouseOver(mouseX, mouseY)) {
      setAngle(getAngle() + (float) verticalAmount);
      applyValue();
      this.lastScroll = Optional.of(System.currentTimeMillis());
      return true;
    }

    return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
  }

  @Override
  protected void onDrag(MouseButtonEvent click, double deltaX, double deltaY) {
    this.isDragging = true;
    super.onDrag(click, deltaX, deltaY);
  }

  @Override
  public void playDownSound(SoundManager soundManager) {
    soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
  }

  private float getAngle() {
    return valueToAngle(this.value, this.min, this.max);
  }

  public void setAngle(float value) {
    setValue(angleToValue(value, this.min, this.max));
  }

  private static double angleToValue(float value, int min, int max) {
    // Map angle (min-max) to value (0-1)
    return (value - min) / (max - min);
  }

  private static float valueToAngle(double value, int min, int max) {
    // Map value (0-1) to angle (min-max)
    return (float) (value * (max - min) + min);
  }

  private void persistValue() {
    // Cancel any pending scroll updates
    this.lastScroll = Optional.empty();

    ClientNetworking.sendSetYawPacket(getAngle());
  }
}
