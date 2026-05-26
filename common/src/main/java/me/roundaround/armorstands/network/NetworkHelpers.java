package me.roundaround.armorstands.network;

import net.minecraft.core.Rotations;
import net.minecraft.network.FriendlyByteBuf;

public class NetworkHelpers {
  public static Rotations readEulerAngle(FriendlyByteBuf buf) {
    return new Rotations(buf.readFloat(), buf.readFloat(), buf.readFloat());
  }

  public static void writeEulerAngle(FriendlyByteBuf buf, Rotations eulerAngle) {
    buf.writeFloat(eulerAngle.x());
    buf.writeFloat(eulerAngle.y());
    buf.writeFloat(eulerAngle.z());
  }
}
