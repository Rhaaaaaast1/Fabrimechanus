package dev.rhaaaaaast1.fabrimechanus.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import net.neoforged.fml.ModList;

/** Recipe-side KubeJS integration for Tinkers' Construct. */
public final class FabriKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        if (!ModList.get().isLoaded("tconstruct")) {
            return;
        }
        RecipeNamespace namespace = registry.namespace("tconstruct");
        namespace.register("casting_table", TinkersRecipeSchemas.CASTING_TABLE);
        namespace.register("casting_basin", TinkersRecipeSchemas.CASTING_BASIN);
        namespace.register("casting_table_potion", TinkersRecipeSchemas.CASTING_TABLE_POTION);
        namespace.register("table_duplication", TinkersRecipeSchemas.TABLE_DUPLICATION);
        namespace.register("table_filling", TinkersRecipeSchemas.TABLE_FILLING);
        namespace.register("basin_filling", TinkersRecipeSchemas.BASIN_FILLING);
        namespace.register("molding_table", TinkersRecipeSchemas.MOLDING_TABLE);
        namespace.register("melting", TinkersRecipeSchemas.MELTING);
        namespace.register("melting_fuel", TinkersRecipeSchemas.MELTING_FUEL);
        namespace.register("alloy", TinkersRecipeSchemas.ALLOY);
    }
}
