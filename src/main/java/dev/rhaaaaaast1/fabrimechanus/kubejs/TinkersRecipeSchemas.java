package dev.rhaaaaaast1.fabrimechanus.kubejs;

import java.util.List;

import com.mojang.datafixers.util.Either;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.SizedFluidIngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ListRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.IntBounds;

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
    // 副产物允许为空表,列表组件默认下限非 0,需要显式放宽边界,否则空表会被判为非法.
    private static final RecipeKey<List<SizedFluidIngredient>> BYPRODUCTS = FLUID_LIST
            .withBounds(IntBounds.OPTIONAL)
            .key("byproducts", ComponentRole.OUTPUT).optional(List.of());
    // 浇铸台与浇铸盆的 cast 在匠魂里是 ALLOW_EMPTY 的默认字段, 无模具配方里整个键都不存在.
    // 缺省值必须是 null 而不是 Ingredient.EMPTY, 否则校验阶段会被 INGREDIENT 组件判为空值非法.
    private static final RecipeKey<Ingredient> CAST_OPTIONAL = IngredientComponent.OPTIONAL_INGREDIENT
            .key("cast", ComponentRole.INPUT).defaultOptional();
    // 模具复制依赖模具本身, 匠魂对这条配方用 DISALLOW_EMPTY 的必填字段, 这里保持必填以尽早暴露脚本错误.
    private static final RecipeKey<Ingredient> CAST = IngredientComponent.INGREDIENT
            .key("cast", ComponentRole.INPUT);
    private static final RecipeKey<Boolean> CAST_CONSUMED = BooleanComponent.BOOLEAN
            .key("cast_consumed", ComponentRole.OTHER).optional(false);
    private static final RecipeKey<Boolean> SWITCH_SLOTS = BooleanComponent.BOOLEAN
            .key("switch_slots", ComponentRole.OTHER).optional(false);
    // 药水浇铸的 bottle 是 DISALLOW_EMPTY 的必填原料, 不能给空默认值.
    private static final RecipeKey<Ingredient> BOTTLE = IngredientComponent.INGREDIENT
            .key("bottle", ComponentRole.INPUT);
    // group 默认空串,普通的 STRING 组件会把空串判为非法,必须用允许空的 OPTIONAL_STRING.
    private static final RecipeKey<String> GROUP = StringComponent.OPTIONAL_STRING
            .key("group", ComponentRole.OTHER).optional("");
    private static final RecipeKey<String> CONTAINER = StringComponent.STRING
            .key("container", ComponentRole.OTHER);
    // molding 的 pattern 是 ALLOW_EMPTY 的默认字段, 缺失时不写键, 让匠魂用自己的空原料默认值.
    private static final RecipeKey<Ingredient> PATTERN = IngredientComponent.OPTIONAL_INGREDIENT
            .key("pattern", ComponentRole.INPUT).defaultOptional();
    private static final RecipeKey<Boolean> PATTERN_CONSUMED = BooleanComponent.BOOLEAN
            .key("pattern_consumed", ComponentRole.OTHER).optional(false);
    private static final RecipeKey<Ingredient> MATERIAL = IngredientComponent.INGREDIENT
            .key("material", ComponentRole.INPUT);
    private static final RecipeKey<Ingredient> INPUT = IngredientComponent.INGREDIENT
            .key("ingredient", ComponentRole.INPUT);
    private static final RecipeKey<String> RESULT = StringComponent.STRING
            .key("result", ComponentRole.OUTPUT);
    // 浇铸与模具配方的 result 是匠魂的 ItemOutput, 既可能是物品 id 字符串, 也可能是 {"tag": "..."} 标签对象.
    // 单用 STRING 会让全部标签形态配方解析失败并回退原版, 这里用字符串优先的 Either 同时容纳两者.
    private static final RecipeComponent<Either<String, Ingredient>> ITEM_OUTPUT_COMPONENT =
            StringComponent.STRING.instance().or(IngredientComponent.INGREDIENT.instance());
    private static final RecipeKey<Either<String, Ingredient>> ITEM_OUTPUT = ITEM_OUTPUT_COMPONENT
            .key("result", ComponentRole.OUTPUT);
    private static final RecipeKey<Integer> COOLING_TIME = NumberComponent.INT
            .key("cooling_time", ComponentRole.OTHER).optional(20);
    // 药水浇铸的冷却时间在匠魂里默认 5 tick, 与普通浇铸的 20 不同.
    private static final RecipeKey<Integer> COOLING_TIME_POTION = NumberComponent.INT
            .key("cooling_time", ComponentRole.OTHER).optional(5);
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

    // 字段顺序对齐匠魂的 LOADER: group, cast, fluid, result, cooling_time, cast_consumed, switch_slots.
    // group 追加在末尾而不是首位, 既不改动既有位置参数, 又能正确读入带分组的配方.
    public static final RecipeSchema CASTING_TABLE =
            new RecipeSchema(ITEM_OUTPUT, FLUID, CAST_OPTIONAL, CAST_CONSUMED, COOLING_TIME, SWITCH_SLOTS, GROUP);
    public static final RecipeSchema CASTING_BASIN =
            new RecipeSchema(ITEM_OUTPUT, FLUID, CAST_OPTIONAL, CAST_CONSUMED, COOLING_TIME, SWITCH_SLOTS, GROUP);
    public static final RecipeSchema CASTING_TABLE_POTION =
            new RecipeSchema(RESULT, BOTTLE, FLUID, COOLING_TIME_POTION, GROUP);
    public static final RecipeSchema CASTING_BASIN_POTION =
            new RecipeSchema(RESULT, BOTTLE, FLUID, COOLING_TIME_POTION, GROUP);
    public static final RecipeSchema TABLE_DUPLICATION =
            new RecipeSchema(CAST, FLUID, COOLING_TIME_DOUBLE);
    public static final RecipeSchema BASIN_DUPLICATION =
            new RecipeSchema(CAST, FLUID, COOLING_TIME_DOUBLE);
    // 灌注配方没有 fluid 字段, 液体量由容器自带的液体决定; 多写 fluid 会生成匠魂不认识的键.
    public static final RecipeSchema TABLE_FILLING =
            new RecipeSchema(CONTAINER, FLUID_AMOUNT, GROUP);
    public static final RecipeSchema BASIN_FILLING =
            new RecipeSchema(CONTAINER, FLUID_AMOUNT, GROUP);
    public static final RecipeSchema MOLDING_TABLE =
            new RecipeSchema(ITEM_OUTPUT, MATERIAL, PATTERN, PATTERN_CONSUMED);
    public static final RecipeSchema MOLDING_BASIN =
            new RecipeSchema(ITEM_OUTPUT, MATERIAL, PATTERN, PATTERN_CONSUMED);
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
