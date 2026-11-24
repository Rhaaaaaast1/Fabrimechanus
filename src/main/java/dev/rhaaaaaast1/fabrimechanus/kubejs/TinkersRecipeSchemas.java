package dev.rhaaaaaast1.fabrimechanus.kubejs;

import java.util.List;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.SizedFluidIngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ListRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/** Recipe schemas for the Tinkers' Construct JSON recipe types supported by this mod. */
public final class TinkersRecipeSchemas {
    private static final RecipeKey<SizedFluidIngredient> FLUID = SizedFluidIngredientComponent.FLAT
            .key("fluid", ComponentRole.INPUT).defaultOptional();
    private static final RecipeKey<SizedFluidIngredient> FLUID_RESULT = SizedFluidIngredientComponent.FLAT
            .key("result", ComponentRole.OUTPUT);
    private static final ListRecipeComponent<SizedFluidIngredient> FLUID_LIST =
            SizedFluidIngredientComponent.FLAT.instance().asList();
    private static final RecipeKey<List<SizedFluidIngredient>> FLUID_INPUTS = FLUID_LIST
            .key("inputs", ComponentRole.INPUT);
    private static final RecipeKey<List<SizedFluidIngredient>> BYPRODUCTS = FLUID_LIST
            .key("byproducts", ComponentRole.OUTPUT).optional(List.of());
    private static final RecipeKey<Ingredient> CAST = IngredientComponent.INGREDIENT
            .key("cast", ComponentRole.INPUT).optional(Ingredient.EMPTY);
    private static final RecipeKey<Boolean> CAST_CONSUMED = BooleanComponent.BOOLEAN
            .key("cast_consumed", ComponentRole.OTHER).optional(false);
    private static final RecipeKey<Boolean> SWITCH_SLOTS = BooleanComponent.BOOLEAN
            .key("switch_slots", ComponentRole.OTHER).optional(false);
    private static final RecipeKey<Ingredient> BOTTLE = IngredientComponent.INGREDIENT
            .key("bottle", ComponentRole.INPUT).optional(Ingredient.EMPTY);
    private static final RecipeKey<String> GROUP = StringComponent.STRING
            .key("group", ComponentRole.OTHER).optional("");
    private static final RecipeKey<String> CONTAINER = StringComponent.STRING
            .key("container", ComponentRole.OTHER);
    private static final RecipeKey<Ingredient> PATTERN = IngredientComponent.INGREDIENT
            .key("pattern", ComponentRole.INPUT).optional(Ingredient.EMPTY);
    private static final RecipeKey<Ingredient> MATERIAL = IngredientComponent.INGREDIENT
            .key("material", ComponentRole.INPUT);
    private static final RecipeKey<Ingredient> INPUT = IngredientComponent.INGREDIENT
            .key("ingredient", ComponentRole.INPUT);
    private static final RecipeKey<String> RESULT = StringComponent.STRING
            .key("result", ComponentRole.OUTPUT);
    private static final RecipeKey<Integer> COOLING_TIME = NumberComponent.INT
            .key("cooling_time", ComponentRole.OTHER).optional(20);
    private static final RecipeKey<Double> COOLING_TIME_DOUBLE = NumberComponent.DOUBLE
            .key("cooling_time", ComponentRole.OTHER).optional(1.0);
    private static final RecipeKey<Integer> FLUID_AMOUNT = NumberComponent.INT
            .key("fluid_amount", ComponentRole.OTHER).optional(100);
    private static final RecipeKey<Integer> DURATION = NumberComponent.INT
            .key("duration", ComponentRole.OTHER).optional(100);
    private static final RecipeKey<Integer> RATE = NumberComponent.INT
            .key("rate", ComponentRole.OTHER);
    private static final RecipeKey<Double> TEMPERATURE = NumberComponent.DOUBLE
            .key("temperature", ComponentRole.OTHER);
    private static final RecipeKey<Double> TIME = NumberComponent.DOUBLE
            .key("time", ComponentRole.OTHER);

    public static final RecipeSchema CASTING_TABLE =
            new RecipeSchema(RESULT, FLUID, CAST, CAST_CONSUMED, COOLING_TIME, SWITCH_SLOTS);
    public static final RecipeSchema CASTING_BASIN =
            new RecipeSchema(RESULT, FLUID, CAST, CAST_CONSUMED, COOLING_TIME, SWITCH_SLOTS);
    public static final RecipeSchema CASTING_TABLE_POTION =
            new RecipeSchema(RESULT, BOTTLE, FLUID, COOLING_TIME);
    public static final RecipeSchema TABLE_DUPLICATION =
            new RecipeSchema(CAST, FLUID, COOLING_TIME_DOUBLE);
    public static final RecipeSchema TABLE_FILLING =
            new RecipeSchema(CONTAINER, FLUID_AMOUNT, FLUID);
    public static final RecipeSchema BASIN_FILLING =
            new RecipeSchema(CONTAINER, FLUID_AMOUNT, FLUID);
    public static final RecipeSchema MOLDING_TABLE =
            new RecipeSchema(RESULT, MATERIAL, PATTERN);
    // 字段顺序与 MeltingRecipe 的 LOADER 对齐,group 与 byproducts 是它的默认字段.
    // 两个可选字段放在末尾,不改变现有 melting(fluid, ingredient, temperature, time) 的调用参数.
    public static final RecipeSchema MELTING =
            new RecipeSchema(FLUID_RESULT, INPUT, TEMPERATURE, TIME, GROUP, BYPRODUCTS);
    public static final RecipeSchema MELTING_FUEL =
            new RecipeSchema(RATE, TEMPERATURE, FLUID, DURATION);
    public static final RecipeSchema ALLOY =
            new RecipeSchema(FLUID_RESULT, FLUID_INPUTS, TEMPERATURE);

    private TinkersRecipeSchemas() {
    }
}
