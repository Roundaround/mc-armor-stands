package me.roundaround.armorstands.mixin;

import me.roundaround.armorstands.client.gui.screen.PassesEventsThrough;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NeoForge gives {@link ToggleKeyMapping} (e.g. the sneak key) an {@code isDown()} override gated by
 * a key conflict context, so it reports not-down whenever a screen is open. The common
 * {@code KeyMappingMixin} only patches the base {@code KeyMapping.isDown()}, which this override
 * never calls, so the sneak key alone stays gated while our pass-through screens are open.
 *
 * <p>Mirror {@code KeyMappingMixin} here: if the held state is set and one of our pass-through
 * screens is open, report down. The raw {@code isDown} flag is read via {@link KeyMappingAccessor}
 * rather than a {@code @Shadow} — the field is declared on the {@code KeyMapping} superclass, and
 * shadowing an inherited field fails to resolve under the FML mixin service. This lives in the
 * loader source set because vanilla/Fabric have no {@code isDown()} override to target; see the
 * Forge copy for the identical fix on that loader.
 */
@Mixin(ToggleKeyMapping.class)
public class ToggleKeyMappingMixin {
  @Inject(method = "isDown()Z", at = @At("HEAD"), cancellable = true)
  private void bypassConflictContextForPassthrough(CallbackInfoReturnable<Boolean> info) {
    if (((KeyMappingAccessor) (Object) this).getIsDown()
        && Minecraft.getInstance().gui.screen() instanceof PassesEventsThrough pst
        && pst.shouldPassEvents()) {
      info.setReturnValue(true);
    }
  }
}
