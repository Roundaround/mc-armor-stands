package me.roundaround.armorstands.client.gui.screen;

import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.client.gui.widget.RotateSliderWidget;
import me.roundaround.armorstands.client.network.ClientNetworking;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.armorstands.network.UtilityAction;
import me.roundaround.trove.client.gui.icon.BuiltinIcon;
import me.roundaround.trove.client.gui.layout.FillerWidget;
import me.roundaround.trove.client.gui.layout.linear.LinearLayoutWidget;
import me.roundaround.trove.client.gui.util.GuiUtil;
import me.roundaround.trove.client.gui.util.Spacing;
import me.roundaround.trove.client.gui.widget.IconButtonWidget;
import me.roundaround.trove.client.gui.widget.drawable.HorizontalLineWidget;
import me.roundaround.trove.client.gui.widget.drawable.LabelWidget;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ArmorStandRotateScreen extends AbstractArmorStandScreen {
  private static final int BUTTON_WIDTH = 46;
  private static final int DIRECTION_BUTTON_WIDTH = 70;
  private static final int MINI_BUTTON_WIDTH = 24;
  private static final int SLIDER_WIDTH = 4 * MINI_BUTTON_WIDTH + 3 * (GuiUtil.PADDING / 2);

  private LabelWidget playerFacingLabel;
  private LabelWidget playerRotationLabel;
  private LabelWidget standFacingLabel;
  private LabelWidget standRotationLabel;
  private RotateSliderWidget rotateSlider;

  public ArmorStandRotateScreen(ArmorStandScreenHandler handler) {
    super(handler, ScreenType.ROTATE.getDisplayName());
    this.supportsUndoRedo = true;
  }

  @Override
  public ScreenType getScreenType() {
    return ScreenType.ROTATE;
  }

  @Override
  protected void populateLayout() {
    super.populateLayout();

    this.initTopLeft();
    this.initBottomLeft();
    this.initBottomRight();
  }

  private void initTopLeft() {
    LinearLayoutWidget labels = LinearLayoutWidget.vertical()
        .spacing(3 * GuiUtil.PADDING)
        .defaultOffAxisContentAlignStart();

    LinearLayoutWidget player = LinearLayoutWidget.vertical().spacing(1).defaultOffAxisContentAlignStart();
    player.add(LabelWidget.builder(this.font, Component.translatable("armorstands.current.player"))
        .bgColor(BACKGROUND_COLOR)
        .build());
    this.playerFacingLabel = player.add(LabelWidget.builder(this.font, getCurrentFacingText(this.getPlayer()))
        .bgColor(BACKGROUND_COLOR)
        .build());
    this.playerRotationLabel = player.add(LabelWidget.builder(
        this.font,
        getCurrentRotationText(this.getPlayer())
    ).bgColor(BACKGROUND_COLOR).build());
    labels.add(player);

    LinearLayoutWidget stand = LinearLayoutWidget.vertical().spacing(1).defaultOffAxisContentAlignStart();
    stand.add(LabelWidget.builder(this.font, Component.translatable("armorstands.current.stand"))
        .bgColor(BACKGROUND_COLOR)
        .build());
    this.standFacingLabel = stand.add(LabelWidget.builder(this.font, getCurrentFacingText(this.getArmorStand()))
        .bgColor(BACKGROUND_COLOR)
        .build());
    this.standRotationLabel = stand.add(LabelWidget.builder(
        this.font,
        getCurrentRotationText(this.getArmorStand())
    ).bgColor(BACKGROUND_COLOR).build());
    labels.add(stand);

    this.layout.topLeft.add(
        new HorizontalLineWidget(this.utilRow.getWidth() - 2 * GuiUtil.PADDING).margin(
            3 * GuiUtil.PADDING),
        (configurator) -> configurator.margin(Spacing.of(0, 0, 0, GuiUtil.PADDING))
    );
    this.layout.topLeft.add(labels);
  }

  private void initBottomLeft() {
    LinearLayoutWidget snaps = LinearLayoutWidget.vertical()
        .spacing(GuiUtil.PADDING / 2)
        .defaultOffAxisContentAlignStart();

    snaps.add(LabelWidget.builder(this.font, Component.translatable("armorstands.rotate.snap"))
        .bgColor(BACKGROUND_COLOR)
        .build());

    LinearLayoutWidget firstRow = LinearLayoutWidget.horizontal().spacing(GuiUtil.PADDING / 2);
    firstRow.add(Button.builder(
        Component.translatable("armorstands.rotate.snap." + Direction.SOUTH.getName()),
        (button) -> ClientNetworking.sendSetYawPacket(Mth.wrapDegrees(Direction.SOUTH.toYRot()))
    ).size(DIRECTION_BUTTON_WIDTH, ELEMENT_HEIGHT).build());
    firstRow.add(Button.builder(
        Component.translatable("armorstands.rotate.snap." + Direction.NORTH.getName()),
        (button) -> ClientNetworking.sendSetYawPacket(Mth.wrapDegrees(Direction.NORTH.toYRot()))
    ).size(DIRECTION_BUTTON_WIDTH, ELEMENT_HEIGHT).build());
    snaps.add(firstRow);

    LinearLayoutWidget secondRow = LinearLayoutWidget.horizontal().spacing(GuiUtil.PADDING / 2);
    secondRow.add(Button.builder(
        Component.translatable("armorstands.rotate.snap." + Direction.EAST.getName()),
        (button) -> ClientNetworking.sendSetYawPacket(Mth.wrapDegrees(Direction.EAST.toYRot()))
    ).size(DIRECTION_BUTTON_WIDTH, ELEMENT_HEIGHT).build());
    secondRow.add(Button.builder(
        Component.translatable("armorstands.rotate.snap." + Direction.WEST.getName()),
        (button) -> ClientNetworking.sendSetYawPacket(Mth.wrapDegrees(Direction.WEST.toYRot()))
    ).size(DIRECTION_BUTTON_WIDTH, ELEMENT_HEIGHT).build());
    snaps.add(secondRow);

    this.layout.bottomLeft.add(snaps);

    LinearLayoutWidget faces = LinearLayoutWidget.vertical()
        .spacing(GuiUtil.PADDING / 2)
        .defaultOffAxisContentAlignStart();

    faces.add(LabelWidget.builder(this.font, Component.translatable("armorstands.rotate.face"))
        .bgColor(BACKGROUND_COLOR)
        .build());

    LinearLayoutWidget buttonRow = LinearLayoutWidget.horizontal().spacing(GuiUtil.PADDING / 2);
    buttonRow.add(Button.builder(
        Component.translatable("armorstands.rotate.face.toward"),
        (button) -> ClientNetworking.sendUtilityActionPacket(UtilityAction.FACE_TOWARD)
    ).size(BUTTON_WIDTH, ELEMENT_HEIGHT).build());
    buttonRow.add(Button.builder(
        Component.translatable("armorstands.rotate.face.away"),
        (button) -> ClientNetworking.sendUtilityActionPacket(UtilityAction.FACE_AWAY)
    ).size(BUTTON_WIDTH, ELEMENT_HEIGHT).build());
    buttonRow.add(Button.builder(
        Component.translatable("armorstands.rotate.face.with"),
        (button) -> ClientNetworking.sendUtilityActionPacket(UtilityAction.FACE_WITH)
    ).size(BUTTON_WIDTH, ELEMENT_HEIGHT).build());
    faces.add(buttonRow);

    this.layout.bottomLeft.add(faces, (configurator) -> configurator.margin(Spacing.of(GuiUtil.PADDING, 0, 0, 0)));
  }

  private void initBottomRight() {
    this.layout.bottomRight.spacing(2 * GuiUtil.PADDING);

    initRotateRow(RotateDirection.CLOCKWISE);
    initRotateRow(RotateDirection.COUNTERCLOCKWISE);

    this.layout.bottomRight.add(FillerWidget.ofHeight(2 * GuiUtil.PADDING));

    LinearLayoutWidget rotateSection = LinearLayoutWidget.vertical()
        .spacing(GuiUtil.PADDING / 2)
        .defaultOffAxisContentAlignEnd();
    LinearLayoutWidget firstRow = LinearLayoutWidget.horizontal()
        .defaultOffAxisContentAlignEnd()
        .spacing(GuiUtil.PADDING / 2);

    firstRow.add(
        LabelWidget.builder(this.font, Component.translatable("armorstands.rotate"))
            .bgColor(BACKGROUND_COLOR)
            .build(), (parent, self) -> self.setWidth(SLIDER_WIDTH - 3 * (ELEMENT_HEIGHT + parent.getSpacing()))
    );
    firstRow.add(IconButtonWidget.builder(BuiltinIcon.MINUS_13, Constants.MOD_ID)
        .dimensions(ELEMENT_HEIGHT)
        .onPress((button) -> this.rotateSlider.decrement())
        .tooltip(Tooltip.create(Component.translatable("armorstands.rotate.subtract")))
        .build());
    firstRow.add(IconButtonWidget.builder(BuiltinIcon.PLUS_13, Constants.MOD_ID)
        .dimensions(ELEMENT_HEIGHT)
        .onPress((button) -> this.rotateSlider.increment())
        .tooltip(Tooltip.create(Component.translatable("armorstands.rotate.add")))
        .build());
    firstRow.add(IconButtonWidget.builder(BuiltinIcon.ROTATE_13, Constants.MOD_ID)
        .dimensions(ELEMENT_HEIGHT)
        .onPress((button) -> this.rotateSlider.zero())
        .tooltip(Tooltip.create(Component.translatable("armorstands.rotate.zero")))
        .build());

    rotateSection.add(firstRow);
    this.rotateSlider = rotateSection.add(new RotateSliderWidget(
        this,
        SLIDER_WIDTH,
        ELEMENT_HEIGHT,
        this.getArmorStand()
    ));
    this.layout.bottomRight.add(rotateSection);
  }

  private void initRotateRow(RotateDirection direction) {
    LinearLayoutWidget block = LinearLayoutWidget.vertical()
        .spacing(GuiUtil.PADDING / 2)
        .defaultOffAxisContentAlignEnd();

    block.add(LabelWidget.builder(this.font, direction.getLabel()).bgColor(BACKGROUND_COLOR).build());

    LinearLayoutWidget row = LinearLayoutWidget.horizontal()
        .spacing(GuiUtil.PADDING / 2)
        .defaultOffAxisContentAlignCenter();

    String modifier = direction.getModifier();

    row.add(Button.builder(
            Component.literal(modifier + "1"),
            (button) -> ClientNetworking.sendAdjustYawPacket(direction.offset())
        )
        .size(MINI_BUTTON_WIDTH, ELEMENT_HEIGHT)
        .build());
    row.add(Button.builder(
            Component.literal(modifier + "5"),
            (button) -> ClientNetworking.sendAdjustYawPacket(direction.offset() * 5)
        )
        .size(MINI_BUTTON_WIDTH, ELEMENT_HEIGHT)
        .build());
    row.add(Button.builder(
            Component.literal(modifier + "15"),
            (button) -> ClientNetworking.sendAdjustYawPacket(direction.offset() * 15)
        )
        .size(MINI_BUTTON_WIDTH, ELEMENT_HEIGHT)
        .build());
    row.add(Button.builder(
            Component.literal(modifier + "45"),
            (button) -> ClientNetworking.sendAdjustYawPacket(direction.offset() * 45)
        )
        .size(MINI_BUTTON_WIDTH, ELEMENT_HEIGHT)
        .build());

    block.add(row);
    this.layout.bottomRight.add(block);
  }

  @Override
  public void containerTick() {
    super.containerTick();

    this.playerFacingLabel.setText(getCurrentFacingText(this.getPlayer()));
    this.playerRotationLabel.setText(getCurrentRotationText(this.getPlayer()));
    this.standFacingLabel.setText(getCurrentFacingText(this.getArmorStand()));
    this.standRotationLabel.setText(getCurrentRotationText(this.getArmorStand()));
    this.rotateSlider.tick();
  }

  @Override
  public void updateYawOnClient(float yaw) {
    if (this.rotateSlider != null && this.rotateSlider.isPending(this)) {
      return;
    }

    super.updateYawOnClient(yaw);

    if (this.rotateSlider != null) {
      this.rotateSlider.setAngle(yaw);
    }
  }

  @Override
  public void onPong() {
    super.onPong();

    if (this.rotateSlider != null) {
      this.rotateSlider.onPong();
    }
  }

  public enum RotateDirection {
    CLOCKWISE(1, "armorstands.rotate.clockwise"), COUNTERCLOCKWISE(-1, "armorstands.rotate.counter");

    private final int offset;
    private final Component label;

    RotateDirection(int offset, String i18n) {
      this.offset = offset;
      label = Component.translatable(i18n);
    }

    public int offset() {
      return offset;
    }

    public Component getLabel() {
      return label;
    }

    public String toString() {
      return label.getString();
    }

    public String getModifier() {
      return this.offset > 0 ? "+" : "-";
    }
  }
}
