package dev.rhaaaaaast1.fabrimechanus.kubejs.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Identity-based recipe id lookup. {@link Recipe} itself does not carry its id.
 */
public final class RecipeIndex {
    private static final Map<Recipe<?>, ResourceLocation> IDS = new IdentityHashMap<>();

    private RecipeIndex() {
    }

    public static synchronized void rebuild(RecipeManager manager) {
        IDS.clear();
        for (RecipeHolder<?> holder : manager.getRecipes()) {
            IDS.put(holder.value(), holder.id());
        }
    }

    public static synchronized String id(Recipe<?> recipe) {
        ResourceLocation id = IDS.get(recipe);
        return id == null ? "unknown" : id.toString();
    }

    public static synchronized void clear() {
        IDS.clear();
    }
}
