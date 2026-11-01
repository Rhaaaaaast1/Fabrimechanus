package dev.rhaaaaaast1.fabrimechanus.primitive;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

/** Crude axe item. Plank sawing is handled by the shared axe interaction event. */
public class CrudeAxeItem extends AxeItem {
    public CrudeAxeItem(Tier tier, Item.Properties properties) {
        super(tier, properties);
    }
}
