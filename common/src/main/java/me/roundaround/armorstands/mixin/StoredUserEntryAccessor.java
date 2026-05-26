package me.roundaround.armorstands.mixin;

import net.minecraft.server.players.StoredUserEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StoredUserEntry.class)
public interface StoredUserEntryAccessor<T> {
  @Invoker
  T invokeGetUser();
}
