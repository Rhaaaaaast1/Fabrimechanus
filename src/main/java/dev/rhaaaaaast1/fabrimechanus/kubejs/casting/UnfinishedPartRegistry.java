package dev.rhaaaaaast1.fabrimechanus.kubejs.casting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps a real Tinkers tool part to the blank/unfinished item that should be cast
 * in its place.
 *
 * <p>Default mapping is {@code tconstruct:tool_binding -> kubejs:unfinished_tool_binding}.
 * Override it with {@code config/fabrimechanus-unfinished-parts.json}:</p>
 *
 * <pre>{@code
 * {
 *   "tconstruct:tool_binding": "kubejs:unfinished_tool_binding",
 *   "tconstruct:tool_handle": "kubejs:unfinished_tool_handle"
 * }
 * }</pre>
 */
public final class UnfinishedPartRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String CONFIG_FILE = "fabrimechanus-unfinished-parts.json";
    private static final Map<ResourceLocation, ResourceLocation> REAL_TO_UNFINISHED = new HashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> UNFINISHED_TO_REAL = new HashMap<>();
    private static boolean loaded;

    private UnfinishedPartRegistry() {
    }

    /**
     * Convert a real casting result into the blank item + Material custom_data.
     * Returns EMPTY when no mapping exists or the result has no material.
     */
    public static ItemStack wrap(ItemStack real) {
        if (real == null || real.isEmpty()) {
            return ItemStack.EMPTY;
        }

        MaterialVariantId material = IMaterialItem.getMaterialFromStack(real);
        if (material == null || IMaterial.UNKNOWN_ID.equals(material.getId())) {
            return ItemStack.EMPTY;
        }

        Item unfinishedItem = unfinishedItem(real.getItem());
        if (unfinishedItem == null) {
            return ItemStack.EMPTY;
        }

        ItemStack unfinished = new ItemStack(unfinishedItem, real.getCount());
        CompoundTag tag = customData(real);
        tag.putString("Material", material.toString());
        unfinished.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return unfinished;
    }

    /**
     * Convert a blank unfinished stack back into the real part. Used by machine
     * integrations that want to complete the item in Java instead of KJS.
     */
    public static ItemStack complete(ItemStack unfinished) {
        if (unfinished == null || unfinished.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ensureLoaded();
        ResourceLocation unfinishedId = BuiltInRegistries.ITEM.getKey(unfinished.getItem());
        ResourceLocation realId = UNFINISHED_TO_REAL.get(unfinishedId);
        if (realId == null) {
            return ItemStack.EMPTY;
        }

        CompoundTag tag = customData(unfinished);
        String material = tag.getString("Material");
        if (material.isEmpty()) {
            return ItemStack.EMPTY;
        }

        Item realItem = BuiltInRegistries.ITEM.get(realId);
        if (realItem == Items.AIR) {
            return ItemStack.EMPTY;
        }

        ItemStack real = new ItemStack(realItem, unfinished.getCount());
        real.set(DataComponents.CUSTOM_DATA, CustomData.of(tag.copy()));
        return real;
    }

    public static boolean isUnfinished(Item item) {
        ensureLoaded();
        return UNFINISHED_TO_REAL.containsKey(BuiltInRegistries.ITEM.getKey(item));
    }

    private static Item unfinishedItem(Item real) {
        ensureLoaded();
        ResourceLocation realId = BuiltInRegistries.ITEM.getKey(real);
        ResourceLocation unfinishedId = REAL_TO_UNFINISHED.get(realId);
        if (unfinishedId == null) {
            // convention fallback: kubejs:unfinished_<path>
            unfinishedId = ResourceLocation.fromNamespaceAndPath("kubejs", "unfinished_" + realId.getPath());
        }
        Item item = BuiltInRegistries.ITEM.get(unfinishedId);
        return item == Items.AIR ? null : item;
    }

    private static CompoundTag customData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;

        Path path = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE);
        if (!Files.isRegularFile(path)) {
            return;
        }

        try {
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                ResourceLocation real = ResourceLocation.tryParse(entry.getKey());
                ResourceLocation unfinished = ResourceLocation.tryParse(entry.getValue().getAsString());
                if (real != null && unfinished != null) {
                    REAL_TO_UNFINISHED.put(real, unfinished);
                    UNFINISHED_TO_REAL.put(unfinished, real);
                }
            }
        } catch (IOException | RuntimeException exception) {
            LOGGER.error("[kubejs-toolkit] failed to read {}", path, exception);
        }
    }
}
