package me.roundaround.armorstands.util.actions;

import me.roundaround.armorstands.util.Clipboard;
import me.roundaround.armorstands.util.Clipboard.Entry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ClipboardPasteAction extends SimpleArmorStandAction<Entry> {
  private ClipboardPasteAction(Entry entry) {
    super(entry);
  }

  public static ClipboardPasteAction create(Entry entry) {
    return new ClipboardPasteAction(entry);
  }

  @Override
  public Component getName(ArmorStand armorStand) {
    return Component.translatable("armorstands.action.paste");
  }

  @Override
  protected Entry get(ArmorStand armorStand) {
    return Entry.everything(armorStand);
  }
}
