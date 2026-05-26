package me.roundaround.armorstands.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.roundaround.armorstands.util.Pose.PoseSupplier;
import net.minecraft.core.Rotations;

public class SavedPose implements PoseSupplier {
  private final String name;
  private final Rotations head;
  private final Rotations body;
  private final Rotations rightArm;
  private final Rotations leftArm;
  private final Rotations rightLeg;
  private final Rotations leftLeg;

  public SavedPose(
      String name,
      Rotations head,
      Rotations body,
      Rotations rightArm,
      Rotations leftArm,
      Rotations rightLeg,
      Rotations leftLeg
  ) {
    this.name = name;
    this.head = head;
    this.body = body;
    this.rightArm = rightArm;
    this.leftArm = leftArm;
    this.rightLeg = rightLeg;
    this.leftLeg = leftLeg;
  }

  public SavedPose(String name, Pose pose) {
    this(
        name,
        pose.getHead(),
        pose.getBody(),
        pose.getRightArm(),
        pose.getLeftArm(),
        pose.getRightLeg(),
        pose.getLeftLeg()
    );
  }

  @Override
  public Pose toPose() {
    return new Pose(head, body, rightArm, leftArm, rightLeg, leftLeg);
  }

  public String getName() {
    return name;
  }

  public Rotations getHead() {
    return head;
  }

  public Rotations getBody() {
    return body;
  }

  public Rotations getRightArm() {
    return rightArm;
  }

  public Rotations getLeftArm() {
    return leftArm;
  }

  public Rotations getRightLeg() {
    return rightLeg;
  }

  public Rotations getLeftLeg() {
    return leftLeg;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("name", name);

    json.add("head", eulerAngleToJson(head));
    json.add("body", eulerAngleToJson(body));
    json.add("rightArm", eulerAngleToJson(rightArm));
    json.add("leftArm", eulerAngleToJson(leftArm));
    json.add("rightLeg", eulerAngleToJson(rightLeg));
    json.add("leftLeg", eulerAngleToJson(leftLeg));

    return json;
  }

  public static SavedPose fromJson(JsonObject json) {
    String name = json.get("name").getAsString();
    Rotations head = jsonToEulerAngle(json.getAsJsonArray("head"));
    Rotations body = jsonToEulerAngle(json.getAsJsonArray("body"));
    Rotations rightArm = jsonToEulerAngle(json.getAsJsonArray("rightArm"));
    Rotations leftArm = jsonToEulerAngle(json.getAsJsonArray("leftArm"));
    Rotations rightLeg = jsonToEulerAngle(json.getAsJsonArray("rightLeg"));
    Rotations leftLeg = jsonToEulerAngle(json.getAsJsonArray("leftLeg"));

    return new SavedPose(name, head, body, rightArm, leftArm, rightLeg, leftLeg);
  }

  private static Rotations jsonToEulerAngle(JsonArray json) {
    float pitch = json.get(0).getAsFloat();
    float yaw = json.get(1).getAsFloat();
    float roll = json.get(2).getAsFloat();

    return new Rotations(pitch, yaw, roll);
  }

  private static JsonArray eulerAngleToJson(Rotations angle) {
    JsonArray json = new JsonArray();
    json.add(angle.x());
    json.add(angle.y());
    json.add(angle.z());

    return json;
  }
}
