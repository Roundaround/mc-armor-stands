package me.roundaround.armorstands.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import me.roundaround.armorstands.entity.ArmorStandInventory;
import me.roundaround.armorstands.mixin.ArmorStandAccessor;
import me.roundaround.armorstands.mixin.LivingEntityAccessor;
import me.roundaround.armorstands.network.ScreenType;
import me.roundaround.armorstands.server.network.ServerNetworking;
import me.roundaround.armorstands.util.ArmorStandEditor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;

public class ArmorStandScreenHandler extends AbstractContainerMenu {
  private static final Identifier EMPTY_MAINHAND_ARMOR_SLOT = Identifier.withDefaultNamespace("container/slot/sword");

  private static final Identifier[] EMPTY_ARMOR_SLOT_TEXTURES = new Identifier[]{
      InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS,
      InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
      InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
      InventoryMenu.EMPTY_ARMOR_SLOT_HELMET
  };
  private static final Identifier[] EMPTY_HAND_SLOT_TEXTURES = new Identifier[]{
      EMPTY_MAINHAND_ARMOR_SLOT, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD
  };
  private static final EquipmentSlot[] EQUIPMENT_SLOT_ORDER = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
  };
  private static final EquipmentSlot[] HAND_SLOT_ORDER = new EquipmentSlot[]{
      EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
  };

  private final Inventory playerInventory;
  private final ArmorStand armorStand;
  private final ScreenType screenType;
  private final ArmorStandInventory inventory;
  private final ArmorStandEditor editor;
  private final ArrayList<Pair<Slot, EquipmentSlot>> armorSlots = new ArrayList<>();

  public ArmorStandScreenHandler(int syncId, Inventory playerInventory, ArmorStand armorStand, ScreenType screenType) {
    super(null, syncId);

    this.playerInventory = playerInventory;
    this.armorStand = armorStand;
    this.screenType = screenType;

    this.inventory = new ArmorStandInventory(armorStand, ((LivingEntityAccessor) armorStand).getEquipment());

    if (playerInventory.player instanceof ServerPlayer serverPlayer) {
      this.editor = ArmorStandEditor.get(serverPlayer, armorStand);
    } else {
      this.editor = null;
    }

    this.inventory.startOpen(this.playerInventory.player);

    if (this.screenType != null && this.screenType.usesInventory()) {
      this.initSlots();
    }
  }

  private void initSlots() {
    for (int col = 0; col < 9; ++col) {
      this.addSlot(new Slot(this.playerInventory, col, 8 + col * 18, 142));
    }
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 9; col++) {
        this.addSlot(new Slot(this.playerInventory, col + (row + 1) * 9, 8 + col * 18, 84 + row * 18));
      }
    }

    for (int i = 0; i < 2; i++) {
      int slotIndex = i;
      final EquipmentSlot equipmentSlot = HAND_SLOT_ORDER[i];

      Slot slot = this.addSlot(new Slot(this.inventory, slotIndex, 116, 44 + slotIndex * 18) {
        @Override
        public void setByPlayer(@NonNull ItemStack stack) {
          ArmorStandScreenHandler.this.armorStand.setItemSlot(equipmentSlot, stack);
          this.setChanged();
        }

        @Override
        public Identifier getNoItemIcon() {
          return EMPTY_HAND_SLOT_TEXTURES[slotIndex];
        }

        @Override
        public boolean mayPickup(@NonNull Player player) {
          if (!ArmorStandScreenHandler.this.playerInventory.player.isCreative() &&
              isSlotDisabled(ArmorStandScreenHandler.this.armorStand, equipmentSlot)) {
            return false;
          }
          return super.mayPickup(player);
        }

        @Override
        public boolean mayPlace(@NonNull ItemStack stack) {
          if (!ArmorStandScreenHandler.this.playerInventory.player.isCreative() &&
              isSlotDisabled(ArmorStandScreenHandler.this.armorStand, equipmentSlot)) {
            return false;
          }
          return super.mayPlace(stack);
        }
      });

      this.armorSlots.add(Pair.of(slot, equipmentSlot));
    }

    for (int i = 0; i < 4; i++) {
      final EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_ORDER[i];

      Slot slot = this.addSlot(new Slot(this.inventory, 2 + 3 - i, 44, 8 + i * 18) {
        @Override
        public void setByPlayer(@NonNull ItemStack stack) {
          ArmorStandScreenHandler.this.armorStand.setItemSlot(equipmentSlot, stack);
          this.setChanged();
        }

        @Override
        public int getMaxStackSize() {
          return 1;
        }

        @Override
        public boolean mayPickup(@NonNull Player player) {
          if (!ArmorStandScreenHandler.this.playerInventory.player.isCreative() &&
              isSlotDisabled(ArmorStandScreenHandler.this.armorStand, equipmentSlot)) {
            return false;
          }
          return super.mayPickup(player);
        }

        @Override
        public boolean mayPlace(@NonNull ItemStack stack) {
          if (!ArmorStandScreenHandler.this.playerInventory.player.isCreative() &&
              isSlotDisabled(ArmorStandScreenHandler.this.armorStand, equipmentSlot)) {
            return false;
          }
          if (equipmentSlot == EquipmentSlot.HEAD) {
            return true;
          }
          return equipmentSlot == ArmorStandScreenHandler.this.armorStand.getEquipmentSlotForItem(stack);
        }

        @Override
        public Identifier getNoItemIcon() {
          return EMPTY_ARMOR_SLOT_TEXTURES[equipmentSlot.getIndex()];
        }
      });

      this.armorSlots.add(Pair.of(slot, equipmentSlot));
    }
  }

  public ImmutableList<Pair<Slot, EquipmentSlot>> getArmorSlots() {
    return ImmutableList.copyOf(this.armorSlots);
  }

  public ScreenType getScreenType() {
    return this.screenType;
  }

  public ArmorStand getArmorStand() {
    return this.armorStand;
  }

  public ArmorStandEditor getEditor() {
    return this.editor;
  }

  public Inventory getPlayerInventory() {
    return this.playerInventory;
  }

  @Override
  public void broadcastChanges() {
    if (this.playerInventory.player instanceof ServerPlayer) {
      ServerNetworking.sendClientUpdatePacket((ServerPlayer) this.playerInventory.player, this.armorStand);
    }

    super.broadcastChanges();
  }

  @Override
  public boolean stillValid(@NonNull Player player) {
    return this.inventory.stillValid(player);
  }

  @Override
  public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
    if (index < 0 || index >= this.slots.size()) {
      return ItemStack.EMPTY;
    }

    Slot slot = this.slots.get(index);
    if (!slot.hasItem()) {
      return ItemStack.EMPTY;
    }

    // 0-8 is hotbar
    // 9-35 is main player inventory
    // 36-37 is hands
    // 38 is head
    // 39 is chest
    // 40 is legs
    // 41 is feet

    ItemStack stack = slot.getItem();
    ItemStack originalStack = stack.copy();

    if (index < Inventory.INVENTORY_SIZE) {
      if (!this.tryTransferArmor(stack) && !this.tryTransferToMainHand(stack) && !this.tryTransferToOffHand(stack)) {
        return ItemStack.EMPTY;
      }
    } else {
      if (!this.moveItemStackTo(stack, 0, Inventory.INVENTORY_SIZE, false)) {
        return ItemStack.EMPTY;
      }
    }

    if (stack.isEmpty()) {
      slot.setByPlayer(ItemStack.EMPTY);
    } else {
      slot.setChanged();
    }

    return originalStack;
  }

  private boolean tryTransferArmor(ItemStack stack) {
    EquipmentSlot equipmentSlot = this.armorStand.getEquipmentSlotForItem(stack);
    if (isSlotDisabled(this.armorStand, equipmentSlot)) {
      return false;
    }

    int targetIndex = this.slots.size() - 1 - equipmentSlot.getIndex();
    return this.moveItemStackTo(stack, targetIndex, targetIndex + 1, false);
  }

  private boolean tryTransferToMainHand(ItemStack stack) {
    if (isSlotDisabled(this.armorStand, EquipmentSlot.MAINHAND)) {
      return false;
    }

    int targetIndex = Inventory.INVENTORY_SIZE;
    return this.moveItemStackTo(stack, targetIndex, targetIndex + 1, false);
  }

  private boolean tryTransferToOffHand(ItemStack stack) {
    if (isSlotDisabled(this.armorStand, EquipmentSlot.OFFHAND)) {
      return false;
    }

    int targetIndex = Inventory.INVENTORY_SIZE + 1;
    return this.moveItemStackTo(stack, targetIndex, targetIndex + 1, false);
  }

  public static boolean isSlotDisabled(ArmorStand armorStand, EquipmentSlot slot) {
    return (((ArmorStandAccessor) armorStand).getDisabledSlots() & 1 << slot.getIndex()) != 0;
  }
}
