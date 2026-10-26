package dev.rhaaaaaast1.fabrimechanus.mixin;

import com.mojang.logging.LogUtils;
import dev.rhaaaaaast1.fabrimechanus.kubejs.casting.UnfinishedPartRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.casting.ICastingContainer;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingRecipe;

/**
 * Replaces a real Tinkers part produced by a material casting recipe with the
 * blank/unfinished item, preserving custom_data (including "Material").
 *
 * <p>This happens before the stack reaches the casting table output slot, so
 * hoppers, spouts and other automation see the unfinished item.</p>
 */
@Mixin(MaterialCastingRecipe.class)
public abstract class MaterialCastingOutputMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(
            method = "assemble(Lslimeknights/tconstruct/library/recipe/casting/ICastingContainer;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void fabrimechanus$replaceCastingResult(
            ICastingContainer input,
            HolderLookup.Provider registries,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        ItemStack real = cir.getReturnValue();
        if (real == null || real.isEmpty()) {
            return;
        }

        // Only gate the casting table; leave the basin alone.
        if (((Recipe<?>) (Object) this).getType() != TinkerRecipeTypes.CASTING_TABLE.get()) {
            return;
        }

        ItemStack unfinished = UnfinishedPartRegistry.wrap(real);
        if (unfinished.isEmpty()) {
            return;
        }

        cir.setReturnValue(unfinished);
        LOGGER.info("[kubejs-toolkit] casting result {} -> {}",
                real.getItem(), unfinished.getItem());
    }
}
