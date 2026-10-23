package dev.rhaaaaaast1.fabrimechanus.tinkers;

import dev.rhaaaaaast1.fabrimechanus.Fabrimechanus;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Tags used by the Tinkers' Construct integration. */
public final class FabriTags {
    /**
     * Vanilla gear that must not spawn on monsters any more.
     *
     * <p>Shipping this as a tag instead of a hardcoded list means packs can drop a single entry (for example to let a
     * modded monster keep one specific item) or add modded weapons to the strip list without touching code.</p>
     */
    public static final TagKey<Item> STRIPPED_MONSTER_GEAR = itemTag("stripped_monster_gear");

    private FabriTags() {
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Fabrimechanus.MODID, path));
    }
}
