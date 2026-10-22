package dev.rhaaaaaast1.fabrimechanus.mixin;

import dev.rhaaaaaast1.fabrimechanus.tinkers.TinkersCrossbowAi;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

/**
 * Teaches the brain based crossbow behavior about Tinkers' Construct crossbows.
 *
 * <p>Piglins (and any other brain driven mob) do not use {@code RangedCrossbowAttackGoal} at all: their shooting is a
 * brain activity, {@code CrossbowAttack}. It is patched here for the same three reasons the goal is patched in
 * {@link RangedCrossbowAttackGoalMixin}: the start condition only accepts {@code CrossbowItem}, the charging hand is
 * looked up with a {@code CrossbowItem} predicate, and the shot goes through {@code CrossbowAttackMob#performCrossbowAttack}.</p>
 */
@Mixin(targets = "net.minecraft.world.entity.ai.behavior.CrossbowAttack")
public abstract class CrossbowAttackBehaviorMixin {
    @Redirect(
            method = "checkExtraStartConditions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;isHolding(Ljava/util/function/Predicate;)Z"
            )
    )
    private boolean tinkersaddition$acceptTinkersCrossbow(Mob mob, Predicate<ItemStack> predicate) {
        return mob.isHolding(predicate) || TinkersCrossbowAi.isHoldingTinkersCrossbow(mob);
    }

    @Redirect(
            method = "crossbowAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getWeaponHoldingHand(Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Predicate;)Lnet/minecraft/world/InteractionHand;"
            )
    )
    private static InteractionHand tinkersaddition$chargingHand(LivingEntity shooter, Predicate<Item> predicate) {
        InteractionHand tinkersHand = TinkersCrossbowAi.holdingHand(shooter);
        return tinkersHand != null ? tinkersHand : ProjectileUtil.getWeaponHoldingHand(shooter, predicate);
    }

    @Redirect(
            method = "crossbowAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/RangedAttackMob;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V"
            )
    )
    private void tinkersaddition$fireTinkersCrossbow(RangedAttackMob attackMob, LivingEntity target, float velocity) {
        if (attackMob instanceof Mob mob && TinkersCrossbowAi.fireLoadedCrossbow(mob)) {
            return;
        }
        attackMob.performRangedAttack(target, velocity);
    }
}
