package me.roundaround.armorstands.util;

import io.netty.buffer.ByteBuf;
import me.roundaround.armorstands.generated.Constants;
import me.roundaround.armorstands.util.Pose.PoseSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Rotations;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum PosePreset implements PoseSupplier {
  DEFAULT(
      "default",
      Source.VANILLA,
      Category.STANDING,
      new Rotations(0f, 0f, 0f),
      new Rotations(0f, 0f, 0f),
      new Rotations(-15f, 0f, 10f),
      new Rotations(-10f, 0f, -10f),
      new Rotations(1f, 0f, 1f),
      new Rotations(-1f, 0f, -1f)
  ), ATTENTION(
      "attention",
      Source.VANILLA_TWEAKS,
      Category.STANDING,
      new Rotations(0f, 0.001f, 0f),
      new Rotations(0f, 0.001f, 0f),
      new Rotations(0f, 0f, 0f),
      new Rotations(0f, 0f, 0f),
      new Rotations(0f, 0f, 0f),
      new Rotations(0f, 0f, 0f)
  ), CONFIDENT(
      "confident",
      Source.VANILLA_TWEAKS,
      Category.STANDING,
      new Rotations(-10f, 20f, 0f),
      new Rotations(-2f, 0f, 0f),
      new Rotations(5f, 0f, 0f),
      new Rotations(5f, 0f, 0f),
      new Rotations(16f, 2f, 10f),
      new Rotations(0f, -10f, -4f)
  ), WALKING(
      "walking",
      Source.VANILLA_TWEAKS,
      Category.MOVING,
      new Rotations(0f, 0.001f, 0f),
      new Rotations(0f, 0.001f, 0f),
      new Rotations(20f, 0f, 10f),
      new Rotations(-20f, 0f, -10f),
      new Rotations(-20f, 0f, 0f),
      new Rotations(20f, 0f, 0f)
  ), RUNNING(
      "running",
      Source.VANILLA_TWEAKS,
      Category.MOVING,
      new Rotations(0f, 0.001f, 0f),
      new Rotations(0f, 0.001f, 0f),
      new Rotations(-40f, 0f, 10f),
      new Rotations(40f, 0f, -10f),
      new Rotations(-40f, 0f, 0f),
      new Rotations(40f, 0f, 0f)
  ), LUNGING(
      "lunging",
      Source.VANILLA_TWEAKS,
      Category.MOVING,
      new Rotations(0.0f, 0.001f, 0.0f),
      new Rotations(15.0f, 0.0f, 0.0f),
      new Rotations(-60.0f, -10.0f, 0.0f),
      new Rotations(10.0f, 0.0f, -10.0f),
      new Rotations(-15.0f, 0.0f, 0.0f),
      new Rotations(30.0f, 0.0f, 0.0f)
  ), POINTING(
      "pointing",
      Source.VANILLA_TWEAKS,
      Category.STANDING,
      new Rotations(0f, 20f, 0f),
      new Rotations(0f, 0.001f, 0f),
      new Rotations(-90f, 18f, 0f),
      new Rotations(0f, 0f, -10f),
      new Rotations(0f, 0f, 0f),
      new Rotations(0f, 0f, 0f)
  ), FACEPALM(
      "facepalm",
      Source.VANILLA_TWEAKS,
      Category.STANDING,
      new Rotations(45f, -4f, 1f),
      new Rotations(10f, 0f, 0f),
      new Rotations(18f, -14f, 0f),
      new Rotations(-72f, 24f, 47f),
      new Rotations(25f, -2f, 0f),
      new Rotations(-4f, -6f, -2f)
  ), BLOCKING(
      "blocking",
      Source.VANILLA_TWEAKS,
      Category.STANDING,
      new Rotations(0f, 0.001f, 0f),
      new Rotations(0f, 0.001f, 0f),
      new Rotations(-20f, -20f, 0f),
      new Rotations(-50f, 50f, 0f),
      new Rotations(-20f, 0f, 0f),
      new Rotations(20f, 0f, 0f)
  ), CONFUSED(
      "confused",
      Source.VANILLA_TWEAKS,
      Category.STANDING,
      new Rotations(0.0f, 30.0f, 0f),
      new Rotations(0.0f, 13.0f, 0.0f),
      new Rotations(-22.0f, 31.0f, 10.0f),
      new Rotations(145.0f, 22.0f, -49.0f),
      new Rotations(6.0f, -20.0f, 0.0f),
      new Rotations(-6.0f, 0.0f, 0.0f)
  ), WINNING(
      "winning",
      Source.VANILLA_TWEAKS,
      Category.STANDING,
      new Rotations(-15.0f, 0.0f, 0.0f),
      new Rotations(0.0f, 0.001f, 0.0f),
      new Rotations(-120.0f, -10.0f, 0.0f),
      new Rotations(10.0f, 0.0f, -10.0f),
      new Rotations(0.0f, 0.0f, 0.0f),
      new Rotations(15.0f, 0.0f, 0.0f)
  ), FORMAL(
      "formal",
      Source.VANILLA_TWEAKS,
      Category.STANDING,
      new Rotations(4.0f, 0.0f, 0.0f),
      new Rotations(4.0f, 0.0f, 0.0f),
      new Rotations(30.0f, 22.0f, -20.0f),
      new Rotations(30.0f, -20.0f, 21.0f),
      new Rotations(0.0f, 0.0f, 5.0f),
      new Rotations(0.0f, 0.0f, -5.0f)
  ), SALUTE(
      "salute",
      Source.VANILLA_TWEAKS,
      Category.STANDING,
      new Rotations(0f, 0.001f, 0f),
      new Rotations(5f, 0f, 0f),
      new Rotations(-124f, -51f, -35f),
      new Rotations(29f, 0f, 25f),
      new Rotations(0f, -4f, -2f),
      new Rotations(0f, 4f, 2f)
  ), SAD(
      "sad",
      Source.VANILLA_TWEAKS,
      Category.STANDING,
      new Rotations(63.0f, 0.0f, 0.0f),
      new Rotations(10.0f, 0.0f, 0.0f),
      new Rotations(-5.0f, 0.0f, 5.0f),
      new Rotations(-5.0f, 0.0f, -5.0f),
      new Rotations(-5.0f, -10.0f, 5.0f),
      new Rotations(-5.0f, 16.0f, -5.0f)
  ), ARABESQUE(
      "arabesque",
      Source.VANILLA_TWEAKS,
      Category.DYNAMIC,
      new Rotations(-15.0f, 0.0f, 0.0f),
      new Rotations(10.0f, 0.0f, 0.0f),
      new Rotations(-140.0f, -10.0f, 0.0f),
      new Rotations(70.0f, 0.0f, -10.0f),
      new Rotations(0.0f, 0.0f, 0.0f),
      new Rotations(75.0f, 0.0f, 0.0f)
  ), CANCAN(
      "cancan",
      Source.BEDROCK,
      Category.DYNAMIC,
      new Rotations(-5.0f, 18.0f, 0.0f),
      new Rotations(0.0f, 22.0f, 0.0f),
      new Rotations(0.0f, 84.0f, 111.0f),
      new Rotations(8.0f, 0.0f, -114.0f),
      new Rotations(0.0f, 23.0f, -13.0f),
      new Rotations(-111.0f, 55.0f, 0.0f)
  ), JOYOUS(
      "joyous",
      Source.VANILLA_TWEAKS,
      Category.DYNAMIC,
      new Rotations(-11.0f, 0.0f, 0.0f),
      new Rotations(-4.0f, 0.0f, 0.0f),
      new Rotations(0.0f, 0.0f, 100.0f),
      new Rotations(0.0f, 0.0f, -100.0f),
      new Rotations(-8.0f, 0.0f, 60.0f),
      new Rotations(-8.0f, 0.0f, -60.0f)
  ), CUPID(
      "cupid",
      Source.VANILLA_TWEAKS,
      Category.DYNAMIC,
      new Rotations(0.0f, 0.001f, 0.0f),
      new Rotations(10.0f, 0.0f, 0.0f),
      new Rotations(-90.0f, -10.0f, 0.0f),
      new Rotations(-75.0f, 0.0f, 10.0f),
      new Rotations(0.0f, 0.0f, 0.0f),
      new Rotations(75.0f, 0.0f, 0.0f)
  ), STARGAZING(
      "stargazing",
      Source.VANILLA_TWEAKS,
      Category.STANDING,
      new Rotations(-22.0f, 25.0f, 0.0f),
      new Rotations(-4.0f, 10.0f, 0.0f),
      new Rotations(-153.0f, 34.0f, -3.0f),
      new Rotations(4.0f, 18.0f, 0.0f),
      new Rotations(-4.0f, 17.0f, 2.0f),
      new Rotations(6.0f, 24.0f, 0.0f)
  ), GUARD(
      "guard",
      Source.BEDROCK,
      Category.STANDING,
      new Rotations(-5.0f, 0.0f, 0.0f),
      new Rotations(0.0f, 0.0f, 2.0f),
      new Rotations(-60.0f, 20.0f, -10.0f),
      new Rotations(10.0f, 0.0f, -5.0f),
      new Rotations(3.0f, 3.0f, 3.0f),
      new Rotations(-3.0f, -3.0f, -3.0f)
  ), BRANDISH(
      "brandish",
      Source.BEDROCK,
      Category.STANDING,
      new Rotations(-15.0f, 0.0f, 0.0f),
      new Rotations(0.0f, 0.0f, -2.0f),
      new Rotations(-110.0f, 50.0f, 0.0f),
      new Rotations(20.0f, 0.0f, -10.0f),
      new Rotations(-5.0f, 3.0f, 3.0f),
      new Rotations(5.0f, -3.0f, -3.0f)
  ), SPEAKING(
      "speaking",
      Source.BEDROCK,
      Category.STANDING,
      new Rotations(-15.0f, 0.0f, 0.0f),
      new Rotations(0.0f, 0.0f, 0.0f),
      new Rotations(-110.0f, 35.0f, 0.0f),
      new Rotations(-110.0f, -35.0f, 0.0f),
      new Rotations(-5.0f, 3.0f, 3.0f),
      new Rotations(5.0f, -3.0f, -3.0f)
  ), INSPIRING(
      "inspiring",
      Source.BEDROCK,
      Category.STANDING,
      new Rotations(-4.0f, 67.0f, 0.0f),
      new Rotations(0.0f, 8.0f, 0.0f),
      new Rotations(-99.0f, 63.0f, 0.0f),
      new Rotations(16.0f, 32.0f, -8.0f),
      new Rotations(4.0f, 63.0f, 8.0f),
      new Rotations(0.0f, -75.0f, -8.0f)
  ), THANKING(
      "thanking",
      Source.BEDROCK,
      Category.STANDING,
      new Rotations(-15.0f, 0.0f, 0.0f),
      new Rotations(0.0f, 0.0f, 0.0f),
      new Rotations(-110.0f, -35.0f, 0.0f),
      new Rotations(-110.0f, 35.0f, 0.0f),
      new Rotations(-5.0f, 3.0f, 3.0f),
      new Rotations(5.0f, -3.0f, -3.0f)
  ), DABBING(
      "dabbing",
      Source.BEDROCK,
      Category.DYNAMIC,
      new Rotations(16.0f, 20.0f, 0.0f),
      new Rotations(0.0f, 0.0f, 0.0f),
      new Rotations(246.0f, 0.0f, 89.0f),
      new Rotations(4.0f, 8.0f, 237.0f),
      new Rotations(8.0f, 20.0f, 4.0f),
      new Rotations(-14.0f, -18.0f, -16.0f)
  ), PLEDGING(
      "pledging",
      Source.BEDROCK,
      Category.STANDING,
      new Rotations(0.0f, 0.0f, 0.0f),
      new Rotations(0.0f, 0.0f, 0.0f),
      new Rotations(-70.0f, -40.0f, 0.0f),
      new Rotations(10.0f, 0.0f, -5.0f),
      new Rotations(1.0f, 0.0f, 1.0f),
      new Rotations(-1.0f, 0.0f, -1.0f)
  ), CONTEMPLATING(
      "contemplating",
      Source.BEDROCK,
      Category.STANDING,
      new Rotations(15.0f, 0.0f, 0.0f),
      new Rotations(0.0f, 0.0f, 2.0f),
      new Rotations(-60.0f, -20.0f, -10.0f),
      new Rotations(-30.0f, 15.0f, 15.0f),
      new Rotations(1.0f, 0.0f, 1.0f),
      new Rotations(-1.0f, 0.0f, -1.0f)
  ), ZOMBIE(
      "zombie",
      Source.BEDROCK,
      Category.MOVING,
      new Rotations(-10.0f, 0.0f, -5.0f),
      new Rotations(0.0f, 0.0f, 0.0f),
      new Rotations(-100.0f, 0.0f, 0.0f),
      new Rotations(-105.0f, 0.0f, 0.0f),
      new Rotations(-46.0f, 0.0f, 0.0f),
      new Rotations(7.0f, 0.0f, 0.0f)
  ), SITTING(
      "sitting",
      Source.VANILLA_TWEAKS,
      Category.SITTING,
      new Rotations(0.0f, 0.001f, 0.0f),
      new Rotations(0.0f, 0.001f, 0.0f),
      new Rotations(-80.0f, 20.0f, 0.0f),
      new Rotations(-80.0f, -20.0f, 0.0f),
      new Rotations(-90.0f, 10.0f, 0.0f),
      new Rotations(-90.0f, -10.0f, 0.0f)
  ), LAZING(
      "lazing",
      Source.VANILLA_TWEAKS,
      Category.SITTING,
      new Rotations(14f, -12f, 6f),
      new Rotations(5f, 0f, 0f),
      new Rotations(-40f, 20f, 0f),
      new Rotations(-4f, -20f, -10f),
      new Rotations(-88f, 71f, 0f),
      new Rotations(-88f, 46f, 0f)
  ), JUMP_ATTACK(
      "jumpAttack",
      Source.ROUNDAROUND,
      Category.DYNAMIC,
      new Rotations(-43f, 0f, 0f),
      new Rotations(4f, 0f, 0f),
      new Rotations(-144f, -10f, 28f),
      new Rotations(-129f, 47f, -7f),
      new Rotations(30f, 0f, 20f),
      new Rotations(30f, 0f, -20f)
  ), STEWARD(
      "steward",
      Source.ROUNDAROUND,
      Category.STANDING,
      new Rotations(-3f, 0f, 0f),
      new Rotations(0f, 0f, 2f),
      new Rotations(-105f, -45f, 180f),
      new Rotations(-2f, 0f, -20f),
      new Rotations(0f, 0f, 5f),
      new Rotations(0f, 0f, -5f)
  ), SCARECROW(
      "scarecrow",
      Source.ROUNDAROUND,
      Category.DYNAMIC,
      new Rotations(8f, 26f, -6f),
      new Rotations(0f, 0f, 5f),
      new Rotations(-15f, 0f, 79f),
      new Rotations(-16f, 0f, -40f),
      new Rotations(0f, 0f, -18f),
      new Rotations(16f, 20f, 0f)
  ), PRETTY(
      "pretty",
      Source.ROUNDAROUND,
      Category.SITTING,
      new Rotations(-17f, 0f, 0f),
      new Rotations(0f, 0f, -8f),
      new Rotations(-12f, 20f, 0f),
      new Rotations(130f, -14f, -3f),
      new Rotations(-100f, -44f, 0f),
      new Rotations(-90f, -10f, 0f)
  ), DRUM_MAJOR(
      "drumMajor",
      Source.ROUNDAROUND,
      Category.DYNAMIC,
      new Rotations(-13f, 0f, 0f),
      new Rotations(9f, 0f, 0f),
      new Rotations(110f, 125f, 180f),
      new Rotations(-10f, 0f, -20f),
      new Rotations(-94f, 0f, 0f),
      new Rotations(15f, 0f, 0f)
  ), FISHING(
      "fishing",
      Source.ROUNDAROUND,
      Category.SITTING,
      new Rotations(-15f, 0f, -12f),
      new Rotations(-8f, 0f, 0f),
      new Rotations(-68f, -24f, 0f),
      new Rotations(-80f, 44f, 0f),
      new Rotations(-90f, 20f, 0f),
      new Rotations(-90f, -20f, 0f)
  );

  private static final Logger LOGGER = LogManager.getLogger(Constants.MOD_ID);

  public static final StreamCodec<ByteBuf, PosePreset> PACKET_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      PosePreset::getId,
      PosePreset::fromString
  );

  private final String id;
  private final Component label;
  private final Source source;
  private final Category category;
  private final Rotations head;
  private final Rotations body;
  private final Rotations rightArm;
  private final Rotations leftArm;
  private final Rotations rightLeg;
  private final Rotations leftLeg;

  PosePreset(
      String id,
      Source source,
      Category category,
      Rotations head,
      Rotations body,
      Rotations rightArm,
      Rotations leftArm,
      Rotations rightLeg,
      Rotations leftLeg
  ) {
    this.id = id;
    this.label = Component.translatable("armorstands.preset." + id);
    this.source = source;
    this.category = category;
    this.head = head;
    this.body = body;
    this.rightArm = rightArm;
    this.leftArm = leftArm;
    this.rightLeg = rightLeg;
    this.leftLeg = leftLeg;
  }

  @Override
  public String toString() {
    return this.id;
  }

  public String getId() {
    return this.id;
  }

  @Override
  public Pose toPose() {
    return new Pose(this.head, this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg);
  }

  public Component getDisplayName() {
    return this.label;
  }

  public Source getSource() {
    return this.source;
  }

  public Category getCategory() {
    return this.category;
  }

  public Rotations getHead() {
    return this.head;
  }

  public Rotations getBody() {
    return this.body;
  }

  public Rotations getRightArm() {
    return this.rightArm;
  }

  public Rotations getLeftArm() {
    return this.leftArm;
  }

  public Rotations getRightLeg() {
    return this.rightLeg;
  }

  public Rotations getLeftLeg() {
    return this.leftLeg;
  }

  public static PosePreset fromString(String value) {
    return Arrays.stream(PosePreset.values()).filter((pose) -> pose.id.equals(value)).findFirst().orElseGet(() -> {
      LOGGER.warn("Unknown id '{}'. Defaulting to DEFAULT.", value);
      return DEFAULT;
    });
  }

  public static List<PosePreset> getPresets() {
    return getPresets(Source.ALL, Category.ALL);
  }

  public static List<PosePreset> getPresets(Source source, Category category) {
    return Arrays.stream(PosePreset.values())
        .filter((pose) -> pose.getSource().matches(source) && pose.getCategory().matches(category))
        .sorted((a, b) -> {
          if (category != Category.ALL) {
            int sourceCompare = a.getSource().compareTo(b.getSource());
            if (sourceCompare != 0) {
              return sourceCompare;
            }
          } else {
            int categoryCompare = a.getCategory().compareTo(b.getCategory());
            if (categoryCompare != 0) {
              return categoryCompare;
            }
          }

          return a.getDisplayName().getString().compareTo(b.getDisplayName().getString());
        })
        .collect(Collectors.toList());
  }

  public enum Source {
    ALL("all", null),
    VANILLA("vanilla", ChatFormatting.WHITE),
    VANILLA_TWEAKS("vanillaTweaks", ChatFormatting.YELLOW),
    BEDROCK("bedrock", ChatFormatting.AQUA),
    ROUNDAROUND("roundaround", ChatFormatting.LIGHT_PURPLE);

    private final String id;
    private final ChatFormatting color;

    Source(String id, ChatFormatting color) {
      this.id = id;
      this.color = color;
    }

    public MutableComponent getDisplayName() {
      return Component.translatable("armorstands.source." + this.id);
    }

    public Component getColoredName() {
      MutableComponent base = this.getDisplayName();
      if (this.color != null) {
        return base.withStyle(this.color);
      }
      return base;
    }

    public boolean matches(Source source) {
      return this == ALL || source == ALL || this == source;
    }

    public static List<Source> getSources() {
      return Arrays.stream(Source.values())
          .filter((source) -> source == Source.ALL ||
                              Arrays.stream(PosePreset.values()).anyMatch((pose) -> pose.getSource() == source))
          .collect(Collectors.toList());
    }
  }

  public enum Category {
    ALL("all"), STANDING("standing"), MOVING("moving"), DYNAMIC("dynamic"), SITTING("sitting"), OTHER("other");

    private final String id;

    Category(String id) {
      this.id = id;
    }

    public Component getDisplayName() {
      return Component.translatable("armorstands.category." + this.id);
    }

    public boolean matches(Category category) {
      return this == ALL || category == ALL || this == category;
    }

    public static List<Category> getCategories() {
      return Arrays.stream(Category.values())
          .filter((category) -> category == Category.ALL ||
                                Arrays.stream(PosePreset.values()).anyMatch((pose) -> pose.getCategory() == category))
          .collect(Collectors.toList());
    }
  }
}
