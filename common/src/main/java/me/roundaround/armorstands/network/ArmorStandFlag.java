package me.roundaround.armorstands.network;

import io.netty.buffer.ByteBuf;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.mixin.ArmorStandAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

public enum ArmorStandFlag {
  UNKNOWN(0, "unknown", false),
  HIDE_BASE_PLATE(1, "base", true),
  SHOW_ARMS(2, "arms", false),
  SMALL(3, "small", false),
  NO_GRAVITY(4, "gravity", true),
  INVISIBLE(5, "visible", false),
  NAME(6, "name", false),
  INVULNERABLE(7, "invulnerable", false),
  LOCK_INVENTORY(8, "inventory", false);

  private static final Logger LOGGER = LogManager.getLogger(Constants.MOD_ID);

  public static final IntFunction<ArmorStandFlag> ID_TO_VALUE_FUNCTION =
      ByIdMap.continuous(ArmorStandFlag::getIndex,
      values(),
      ByIdMap.OutOfBoundsStrategy.ZERO
  );
  public static final StreamCodec<ByteBuf, ArmorStandFlag> PACKET_CODEC = ByteBufCodecs.idMapper(
      ID_TO_VALUE_FUNCTION,
      ArmorStandFlag::getIndex
  );

  // Magic number from Vanilla Tweaks armor stand data pack. ¯\_(ツ)_/¯
  private static final int ALL_SLOTS_DISABLED = 4144959;

  private final int index;
  private final String id;
  private final boolean invertControl;

  ArmorStandFlag(int index, String id, boolean invertControl) {
    this.index = index;
    this.id = id;
    this.invertControl = invertControl;
  }

  @Override
  public String toString() {
    return this.id;
  }

  public int getIndex() {
    return this.index;
  }

  public Component getDisplayName() {
    return Component.translatable("armorstands.flags." + this.id);
  }

  public boolean invertControl() {
    return this.invertControl;
  }

  public boolean getValue(ArmorStand armorStand) {
    ArmorStandAccessor accessor = (ArmorStandAccessor) armorStand;

    return switch (this) {
      case HIDE_BASE_PLATE -> !armorStand.showBasePlate();
      case SHOW_ARMS -> armorStand.showArms();
      case SMALL -> armorStand.isSmall();
      case NO_GRAVITY -> armorStand.isNoGravity();
      case INVISIBLE -> armorStand.isInvisible();
      case NAME -> armorStand.isCustomNameVisible();
      case INVULNERABLE -> armorStand.isInvulnerable();
      case LOCK_INVENTORY -> accessor.getDisabledSlots() == ALL_SLOTS_DISABLED;
      default -> false;
    };
  }

  public void setValue(ArmorStand armorStand, boolean value) {
    ArmorStandAccessor accessor = (ArmorStandAccessor) armorStand;

    switch (this) {
      case HIDE_BASE_PLATE:
        accessor.invokeSetHideBasePlate(value);
        break;
      case SHOW_ARMS:
        accessor.invokeSetShowArms(value);
        break;
      case SMALL:
        accessor.invokeSetSmall(value);
        break;
      case NO_GRAVITY:
        armorStand.setNoGravity(value);

        Vec3 vel = armorStand.getDeltaMovement();
        armorStand.setDeltaMovement(vel.x, 0, vel.z);

        if (!value) {
          armorStand.noPhysics = false;
          armorStand.snapTo(armorStand.getX(), armorStand.getY() + 0.01, armorStand.getZ(), armorStand.getYRot(), armorStand.getXRot());
          armorStand.move(MoverType.SELF, new Vec3(0, -0.009, 0));
        }
        break;
      case INVISIBLE:
        armorStand.setInvisible(value);
        break;
      case NAME:
        armorStand.setCustomNameVisible(value);
        break;
      case INVULNERABLE:
        armorStand.setInvulnerable(value);
        break;
      case LOCK_INVENTORY:
        accessor.setDisabledSlots(value ? ALL_SLOTS_DISABLED : 0);
        break;
      default:
        // Do nothing for unknown
        LOGGER.warn("Tried to set value to flag {}. Ignoring.", name());
    }
  }

  public static ArmorStandFlag fromString(String value) {
    return Arrays.stream(ArmorStandFlag.values()).filter((flag) -> flag.id.equals(value)).findFirst().orElseGet(() -> {
      LOGGER.warn("Unknown flag id '{}'. Returning UNKNOWN.", value);
      return UNKNOWN;
    });
  }

  public static List<ArmorStandFlag> getFlags() {
    return Arrays.stream(values()).filter((flag) -> flag != UNKNOWN).toList();
  }
}
