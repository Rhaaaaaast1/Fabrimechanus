package dev.rhaaaaaast1.fabrimechanus.mixin;

import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableCrossbowItem;

/**
 * Makes piglins treat a Tinkers' Construct crossbow as a usable ranged weapon.
 *
 * <p>This looks unrelated to shooting, but it is the reason a piglin never picks up its crossbow at range:
 * {@code BehaviorUtils.isWithinAttackRange} asks {@code Mob#canFireProjectileWeapon}, and the piglin implementation
 * hardcodes {@code weapon == Items.CROSSBOW}. For any other weapon the helper silently degrades to "within melee
 * range", so the {@code CrossbowAttack} behavior only starts once the piglin is already next to its target - it then
 * plays the charge animation while its melee behavior does the actual fighting.</p>
 */
@Mixin(Piglin.class)
public abstract class PiglinCrossbowMixin {
    @Inject(method = "canFireProjectileWeapon", at = @At("HEAD"), cancellable = true)
    private void tinkersaddition$acceptTinkersCrossbow(ProjectileWeaponItem weapon, CallbackInfoReturnable<Boolean> cir) {
        if (weapon instanceof ModifiableCrossbowItem) {
            cir.setReturnValue(true);
        }
    }
}
