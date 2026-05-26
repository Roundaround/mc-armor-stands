package me.roundaround.armorstands.client.gui.screen;

import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.client.gui.widget.AdjustPoseSliderWidget;
import me.roundaround.armorstands.client.gui.widget.ScaleSliderWidget;
import me.roundaround.armorstands.client.network.ClientNetworking;
import me.roundaround.armorstands.network.EulerAngleParameter;
import me.roundaround.armorstands.network.PosePart;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.trove.client.gui.icon.BuiltinIcon;
import me.roundaround.trove.client.gui.icon.CustomIcon;
import me.roundaround.trove.client.gui.layout.linear.LinearLayoutWidget;
import me.roundaround.trove.client.gui.util.GuiUtil;
import me.roundaround.trove.client.gui.util.Spacing;
import me.roundaround.trove.client.gui.widget.IconButtonWidget;
import me.roundaround.trove.client.gui.widget.drawable.FrameWidget;
import me.roundaround.trove.client.gui.widget.drawable.HorizontalLineWidget;
import me.roundaround.trove.client.gui.widget.drawable.LabelWidget;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import me.roundaround.armorstands.util.Pose;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorStandPoseScreen extends AbstractArmorStandScreen {
  private static final int SLIDER_WIDTH = 100;
  protected static final CustomIcon HEAD_ICON = new CustomIcon("head", 20);
  protected static final CustomIcon BODY_ICON = new CustomIcon("body", 20);
  protected static final CustomIcon RIGHT_ARM_ICON = new CustomIcon("rightarm", 20);
  protected static final CustomIcon LEFT_ARM_ICON = new CustomIcon("leftarm", 20);
  protected static final CustomIcon RIGHT_LEG_ICON = new CustomIcon("rightleg", 20);
  protected static final CustomIcon LEFT_LEG_ICON = new CustomIcon("leftleg", 20);

  private PosePart posePart = PosePart.HEAD;

  private ScaleSliderWidget scaleSlider;
  private Button activePosePartButton;
  private FrameWidget activePosePartFrame;
  private LabelWidget posePartLabelLeft;
  private LabelWidget posePartLabelRight;
  private AdjustPoseSliderWidget pitchSlider;
  private AdjustPoseSliderWidget yawSlider;
  private AdjustPoseSliderWidget rollSlider;

  public ArmorStandPoseScreen(ArmorStandScreenHandler handler) {
    super(handler, ScreenType.POSE.getDisplayName());
    this.supportsUndoRedo = true;
  }

  public void onArmorStandPoseChanged(ArmorStand armorStand, PosePart part) {
    if (this.pitchSlider == null || this.yawSlider == null || this.rollSlider == null) {
      return;
    }

    if (armorStand.getId() != this.getArmorStand().getId() || part != this.posePart) {
      return;
    }

    this.pitchSlider.refresh();
    this.yawSlider.refresh();
    this.rollSlider.refresh();
  }

  @Override
  public ScreenType getScreenType() {
    return ScreenType.POSE;
  }

  @Override
  protected void populateLayout() {
    super.populateLayout();

    this.initBottomLeft();
    this.initBottomRight();

    this.activePosePartFrame = this.layout.nonPositioned.add(
        new FrameWidget(),
        (parent, self) -> self.frame(this.activePosePartButton)
    );
  }

  private void initBottomLeft() {
    this.layout.bottomLeft.defaultOffAxisContentAlignCenter();

    LinearLayoutWidget partPicker = LinearLayoutWidget.vertical()
        .spacing(GuiUtil.PADDING)
        .defaultOffAxisContentAlignCenter();

    this.posePartLabelLeft = partPicker.add(LabelWidget.builder(
            this.font,
            Component.translatable("armorstands.pose.editing", this.posePart.getDisplayName())
        )
        .width(SLIDER_WIDTH)
        .overflowBehavior(LabelWidget.OverflowBehavior.SCROLL)
        .alignTextCenterX()
        .bgColor(BACKGROUND_COLOR)
        .build());

    this.activePosePartButton = partPicker.add(IconButtonWidget.builder(HEAD_ICON, Constants.MOD_ID)
        .vanillaSize()
        .disableIconDim()
        .messageAndTooltip(PosePart.HEAD.getDisplayName())
        .onPress((button) -> this.setActivePosePart(button, PosePart.HEAD))
        .build());
    this.activePosePartButton.active = false;

    LinearLayoutWidget torsoRow = LinearLayoutWidget.horizontal().spacing(GuiUtil.PADDING);
    torsoRow.add(IconButtonWidget.builder(RIGHT_ARM_ICON, Constants.MOD_ID)
        .vanillaSize()
        .disableIconDim()
        .messageAndTooltip(PosePart.RIGHT_ARM.getDisplayName())
        .onPress((button) -> this.setActivePosePart(button, PosePart.RIGHT_ARM))
        .build());
    torsoRow.add(IconButtonWidget.builder(BODY_ICON, Constants.MOD_ID)
        .vanillaSize()
        .disableIconDim()
        .messageAndTooltip(PosePart.BODY.getDisplayName())
        .onPress((button) -> this.setActivePosePart(button, PosePart.BODY))
        .build());
    torsoRow.add(IconButtonWidget.builder(LEFT_ARM_ICON, Constants.MOD_ID)
        .vanillaSize()
        .disableIconDim()
        .messageAndTooltip(PosePart.LEFT_ARM.getDisplayName())
        .onPress((button) -> this.setActivePosePart(button, PosePart.LEFT_ARM))
        .build());
    partPicker.add(torsoRow);

    LinearLayoutWidget feetRow = LinearLayoutWidget.horizontal().spacing(GuiUtil.PADDING);
    feetRow.add(IconButtonWidget.builder(RIGHT_LEG_ICON, Constants.MOD_ID)
        .vanillaSize()
        .disableIconDim()
        .messageAndTooltip(PosePart.RIGHT_LEG.getDisplayName())
        .onPress((button) -> this.setActivePosePart(button, PosePart.RIGHT_LEG))
        .build());
    feetRow.add(IconButtonWidget.builder(LEFT_LEG_ICON, Constants.MOD_ID)
        .vanillaSize()
        .disableIconDim()
        .messageAndTooltip(PosePart.LEFT_LEG.getDisplayName())
        .onPress((button) -> this.setActivePosePart(button, PosePart.LEFT_LEG))
        .build());
    partPicker.add(feetRow);

    this.layout.bottomLeft.add(partPicker);

    this.layout.bottomLeft.add(new HorizontalLineWidget(SLIDER_WIDTH - 2 * GuiUtil.PADDING).margin(
        2 * GuiUtil.PADDING));

    this.layout.bottomLeft.add(Button.builder(Component.translatable("armorstands.pose.mirror"), this::handleMirrorPose)
        .size(SLIDER_WIDTH, ELEMENT_HEIGHT)
        .build());

    this.layout.bottomLeft.add(new HorizontalLineWidget(SLIDER_WIDTH - 2 * GuiUtil.PADDING).margin(
        2 * GuiUtil.PADDING));

    LinearLayoutWidget scaleSection = LinearLayoutWidget.vertical().spacing(GuiUtil.PADDING / 2);
    LinearLayoutWidget firstRow = LinearLayoutWidget.horizontal()
        .spacing(GuiUtil.PADDING / 2)
        .defaultOffAxisContentAlignEnd();

    firstRow.add(
        LabelWidget.builder(this.font, Component.translatable("armorstands.scale"))
            .bgColor(BACKGROUND_COLOR)
            .build(),
        (parent, self) -> self.setWidth(SLIDER_WIDTH - 3 * (ELEMENT_HEIGHT + parent.getSpacing()))
    );
    firstRow.add(IconButtonWidget.builder(BuiltinIcon.MINUS_13, Constants.MOD_ID)
        .dimensions(ELEMENT_HEIGHT)
        .onPress((button) -> this.scaleSlider.decrement())
        .tooltip(Tooltip.create(Component.translatable("armorstands.scale.subtract")))
        .build());
    firstRow.add(IconButtonWidget.builder(BuiltinIcon.PLUS_13, Constants.MOD_ID)
        .dimensions(ELEMENT_HEIGHT)
        .onPress((button) -> this.scaleSlider.increment())
        .tooltip(Tooltip.create(Component.translatable("armorstands.scale.add")))
        .build());
    firstRow.add(IconButtonWidget.builder(BuiltinIcon.ROTATE_13, Constants.MOD_ID)
        .dimensions(ELEMENT_HEIGHT)
        .onPress((button) -> this.scaleSlider.setToOne())
        .tooltip(Tooltip.create(Component.translatable("armorstands.scale.zero")))
        .build());

    scaleSection.add(firstRow);
    this.scaleSlider = scaleSection.add(new ScaleSliderWidget(
        this,
        SLIDER_WIDTH,
        ELEMENT_HEIGHT,
        this.getArmorStand()
    ));
    this.layout.bottomLeft.add(scaleSection);
  }

  private void initBottomRight() {
    this.layout.bottomRight.spacing(3 * GuiUtil.PADDING);

    LinearLayoutWidget block = LinearLayoutWidget.vertical()
        .spacing(GuiUtil.PADDING / 2)
        .defaultOffAxisContentAlignEnd();

    this.posePartLabelRight = block.add(LabelWidget.builder(
            this.font,
            Component.translatable("armorstands.pose.editing", this.posePart.getDisplayName())
        )
        .width(SLIDER_WIDTH)
        .overflowBehavior(LabelWidget.OverflowBehavior.SCROLL)
        .alignTextRight()
        .bgColor(BACKGROUND_COLOR)
        .build());

    block.add(
        CycleButton.builder(SliderRange::getDisplayName, SliderRange.FULL)
            .withValues(SliderRange.values())
            .displayOnlyValue()
            .create(
                Component.translatable("armorstands.pose.range"), (button, value) -> {
                  this.pitchSlider.setRange(value.getMin(), value.getMax());
                  this.yawSlider.setRange(value.getMin(), value.getMax());
                  this.rollSlider.setRange(value.getMin(), value.getMax());
                }
            ), (adder) -> {
          adder.layoutHook((parent, self) -> self.setSize(SLIDER_WIDTH, ELEMENT_HEIGHT));
          adder.margin(Spacing.of(0, 0, 2 * GuiUtil.PADDING, 0));
        }
    );

    this.layout.bottomRight.add(block);

    this.pitchSlider = this.addAdjustSlider(EulerAngleParameter.PITCH);
    this.yawSlider = this.addAdjustSlider(EulerAngleParameter.YAW);
    this.rollSlider = this.addAdjustSlider(EulerAngleParameter.ROLL);
  }

  private AdjustPoseSliderWidget addAdjustSlider(EulerAngleParameter parameter) {
    LinearLayoutWidget block = LinearLayoutWidget.vertical().spacing(GuiUtil.PADDING / 2);

    AdjustPoseSliderWidget slider = new AdjustPoseSliderWidget(
        SLIDER_WIDTH,
        ELEMENT_HEIGHT,
        this.posePart,
        parameter,
        this.getArmorStand()
    );

    LinearLayoutWidget firstRow = LinearLayoutWidget.horizontal()
        .defaultOffAxisContentAlignEnd()
        .spacing(GuiUtil.PADDING);
    firstRow.add(
        LabelWidget.builder(this.font, parameter.getDisplayName()).bgColor(BACKGROUND_COLOR).build(),
        (parent, self) -> self.setWidth(SLIDER_WIDTH - 3 * (ELEMENT_HEIGHT + parent.getSpacing()))
    );
    firstRow.add(IconButtonWidget.builder(BuiltinIcon.MINUS_13, Constants.MOD_ID)
        .dimensions(ELEMENT_HEIGHT)
        .onPress((button) -> slider.decrement())
        .tooltip(Tooltip.create(Component.translatable("armorstands.pose.subtract")))
        .build());
    firstRow.add(IconButtonWidget.builder(BuiltinIcon.PLUS_13, Constants.MOD_ID)
        .dimensions(ELEMENT_HEIGHT)
        .onPress((button) -> slider.increment())
        .tooltip(Tooltip.create(Component.translatable("armorstands.pose.add")))
        .build());
    firstRow.add(IconButtonWidget.builder(BuiltinIcon.ROTATE_13, Constants.MOD_ID)
        .dimensions(ELEMENT_HEIGHT)
        .onPress((button) -> slider.zero())
        .tooltip(Tooltip.create(Component.translatable("armorstands.pose.zero")))
        .build());

    block.add(firstRow);
    block.add(slider);
    this.layout.bottomRight.add(block);

    return slider;
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    if (this.pitchSlider != null && this.pitchSlider.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
      return true;
    }
    if (this.yawSlider != null && this.yawSlider.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
      return true;
    }
    if (this.rollSlider != null && this.rollSlider.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
      return true;
    }
    return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
  }

  @Override
  protected void containerTick() {
    super.containerTick();

    this.scaleSlider.tick();
    this.pitchSlider.tick();
    this.yawSlider.tick();
    this.rollSlider.tick();
  }

  @Override
  public void onPong() {
    super.onPong();

    if (this.scaleSlider != null) {
      this.scaleSlider.onPong();
    }
  }

  private void setActivePosePart(Button button, PosePart part) {
    this.posePart = part;
    this.pitchSlider.setPart(part);
    this.yawSlider.setPart(part);
    this.rollSlider.setPart(part);
    this.posePartLabelLeft.setText(Component.translatable("armorstands.pose.editing", this.posePart.getDisplayName()));
    this.posePartLabelRight.setText(Component.translatable("armorstands.pose.editing", this.posePart.getDisplayName()));

    if (this.activePosePartButton != null) {
      this.activePosePartButton.active = true;
    }
    this.activePosePartButton = button;
    this.activePosePartButton.active = false;

    this.activePosePartFrame.frame(this.activePosePartButton);

    this.layout.arrangeElements();
  }

  private void handleMirrorPose(Button button) {
    ClientNetworking.sendSetPosePacket(new Pose(this.getArmorStand()).mirror());

    this.pitchSlider.refresh();
    this.yawSlider.refresh();
    this.rollSlider.refresh();
  }

  private enum SliderRange {
    FULL(-180, 180), HALF(-90, 90), TIGHT(-35, 35);

    private final int min;
    private final int max;

    SliderRange(int min, int max) {
      this.min = min;
      this.max = max;
    }

    public int getMin() {
      return this.min;
    }

    public int getMax() {
      return this.max;
    }

    public Component getDisplayName() {
      return Component.translatable("armorstands.pose.range." + this.name().toLowerCase());
    }
  }
}
