package me.roundaround.armorstands.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import me.roundaround.armorstands.client.ArmorStandsKeyMappings;
import me.roundaround.armorstands.client.gui.MessageRenderer;
import me.roundaround.armorstands.client.network.ClientNetworking;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.mixin.ArmorStandAccessor;
import me.roundaround.armorstands.mixin.GuiAccessor;
import me.roundaround.armorstands.mixin.MouseHandlerAccessor;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.armorstands.network.UtilityAction;
import me.roundaround.armorstands.screen.ArmorStandScreenHandler;
import me.roundaround.trove.client.gui.icon.BuiltinIcon;
import me.roundaround.trove.client.gui.icon.CustomIcon;
import me.roundaround.trove.client.gui.layout.linear.LinearLayoutWidget;
import me.roundaround.trove.client.gui.util.GuiUtil;
import me.roundaround.trove.client.gui.widget.IconButtonWidget;
import me.roundaround.trove.client.gui.widget.drawable.FrameWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractArmorStandScreen extends AbstractContainerScreen<ArmorStandScreenHandler> implements
    PassesEventsThrough {
  protected static final CustomIcon COPY_ICON = new CustomIcon("copy", 20);
  protected static final CustomIcon PASTE_ICON = new CustomIcon("paste", 20);
  protected static final int BACKGROUND_COLOR = GuiUtil.genColorInt(0f, 0f, 0f, 0.7f);
  protected static final int ELEMENT_HEIGHT = 16;

  protected final ArmorStandLayoutWidget layout = new ArmorStandLayoutWidget(this);
  protected final ArmorStand armorStand;
  protected final MessageRenderer messageRenderer;

  protected LinearLayoutWidget utilRow;
  protected LinearLayoutWidget navRow;
  protected IconButtonWidget activePageButton;
  protected boolean supportsUndoRedo = false;
  protected boolean utilizesInventory = false;
  protected boolean passEvents = true;
  protected long currentSyncDelay = 0;

  private boolean cursorLocked = false;
  private long lastPing = 0;

  protected AbstractArmorStandScreen(ArmorStandScreenHandler handler, Component title) {
    super(handler, handler.getPlayerInventory(), title);
    this.armorStand = handler.getArmorStand();

    this.messageRenderer = new MessageRenderer(this);
  }

  public abstract ScreenType getScreenType();

  public ArmorStand getArmorStand() {
    return this.armorStand;
  }

  public MessageRenderer getMessageRenderer() {
    return this.messageRenderer;
  }

  @Override
  public boolean shouldPassEvents() {
    return this.passEvents;
  }

  @Override
  public void init() {
    this.populateLayout();
    this.collectElements();
    this.repositionElements();
  }

  protected Minecraft getClient() {
    return Objects.requireNonNull(this.minecraft);
  }

  protected LocalPlayer getPlayer() {
    return this.getClient().player;
  }

  protected void populateLayout() {
    this.initUtilityButtons();
    this.initNavigationButtons();
  }

  protected void initUtilityButtons() {
    if (!this.supportsUndoRedo) {
      return;
    }

    this.utilRow = LinearLayoutWidget.horizontal().spacing(GuiUtil.PADDING / 2);
    this.utilRow.add(IconButtonWidget.builder(BuiltinIcon.HELP_18, Constants.MOD_ID)
        .vanillaSize()
        .messageAndTooltip(this.buildHelpTooltipText())
        .build());
    this.utilRow.add(IconButtonWidget.builder(COPY_ICON, Constants.MOD_ID)
        .vanillaSize()
        .messageAndTooltip(Component.translatable("armorstands.utility.copy"))
        .onPress((_) -> ClientNetworking.sendUtilityActionPacket(UtilityAction.COPY))
        .build());
    this.utilRow.add(IconButtonWidget.builder(PASTE_ICON, Constants.MOD_ID)
        .vanillaSize()
        .messageAndTooltip(Component.translatable("armorstands.utility.paste"))
        .onPress((_) -> ClientNetworking.sendUtilityActionPacket(UtilityAction.PASTE))
        .build());
    this.utilRow.add(IconButtonWidget.builder(BuiltinIcon.UNDO_18, Constants.MOD_ID)
        .vanillaSize()
        .messageAndTooltip(Component.translatable("armorstands.utility.undo"))
        .onPress((_) -> ClientNetworking.sendUndoPacket(false))
        .build());
    this.utilRow.add(IconButtonWidget.builder(BuiltinIcon.REDO_18, Constants.MOD_ID)
        .vanillaSize()
        .messageAndTooltip(Component.translatable("armorstands.utility.redo"))
        .onPress((_) -> ClientNetworking.sendUndoPacket(true))
        .build());
    this.layout.topLeft.add(this.utilRow);
  }

  private Component buildHelpTooltipText() {
    String control = Component.translatable("armorstands.help." + (InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY ? "cmd" : "ctrl")).getString();

    ArrayList<Component> lines = new ArrayList<>();
    lines.add(Component.translatable("armorstands.help.main").append(CommonComponents.NEW_LINE));
    lines.add(Component.translatable("armorstands.help.shortcuts"));
    lines.add(Component.translatable("armorstands.help.look", Component.translatable("armorstands.help.alt")));
    lines.add(Component.translatable(
        "armorstands.help.close",
        this.getStyledBoundText(this.getClient().options.keyInventory),
        InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_ESCAPE).getDisplayName()
    ));
    lines.add(Component.translatable("armorstands.help.change", ScreenType.values().length));
    lines.add(Component.translatable(
        "armorstands.help.highlight",
        this.getStyledBoundText(ArmorStandsKeyMappings.highlightArmorStand)
    ));
    lines.add(Component.translatable(
        "armorstands.help.undo",
        control,
        InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_Z).getDisplayName()
    ));
    lines.add(Component.translatable(
        "armorstands.help.redo",
        control,
        InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_Y).getDisplayName()
    ));
    lines.add(Component.translatable(
        "armorstands.help.copy",
        control,
        InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_C).getDisplayName()
    ));
    lines.add(Component.translatable(
        "armorstands.help.paste",
        control,
        InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_V).getDisplayName()
    ));
    return CommonComponents.joinLines(lines);
  }

  private Component getStyledBoundText(KeyMapping binding) {
    Component base = binding.getTranslatedKeyMessage();
    if (binding.isUnbound()) {
      return Component.literal("(").append(base).append(")").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
    }
    return base;
  }

  protected void initNavigationButtons() {
    this.navRow = LinearLayoutWidget.horizontal().spacing(GuiUtil.PADDING / 2);

    for (ScreenType screenType : ScreenType.values()) {
      IconButtonWidget navButton = IconButtonWidget.builder(screenType.getIcon(), Constants.MOD_ID)
          .vanillaSize()
          .disableIconDim()
          .messageAndTooltip(screenType.getDisplayName())
          .onPress((_) -> ClientNetworking.sendRequestScreenPacket(this.getArmorStand(), screenType))
          .build();
      if (this.getScreenType() == screenType) {
        navButton.active = false;
        this.activePageButton = navButton;
      }
      this.navRow.add(navButton);
    }

    this.layout.topRight.add(this.navRow);

    if (this.activePageButton != null) {
      this.layout.nonPositioned.add(new FrameWidget(this.activePageButton));
    }
  }

  protected void collectElements() {
    this.layout.visitWidgets(this::addRenderableWidget);
  }

  @Override
  protected void repositionElements() {
    this.layout.arrangeElements();
  }

  @Override
  protected void containerTick() {
    ((GuiAccessor) this.minecraft.gui).invokeUpdateVignetteDarkness(this.minecraft.getCameraEntity());

    this.messageRenderer.tick();

    //    if (this.shouldPassEvents()) {
    //      // TODO: Is there a better way than updating all?
    //      KeyBinding.updatePressedStates();
    //    }
  }

  @Override
  public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    int adjustedMouseX = this.cursorLocked ? -1 : mouseX;
    int adjustedMouseY = this.cursorLocked ? -1 : mouseY;

    ((GuiAccessor) this.minecraft.gui).invokeExtractVignette(context, this.minecraft.getCameraEntity());

    super.extractRenderState(context, adjustedMouseX, adjustedMouseY, delta);

    this.messageRenderer.render(context);
  }

  @Override
  public void extractBlurredBackground(@NonNull GuiGraphicsExtractor context) {
    // No grayed out background
  }

  @Override
  public void extractBackground(@NonNull GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float delta) {
  }

  @Override
  protected void extractLabels(@NonNull GuiGraphicsExtractor drawContext, int mouseX, int mouseY) {
  }

  @Override
  public void mouseMoved(double mouseX, double mouseY) {
    if (this.cursorLocked) {
      return;
    }
    super.mouseMoved(mouseX, mouseY);
  }

  @Override
  public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
    if (this.cursorLocked) {
      return false;
    }
    GuiEventListener focused = this.getFocused();
    boolean result = super.mouseClicked(click, doubled);

    if (this.utilizesInventory) {
      this.setFocused(focused);
    }

    return result;
  }

  @Override
  public boolean mouseDragged(@NonNull MouseButtonEvent click, double deltaX, double deltaY) {
    if (this.cursorLocked) {
      return false;
    }
    if (this.utilizesInventory) {
      return super.mouseDragged(click, deltaX, deltaY);
    }

    if (this.getFocused() != null && this.isDragging() && click.button() == 0) {
      return this.getFocused().mouseDragged(click, deltaX, deltaY);
    }
    return false;
  }

  @Override
  public boolean mouseReleased(@NonNull MouseButtonEvent click) {
    if (this.cursorLocked) {
      return false;
    }
    if (this.utilizesInventory) {
      return super.mouseReleased(click);
    }

    if (this.isDragging() && this.getFocused() != null && click.button() == 0) {
      this.setDragging(false);
      return this.getFocused().mouseReleased(click);
    }

    this.setDragging(false);
    return this.getChildAt(click.x(), click.y()).filter((element) -> element.mouseReleased(click)).isPresent();
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    if (this.cursorLocked) {
      return false;
    }

    if (this.navRow != null && this.navRow.getBounds().contains(mouseX, mouseY)) {
      if (verticalAmount > 0) {
        this.goToPreviousScreen();
      } else {
        this.goToNextScreen();
      }
      return true;
    }

    return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
  }

  @Override
  public @NonNull Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
    if (this.cursorLocked) {
      return Optional.empty();
    }
    return super.getChildAt(mouseX, mouseY);
  }

  @Override
  public boolean keyPressed(@NonNull KeyEvent input) {
    // Allow jump to pass through without pressing buttons
    if (this.minecraft.options.keyJump.matches(input)) {
      return false;
    }

    if (this.minecraft.options.keyInventory.matches(input)) {
      this.onClose();
      return true;
    }

    switch (input.input()) {
      case GLFW.GLFW_KEY_ESCAPE:
        this.onClose();
        return true;
      case GLFW.GLFW_KEY_LEFT_ALT:
      case GLFW.GLFW_KEY_RIGHT_ALT:
        if (!this.passEvents) {
          break;
        }
        this.lockCursor();
        return true;
      case GLFW.GLFW_KEY_LEFT:
        if (!input.hasControlDown()) {
          break;
        }
        GuiUtil.playClickSound();
        this.goToPreviousScreen();
        return true;
      case GLFW.GLFW_KEY_RIGHT:
        if (!input.hasControlDown()) {
          break;
        }
        GuiUtil.playClickSound();
        this.goToNextScreen();
        return true;
      case GLFW.GLFW_KEY_Z:
        if (!this.supportsUndoRedo || !input.hasControlDown()) {
          break;
        }
        GuiUtil.playClickSound();
        ClientNetworking.sendUndoPacket(false);
        return true;
      case GLFW.GLFW_KEY_Y:
        if (!this.supportsUndoRedo || !input.hasControlDown()) {
          break;
        }
        GuiUtil.playClickSound();
        ClientNetworking.sendUndoPacket(true);
        return true;
      case GLFW.GLFW_KEY_C:
        if (!this.supportsUndoRedo || !input.hasControlDown()) {
          break;
        }
        GuiUtil.playClickSound();
        ClientNetworking.sendUtilityActionPacket(UtilityAction.COPY);
        return true;
      case GLFW.GLFW_KEY_V:
        if (!this.supportsUndoRedo || !input.hasControlDown()) {
          break;
        }
        GuiUtil.playClickSound();
        ClientNetworking.sendUtilityActionPacket(UtilityAction.PASTE);
        return true;
    }

    for (ScreenType screenType : ScreenType.values()) {
      if (screenType == this.getScreenType()) {
        continue;
      }
      if (this.minecraft.options.keyHotbarSlots[screenType.getIndex()].matches(input)) {
        GuiUtil.playClickSound();
        ClientNetworking.sendRequestScreenPacket(this.armorStand, screenType);
        return true;
      }
    }

    if (this.getFocused() != null && this.getFocused().keyPressed(input)) {
      return true;
    }

    return super.keyPressed(input);
  }

  @Override
  public boolean keyReleased(@NonNull KeyEvent input) {
    if (this.passEvents &&
        (input.input() == GLFW.GLFW_KEY_LEFT_ALT || input.input() == GLFW.GLFW_KEY_RIGHT_ALT)) {
      this.unlockCursor();
      return true;
    }

    return this.getFocused() != null && this.getFocused().keyReleased(input);
  }

  public boolean shouldHighlight(Entity entity) {
    return ArmorStandsKeyMappings.highlightArmorStand.isDown() && entity == this.armorStand;
  }

  public boolean isCursorLocked() {
    return this.cursorLocked;
  }

  public void sendPing() {
    this.lastPing = System.currentTimeMillis();
    ClientNetworking.sendPingPacket(Objects.requireNonNull(this.minecraft).player);
  }

  public void onPong() {
    this.currentSyncDelay = System.currentTimeMillis() - this.lastPing;
  }

  public void updatePosOnClient(double x, double y, double z) {
    this.armorStand.setPosRaw(x, y, z);
  }

  public void updateYawOnClient(float yaw) {
    this.armorStand.setYRot(yaw);
  }

  public void updatePitchOnClient(float pitch) {
    this.armorStand.setXRot(pitch);
  }

  public void updateInvulnerableOnClient(boolean invulnerable) {
    this.armorStand.setInvulnerable(invulnerable);
  }

  public void updateDisabledSlotsOnClient(int disabledSlots) {
    ((ArmorStandAccessor) this.armorStand).setDisabledSlots(disabledSlots);
  }

  protected void lockCursor() {
    this.cursorLocked = true;
    int x = this.minecraft.getWindow().getScreenWidth() / 2;
    int y = this.minecraft.getWindow().getScreenHeight() / 2;
    ((MouseHandlerAccessor) this.minecraft.mouseHandler).setXpos(x);
    ((MouseHandlerAccessor) this.minecraft.mouseHandler).setYpos(y);
    InputConstants.grabOrReleaseMouse(this.minecraft.getWindow(), InputConstants.CURSOR_DISABLED, x, y);
  }

  protected void unlockCursor() {
    this.cursorLocked = false;
    int x = this.minecraft.getWindow().getScreenWidth() / 2;
    int y = this.minecraft.getWindow().getScreenHeight() / 2;
    ((MouseHandlerAccessor) this.minecraft.mouseHandler).setXpos(x);
    ((MouseHandlerAccessor) this.minecraft.mouseHandler).setYpos(y);
    InputConstants.grabOrReleaseMouse(this.minecraft.getWindow(), InputConstants.CURSOR_NORMAL, x, y);
  }

  protected void goToPreviousScreen() {
    ClientNetworking.sendRequestScreenPacket(this.armorStand, this.getScreenType().previous());
  }

  protected void goToNextScreen() {
    ClientNetworking.sendRequestScreenPacket(this.armorStand, this.getScreenType().next());
  }

  protected static Component getCurrentPosText(Entity entity) {
    String xStr = String.format("%.2f", entity.getX());
    String yStr = String.format("%.2f", entity.getY());
    String zStr = String.format("%.2f", entity.getZ());
    return Component.translatable("armorstands.current.position", xStr, yStr, zStr);
  }

  protected static Component getCurrentBlockPosText(Entity entity) {
    BlockPos pos = entity.blockPosition();
    return Component.translatable("armorstands.current.block", pos.getX(), pos.getY(), pos.getZ());
  }

  protected static Component getCurrentRotationText(Entity entity) {
    float currentRotation = entity.getYRot();
    return Component.translatable(
        "armorstands.current.rotation",
        String.format(Locale.ROOT, "%.1f", Mth.wrapDegrees(currentRotation))
    );
  }

  protected static Component getCurrentFacingText(Entity entity) {
    return getFacingText(Direction.fromYRot(entity.getYRot()));
  }

  protected static Component getFacingText(Direction facing) {
    String towardsI18n = switch (facing) {
      case NORTH -> "negZ";
      case SOUTH -> "posZ";
      case WEST -> "negX";
      default -> "posX";
    };
    Component towards = Component.translatable("armorstands.current.facing." + towardsI18n);
    return Component.translatable("armorstands.current.facing", facing.toString(), towards.getString());
  }
}
