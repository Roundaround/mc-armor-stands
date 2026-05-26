package me.roundaround.armorstands.client.gui.screen;

import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.client.gui.widget.PresetPoseButtonWidget;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.trove.client.gui.icon.BuiltinIcon;
import me.roundaround.trove.client.gui.layout.linear.LinearLayoutWidget;
import me.roundaround.trove.client.gui.util.GuiUtil;
import me.roundaround.trove.client.gui.widget.IconButtonWidget;
import me.roundaround.trove.client.gui.widget.drawable.LabelWidget;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import me.roundaround.armorstands.util.PosePreset;
import me.roundaround.armorstands.util.PosePreset.Category;
import me.roundaround.armorstands.util.PosePreset.Source;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import java.util.ArrayList;
import java.util.List;

public class ArmorStandPresetsScreen extends AbstractArmorStandScreen {
  private static final int CONTROL_WIDTH = 120;
  private static final int CONTROL_HEIGHT = 16;
  private static final int BUTTONS_PER_PAGE = 6;

  private final ArrayList<PresetPoseButtonWidget> presetButtons = new ArrayList<>();

  private IconButtonWidget prevPageButton;
  private IconButtonWidget nextPageButton;
  private LabelWidget pageLabel;
  private int page = 0;
  private Source source = Source.ALL;
  private Category category = Category.ALL;
  private List<PosePreset> matchingPresets = new ArrayList<>();

  public ArmorStandPresetsScreen(ArmorStandScreenHandler handler) {
    super(handler, ScreenType.PRESETS.getDisplayName());
    this.supportsUndoRedo = true;
  }

  @Override
  public ScreenType getScreenType() {
    return ScreenType.PRESETS;
  }

  @Override
  protected void populateLayout() {
    super.populateLayout();

    this.initBottomLeft();
    this.initBottomRight();
  }

  @Override
  protected void collectElements() {
    this.updateFilters();
    super.collectElements();
  }

  private void initBottomLeft() {
    this.layout.bottomLeft.spacing(3 * GuiUtil.PADDING);

    LinearLayoutWidget first = LinearLayoutWidget.vertical()
        .spacing(GuiUtil.PADDING / 2)
        .defaultOffAxisContentAlignStart();
    first.add(LabelWidget.builder(this.font, Component.translatable("armorstands.presets.source.label"))
        .bgColor(BACKGROUND_COLOR)
        .build());
    first.add(CycleButton.builder(Source::getDisplayName, Source.ALL)
        .withValues(Source.getSources())
        .displayOnlyValue()
        .create(
            0,
            0,
            CONTROL_WIDTH,
            CONTROL_HEIGHT,
            Component.translatable("armorstands.presets.source.label"),
            (button, source) -> this.filter(source)
        ));
    this.layout.bottomLeft.add(first);

    LinearLayoutWidget second = LinearLayoutWidget.vertical()
        .spacing(GuiUtil.PADDING / 2)
        .defaultOffAxisContentAlignStart();
    second.add(LabelWidget.builder(this.font, Component.translatable("armorstands.presets.category.label"))
        .bgColor(BACKGROUND_COLOR)
        .build());
    second.add(CycleButton.builder(Category::getDisplayName, Category.ALL)
        .withValues(Category.getCategories())
        .displayOnlyValue()
        .create(
            0,
            0,
            CONTROL_WIDTH,
            CONTROL_HEIGHT,
            Component.translatable("armorstands.presets.category.label"),
            (button, category) -> this.filter(category)
        ));
    this.layout.bottomLeft.add(second);
  }

  private void initBottomRight() {
    for (int i = BUTTONS_PER_PAGE; i > 0; i--) {
      PresetPoseButtonWidget button = this.layout.bottomRight.add(new PresetPoseButtonWidget(
          CONTROL_WIDTH,
          CONTROL_HEIGHT
      ));
      this.presetButtons.add(button);
    }

    LinearLayoutWidget pagination = LinearLayoutWidget.horizontal()
        .spacing(GuiUtil.PADDING / 2)
        .defaultOffAxisContentAlignCenter();
    this.prevPageButton = pagination.add(IconButtonWidget.builder(BuiltinIcon.PREV_18, Constants.MOD_ID)
        .vanillaSize()
        .messageAndTooltip(Component.translatable("armorstands.presets.previous"))
        .onPress((button) -> this.previousPage())
        .build());

    int maxPage =
        Mth.ceil(PosePreset.getPresets(this.source, this.category).size() / (float) BUTTONS_PER_PAGE) - 1;
    this.pageLabel = pagination.add(
        LabelWidget.builder(
                this.font,
                Component.translatable("armorstands.presets.page", this.page + 1, maxPage + 1)
            )
            .alignTextCenterX()
            .bgColor(BACKGROUND_COLOR)
            .build(), (parent, self) -> self.setWidth(CONTROL_WIDTH - 2 * IconButtonWidget.SIZE_V - GuiUtil.PADDING)
    );

    this.nextPageButton = pagination.add(IconButtonWidget.builder(BuiltinIcon.NEXT_18, Constants.MOD_ID)
        .vanillaSize()
        .messageAndTooltip(Component.translatable("armorstands.presets.next"))
        .onPress((button) -> this.nextPage())
        .build());

    this.layout.bottomRight.add(pagination);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    if (this.layout.bottomRight.getBounds().contains(mouseX, mouseY)) {
      if (verticalAmount < 0) {
        this.nextPage();
      } else {
        this.previousPage();
      }
      return true;
    }
    return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
  }

  private void filter(Source source) {
    this.source = source;
    this.updateFilters();
  }

  private void filter(Category category) {
    this.category = category;
    this.updateFilters();
  }

  private void updateFilters() {
    this.matchingPresets = PosePreset.getPresets(this.source, this.category);
    this.setPage(0);
  }

  private void setPage(int page) {
    int maxPage = Math.max(0, Mth.ceil(this.matchingPresets.size() / (float) BUTTONS_PER_PAGE) - 1);

    this.page = page;
    List<PosePreset> presets = this.matchingPresets.subList(
        page * BUTTONS_PER_PAGE,
        Math.min((page + 1) * BUTTONS_PER_PAGE, this.matchingPresets.size())
    );

    for (int i = 0; i < BUTTONS_PER_PAGE; i++) {
      if (i < presets.size()) {
        this.presetButtons.get(i).setPose(presets.get(i));
        this.presetButtons.get(i).visible = true;
      } else {
        this.presetButtons.get(i).visible = false;
      }
    }

    GuiEventListener focused = this.getFocused();
    if (focused instanceof PresetPoseButtonWidget && !this.presetButtons.contains(focused)) {
      this.setFocused(this.presetButtons.getFirst());
    }

    this.prevPageButton.active = this.page > 0;
    this.nextPageButton.active = this.page < maxPage;
    this.pageLabel.setText(Component.translatable("armorstands.presets.page", this.page + 1, maxPage + 1));
  }

  private void nextPage() {
    int maxPage = Mth.ceil(this.matchingPresets.size() / (float) BUTTONS_PER_PAGE) - 1;
    if (this.page < maxPage) {
      this.setPage(this.page + 1);
    }
  }

  private void previousPage() {
    if (this.page > 0) {
      this.setPage(this.page - 1);
    }
  }
}
