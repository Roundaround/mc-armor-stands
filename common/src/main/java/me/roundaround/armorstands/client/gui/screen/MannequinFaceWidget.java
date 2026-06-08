package me.roundaround.armorstands.client.gui.screen;

import me.roundaround.trove.client.gui.util.GuiUtil;
import me.roundaround.trove.client.gui.widget.drawable.DrawableWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class MannequinFaceWidget extends DrawableWidget {
  private static final int BACKGROUND_PADDING = 2;
  private static final int BACKGROUND_COLOR = GuiUtil.genColorInt(0f, 0f, 0f, 0.7f);

  private final int size;
  private final Supplier<PlayerSkin> skinSupplier;

  public MannequinFaceWidget(int size, Supplier<PlayerSkin> skinSupplier) {
    super(size, size);
    this.size = size;
    this.skinSupplier = skinSupplier;
  }

  @Override
  public int getWidth() {
    return this.size;
  }

  @Override
  public int getHeight() {
    return this.size;
  }

  @Override
  public void extractWidgetRenderState(@NotNull GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
    extractor.fill(
        this.getX() - BACKGROUND_PADDING,
        this.getY() - BACKGROUND_PADDING,
        this.getRight() + BACKGROUND_PADDING,
        this.getBottom() + BACKGROUND_PADDING,
        BACKGROUND_COLOR
    );

    PlayerSkin skin = this.skinSupplier.get();
    if (skin == null) {
      return;
    }
    PlayerFaceExtractor.extractRenderState(extractor, skin, this.getX(), this.getY(), this.getWidth());
  }
}
