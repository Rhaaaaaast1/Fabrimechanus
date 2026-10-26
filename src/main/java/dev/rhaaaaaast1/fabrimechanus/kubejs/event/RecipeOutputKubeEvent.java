package dev.rhaaaaaast1.fabrimechanus.kubejs.event;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

/**
 * Fired from the hooked {@code Recipe#assemble} implementations.
 *
 * <p>Scripts may replace {@link #output} directly or call {@link #setOutput(ItemStack)}.
 * The event is intentionally mutable and has no return value.</p>
 */
public class RecipeOutputKubeEvent implements KubeEvent {
    public final String recipeId;
    public final String recipeType;
    public final List<ItemStack> inputs;
    public final Recipe<?> recipe;

    public ItemStack output;

    public RecipeOutputKubeEvent(String recipeId, String recipeType, List<ItemStack> inputs, Recipe<?> recipe, ItemStack output) {
        this.recipeId = recipeId;
        this.recipeType = recipeType;
        this.inputs = List.copyOf(inputs);
        this.recipe = recipe;
        this.output = output == null ? ItemStack.EMPTY : output.copy();
    }

    public void setOutput(ItemStack stack) {
        this.output = stack == null ? ItemStack.EMPTY : stack;
    }

    public ItemStack getOutput() {
        return output;
    }
}
