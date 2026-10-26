package dev.rhaaaaaast1.fabrimechanus.mixin;

import dev.rhaaaaaast1.fabrimechanus.kubejs.recipe.RecipeIndex;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerIndexMixin {
    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("TAIL")
    )
    private void fabrimechanus$indexRecipes(CallbackInfo ci) {
        RecipeIndex.rebuild((RecipeManager) (Object) this);
    }

    @Inject(method = "replaceRecipes", at = @At("TAIL"))
    private void fabrimechanus$indexReplacedRecipes(CallbackInfo ci) {
        RecipeIndex.rebuild((RecipeManager) (Object) this);
    }
}
