package me.roundaround.armorstands.util.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

public class ComboAction implements ArmorStandAction {
  protected Function<ArmorStand, Component> nameFunction;
  protected final ArrayList<ArmorStandAction> actions = new ArrayList<>();

  protected ComboAction(Function<ArmorStand, Component> nameFunction, Collection<ArmorStandAction> actions) {
    this.nameFunction = nameFunction;
    this.actions.addAll(actions);
  }

  protected ComboAction(Component name, Collection<ArmorStandAction> actions) {
    this((armorStand) -> name, actions);
  }

  @Override
  public Component getName(ArmorStand armorStand) {
    return nameFunction.apply(armorStand);
  }

  @Override
  public void apply(Player player, ArmorStand armorStand) {
    for (int i = 0; i < actions.size(); i++) {
      ArmorStandAction action = actions.get(i);
      if (action != null) {
        action.apply(player, armorStand);
      }
    }
  }

  @Override
  public void undo(Player player, ArmorStand armorStand) {
    for (int i = actions.size() - 1; i >= 0; i--) {
      actions.get(i).undo(player, armorStand);
    }
  }
}
