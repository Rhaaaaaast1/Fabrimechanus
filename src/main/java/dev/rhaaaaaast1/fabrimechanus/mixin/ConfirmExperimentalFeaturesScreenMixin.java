package dev.rhaaaaaast1.fabrimechanus.mixin;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Skips the vanilla "experimental features" confirmation screen. */
@Mixin(ConfirmExperimentalFeaturesScreen.class)
public abstract class ConfirmExperimentalFeaturesScreenMixin {
    @Shadow
    @Final
    private BooleanConsumer callback;

    @Unique
    private boolean fabrimechanus$confirmed;

    @Inject(method = "init", at = @At("TAIL"))
    private void fabrimechanus$confirm(CallbackInfo ci) {
        if (!fabrimechanus$confirmed) {
            fabrimechanus$confirmed = true;
            callback.accept(true);
        }
    }
}
