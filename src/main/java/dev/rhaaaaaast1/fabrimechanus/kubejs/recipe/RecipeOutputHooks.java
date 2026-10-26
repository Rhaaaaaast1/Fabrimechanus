package dev.rhaaaaaast1.fabrimechanus.kubejs.recipe;

import dev.latvian.mods.kubejs.KubeJS;
import dev.rhaaaaaast1.fabrimechanus.kubejs.KubeJSToolkitEvents;
import dev.rhaaaaaast1.fabrimechanus.kubejs.event.RecipeOutputKubeEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared entry point used by the concrete {@code assemble} mixins.
 */
public final class RecipeOutputHooks {
    private static final ThreadLocal<Boolean> FIRING = ThreadLocal.withInitial(() -> false);

    private RecipeOutputHooks() {
    }

    public static ItemStack fire(Recipe<?> recipe, RecipeInput input, ItemStack output) {
        if (!ModList.get().isLoaded("kubejs") || !KubeJSToolkitEvents.hasRecipeListeners()) {
            return output;
        }
        if (FIRING.get()) {
            return output;
        }

        FIRING.set(true);
        try {
            List<ItemStack> inputs = new ArrayList<>(input.size());
            for (int i = 0; i < input.size(); i++) {
                inputs.add(input.getItem(i).copy());
            }

            ResourceLocation typeId = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
            String recipeType = typeId == null ? recipe.getType().toString() : typeId.toString();
            String recipeId = RecipeIndex.id(recipe);

            RecipeOutputKubeEvent event = new RecipeOutputKubeEvent(
                    recipeId,
                    recipeType,
                    inputs,
                    recipe,
                    output
            );

            KubeJSToolkitEvents.RECIPE_OUTPUT.post(event);

            KubeJS.LOGGER.info("[kubejs-toolkit] recipeOutput {} ({}) -> {}",
                    recipeId, recipeType, event.output.getItem());

            return event.output == null ? ItemStack.EMPTY : event.output.copy();
        } catch (Throwable throwable) {
            KubeJS.LOGGER.error("[kubejs-toolkit] recipeOutput failed for {}", recipe, throwable);
            return output;
        } finally {
            FIRING.set(false);
        }
    }
}
