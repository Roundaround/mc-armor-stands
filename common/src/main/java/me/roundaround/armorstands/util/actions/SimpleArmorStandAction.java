package me.roundaround.armorstands.util.actions;

import java.util.Optional;

import me.roundaround.armorstands.util.ArmorStandApplyable;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

public abstract class SimpleArmorStandAction<T extends ArmorStandApplyable> implements ArmorStandAction {
  private final T value;
  private Optional<T> original = Optional.empty();

  protected SimpleArmorStandAction(T value) {
    this.value = value;
  }

  protected abstract T get(ArmorStand armorStand);

  @Override
  public void apply(Player player, ArmorStand armorStand) {
    original = Optional.of(get(armorStand));
    value.apply(player, armorStand);
  }

  @Override
  public void undo(Player player, ArmorStand armorStand) {
    original.ifPresent((original) -> original.apply(player, armorStand));
  }
}
