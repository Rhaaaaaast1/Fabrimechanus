package dev.rhaaaaaast1.fabrimechanus.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Forces custom-dimension/world-setting worlds to report a stable lifecycle. */
@Mixin(PrimaryLevelData.class)
public abstract class PrimaryLevelDataMixin {
    @Inject(method = "worldGenSettingsLifecycle", at = @At("HEAD"), cancellable = true)
    private void fabrimechanus$forceStableLifecycle(CallbackInfoReturnable<Lifecycle> cir) {
        cir.setReturnValue(Lifecycle.stable());
    }
}
