package dev.rhaaaaaast1.fabrimechanus.mixin;

import dev.rhaaaaaast1.fabrimechanus.tinkers.TinkersSpearAi;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Drowned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets a spear carrying drowned fight in melee during the day too.
 *
 * <p>Vanilla drowned only melee at night or when their target is in water ({@code Drowned#okTarget}). That rule exists
 * because a drowned without a trident is a slow land mob, but it would leave a spear thrower standing at range doing
 * nothing but throwing during the day. With a spear in hand the drowned behaves like any other zombie: it walks up and
 * swings, and the throwing AI keeps working at range.</p>
 */
@Mixin(Drowned.class)
public abstract class DrownedMeleeMixin {
    @Inject(method = "okTarget", at = @At("HEAD"), cancellable = true)
    private void tinkersaddition$allowSpearMelee(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (target != null && TinkersSpearAi.isHoldingSpear((Drowned) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
