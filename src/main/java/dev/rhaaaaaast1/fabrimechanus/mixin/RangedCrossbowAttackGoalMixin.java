package dev.rhaaaaaast1.fabrimechanus.mixin;

import dev.rhaaaaaast1.fabrimechanus.tinkers.TinkersCrossbowAi;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

/**
 * Teaches vanilla's crossbow AI about Tinkers' Construct crossbows.
 *
 * <p>{@code RangedCrossbowAttackGoal} is shared by every mob using a crossbow (pillagers, piglins, ...), so patching the
 * goal instead of the individual mobs covers all of them, including modded ones. Three things break with a Tinkers'
 * crossbow:</p>
 * <ol>
 *     <li>{@code isHoldingCrossbow} checks {@code instanceof CrossbowItem}, so the goal never starts.</li>
 *     <li>the weapon hand lookup uses the same predicate, so the mob would draw the empty off hand.</li>
 *     <li>the final attack goes through vanilla shooting code that cannot fire a Tinkers' crossbow.</li>
 * </ol>
 */
@Mixin(RangedCrossbowAttackGoal.class)
public abstract class RangedCrossbowAttackGoalMixin {
    @Shadow
    @Final
    private Mob mob;

    @Inject(method = "isHoldingCrossbow", at = @At("RETURN"), cancellable = true)
    private void fabrimechanus$acceptTinkersCrossbow(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && TinkersCrossbowAi.isHoldingTinkersCrossbow(this.mob)) {
            cir.setReturnValue(true);
        }
    }

    @ModifyArg(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getWeaponHoldingHand(Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Predicate;)Lnet/minecraft/world/InteractionHand;"
            ),
            index = 1
    )
    private Predicate<Item> fabrimechanus$launcherPredicate(Predicate<Item> vanilla) {
        return item -> vanilla.test(item) || item instanceof slimeknights.tconstruct.library.tools.item.ranged.ModifiableCrossbowItem;
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/RangedAttackMob;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V"
            )
    )
    private void fabrimechanus$fireTinkersCrossbow(RangedAttackMob attackMob, LivingEntity target, float velocity) {
        if (attackMob instanceof Mob shooter && TinkersCrossbowAi.fireLoadedCrossbow(shooter)) {
            return;
        }
        attackMob.performRangedAttack(target, velocity);
    }
}
