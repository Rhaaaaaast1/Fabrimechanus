package dev.rhaaaaaast1.fabrimechanus.mixin;

import dev.rhaaaaaast1.fabrimechanus.tinkers.TinkersCrossbowAi;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

/**
 * Keeps the vanilla "holding a crossbow" arm pose on pillagers that carry a Tinkers' Construct crossbow.
 *
 * <p>{@code Pillager#getArmPose} picks {@code CROSSBOW_HOLD} with {@code isHolding(is -> is.getItem() instanceof
 * CrossbowItem)} and otherwise falls back to the melee {@code ATTACKING} pose when aggressive. Widening that check to
 * accept Tinkers' crossbows restores the weapon pose; the charge pose already works because the vanilla goal toggles
 * {@code setChargingCrossbow} itself.</p>
 */
@Mixin(Pillager.class)
public abstract class PillagerPoseMixin {
    @Redirect(
            method = "getArmPose",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Pillager;isHolding(Ljava/util/function/Predicate;)Z"
            )
    )
    private boolean tinkersaddition$holdTinkersCrossbow(Pillager pillager, Predicate<ItemStack> predicate) {
        return pillager.isHolding(predicate) || TinkersCrossbowAi.isHoldingTinkersCrossbow(pillager);
    }
}
