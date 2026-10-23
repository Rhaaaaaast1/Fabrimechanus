package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.hook.build.ConditionalStatModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.BowAmmoModifierHook;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableBowItem;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableCrossbowItem;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableLauncherItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.EnumSet;

/**
 * Ranged attack goal for mobs holding a Tinkers' Construct launcher.
 *
 * <p>Vanilla's {@code RangedBowAttackGoal} only recognizes {@code BowItem}, and Tinkers' bows are
 * {@code ProjectileWeaponItem}s, so mobs holding one simply never shoot. This goal drives the real Tinkers' charge and
 * release pipeline instead:</p>
 * <ul>
 *   <li>bow: charge for the tool's draw speed, then release, which fires through {@code ModifiableBowItem}</li>
 *   <li>crossbow: charge, release to load, then fire the loaded projectile through
 *       {@code ModifiableCrossbowItem#fireCrossbow}</li>
 *   <li>spear: throw a {@code ThrownTool} like a drowned throws its trident, without consuming the spear</li>
 * </ul>
 * Ammo is supplied for mobs by {@link MobAmmoHandler}.
 */
public class TinkersRangedAttackGoal extends Goal {
    /** Which weapon the mob is holding; the goal only knows these kinds. */
    public enum Ranged {
        NONE,
        BOW,
        CROSSBOW,
        SPEAR
    }

    /** Vanilla arrow gravity, used to compensate the drop when aiming. */
    private static final double ARROW_GRAVITY = 0.05D;
    /** Rough launch speed of a fully charged Tinkers' projectile. */
    private static final double NOMINAL_ARROW_SPEED = 3.0D;

    private final Mob mob;
    private final Ranged mode;
    private final double speedModifier;
    private final int attackIntervalMin;
    private final float attackRadiusSqr;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;
    /** distance a spear thrower stops at, close enough to reliably hit but far enough to stay out of reach */
    private static final double SPEAR_HOLD_SQR = 36.0D;
    /** ticks the spear is raised before it leaves the hand, the trident pose is the winding up animation */
    private static final int SPEAR_CHARGE_TICKS = 12;
    /** ticks the current charge needs, cached so the stat lookup does not run every tick while using */
    private int chargeTicks = 20;

    public TinkersRangedAttackGoal(Mob mob, Ranged mode, double speedModifier, int attackIntervalMin, float attackRadius) {
        this.mob = mob;
        this.mode = mode;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }


    @Override
    public boolean canUse() {
        return mob.getTarget() != null && isHoldingLauncher();
    }

    @Override
    public boolean canContinueToUse() {
        return mob.getTarget() != null && isHoldingLauncher();
    }

    @Override
    public void start() {
        super.start();
        mob.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        mob.setAggressive(false);
        seeTime = 0;
        attackTime = -1;
        mob.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return;
        }
        double distSqr = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean canSee = mob.getSensing().hasLineOfSight(target);
        boolean sawBefore = seeTime > 0;
        if (canSee != sawBefore) {
            seeTime = 0;
        }
        seeTime += canSee ? 1 : -1;

        if (mode == Ranged.SPEAR) {
            // walk in until throwing range, then hold position and lob spears like a trident carrying drowned
            if (mob.isUsingItem()) {
                mob.getNavigation().stop();
            } else if (distSqr > SPEAR_HOLD_SQR) {
                mob.getNavigation().moveTo(target, speedModifier);
            } else {
                mob.getNavigation().stop();
            }
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (mob.isUsingItem()) {
                // the raised spear is the winding up animation, release it once the charge is done
                if (mob.getTicksUsingItem() >= chargeTicks && canSee) {
                    snapAim(target);
                    mob.releaseUsingItem();
                    if (TinkersSpearAi.throwSpear(mob, target)) {
                        attackTime = attackIntervalMin + mob.getRandom().nextInt(10);
                    }
                }
                return;
            }
            if (attackTime > 0) {
                attackTime--;
                return;
            }
            if (seeTime >= 10 && canSee && distSqr <= attackRadiusSqr) {
                snapAim(target);
                chargeTicks = SPEAR_CHARGE_TICKS;
                mob.startUsingItem(InteractionHand.MAIN_HAND);
            }
            return;
        }

        if (distSqr <= attackRadiusSqr && seeTime >= 20) {
            mob.getNavigation().stop();
            strafingTime++;
        } else {
            mob.getNavigation().moveTo(target, speedModifier);
            strafingTime = -1;
        }

        if (strafingTime >= 20) {
            if (mob.getRandom().nextFloat() < 0.3F) {
                strafingClockwise = !strafingClockwise;
            }
            if (mob.getRandom().nextFloat() < 0.3F) {
                strafingBackwards = !strafingBackwards;
            }
            strafingTime = 0;
        }

        if (strafingTime > -1) {
            if (distSqr > attackRadiusSqr * 0.75F) {
                strafingBackwards = false;
            } else if (distSqr < attackRadiusSqr * 0.25F) {
                strafingBackwards = true;
            }
            mob.getMoveControl().strafe(strafingBackwards ? -0.5F : 0.5F, strafingClockwise ? 0.5F : -0.5F);
            mob.lookAt(target, 30.0F, 30.0F);
        } else {
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (mob.isUsingItem()) {
            if (!canSee && seeTime < -60) {
                mob.stopUsingItem();
            } else if (canSee && mob.getTicksUsingItem() >= chargeTicks) {
                snapAim(target);
                releaseAndFire();
            }
            return;
        }


        if (attackTime > 0) {
            attackTime--;
            return;
        }
        if (seeTime < -60 || !canSee || distSqr > attackRadiusSqr) {
            return;
        }
        // a loaded crossbow fires without another draw
        if (mode == Ranged.CROSSBOW && isCrossbowLoaded()) {
            snapAim(target);
            fireLoadedCrossbow();
            attackTime = attackIntervalMin;
            return;
        }
        if (!hasAmmo()) {
            attackTime = 20;
            return;
        }
        snapAim(target);
        chargeTicks = requiredDrawTicks();
        mob.startUsingItem(InteractionHand.MAIN_HAND);
    }

    /** Releases the charge and fires, if the tool managed to load. */
    private void releaseAndFire() {
        if (mode == Ranged.BOW) {
            mob.releaseUsingItem();
            attackTime = attackIntervalMin;
            return;
        }
        // releasing a crossbow loads the projectile; firing it is a separate step
        if (isCrossbowLoaded()) {
            mob.stopUsingItem();
        } else {
            mob.releaseUsingItem();
        }
        fireLoadedCrossbow();
        attackTime = attackIntervalMin;
    }

    private void fireLoadedCrossbow() {
        ItemStack stack = mob.getMainHandItem();
        if (!(stack.getItem() instanceof ModifiableCrossbowItem)) {
            return;
        }
        ToolStack tool = ToolStack.from(stack);
        CompoundTag ammo = tool.getPersistentData().getCompound(ModifiableCrossbowItem.KEY_CROSSBOW_AMMO);
        if (!ammo.isEmpty()) {
            ModifiableCrossbowItem.fireCrossbow(tool, mob, false, InteractionHand.MAIN_HAND, ammo);
        }
    }

    private boolean isCrossbowLoaded() {
        ItemStack stack = mob.getMainHandItem();
        if (!(stack.getItem() instanceof ModifiableCrossbowItem)) {
            return false;
        }
        return !ToolStack.from(stack).getPersistentData().getCompound(ModifiableCrossbowItem.KEY_CROSSBOW_AMMO).isEmpty();
    }

    private boolean hasAmmo() {
        ItemStack stack = mob.getMainHandItem();
        if (!(stack.getItem() instanceof ModifiableLauncherItem launcher)) {
            return false;
        }
        ItemStack ammo = BowAmmoModifierHook.getAmmo(ToolStack.from(stack), stack, mob, launcher.getAllSupportedProjectiles());
        return !ammo.isEmpty();
    }

    /** Number of ticks the mob has to hold the launcher before a full-power shot. */
    private int requiredDrawTicks() {
        int base = mode == Ranged.CROSSBOW ? 25 : 20;
        ItemStack stack = mob.getMainHandItem();
        if (!(stack.getItem() instanceof ModifiableLauncherItem)) {
            return base;
        }
        float drawSpeed = ConditionalStatModifierHook.getModifiedStat(ToolStack.from(stack), mob, ToolStats.DRAW_SPEED);
        if (drawSpeed <= 0.05F) {
            return base * 20;
        }
        return Math.max(3, Math.round(base / drawSpeed));
    }

    private boolean isHoldingLauncher() {
        ItemStack stack = mob.getMainHandItem();
        return switch (mode) {
            case BOW -> stack.getItem() instanceof ModifiableBowItem;
            case CROSSBOW -> stack.getItem() instanceof ModifiableCrossbowItem;
            case SPEAR -> TinkersSpearAi.isHoldingSpear(mob);
            case NONE -> false;
        };
    }

    /** Snaps the mob onto the target, compensating the projectile drop for the current distance. */
    private void snapAim(LivingEntity target) {
        double dx = target.getX() - mob.getX();
        double dz = target.getZ() - mob.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double dy = target.getY(0.6D) - mob.getEyeY();
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.max(horizontal, 0.05D)));
        if (horizontal > 1.0D) {
            double flightTicks = horizontal / NOMINAL_ARROW_SPEED;
            double drop = 0.5D * ARROW_GRAVITY * flightTicks * flightTicks;
            pitch -= (float) Math.toDegrees(Math.atan2(drop, horizontal));
        }
        mob.setYRot(yaw);
        mob.setYHeadRot(yaw);
        mob.setYBodyRot(yaw);
        mob.setXRot(pitch);
    }
}
