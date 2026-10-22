package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingGetProjectileEvent;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableLauncherItem;

/**
 * Gives monsters ammunition for Tinkers' Construct launchers.
 *
 * <p>{@code LivingEntity#getProjectile} is what {@code BowAmmoModifierHook} falls back to for non-player shooters, and
 * mobs do not keep arrows in an inventory. Supplying the projectile through the NeoForge event means Tinkers' own
 * ammunition logic (charge, draw speed, projectile stats) stays in charge, and every mob shot consumes a fresh copy
 * instead of a real inventory slot.</p>
 */
public final class MobAmmoHandler {
    private MobAmmoHandler() {
    }

    public static void onGetProjectile(LivingGetProjectileEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) {
            return;
        }
        if (!(event.getProjectileWeaponItemStack().getItem() instanceof ModifiableLauncherItem)) {
            return;
        }
        if (!event.getProjectileItemStack().isEmpty()) {
            return;
        }
        event.setProjectileItemStack(new ItemStack(Items.ARROW));
    }
}
