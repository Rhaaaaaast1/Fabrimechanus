package dev.rhaaaaaast1.fabrimechanus.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Reads ItemEntity's private pickup delay for cleanup preservation. */
@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {
    @Accessor("pickupDelay")
    int fabrimechanus$pickupDelay();
}
