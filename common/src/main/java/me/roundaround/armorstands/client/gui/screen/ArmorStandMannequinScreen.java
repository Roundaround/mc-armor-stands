package me.roundaround.armorstands.client.gui.screen;

import me.roundaround.armorstands.client.network.ClientNetworking;
import me.roundaround.armorstands.client.render.MannequinRenderer;
import me.roundaround.armorstands.client.render.MannequinSettings;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.interfaces.MannequinSettingsAccess;
import me.roundaround.armorstands.network.MannequinFlag;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import me.roundaround.trove.client.gui.layout.linear.LinearLayoutWidget;
import me.roundaround.trove.client.gui.util.GuiUtil;
import me.roundaround.trove.client.gui.util.Spacing;
import me.roundaround.trove.client.gui.widget.ToggleWidget;
import me.roundaround.trove.client.gui.widget.drawable.HorizontalLineWidget;
import me.roundaround.trove.client.gui.widget.drawable.LabelWidget;
import me.roundaround.trove.observable.Subject;
import me.roundaround.trove.observable.Subscription;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.ArrayList;
import java.util.List;

public class ArmorStandMannequinScreen extends AbstractArmorStandScreen {
  private static final int BUTTON_WIDTH = 100;
  private static final int BUTTON_HEIGHT = 16;
  private static final int TOGGLE_WIDTH = 150;
  private static final int FACE_SIZE = 48;

  private final ArrayList<MannequinToggle> toggles = new ArrayList<>();
  private final ArrayList<Subscription> subscriptions = new ArrayList<>();

  private CycleButton<Boolean> mainToggle;
  private LabelWidget nameValueLabel;
  private Button refreshButton;
  private boolean wasMatched = false;

  public ArmorStandMannequinScreen(ArmorStandScreenHandler handler) {
    super(handler, ScreenType.MANNEQUIN.getDisplayName());
    this.supportsUndoRedo = true;

    MannequinSettings settings = this.getSettings();
    for (MannequinFlag flag : MannequinFlag.values()) {
      this.toggles.add(new MannequinToggle(flag, Subject.of(flag.get(settings))));
    }
  }

  @Override
  public ScreenType getScreenType() {
    return ScreenType.MANNEQUIN;
  }

  @Override
  protected void populateLayout() {
    super.populateLayout();

    this.initTopLeft();
    this.initBottomLeft();
    this.initBottomRight();
  }

  private void initTopLeft() {
    this.layout.topLeft.add(
        new HorizontalLineWidget(this.utilRow.getWidth() - 2 * GuiUtil.PADDING).margin(3 * GuiUtil.PADDING),
        (configurator) -> configurator.margin(Spacing.of(0, 0, 0, GuiUtil.PADDING))
    );

    LinearLayoutWidget first = LinearLayoutWidget.vertical()
        .spacing(GuiUtil.PADDING / 2)
        .defaultOffAxisContentAlignStart();
    first.add(LabelWidget.builder(this.font, Component.translatable("armorstands.mannequin.enabled"))
        .bgColor(BACKGROUND_COLOR)
        .build());

    MannequinToggle enabledToggleData = this.toggles.stream()
        .filter((toggle) -> toggle.flag() == MannequinFlag.ENABLED)
        .findFirst()
        .orElseThrow();
    this.mainToggle = CycleButton.builder(
        (v) -> v ?
            Component.translatable(Constants.MOD_ID + ".trove.toggle.enabled") :
            Component.translatable(Constants.MOD_ID + ".trove.toggle.disabled"), false
    ).withValues(List.of(true, false)).displayOnlyValue().create(
        0,
        0,
        BUTTON_WIDTH,
        BUTTON_HEIGHT,
        Component.translatable("armorstands.mannequin.enabled"),
        (button, source) -> this.setMannequinFlag(enabledToggleData, button.getValue())
    );
    first.add(this.mainToggle);
    this.subscriptions.add(enabledToggleData.value.subscribe(this.mainToggle::setValue));

    this.layout.topLeft.add(first);
  }

  private void initBottomLeft() {
    LinearLayoutWidget wrapperPanel = LinearLayoutWidget.vertical()
        .defaultOffAxisContentAlignStart()
        .spacing(GuiUtil.PADDING);

    LinearLayoutWidget namePanel = LinearLayoutWidget.vertical()
        .defaultOffAxisContentAlignStart()
        .spacing(GuiUtil.PADDING / 2);

    namePanel.add(LabelWidget.builder(this.font, Component.translatable("armorstands.mannequin.name"))
        .bgColor(BACKGROUND_COLOR)
        .build());
    this.nameValueLabel = LabelWidget.builder(this.font, List.of()).bgColor(BACKGROUND_COLOR).build();
    namePanel.add(this.nameValueLabel);
    this.updateNameLabel();

    wrapperPanel.add(namePanel);

    wrapperPanel.add(
        new HorizontalLineWidget(BUTTON_WIDTH - 2 * GuiUtil.PADDING).margin(2 * GuiUtil.PADDING),
        (configurator) -> configurator.margin(Spacing.of(0, 0, 0, GuiUtil.PADDING))
    );

    LinearLayoutWidget profilePanel = LinearLayoutWidget.vertical()
        .defaultOffAxisContentAlignCenter()
        .spacing(GuiUtil.PADDING);

    profilePanel.add(new MannequinFaceWidget(FACE_SIZE, this::getMatchedSkin));

    this.refreshButton = Button.builder(
        Component.translatable("armorstands.mannequin.refresh"),
        (_) -> MannequinRenderer.forceRefresh(this.getArmorStand())
    ).bounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT).build();
    profilePanel.add(this.refreshButton);

    wrapperPanel.add(profilePanel);

    this.layout.bottomLeft.add(wrapperPanel);
    this.wasMatched = this.getMatchedSkin() != null;
  }

  private void initBottomRight() {
    for (MannequinToggle toggle : this.toggles) {
      if (toggle.flag() != MannequinFlag.ENABLED) {
        this.layout.bottomRight.add(this.createToggleWidget(toggle));
      }
    }
  }

  private PlayerSkin getMatchedSkin() {
    if (!this.getSettings().enabled()) {
      return null;
    }
    Component name = this.getArmorStand().getCustomName();
    return name == null ? null : MannequinRenderer.resolveMannequinSkin(name.getString());
  }

  private boolean hasCustomName() {
    Component name = this.getArmorStand().getCustomName();
    return name != null && !name.getString().isBlank();
  }

  private void updateNameLabel() {
    this.nameValueLabel.setText(this.hasCustomName() ?
        Component.literal(this.getArmorStand().getCustomName().getString()).withStyle(ChatFormatting.WHITE) :
        Component.translatable("armorstands.mannequin.name.none").withStyle(ChatFormatting.GRAY));
  }

  private ToggleWidget createToggleWidget(MannequinToggle toggle) {
    ToggleWidget widget = ToggleWidget.yesNoBuilder(this.font, (value) -> toggle.flag.getDisplayName())
        .initially(toggle.value.get())
        .setWidth(TOGGLE_WIDTH)
        .onPress((control) -> this.onToggle(toggle))
        .matchTooltipToLabel()
        .setHeight(BUTTON_HEIGHT)
        .labelBgColor(BACKGROUND_COLOR)
        .build();
    this.subscriptions.add(toggle.value.subscribe(widget::setValue));
    return widget;
  }

  private void onToggle(MannequinToggle toggle) {
    this.setMannequinFlag(toggle, !toggle.value.get());
  }

  private void setMannequinFlag(MannequinToggle toggle, boolean value) {
    // Optimistically write the client entity's settings so the widget and in-world render update
    // instantly; the authoritative editor packet follows and containerTick reads it back. The C2S
    // packet is what makes the server persist it — without it (as the main toggle used to do) the
    // change lives only on the client and is lost on rejoin.
    MannequinSettingsAccess access = (MannequinSettingsAccess) this.getArmorStand();
    access.armorstands$setMannequinSettings(toggle.flag.with(access.armorstands$getMannequinSettings(), value));
    toggle.value.set(value);
    ClientNetworking.sendSetMannequinFlagPacket(toggle.flag, value);
  }

  @Override
  public void containerTick() {
    super.containerTick();
    MannequinSettings settings = this.getSettings();
    for (MannequinToggle toggle : this.toggles) {
      toggle.value.set(toggle.flag.get(settings));
    }

    boolean matched = this.getMatchedSkin() != null;
    this.updateNameLabel();
    this.refreshButton.active = this.hasCustomName() && this.mainToggle.getValue();

    if (matched != this.wasMatched) {
      this.wasMatched = matched;
      this.repositionElements();
    }
  }

  @Override
  public void onClose() {
    this.subscriptions.forEach(Subscription::close);
    super.onClose();
  }

  private MannequinSettings getSettings() {
    return ((MannequinSettingsAccess) this.getArmorStand()).armorstands$getMannequinSettings();
  }

  private record MannequinToggle(MannequinFlag flag, Subject<Boolean> value) {
  }
}
