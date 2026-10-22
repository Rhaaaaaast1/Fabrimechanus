package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableCrossbowItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;

/**
 * Glue between vanilla's crossbow AI and Tinkers' crossbows.
 *
 * <p>Vanilla's {@code RangedCrossbowAttackGoal} only accepts {@code CrossbowItem} as a weapon, so a pillager holding a
 * Tinkers' crossbow never draws it. These helpers let the vanilla state machine drive Tinkers' crossbows through the
 * regular charge/release pipeline; the goal itself is patched by
 * {@link dev.rhaaaaaast1.fabrimechanus.mixin.RangedCrossbowAttackGoalMixin}.</p>
 */
public final class TinkersCrossbowAi {
    private TinkersCrossbowAi() {
    }

    /** True when the mob holds a Tinkers' crossbow in either hand. */
    public static boolean isHoldingTinkersCrossbow(Mob mob) {
        return mob.getMainHandItem().getItem() instanceof ModifiableCrossbowItem
                || mob.getOffhandItem().getItem() instanceof ModifiableCrossbowItem;
    }

    /**
     * Fires a loaded Tinkers' crossbow, consuming the bolt stored in the tool's persistent data.
     *
     * @return true when a Tinkers' crossbow was fired, in which case the vanilla attack must be skipped
     */
    public static boolean fireLoadedCrossbow(Mob mob) {
        InteractionHand hand = holdingHand(mob);
        if (hand == null) {
            return false;
        }
        ItemStack stack = mob.getItemInHand(hand);
        ToolStack tool = ToolStack.from(stack);
        CompoundTag ammo = tool.getPersistentData().getCompound(ModifiableCrossbowItem.KEY_CROSSBOW_AMMO);
        if (ammo.isEmpty()) {
            return false;
        }
        ModifiableCrossbowItem.fireCrossbow(tool, mob, false, hand, ammo);
        return true;
    }

    @Nullable
    private static InteractionHand holdingHand(Mob mob) {
        if (mob.getMainHandItem().getItem() instanceof ModifiableCrossbowItem) {
            return InteractionHand.MAIN_HAND;
        }
        if (mob.getOffhandItem().getItem() instanceof ModifiableCrossbowItem) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }
}
