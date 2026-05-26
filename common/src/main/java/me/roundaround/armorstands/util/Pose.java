package me.roundaround.armorstands.util;

import java.util.Optional;
import net.minecraft.core.Rotations;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

public class Pose implements ArmorStandApplyable {
  private final Optional<Rotations> optionalHead;
  private final Optional<Rotations> optionalBody;
  private final Optional<Rotations> optionalRightArm;
  private final Optional<Rotations> optionalLeftArm;
  private final Optional<Rotations> optionalRightLeg;
  private final Optional<Rotations> optionalLeftLeg;

  public Pose(ArmorStand armorStand) {
    this(
        armorStand.getHeadPose(),
        armorStand.getBodyPose(),
        armorStand.getRightArmPose(),
        armorStand.getLeftArmPose(),
        armorStand.getRightLegPose(),
        armorStand.getLeftLegPose()
    );
  }

  public Pose(
      Rotations head,
      Rotations body,
      Rotations rightArm,
      Rotations leftArm,
      Rotations rightLeg,
      Rotations leftLeg
  ) {
    this(
        Optional.of(head),
        Optional.of(body),
        Optional.of(rightArm),
        Optional.of(leftArm),
        Optional.of(rightLeg),
        Optional.of(leftLeg)
    );
  }

  public Pose(
      Optional<Rotations> optionalHead,
      Optional<Rotations> optionalBody,
      Optional<Rotations> optionalRightArm,
      Optional<Rotations> optionalLeftArm,
      Optional<Rotations> optionalRightLeg,
      Optional<Rotations> optionalLeftLeg
  ) {
    this.optionalHead = optionalHead;
    this.optionalBody = optionalBody;
    this.optionalRightArm = optionalRightArm;
    this.optionalLeftArm = optionalLeftArm;
    this.optionalRightLeg = optionalRightLeg;
    this.optionalLeftLeg = optionalLeftLeg;
  }

  @Override
  public void apply(Player player, ArmorStand armorStand) {
    optionalHead.ifPresent(armorStand::setHeadPose);
    optionalBody.ifPresent(armorStand::setBodyPose);
    optionalRightArm.ifPresent(armorStand::setRightArmPose);
    optionalLeftArm.ifPresent(armorStand::setLeftArmPose);
    optionalRightLeg.ifPresent(armorStand::setRightLegPose);
    optionalLeftLeg.ifPresent(armorStand::setLeftLegPose);
  }

  public Rotations getHead() {
    return optionalHead.orElse(PosePreset.DEFAULT.getHead());
  }

  public Rotations getBody() {
    return optionalBody.orElse(PosePreset.DEFAULT.getBody());
  }

  public Rotations getRightArm() {
    return optionalRightArm.orElse(PosePreset.DEFAULT.getRightArm());
  }

  public Rotations getLeftArm() {
    return optionalLeftArm.orElse(PosePreset.DEFAULT.getLeftArm());
  }

  public Rotations getRightLeg() {
    return optionalRightLeg.orElse(PosePreset.DEFAULT.getRightLeg());
  }

  public Rotations getLeftLeg() {
    return optionalLeftLeg.orElse(PosePreset.DEFAULT.getLeftLeg());
  }

  public Pose mirror() {
    return new Pose(
        optionalHead.map((head) -> new Rotations(head.x(), -head.y(), -head.z())),
        optionalBody.map((body) -> new Rotations(body.x(), -body.y(), -body.z())),
        optionalLeftArm.map((leftArm) -> new Rotations(leftArm.x(), -leftArm.y(), -leftArm.z())),
        optionalRightArm.map((rightArm) -> new Rotations(rightArm.x(), -rightArm.y(), -rightArm.z())),
        optionalLeftLeg.map((leftLeg) -> new Rotations(leftLeg.x(), -leftLeg.y(), -leftLeg.z())),
        optionalRightLeg.map((rightLeg) -> new Rotations(rightLeg.x(), -rightLeg.y(), -rightLeg.z()))
    );
  }

  @FunctionalInterface
  public static interface PoseSupplier {
    public Pose toPose();
  }
}
