package me.roundaround.armorstands.client.gui.screen;

import me.roundaround.armorstands.client.network.ClientNetworking;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.armorstands.network.UtilityAction;
import me.roundaround.armorstands.util.ArmorStandHelper;
import me.roundaround.trove.client.gui.layout.FillerWidget;
import me.roundaround.trove.client.gui.layout.linear.LinearLayoutWidget;
import me.roundaround.trove.client.gui.util.GuiUtil;
import me.roundaround.trove.client.gui.util.Spacing;
import me.roundaround.trove.client.gui.widget.drawable.HorizontalLineWidget;
import me.roundaround.trove.client.gui.widget.drawable.LabelWidget;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import me.roundaround.armorstands.util.MoveMode;
import me.roundaround.armorstands.util.MoveUnits;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArmorStandMoveScreen extends AbstractArmorStandScreen {
  private static final int MINI_BUTTON_WIDTH = 28;
  private static final int BUTTON_WIDTH = 46;
  private static final int BUTTON_HEIGHT = 16;
  private static final int LARGE_BUTTON_WIDTH = 118;
  private static final List<Direction> DIRECTIONS = List.of(
      Direction.UP,
      Direction.DOWN,
      Direction.SOUTH,
      Direction.NORTH,
      Direction.EAST,
      Direction.WEST
  );

  private final HashMap<Direction, LabelWidget> directionLabels = new HashMap<>();
  private final ArrayList<MoveButtonRef> moveButtons = new ArrayList<>();

  private LabelWidget playerPosLabel;
  private LabelWidget playerBlockLabel;
  private LabelWidget standPosLabel;
  private LabelWidget standBlockLabel;
  private Direction prevFacing;
  private LabelWidget facingLabel;
  private CycleButton<MoveUnits> unitsButton;
  private MoveMode mode = MoveMode.RELATIVE;
  private MoveUnits units = MoveUnits.PIXELS;

  public ArmorStandMoveScreen(ArmorStandScreenHandler handler) {
    super(handler, ScreenType.MOVE.getDisplayName());
    this.supportsUndoRedo = true;
  }

  @Override
  public ScreenType getScreenType() {
    return ScreenType.MOVE;
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
    this.playerPosLabel = player.add(LabelWidget.builder(this.font, getCurrentPosText(this.getPlayer()))
        .bgColor(BACKGROUND_COLOR)
        .build());
    this.playerBlockLabel = player.add(LabelWidget.builder(this.font, getCurrentBlockPosText(this.getPlayer()))
        .bgColor(BACKGROUND_COLOR)
        .build());
    labels.add(player);

    LinearLayoutWidget stand = LinearLayoutWidget.vertical().spacing(1).defaultOffAxisContentAlignStart();
    stand.add(LabelWidget.builder(this.font, Component.translatable("armorstands.current.stand"))
        .bgColor(BACKGROUND_COLOR)
        .build());
    this.standPosLabel = stand.add(LabelWidget.builder(this.font, getCurrentPosText(this.getArmorStand()))
        .bgColor(BACKGROUND_COLOR)
        .build());
    this.standBlockLabel = stand.add(LabelWidget.builder(this.font, getCurrentBlockPosText(this.getArmorStand()))
        .bgColor(BACKGROUND_COLOR)
        .build());
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

    snaps.add(LabelWidget.builder(this.font, Component.translatable("armorstands.move.snap"))
        .bgColor(BACKGROUND_COLOR)
        .build());

    LinearLayoutWidget firstRow = LinearLayoutWidget.horizontal().spacing(GuiUtil.PADDING / 2);
    firstRow.add(Button.builder(
        Component.translatable("armorstands.move.snap.standing"),
        (button) -> ClientNetworking.sendUtilityActionPacket(UtilityAction.SNAP_STANDING)
    ).size(BUTTON_WIDTH, BUTTON_HEIGHT).build());
    firstRow.add(Button.builder(
        Component.translatable("armorstands.move.snap.sitting"),
        (button) -> ClientNetworking.sendUtilityActionPacket(UtilityAction.SNAP_SITTING)
    ).size(BUTTON_WIDTH, BUTTON_HEIGHT).build());
    snaps.add(firstRow);

    LinearLayoutWidget secondRow = LinearLayoutWidget.horizontal().spacing(GuiUtil.PADDING / 2);
    secondRow.add(Button.builder(
        Component.translatable("armorstands.move.snap.corner"),
        (button) -> ClientNetworking.sendUtilityActionPacket(UtilityAction.SNAP_CORNER)
    ).size(BUTTON_WIDTH, BUTTON_HEIGHT).build());
    secondRow.add(Button.builder(
        Component.translatable("armorstands.move.snap.center"),
        (button) -> ClientNetworking.sendUtilityActionPacket(UtilityAction.SNAP_CENTER)
    ).size(BUTTON_WIDTH, BUTTON_HEIGHT).build());
    secondRow.add(Button.builder(
        Component.translatable("armorstands.move.snap.player"),
        (button) -> ClientNetworking.sendUtilityActionPacket(UtilityAction.SNAP_PLAYER)
    ).size(BUTTON_WIDTH, BUTTON_HEIGHT).build());
    snaps.add(secondRow);

    this.layout.bottomLeft.add(snaps);
  }

  private void initBottomRight() {
    this.layout.bottomRight.defaultOffAxisContentAlignEnd();

    this.layout.bottomRight.add(
        CycleButton.builder(MoveMode::getOptionValueText, this.mode)
            .withValues(MoveMode.values())
            .create(MoveMode.getOptionLabelText(), this::onMoveModeChange),
        (parent, self) -> self.setSize(LARGE_BUTTON_WIDTH, BUTTON_HEIGHT)
    );

    this.unitsButton = this.layout.bottomRight.add(
        CycleButton.builder(MoveUnits::getOptionValueText, this.units)
            .withValues(MoveUnits.values())
            .create(MoveUnits.getOptionLabelText(), this::onMoveUnitsChange),
        (parent, self) -> self.setSize(LARGE_BUTTON_WIDTH, BUTTON_HEIGHT)
    );

    this.layout.bottomRight.add(FillerWidget.ofHeight(2 * GuiUtil.PADDING));

    this.facingLabel = this.layout.bottomRight.add(LabelWidget.builder(
        this.font,
        getFacingText(this.getCurrentFacing())
    ).bgColor(BACKGROUND_COLOR).build());

    DIRECTIONS.forEach(this::initDirectionRow);
  }

  private void initDirectionRow(Direction direction) {
    LinearLayoutWidget row = LinearLayoutWidget.horizontal()
        .spacing(GuiUtil.PADDING / 2)
        .defaultOffAxisContentAlignCenter();

    LabelWidget label = LabelWidget.builder(this.font, this.mode.getDirectionText(direction))
        .bgColor(BACKGROUND_COLOR)
        .build();
    this.directionLabels.put(direction, label);
    row.add(label);

    row.add(this.createMoveButton(direction, 1));
    row.add(this.createMoveButton(direction, 2));
    row.add(this.createMoveButton(direction, 3));

    this.layout.bottomRight.add(row);
  }

  private Button createMoveButton(Direction direction, int amount) {
    MoveButtonRef ref = new MoveButtonRef(direction, amount, this.mode, this.units);
    this.moveButtons.add(ref);
    return ref.getButton();
  }

  @Override
  public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    super.extractRenderState(context, mouseX, mouseY, delta);

    Direction hoveredDirection = this.getHoveredMoveDirection();
    if (hoveredDirection != null) {
      Vec3 worldDir = this.getWorldDirection(hoveredDirection);
      float scale = this.armorStand.getScale() * this.armorStand.getAgeScale();
      Vec3 start = this.armorStand.position().add(0, 1.0 * scale, 0);
      Vec3 end = start.add(worldDir.scale(1.5));
      Gizmos.arrow(start, end, ARGB.color(255, 50, 200, 255));
    }
  }

  @Nullable
  private Direction getHoveredMoveDirection() {
    for (MoveButtonRef ref : this.moveButtons) {
      if (ref.getButton().isHovered()) {
        return ref.direction;
      }
    }
    return null;
  }

  private Vec3 getWorldDirection(Direction direction) {
    Vec3 localVec = new Vec3(direction.step());
    return switch (this.mode) {
      case RELATIVE -> localVec;
      case LOCAL_TO_STAND -> ArmorStandHelper.localToRelative(this.armorStand, localVec);
      case LOCAL_TO_PLAYER -> ArmorStandHelper.localToRelative(this.getPlayer(), localVec);
    };
  }

  @Override
  public void containerTick() {
    super.containerTick();

    this.playerPosLabel.setText(getCurrentPosText(this.getPlayer()));
    this.playerBlockLabel.setText(getCurrentBlockPosText(this.getPlayer()));
    this.standPosLabel.setText(getCurrentPosText(this.getArmorStand()));
    this.standBlockLabel.setText(getCurrentBlockPosText(this.getArmorStand()));

    Direction currentFacing = this.getCurrentFacing();
    if (!currentFacing.equals(this.prevFacing)) {
      this.facingLabel.setText(getFacingText(this.getCurrentFacing()));
      this.layout.arrangeElements();
    }
    this.prevFacing = currentFacing;
  }

  private void onMoveModeChange(CycleButton<MoveMode> button, MoveMode value) {
    this.mode = value;
    this.units = this.mode.getDefaultUnits();
    this.unitsButton.setValue(this.units);
    this.prevFacing = this.getCurrentFacing();
    this.facingLabel.setText(getFacingText(this.prevFacing));
    this.moveButtons.forEach((moveButton) -> {
      moveButton.setMode(this.mode);
      moveButton.setUnits(this.units);
    });

    this.directionLabels.forEach((direction, label) -> label.setText(this.mode.getDirectionText(direction)));
    this.layout.bottomRight.arrangeElements();
    this.layout.arrangeElements();
  }

  private void onMoveUnitsChange(CycleButton<MoveUnits> button, MoveUnits value) {
    this.units = value;
    this.moveButtons.forEach((moveButton) -> moveButton.setUnits(this.units));
  }

  private Direction getCurrentFacing() {
    Entity entity = this.mode.equals(MoveMode.LOCAL_TO_STAND) ? this.getArmorStand() : this.getPlayer();
    return Direction.fromYRot(entity.getYRot());
  }

  private static class MoveButtonRef {
    private final Button button;
    private final Direction direction;
    private final int amount;

    private MoveMode mode;
    private MoveUnits units;

    public MoveButtonRef(Direction direction, int amount, MoveMode mode, MoveUnits units) {
      this.direction = direction;
      this.amount = amount;
      this.mode = mode;
      this.units = units;

      this.button = Button.builder(this.getMessage(), this::onPress).size(MINI_BUTTON_WIDTH, BUTTON_HEIGHT).build();
    }

    public Button getButton() {
      return this.button;
    }

    public void setUnits(MoveUnits units) {
      this.units = units;
      this.button.setMessage(this.units.getButtonText(this.amount));
    }

    public void setMode(MoveMode mode) {
      this.mode = mode;
    }

    private Component getMessage() {
      return this.units.getButtonText(this.amount);
    }

    private void onPress(Button button) {
      ClientNetworking.sendAdjustPosPacket(this.direction, this.amount, this.mode, this.units);
    }
  }
}
