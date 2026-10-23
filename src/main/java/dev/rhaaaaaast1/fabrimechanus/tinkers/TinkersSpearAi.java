package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.modifiers.hook.build.ConditionalStatModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import com.google.common.annotations.VisibleForTesting;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.entity.ThrownTool;

/**
 * Lets mobs throw Tinkers' spears, following what the vanilla trident does for drowned.
 *
 * <p>Vanilla drowned throw a trident through {@code ThrownTrident} and keep an infinite supply: the trident is spawned
 * with no pickup permission, so it drops nothing and simply despawns. Tinkers' javelins are not {@code ItemStack}s that
 * vanilla AI understands, and their throwing logic ({@code ThrowingModule}) is hardwired to a {@link
 * net.minecraft.world.entity.player.Player} inventory, so mobs never throw them on their own.</p>
 *
 * <p>This class replays the same steps the player path performs - full charge, velocity from the tool stats, the
 * {@code ThrownTool} entity, release hook and throw sound - but skips the inventory removal and the durability cost.
 * The result is the trident behaviour players already know: the mob keeps the spear in hand and may throw again after
 * its attack cooldown.</p>
 */
public final class TinkersSpearAi {
    private TinkersSpearAi() {
    }

    /**
     * Raises the spear and starts the wind up. Mobs never call {@code Item#use}, so the active modifier that normally
     * wakes up the use animation has to be set by hand; the vanilla item use duration then reports a spear pose.
     */
    public static boolean beginCharge(Mob mob) {
        ItemStack stack = mob.getMainHandItem();
        if (!isSpear(stack)) {
            return false;
        }
        mob.startUsingItem(InteractionHand.MAIN_HAND);
        return mob.isUsingItem();
    }

    /** True when the stack is a modifiable tool that carries the throwing modifier (all Tinkers' javelins do). */
    public static boolean isSpear(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ToolStack tool = ToolStack.from(stack);
        return !tool.isBroken() && tool.getModifiers().getLevel(ModifierIds.throwing) > 0;
    }

    public static boolean isHoldingSpear(Mob mob) {
        return isSpear(mob.getMainHandItem());
    }

    /**
     * Spawns a thrown copy of the held spear, aimed at the target with a small drop compensation.
     *
     * <p>The direction is computed from the eye position instead of the entity rotation: {@code Mob} look control keeps
     * rewriting the head angles every tick, so a spear thrown from {@code getXRot()}/{@code getYRot()} tends to fly flat
     * over the target.</p>
     *
     * @return true when a projectile was created
     */
    public static boolean throwSpear(Mob mob, LivingEntity target) {
        ItemStack stack = mob.getMainHandItem();
        if (!isSpear(stack)) {
            return false;
        }
        Level level = mob.level();
        if (level.isClientSide()) {
            return false;
        }
        ToolStack tool = ToolStack.from(stack);
        float velocity = Math.max(0.1F, ConditionalStatModifierHook.getModifiedStat(tool, mob, ToolStats.VELOCITY));
        float waterInertia = ConditionalStatModifierHook.getModifiedStat(tool, mob, ToolStats.WATER_INERTIA);
        // a raw stat multiplier can produce absurd speeds that tunnel straight through the target
        float speed = Mth.clamp(velocity * 2.0F, 1.2F, 3.0F);
        // full charge: the mob has no wind-up animation to wait for, its attack cooldown plays that role
        ThrownTool thrown = new ThrownTool(level, mob, stack.copy(), 1.0F, velocity, waterInertia);
        Vec3 eye = mob.getEyePosition();
        Vec3 aim = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ());
        Vec3 direction = solveArc(eye, aim, speed);
        // spawn just outside the thrower, otherwise the projectile collides with its own owner on the first tick
        thrown.setPos(eye.x + direction.x * 0.9D, eye.y - 0.1D + direction.y * 0.9D, eye.z + direction.z * 0.9D);
        thrown.shoot(direction.x, direction.y, direction.z, speed, 0.0F);
        thrown.onRelease(mob, PersistentDataCapability.getOrWarn(thrown));
        level.addFreshEntity(thrown);
        level.playSound(null, mob, SoundEvents.TRIDENT_THROW.value(), SoundSource.HOSTILE, 1.0F, 1.0F);
        // deliberately no ToolDamageUtil and no inventory removal: mob spears are not consumed
        return true;
    }

    /**
     * Launch direction that lands on {@code aim} for a projectile launched at {@code speed}, using the same constant
     * gravity vanilla arrows apply ({@code 0.05} blocks per tick). Aiming straight at the target leaves the spear short
     * on longer throws, aiming above it by a fixed guess overshoots, so the low arc is solved directly.
     */
    @VisibleForTesting
    static Vec3 solveArc(Vec3 eye, Vec3 aim, double speed) {
        double dx = aim.x - eye.x;
        double dz = aim.z - eye.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double height = aim.y - eye.y;
        if (horizontal < 0.05D) {
            return new Vec3(0.0D, Math.signum(height), 0.0D);
        }
        double gravity = 0.05D;
        double v2 = speed * speed;
        double discriminant = v2 * v2 - gravity * (gravity * horizontal * horizontal + 2.0D * height * v2);
        if (discriminant < 0.0D) {
            // target out of reach at this speed, throw as flat as possible towards it
            return new Vec3(dx, height, dz).normalize();
        }
        double tan = (v2 - Math.sqrt(discriminant)) / (gravity * horizontal);
        double cos = 1.0D / Math.sqrt(1.0D + tan * tan);
        double sin = tan * cos;
        return new Vec3(dx / horizontal * cos, sin, dz / horizontal * cos);
    }
}
