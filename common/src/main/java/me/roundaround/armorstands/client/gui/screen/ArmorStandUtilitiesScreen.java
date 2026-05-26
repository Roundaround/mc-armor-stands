package me.roundaround.armorstands.client.gui.screen;

import java.util.ArrayList;
import java.util.HashMap;

import me.roundaround.armorstands.client.network.ClientNetworking;
import me.roundaround.armorstands.network.ArmorStandFlag;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.armorstands.network.UtilityAction;
import me.roundaround.trove.client.gui.layout.linear.LinearLayoutWidget;
import me.roundaround.trove.client.gui.util.GuiUtil;
import me.roundaround.trove.client.gui.widget.ToggleWidget;
import me.roundaround.trove.client.gui.widget.drawable.LabelWidget;
import me.roundaround.trove.observable.Subscription;
import me.roundaround.trove.observable.Subject;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class ArmorStandUtilitiesScreen extends AbstractArmorStandScreen {
  private static final int BUTTON_WIDTH = 60;
  private static final int BUTTON_HEIGHT = 16;

  private final HashMap<ArmorStandFlag, Subject<Boolean>> values = new HashMap<>();
  private final ArrayList<Subscription> subscriptions = new ArrayList<>();

  public ArmorStandUtilitiesScreen(ArmorStandScreenHandler handler) {
    super(handler, ScreenType.UTILITIES.getDisplayName());
    this.supportsUndoRedo = true;

    ArmorStandFlag.getFlags()
        .forEach((flag) -> this.values.put(flag, Subject.of(flag.getValue(this.getArmorStand()))));
  }

  @Override
  public ScreenType getScreenType() {
    return ScreenType.UTILITIES;
  }

  @Override
  protected void populateLayout() {
    super.populateLayout();

    this.initBottomLeft();
    this.initBottomRight();
  }

  private void initBottomLeft() {
    this.layout.bottomLeft.add(LabelWidget.builder(this.font, Component.translatable("armorstands.utility.setup"))
        .bgColor(BACKGROUND_COLOR)
        .build());

    LinearLayoutWidget row1 = LinearLayoutWidget.horizontal().spacing(GuiUtil.PADDING / 2);
    row1.add(Button.builder(
            Component.translatable("armorstands.utility.prepare"),
            (button) -> ClientNetworking.sendUtilityActionPacket(UtilityAction.PREPARE)
        )
        .size(BUTTON_WIDTH, BUTTON_HEIGHT)
        .tooltip(Tooltip.create(Component.translatable("armorstands.utility.prepare.tooltip")))
        .build());
    row1.add(Button.builder(
            Component.translatable("armorstands.utility.toolRack"),
            (button) -> ClientNetworking.sendUtilityActionPacket(UtilityAction.TOOL_RACK)
        )
        .size(BUTTON_WIDTH, BUTTON_HEIGHT)
        .tooltip(Tooltip.create(Component.translatable("armorstands.utility.toolRack.tooltip")))
        .build());
    this.layout.bottomLeft.add(row1);

    LinearLayoutWidget row2 = LinearLayoutWidget.horizontal().spacing(GuiUtil.PADDING / 2);
    row2.add(Button.builder(
            Component.translatable("armorstands.utility.uprightItem"), (button) -> ClientNetworking.sendUtilityActionPacket(
                UtilityAction.UPRIGHT_ITEM.forSmall(ArmorStandFlag.SMALL.getValue(armorStand))))
        .size(BUTTON_WIDTH, BUTTON_HEIGHT)
        .tooltip(Tooltip.create(Component.translatable("armorstands.utility.uprightItem.tooltip")))
        .build());
    row2.add(Button.builder(
            Component.translatable("armorstands.utility.flatItem"), (button) -> ClientNetworking.sendUtilityActionPacket(
                UtilityAction.FLAT_ITEM.forSmall(ArmorStandFlag.SMALL.getValue(armorStand))))
        .size(BUTTON_WIDTH, BUTTON_HEIGHT)
        .tooltip(Tooltip.create(Component.translatable("armorstands.utility.flatItem.tooltip")))
        .build());
    this.layout.bottomLeft.add(row2);

    LinearLayoutWidget row3 = LinearLayoutWidget.horizontal().spacing(GuiUtil.PADDING / 2);
    row3.add(Button.builder(
            Component.translatable("armorstands.utility.block"), (button) -> ClientNetworking.sendUtilityActionPacket(
                UtilityAction.BLOCK.forSmall(ArmorStandFlag.SMALL.getValue(armorStand))))
        .size(BUTTON_WIDTH, BUTTON_HEIGHT)
        .tooltip(Tooltip.create(Component.translatable("armorstands.utility.block.tooltip")))
        .build());
    row3.add(Button.builder(
            Component.translatable("armorstands.utility.tool"), (button) -> ClientNetworking.sendUtilityActionPacket(
                UtilityAction.TOOL.forSmall(ArmorStandFlag.SMALL.getValue(armorStand))))
        .size(BUTTON_WIDTH, BUTTON_HEIGHT)
        .tooltip(Tooltip.create(Component.translatable("armorstands.utility.tool.tooltip")))
        .build());
    this.layout.bottomLeft.add(row3);
  }

  private void initBottomRight() {
    ArmorStandFlag.getFlags().forEach((flag) -> this.layout.bottomRight.add(this.createToggleWidget(flag)));
  }

  private ToggleWidget createToggleWidget(ArmorStandFlag flag) {
    ToggleWidget widget = ToggleWidget.yesNoBuilder(this.font, (value) -> flag.getDisplayName())
        .initially(this.values.get(flag).get() ^ flag.invertControl())
        .onPress((toggle) -> ClientNetworking.sendSetFlagPacket(flag, !this.values.get(flag).get()))
        .matchTooltipToLabel()
        .setHeight(BUTTON_HEIGHT)
        .labelBgColor(BACKGROUND_COLOR)
        .build();
    this.subscriptions.add(this.values.get(flag)
        .subscribe((value) -> widget.setValue(value ^ flag.invertControl())));
    return widget;
  }

  @Override
  public void containerTick() {
    super.containerTick();
    ArmorStandFlag.getFlags().forEach((flag) -> this.values.get(flag).set(flag.getValue(this.getArmorStand())));
  }

  @Override
  public void onClose() {
    this.subscriptions.forEach(Subscription::close);
    super.onClose();
  }
}
