package dev.rhaaaaaast1.fabrimechanus.tinkers;

import dev.rhaaaaaast1.fabrimechanus.Fabrimechanus;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Tags used by the Tinkers' Construct integration. */
public final class FabriTags {
    /**
     * Vanilla gear that must not spawn on monsters any more.
     *
     * <p>Shipping this as a tag instead of a hardcoded list means packs can drop a single entry (for example to let a
     * modded monster keep one specific item) or add modded weapons to the strip list without touching code.</p>
     */
    public static final TagKey<Item> STRIPPED_MONSTER_GEAR = itemTag("stripped_monster_gear");

    /** Blocks a loose rock can rest on. Kept in data so packs can extend it. */
    public static final TagKey<Block> PEBBLE_SUPPORT = blockTag("pebble_support");
    /** Blocks a flint pebble can rest on. */
    public static final TagKey<Block> FLINT_SUPPORT = blockTag("flint_support");
    /** Blocks that knap a pebble into a shard. */
    public static final TagKey<Block> PEBBLE_KNAPPING_BLOCKS = blockTag("pebble_knapping_blocks");
    /** Blocks that can be cut for plant fiber. */
    public static final TagKey<Block> PLANT_FIBER_SOURCES = blockTag("plant_fiber_sources");
    /** Wooden blocks that require an axe to break. */
    public static final TagKey<Block> REQUIRES_AXE = blockTag("requires_axe");

    private FabriTags() {
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Fabrimechanus.MODID, path));
    }

    private static TagKey<Block> blockTag(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Fabrimechanus.MODID, path));
    }
}
