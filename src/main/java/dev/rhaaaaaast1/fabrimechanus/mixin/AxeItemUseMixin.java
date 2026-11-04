package dev.rhaaaaaast1.fabrimechanus.mixin;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Gives every axe-tag item a visible 20-tick sawing animation. */
@Mixin(Item.class)
public abstract class AxeItemUseMixin {
    @Inject(method = "getUseDuration", at = @At("RETURN"), cancellable = true)
    private void fabrimechanus$axeUseDuration(ItemStack stack, LivingEntity entity,
                                              CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValueI() <= 0 && stack.is(ItemTags.AXES)) {
            cir.setReturnValue(20);
        }
    }

    @Inject(method = "getUseAnimation", at = @At("RETURN"), cancellable = true)
    private void fabrimechanus$axeUseAnimation(ItemStack stack, CallbackInfoReturnable<UseAnim> cir) {
        if (cir.getReturnValue() == UseAnim.NONE && stack.is(ItemTags.AXES)) {
            cir.setReturnValue(UseAnim.BOW);
        }
    }
}
