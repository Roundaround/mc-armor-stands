package me.roundaround.armorstands.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Map;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ArmorStandInventory implements Container {
  public static final Int2ObjectMap<EquipmentSlot> EQUIPMENT_SLOTS = new Int2ObjectArrayMap<>(Map.of(
      EquipmentSlot.MAINHAND.getIndex(),
      EquipmentSlot.MAINHAND,
      EquipmentSlot.OFFHAND.getIndex(),
      EquipmentSlot.OFFHAND,
      EquipmentSlot.FEET.getIndex(2),
      EquipmentSlot.FEET,
      EquipmentSlot.LEGS.getIndex(2),
      EquipmentSlot.LEGS,
      EquipmentSlot.CHEST.getIndex(2),
      EquipmentSlot.CHEST,
      EquipmentSlot.HEAD.getIndex(2),
      EquipmentSlot.HEAD
  ));

  public final ArmorStand armorStand;

  private final EntityEquipment equipment;

  public ArmorStandInventory(ArmorStand armorStand, EntityEquipment equipment) {
    this.armorStand = armorStand;
    this.equipment = equipment;
  }

  @Override
  public int getContainerSize() {
    return EQUIPMENT_SLOTS.size();
  }

  @Override
  public void clearContent() {
    this.equipment.clear();
  }

  @Override
  public boolean isEmpty() {
    for (EquipmentSlot equipmentSlot : EQUIPMENT_SLOTS.values()) {
      if (!this.equipment.get(equipmentSlot).isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public ItemStack getItem(int slot) {
    EquipmentSlot equipmentSlot = EQUIPMENT_SLOTS.get(slot);
    return equipmentSlot != null ? this.equipment.get(equipmentSlot) : ItemStack.EMPTY;
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    EquipmentSlot equipmentSlot = EQUIPMENT_SLOTS.get(slot);
    if (equipmentSlot != null) {
      this.equipment.set(equipmentSlot, stack);
    }
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) {
    EquipmentSlot equipmentSlot = EQUIPMENT_SLOTS.get(slot);
    return equipmentSlot != null ? this.equipment.set(equipmentSlot, ItemStack.EMPTY) : ItemStack.EMPTY;
  }

  @Override
  public ItemStack removeItem(int slot, int amount) {
    EquipmentSlot equipmentSlot = EQUIPMENT_SLOTS.get(slot);
    if (equipmentSlot != null) {
      ItemStack itemStack = this.equipment.get(equipmentSlot);
      if (!itemStack.isEmpty()) {
        return itemStack.split(amount);
      }
    }

    return ItemStack.EMPTY;
  }

  @Override
  public void setChanged() {
  }

  @Override
  public boolean stillValid(Player player) {
    return player.distanceToSqr(this.armorStand) <= 64;
  }
}
