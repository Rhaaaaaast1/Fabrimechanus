package dev.rhaaaaaast1.fabrimechanus.primitive;

import dev.rhaaaaaast1.fabrimechanus.Fabrimechanus;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Optional JEI information pages for the primitive items. */
@JeiPlugin
public final class PrimitiveJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(Fabrimechanus.MODID, "primitive");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(PrimitiveRegistry.SMALL_PEBBLE.get().asItem(),
                Component.translatable("jei.fabrimechanus.small_pebble"));
        registration.addIngredientInfo(PrimitiveRegistry.SMALL_FLINT.get().asItem(),
                Component.translatable("jei.fabrimechanus.small_flint"));
        registration.addIngredientInfo(PrimitiveRegistry.STONE_SHARD.get(),
                Component.translatable("jei.fabrimechanus.stone_shard"));
        registration.addIngredientInfo(PrimitiveRegistry.FLINT_SHARD.get(),
                Component.translatable("jei.fabrimechanus.flint_shard"));
        registration.addIngredientInfo(PrimitiveRegistry.PLANT_FIBER.get(),
                Component.translatable("jei.fabrimechanus.plant_fiber"));
        registration.addIngredientInfo(PrimitiveRegistry.PLANT_CORD.get(),
                Component.translatable("jei.fabrimechanus.plant_cord"));
        registration.addIngredientInfo(PrimitiveRegistry.STONE_AXE.get(),
                Component.translatable("jei.fabrimechanus.stone_axe"));
        registration.addIngredientInfo(PrimitiveRegistry.FLINT_AXE.get(),
                Component.translatable("jei.fabrimechanus.flint_axe"));
    }
}
