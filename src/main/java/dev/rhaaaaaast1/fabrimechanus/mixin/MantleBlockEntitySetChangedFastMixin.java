package dev.rhaaaaaast1.fabrimechanus.mixin;

import dev.rhaaaaaast1.fabrimechanus.kubejs.block.BlockEntitySlotTracker;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optional Mantle integration. TConstruct casting tables inherit from
 * Mantle's InventoryBlockEntity, which calls setChangedFast() instead of
 * vanilla BlockEntity#setChanged(). Without this hook the KubeJS slot event
 * would never see casting table output changes.
 */
@Mixin(targets = "slimeknights.mantle.block.entity.MantleBlockEntity", remap = false)
public abstract class MantleBlockEntitySetChangedFastMixin {
    @Inject(method = "setChangedFast", at = @At("TAIL"))
    private void fabrimechanus$afterSetChangedFast(CallbackInfo ci) {
        BlockEntitySlotTracker.onChanged((BlockEntity) (Object) this);
    }
}
