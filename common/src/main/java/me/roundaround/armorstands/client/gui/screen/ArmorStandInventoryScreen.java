package me.roundaround.armorstands.client.gui.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.client.network.ClientNetworking;
import me.roundaround.armorstands.network.ArmorStandFlag;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import me.roundaround.trove.client.gui.widget.ToggleWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.NonNull;

public class ArmorStandInventoryScreen extends AbstractArmorStandScreen {
  private static final int BACKGROUND_WIDTH = 176;
  private static final int BACKGROUND_HEIGHT = 166;
  private static final int TOGGLE_HEIGHT = 16;
  private static final Identifier CUSTOM_TEXTURE = Identifier.fromNamespaceAndPath(
      Constants.MOD_ID,
      "textures/gui/container/inventory.png"
  );

  private float prevMouseX;
  private float prevMouseY;
  private ToggleWidget showArmsToggle;
  private ToggleWidget lockInventoryToggle;

  public ArmorStandInventoryScreen(ArmorStandScreenHandler handler) {
    super(handler, ScreenType.INVENTORY.getDisplayName());

    this.utilizesInventory = true;
    this.passEvents = false;
  }

  @Override
  public ScreenType getScreenType() {
    return ScreenType.INVENTORY;
  }

  @Override
  protected void populateLayout() {
    super.populateLayout();

    this.showArmsToggle = this.layout.bottomRight.add(this.createToggleWidget(ArmorStandFlag.SHOW_ARMS));
    this.lockInventoryToggle = this.layout.bottomRight.add(this.createToggleWidget(ArmorStandFlag.LOCK_INVENTORY));
  }

  private ToggleWidget createToggleWidget(ArmorStandFlag flag) {
    return ToggleWidget.yesNoBuilder(this.font, (value) -> flag.getDisplayName())
        .initially(this.getFlagValue(flag))
        .onPress((toggle) -> ClientNetworking.sendSetFlagPacket(flag, !this.getFlagValue(flag)))
        .matchTooltipToLabel()
        .setHeight(TOGGLE_HEIGHT)
        .labelBgColor(BACKGROUND_COLOR)
        .build();
  }

  private boolean getFlagValue(ArmorStandFlag flag) {
    return flag.getValue(this.getArmorStand());
  }

  @Override
  protected void repositionElements() {
    this.leftPos = (this.width - BACKGROUND_WIDTH) / 2;
    this.topPos = (this.height - BACKGROUND_HEIGHT) / 2;
    super.repositionElements();
  }

  @Override
  public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    super.extractRenderState(context, mouseX, mouseY, delta);
    this.extractTooltip(context, mouseX, mouseY);
    this.prevMouseX = mouseX;
    this.prevMouseY = mouseY;
  }

  @Override
  public void extractBackground(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    context.blit(
        RenderPipelines.GUI_TEXTURED,
        CUSTOM_TEXTURE,
        this.leftPos,
        this.topPos,
        0,
        0,
        BACKGROUND_WIDTH,
        BACKGROUND_HEIGHT,
        256,
        256
    );

    ImmutableList<Pair<Slot, EquipmentSlot>> armorSlots = this.menu.getArmorSlots();
    for (int index = 0; index < armorSlots.size(); index++) {
      Slot slot = armorSlots.get(index).getFirst();
      EquipmentSlot equipmentSlot = armorSlots.get(index).getSecond();
      if (ArmorStandScreenHandler.isSlotDisabled(this.armorStand, equipmentSlot)) {
        context.fill(
            this.leftPos + slot.x,
            this.topPos + slot.y,
            this.leftPos + slot.x + 16,
            this.topPos + slot.y + 16,
            0x80000000
        );
      }
    }

    InventoryScreen.extractEntityInInventoryFollowsMouse(
        context,
        this.leftPos + 62,
        this.topPos + 8,
        this.leftPos + 114,
        this.topPos + 78,
        30,
        0.0625f,
        this.prevMouseX,
        this.prevMouseY,
        this.getArmorStand()
    );
  }

  @Override
  public void containerTick() {
    super.containerTick();

    this.showArmsToggle.setValue(ArmorStandFlag.SHOW_ARMS.getValue(this.getArmorStand()));
    this.lockInventoryToggle.setValue(ArmorStandFlag.LOCK_INVENTORY.getValue(this.getArmorStand()));
  }
}
