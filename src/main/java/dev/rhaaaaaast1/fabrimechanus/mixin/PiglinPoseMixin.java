package dev.rhaaaaaast1.fabrimechanus.mixin;

import dev.rhaaaaaast1.fabrimechanus.tinkers.TinkersCrossbowAi;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

/**
 * Keeps the vanilla "holding a crossbow" arm pose on piglins that carry a Tinkers' Construct crossbow.
 *
 * <p>{@code Piglin#getArmPose} only returns {@code CROSSBOW_HOLD} when it is aggressive and
 * {@code isHolding(is -> is.getItem() instanceof CrossbowItem)}; with a Tinkers' crossbow the piglin would show the
 * plain {@code DEFAULT} pose while aiming.</p>
 */
@Mixin(Piglin.class)
public abstract class PiglinPoseMixin {
    @Redirect(
            method = "getArmPose",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/piglin/Piglin;isHolding(Ljava/util/function/Predicate;)Z"
            )
    )
    private boolean tinkersaddition$holdTinkersCrossbow(Piglin piglin, Predicate<ItemStack> predicate) {
        return piglin.isHolding(predicate) || TinkersCrossbowAi.isHoldingTinkersCrossbow(piglin);
    }
}
