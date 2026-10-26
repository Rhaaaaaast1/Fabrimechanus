package dev.rhaaaaaast1.fabrimechanus.mixin;

import dev.rhaaaaaast1.fabrimechanus.kubejs.block.BlockEntitySlotTracker;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntitySetChangedMixin {
    @Inject(method = "setChanged", at = @At("TAIL"))
    private void fabrimechanus$afterSetChanged(CallbackInfo ci) {
        BlockEntitySlotTracker.onChanged((BlockEntity) (Object) this);
    }
}
