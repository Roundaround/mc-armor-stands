package me.roundaround.armorstands.client.gui.screen;

import me.roundaround.trove.client.gui.layout.NonPositioningLayoutWidget;
import me.roundaround.trove.client.gui.layout.SizableLayoutWidget;
import me.roundaround.trove.client.gui.layout.linear.LinearLayoutWidget;
import me.roundaround.trove.client.gui.util.GuiUtil;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class ArmorStandLayoutWidget extends SizableLayoutWidget {
  public final LinearLayoutWidget topLeft;
  public final LinearLayoutWidget bottomLeft;
  public final LinearLayoutWidget topRight;
  public final LinearLayoutWidget bottomRight;
  public final NonPositioningLayoutWidget nonPositioned;

  private final Screen screen;

  public ArmorStandLayoutWidget(Screen screen) {
    super(0, 0, screen.width, screen.height);

    this.screen = screen;

    this.topLeft = LinearLayoutWidget.vertical()
        .spacing(GuiUtil.PADDING / 2)
        .alignSelfTop()
        .alignSelfLeft()
        .defaultOffAxisContentAlignStart();
    this.bottomLeft = LinearLayoutWidget.vertical()
        .spacing(GuiUtil.PADDING / 2)
        .alignSelfBottom()
        .alignSelfLeft()
        .defaultOffAxisContentAlignStart();
    this.topRight = LinearLayoutWidget.vertical()
        .spacing(GuiUtil.PADDING / 2)
        .alignSelfTop()
        .alignSelfRight()
        .defaultOffAxisContentAlignEnd();
    this.bottomRight = LinearLayoutWidget.vertical()
        .spacing(GuiUtil.PADDING / 2)
        .alignSelfBottom()
        .alignSelfRight()
        .defaultOffAxisContentAlignEnd();
    this.nonPositioned = new NonPositioningLayoutWidget();
  }

  @Override
  public void visitChildren(@NonNull Consumer<LayoutElement> consumer) {
    this.topLeft.visitChildren(consumer);
    this.bottomLeft.visitChildren(consumer);
    this.topRight.visitChildren(consumer);
    this.bottomRight.visitChildren(consumer);
    this.nonPositioned.visitChildren(consumer);
  }

  @Override
  public void arrangeElements() {
    this.topLeft.setPosition(GuiUtil.PADDING, GuiUtil.PADDING);
    this.bottomLeft.setPosition(GuiUtil.PADDING, this.screen.height - GuiUtil.PADDING);
    this.topRight.setPosition(this.screen.width - GuiUtil.PADDING, GuiUtil.PADDING);
    this.bottomRight.setPosition(this.screen.width - GuiUtil.PADDING, this.screen.height - GuiUtil.PADDING);

    this.topLeft.arrangeElements();
    this.bottomLeft.arrangeElements();
    this.topRight.arrangeElements();
    this.bottomRight.arrangeElements();
    this.nonPositioned.arrangeElements();
  }
}
