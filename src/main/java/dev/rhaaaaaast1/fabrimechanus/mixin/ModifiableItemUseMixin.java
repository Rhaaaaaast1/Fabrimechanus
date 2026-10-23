package dev.rhaaaaaast1.fabrimechanus.mixin;

import dev.rhaaaaaast1.fabrimechanus.tinkers.TinkersSpearAi;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;

/**
 * Gives Tinkers' spears a usable wind up for entities that never call {@code Item#use}.
 *
 * <p>A tool reports its use duration and pose from the active interaction modifier, and that modifier is only recorded by
 * {@code ModifiableItem#use}: a mob calling {@code LivingEntity#startUsingItem} therefore gets a duration of zero and the
 * {@code NONE} pose, which means no charge and no animation. Falling back to the spear throw for stacks that have the
 * throwing modifier restores both, so a drowned can raise the spear like a trident before it throws.</p>
 */
@Mixin(ModifiableItem.class)
public abstract class ModifiableItemUseMixin {
    @Inject(method = "getUseDuration(Lnet/minecraft/world/item/ItemStack;)I", at = @At("RETURN"), cancellable = true)
    private void tinkersaddition$spearUseDuration(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValueI() == 0 && TinkersSpearAi.isSpear(stack)) {
            cir.setReturnValue(72000);
        }
    }

    @Inject(method = "getUseAnimation(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/UseAnim;", at = @At("RETURN"), cancellable = true)
    private void tinkersaddition$spearUseAnimation(ItemStack stack, CallbackInfoReturnable<UseAnim> cir) {
        if (cir.getReturnValue() == UseAnim.NONE && TinkersSpearAi.isSpear(stack)) {
            cir.setReturnValue(UseAnim.SPEAR);
        }
    }
}
