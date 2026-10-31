package dev.rhaaaaaast1.fabrimechanus.primitive;

import dev.rhaaaaaast1.fabrimechanus.tinkers.TinkersIntegration;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.util.ArrayList;
import java.util.List;

/** Removes vanilla recipes for the replaced gear and the old 4-plank recipes. */
public final class PrimitiveRecipes {
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    private PrimitiveRecipes() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PrimitiveRecipes::onServerAboutToStart);
        NeoForge.EVENT_BUS.addListener(PrimitiveRecipes::onTooltip);
    }

    private static void onServerAboutToStart(ServerAboutToStartEvent event) {
        if (!TinkersIntegration.isLoaded()) {
            return;
        }
        MinecraftServer server = event.getServer();
        RecipeManager manager = server.getRecipeManager();
        List<RecipeHolder<?>> kept = new ArrayList<>();
        int removed = 0;

        for (RecipeHolder<?> holder : manager.getRecipes()) {
            ItemStack result = holder.value().getResultItem(server.registryAccess());
            ResourceLocation id = holder.id();
            boolean oldPlanks = id.getNamespace().equals("minecraft")
                    && id.getPath().endsWith("_planks")
                    && result.is(ItemTags.PLANKS);
            boolean disabledGear = !result.isEmpty() && PrimitiveTargets.contains(result.getItem());

            if (oldPlanks || disabledGear) {
                removed++;
            } else {
                kept.add(holder);
            }
        }

        if (removed > 0) {
            manager.replaceRecipes(kept);
            LOGGER.info("[fabrimechanus] removed {} disabled vanilla recipes", removed);
        }
    }

    private static void onTooltip(ItemTooltipEvent event) {
        if (PrimitiveTargets.contains(event.getItemStack().getItem())) {
            event.getToolTip().add(Component.translatable("tooltip.fabrimechanus.disabled")
                    .withStyle(ChatFormatting.RED));
        }
    }
}
