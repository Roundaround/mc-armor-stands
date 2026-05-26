package me.roundaround.armorstands.client.gui.screen;

import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.client.gui.widget.RotateSliderWidget;
import me.roundaround.armorstands.client.network.ClientNetworking;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.armorstands.network.UtilityAction;
import me.roundaround.armorstands.util.ArmorStandHelper;
import me.roundaround.trove.client.gui.icon.BuiltinIcon;
import me.roundaround.trove.client.gui.layout.FillerWidget;
import me.roundaround.trove.client.gui.layout.linear.LinearLayoutWidget;
import me.roundaround.trove.client.gui.util.GuiUtil;
import me.roundaround.trove.client.gui.util.Spacing;
import me.roundaround.trove.client.gui.widget.IconButtonWidget;
import me.roundaround.trove.client.gui.widget.drawable.HorizontalLineWidget;
import me.roundaround.trove.client.gui.widget.drawable.LabelWidget;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ArmorStandRotateScreen extends AbstractArmorStandScreen {
  private static final int BUTTON_WIDTH = 46;
  private static final int DIRECTION_BUTTON_WIDTH = 70;
  private static final int MINI_BUTTON_WIDTH = 24;
  private static final int SLIDER_WIDTH = 4 * MINI_BUTTON_WIDTH + 3 * (GuiUtil.PADDING / 2);

  private final List<YawPreviewButton> yawPreviewButtons = new ArrayList<>();
  private final List<AbstractWidget> sliderWidgets = new ArrayList<>();

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
    firstRow.add(this.createSnapButton(Direction.SOUTH));
    firstRow.add(this.createSnapButton(Direction.NORTH));
    snaps.add(firstRow);

    LinearLayoutWidget secondRow = LinearLayoutWidget.horizontal().spacing(GuiUtil.PADDING / 2);
    secondRow.add(this.createSnapButton(Direction.EAST));
    secondRow.add(this.createSnapButton(Direction.WEST));
    snaps.add(secondRow);

    this.layout.bottomLeft.add(snaps);

    LinearLayoutWidget faces = LinearLayoutWidget.vertical()
        .spacing(GuiUtil.PADDING / 2)
        .defaultOffAxisContentAlignStart();

    faces.add(LabelWidget.builder(this.font, Component.translatable("armorstands.rotate.face"))
        .bgColor(BACKGROUND_COLOR)
        .build());

    LinearLayoutWidget buttonRow = LinearLayoutWidget.horizontal().spacing(GuiUtil.PADDING / 2);
    buttonRow.add(this.createFaceButton("armorstands.rotate.face.toward", UtilityAction.FACE_TOWARD,
        () -> ArmorStandHelper.getLookYaw(this.armorStand, this.getPlayer().position())));
    buttonRow.add(this.createFaceButton("armorstands.rotate.face.away", UtilityAction.FACE_AWAY,
        () -> ArmorStandHelper.getLookYaw(this.armorStand, this.getPlayer().position()) + 180f));
    buttonRow.add(this.createFaceButton("armorstands.rotate.face.with", UtilityAction.FACE_WITH,
        () -> this.getPlayer().getYRot()));
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
    this.sliderWidgets.add(firstRow.add(IconButtonWidget.builder(BuiltinIcon.MINUS_13, Constants.MOD_ID)
        .dimensions(ELEMENT_HEIGHT)
        .onPress((button) -> this.rotateSlider.decrement())
        .tooltip(Tooltip.create(Component.translatable("armorstands.rotate.subtract")))
        .build()));
    this.sliderWidgets.add(firstRow.add(IconButtonWidget.builder(BuiltinIcon.PLUS_13, Constants.MOD_ID)
        .dimensions(ELEMENT_HEIGHT)
        .onPress((button) -> this.rotateSlider.increment())
        .tooltip(Tooltip.create(Component.translatable("armorstands.rotate.add")))
        .build()));
    this.sliderWidgets.add(firstRow.add(IconButtonWidget.builder(BuiltinIcon.ROTATE_13, Constants.MOD_ID)
        .dimensions(ELEMENT_HEIGHT)
        .onPress((button) -> this.rotateSlider.zero())
        .tooltip(Tooltip.create(Component.translatable("armorstands.rotate.zero")))
        .build()));

    rotateSection.add(firstRow);
    this.rotateSlider = rotateSection.add(new RotateSliderWidget(
        this,
        SLIDER_WIDTH,
        ELEMENT_HEIGHT,
        this.getArmorStand()
    ));
    this.sliderWidgets.add(this.rotateSlider);
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

    for (int amount : new int[]{1, 5, 15, 45}) {
      int yawOffset = direction.offset() * amount;
      Button button = Button.builder(
              Component.literal(direction.getModifier() + amount),
              (b) -> ClientNetworking.sendAdjustYawPacket(yawOffset)
          )
          .size(MINI_BUTTON_WIDTH, ELEMENT_HEIGHT)
          .build();
      this.yawPreviewButtons.add(new YawPreviewButton(button,
          () -> Mth.wrapDegrees(this.armorStand.getYRot() + yawOffset)));
      row.add(button);
    }

    block.add(row);
    this.layout.bottomRight.add(block);
  }

  private static final int TARGET_COLOR = ARGB.color(255, 50, 200, 255);
  private static final int CURRENT_COLOR = ARGB.color(255, 150, 150, 150);
  private static final float ARROW_LENGTH = 1.5f;
  private static final float ARC_RADIUS = 1.0f;

  @Override
  public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    super.extractRenderState(context, mouseX, mouseY, delta);

    for (YawPreviewButton preview : this.yawPreviewButtons) {
      if (preview.button.isHovered()) {
        float scale = this.armorStand.getScale() * this.armorStand.getAgeScale();
        Vec3 center = this.armorStand.position().add(0, 1.0 * scale, 0);
        float currentYaw = this.armorStand.getYRot();
        float targetYaw = Mth.wrapDegrees(preview.targetYaw.get());

        Gizmos.arrow(center, center.add(yawToDir(currentYaw).scale(ARROW_LENGTH)), CURRENT_COLOR);
        Gizmos.arrow(center, center.add(yawToDir(targetYaw).scale(ARROW_LENGTH)), TARGET_COLOR);

        float angleDiff = Mth.wrapDegrees(targetYaw - currentYaw);
        if (Math.abs(angleDiff) > 0.5f) {
          int segments = Math.max(2, (int) (Math.abs(angleDiff) / 5));
          for (int i = 0; i < segments; i++) {
            float a1 = currentYaw + angleDiff * i / segments;
            float a2 = currentYaw + angleDiff * (i + 1) / segments;
            Vec3 p1 = center.add(yawToDir(a1).scale(ARC_RADIUS));
            Vec3 p2 = center.add(yawToDir(a2).scale(ARC_RADIUS));
            Gizmos.line(p1, p2, TARGET_COLOR);
          }
        }
        break;
      }
    }

    if (this.sliderWidgets.stream().anyMatch(AbstractWidget::isHovered)) {
      Vec3 center = this.armorStand.position().add(0, 1.0, 0);
      Vec3 right = new Vec3(1, 0, 0);
      Vec3 forward = new Vec3(0, 0, 1);
      ArmorStandPoseScreen.emitCircleGizmo(center, right, forward, ARC_RADIUS, TARGET_COLOR);
    }
  }

  private static Vec3 yawToDir(float yaw) {
    double rad = Math.toRadians(yaw);
    return new Vec3(-Math.sin(rad), 0, Math.cos(rad));
  }

  private Button createSnapButton(Direction direction) {
    float targetYaw = Mth.wrapDegrees(direction.toYRot());
    Button button = Button.builder(
        Component.translatable("armorstands.rotate.snap." + direction.getName()),
        (b) -> ClientNetworking.sendSetYawPacket(targetYaw)
    ).size(DIRECTION_BUTTON_WIDTH, ELEMENT_HEIGHT).build();
    this.yawPreviewButtons.add(new YawPreviewButton(button, () -> targetYaw));
    return button;
  }

  private Button createFaceButton(String i18nKey, UtilityAction action, Supplier<Float> targetYaw) {
    Button button = Button.builder(
        Component.translatable(i18nKey),
        (b) -> ClientNetworking.sendUtilityActionPacket(action)
    ).size(BUTTON_WIDTH, ELEMENT_HEIGHT).build();
    this.yawPreviewButtons.add(new YawPreviewButton(button, targetYaw));
    return button;
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

  private record YawPreviewButton(Button button, Supplier<Float> targetYaw) {
  }
}
