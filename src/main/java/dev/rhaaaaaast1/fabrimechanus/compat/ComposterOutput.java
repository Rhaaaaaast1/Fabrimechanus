package dev.rhaaaaaast1.fabrimechanus.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Static output selected by the KubeJS startup script. */
public final class ComposterOutput {
    private static volatile String outputId = "minecraft:bone_meal";

    private ComposterOutput() {
    }

    public static Item get() {
        ResourceLocation rl = ResourceLocation.tryParse(outputId);
        if (rl == null) {
            return Items.BONE_MEAL;
        }
        Item item = BuiltInRegistries.ITEM.get(rl);
        return item == Items.AIR ? Items.BONE_MEAL : item;
    }

    public static void setOutput(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl != null) {
            outputId = id;
        }
    }

    public static String getOutput() {
        return outputId;
    }

    public static void reset() {
        outputId = "minecraft:bone_meal";
    }
}
