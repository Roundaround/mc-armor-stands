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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Rotations;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ArmorStandPoseScreen extends AbstractArmorStandScreen {
  private static final int SLIDER_WIDTH = 100;
  protected static final CustomIcon HEAD_ICON = new CustomIcon("head", 20);
  protected static final CustomIcon BODY_ICON = new CustomIcon("body", 20);
  protected static final CustomIcon RIGHT_ARM_ICON = new CustomIcon("rightarm", 20);
  protected static final CustomIcon LEFT_ARM_ICON = new CustomIcon("leftarm", 20);
  protected static final CustomIcon RIGHT_LEG_ICON = new CustomIcon("rightleg", 20);
  protected static final CustomIcon LEFT_LEG_ICON = new CustomIcon("leftleg", 20);

  private static final int GIZMO_COLOR = ARGB.color(255, 50, 200, 255);
  private static final int AXIS_COLOR = ARGB.color(255, 150, 150, 150);
  private static final float CIRCLE_RADIUS = 0.25f;
  private static final float AXIS_LENGTH = 0.4f;
  private static final int CIRCLE_SEGMENTS = 32;

  private final List<AxisWidgetGroup> axisWidgetGroups = new ArrayList<>();

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
    Button minusButton = firstRow.add(IconButtonWidget.builder(BuiltinIcon.MINUS_13, Constants.MOD_ID)
        .dimensions(ELEMENT_HEIGHT)
        .onPress((button) -> slider.decrement())
        .tooltip(Tooltip.create(Component.translatable("armorstands.pose.subtract")))
        .build());
    Button plusButton = firstRow.add(IconButtonWidget.builder(BuiltinIcon.PLUS_13, Constants.MOD_ID)
        .dimensions(ELEMENT_HEIGHT)
        .onPress((button) -> slider.increment())
        .tooltip(Tooltip.create(Component.translatable("armorstands.pose.add")))
        .build());
    Button resetButton = firstRow.add(IconButtonWidget.builder(BuiltinIcon.ROTATE_13, Constants.MOD_ID)
        .dimensions(ELEMENT_HEIGHT)
        .onPress((button) -> slider.zero())
        .tooltip(Tooltip.create(Component.translatable("armorstands.pose.zero")))
        .build());

    this.axisWidgetGroups.add(new AxisWidgetGroup(parameter, List.of(slider, minusButton, plusButton, resetButton)));

    block.add(firstRow);
    block.add(slider);
    this.layout.bottomRight.add(block);

    return slider;
  }

  private record AxisWidgetGroup(EulerAngleParameter parameter, List<AbstractWidget> widgets) {
    boolean isAnyHovered() {
      return this.widgets.stream().anyMatch(AbstractWidget::isHovered);
    }
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
  public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    super.extractRenderState(context, mouseX, mouseY, delta);

    EulerAngleParameter hoveredParam = this.getHoveredParameter();
    if (hoveredParam != null) {
      Vec3 center = this.getJointPosition(this.posePart);
      float scale = this.armorStand.getScale() * this.armorStand.getAgeScale();

      Rotations pose = this.posePart.get(this.armorStand);
      float poseX = (float) Math.toRadians(pose.x());
      float poseY = (float) Math.toRadians(pose.y());
      float poseZ = (float) Math.toRadians(pose.z());

      // Replicate the exact transform the renderer applies to body parts:
      // 1. Entity yaw: rotateY(180 - bodyRot)
      // 2. Part pose: rotationZYX(zRot, yRot, xRot)
      // Build a quaternion matching the renderer, then extract the axis
      // for the hovered parameter by comparing with/without a small delta.
      // The renderer applies rotateY(180 - yaw) which flips the visual
      // Y rotation direction. Negate poseY so the gizmo matches the
      // rendered orientation, not the logical model orientation.
      Quaternionf standQ = new Quaternionf().rotateY((float) Math.toRadians(180.0f - this.armorStand.getYRot()));
      Quaternionf basePose = new Quaternionf().rotationZYX(poseZ, -poseY, poseX);

      float nudge = 0.1f;
      Quaternionf tweaked;
      switch (hoveredParam) {
        case PITCH -> tweaked = new Quaternionf().rotationZYX(poseZ, -poseY, poseX + nudge);
        case YAW -> tweaked = new Quaternionf().rotationZYX(poseZ, -poseY + nudge, poseX);
        case ROLL -> tweaked = new Quaternionf().rotationZYX(poseZ + nudge, -poseY, poseX);
        default -> { return; }
      }

      // The incremental rotation in model space: tweaked * basePose^-1
      Quaternionf diff = new Quaternionf(tweaked).mul(new Quaternionf(basePose).invert());
      // Extract rotation axis from the quaternion (the normalized xyz components)
      Vector3f modelAxis = new Vector3f(diff.x, diff.y, diff.z).normalize();

      // Transform to world space
      Vec3 axis = toVec3(standQ.transform(new Vector3f(modelAxis)));

      // Build two perpendicular vectors for the circle
      Vector3f arbitrary = Math.abs(modelAxis.dot(new Vector3f(0, 1, 0))) < 0.9f
          ? new Vector3f(0, 1, 0) : new Vector3f(1, 0, 0);
      Vector3f uLocal = new Vector3f(modelAxis).cross(arbitrary).normalize();
      Vector3f vLocal = new Vector3f(modelAxis).cross(uLocal).normalize();
      Vec3 u = toVec3(standQ.transform(new Vector3f(uLocal)));
      Vec3 v = toVec3(standQ.transform(new Vector3f(vLocal)));

      emitCircleGizmo(center, u, v, CIRCLE_RADIUS * scale, GIZMO_COLOR);
      Gizmos.line(
          center.add(axis.scale(-AXIS_LENGTH * scale)),
          center.add(axis.scale(AXIS_LENGTH * scale)),
          AXIS_COLOR);
    }
  }

  private static Vec3 toVec3(Vector3f v) {
    return new Vec3(v.x, v.y, v.z);
  }

  private Vec3 getJointPosition(PosePart part) {
    Vec3 pos = this.armorStand.position();
    float yaw = this.armorStand.getYRot();
    double yawRad = Math.toRadians(yaw);
    // Combined visual scale: attribute scale (slider) × age scale (small toggle)
    float scale = this.armorStand.getScale() * this.armorStand.getAgeScale();

    // Model X axis in world space (positive = stand's left side)
    Vec3 modelX = new Vec3(Math.cos(yawRad), 0, Math.sin(yawRad));

    return switch (part) {
      case HEAD -> pos.add(0, 1.4375 * scale, 0);
      case BODY -> pos.add(0, 1.5 * scale, 0);
      case RIGHT_ARM -> pos.add(0, 1.375 * scale, 0).add(modelX.scale(-5.0 / 16.0 * scale));
      case LEFT_ARM -> pos.add(0, 1.375 * scale, 0).add(modelX.scale(5.0 / 16.0 * scale));
      case RIGHT_LEG -> pos.add(0, 0.75 * scale, 0).add(modelX.scale(-1.9 / 16.0 * scale));
      case LEFT_LEG -> pos.add(0, 0.75 * scale, 0).add(modelX.scale(1.9 / 16.0 * scale));
    };
  }

  @Nullable
  private EulerAngleParameter getHoveredParameter() {
    for (AxisWidgetGroup group : this.axisWidgetGroups) {
      if (group.isAnyHovered()) {
        return group.parameter;
      }
    }
    return null;
  }

  static void emitCircleGizmo(Vec3 center, Vec3 u, Vec3 v, float radius, int color) {
    for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
      double a1 = 2 * Math.PI * i / CIRCLE_SEGMENTS;
      double a2 = 2 * Math.PI * (i + 1) / CIRCLE_SEGMENTS;
      Vec3 p1 = center.add(u.scale(Math.cos(a1) * radius)).add(v.scale(Math.sin(a1) * radius));
      Vec3 p2 = center.add(u.scale(Math.cos(a2) * radius)).add(v.scale(Math.sin(a2) * radius));

      if (i == CIRCLE_SEGMENTS - 1) {
        Gizmos.arrow(p1, p2, color);
      } else {
        Gizmos.line(p1, p2, color);
      }
    }
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
