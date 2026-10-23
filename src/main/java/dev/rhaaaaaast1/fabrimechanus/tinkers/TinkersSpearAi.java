package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.modifiers.hook.build.ConditionalStatModifierHook;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
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
        Vec3 direction = aim.subtract(eye);
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        if (horizontal > 0.01D) {
            double flight = horizontal / speed;
            double drop = 0.5D * 0.05D * flight * flight;
            direction = direction.add(0.0D, drop, 0.0D);
        }
        direction = direction.normalize();
        // spawn just outside the thrower, otherwise the projectile collides with its own owner on the first tick
        thrown.setPos(eye.x + direction.x * 0.9D, eye.y - 0.1D + direction.y * 0.9D, eye.z + direction.z * 0.9D);
        thrown.shoot(direction.x, direction.y, direction.z, speed, ModifierUtil.getInaccuracy(tool, mob));
        thrown.onRelease(mob, PersistentDataCapability.getOrWarn(thrown));
        level.addFreshEntity(thrown);
        level.playSound(null, mob, SoundEvents.TRIDENT_THROW.value(), SoundSource.HOSTILE, 1.0F, 1.0F);
        // deliberately no ToolDamageUtil and no inventory removal: mob spears are not consumed
        return true;
    }
}
