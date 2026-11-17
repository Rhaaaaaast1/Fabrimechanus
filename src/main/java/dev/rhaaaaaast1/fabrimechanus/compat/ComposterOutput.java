package dev.rhaaaaaast1.fabrimechanus.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Static output selected by the KubeJS startup script. */
public final class ComposterOutput {
    private static volatile Item value = Items.BONE_MEAL;

    private ComposterOutput() {
    }

    public static Item get() {
        return value;
    }

    public static void setOutput(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(rl);
        if (item != Items.AIR) {
            value = item;
        }
    }

    public static String getOutput() {
        return BuiltInRegistries.ITEM.getKey(value).toString();
    }

    public static void reset() {
        value = Items.BONE_MEAL;
    }
}
