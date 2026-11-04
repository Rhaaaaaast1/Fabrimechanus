package dev.rhaaaaaast1.fabrimechanus.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Skips the lifecycle-based experimental/deprecated world warning on creation. */
@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {
    @Inject(method = "confirmWorldCreation", at = @At("HEAD"), cancellable = true)
    private static void fabrimechanus$skipWarning(Minecraft minecraft, CreateWorldScreen screen, Lifecycle lifecycle,
                                                   Runnable onProceed, boolean experimental, CallbackInfo ci) {
        if (experimental || lifecycle == Lifecycle.experimental()) {
            onProceed.run();
            ci.cancel();
        }
    }
}
