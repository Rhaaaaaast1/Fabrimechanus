package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableBowItem;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableCrossbowItem;

/**
 * Replaces the vanilla equipment of newly spawned monsters with Tinkers' Construct gear.
 *
 * <p>This runs when the mob joins the level, which is after {@code Mob#finalizeSpawn} finished rolling the vanilla
 * equipment. Doing it here instead of through {@code FinalizeSpawnEvent} means the event is never cancelled: spawn group
 * data (raids, patrols, reinforcement data) keeps propagating, and Tinkers' Construct's own mob equipment tables are
 * left completely untouched - they simply run first, and this handler only replaces the vanilla items listed in
 * {@link VanillaGear}.</p>
 */
public final class MobGearSpawnHandler {
    /** Marks a mob as already handled, so loading it back from disk never re-rolls its materials. */
    private static final String PROCESSED_TAG = "fabrimechanus:tinkers_gear";

    private static final EquipmentSlot[] EQUIPMENT_SLOTS = {
        EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private MobGearSpawnHandler() {
    }

    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob)) {
            return;
        }
        MobGearRule plan = MobGearRegistry.find(mob);
        boolean stripVanilla = mob instanceof Monster;
        if (plan == null && !stripVanilla) {
            return;
        }
        CompoundTag data = mob.getPersistentData();
        if (data.getBoolean(PROCESSED_TAG)) {
            // goals are not part of the saved entity, so re-attach the ranged AI when the mob still carries its launcher
            addRangedGoal(mob);
            return;
        }
        data.putBoolean(PROCESSED_TAG, true);
        if (stripVanilla) {
            stripVanillaGear(mob);
        }
        if (plan != null) {
            equip(mob, plan);
        }
        addRangedGoal(mob);
    }

    /** Removes exactly the vanilla tools and armor listed in {@link VanillaGear}; everything else is left alone. */
    private static void stripVanillaGear(Mob mob) {
        for (EquipmentSlot slot : EQUIPMENT_SLOTS) {
            if (VanillaGear.isBlocked(mob.getItemBySlot(slot))) {
                mob.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    private static void equip(Mob mob, MobGearRule plan) {
        RandomSource random = mob.getRandom();
        RegistryAccess access = mob.level().registryAccess();

        // any slot that still holds an item belongs to Tinkers' Construct's own mob equipment tables, so it is kept as is
        if (plan.hasWeapon() && mob.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && random.nextFloat() < plan.mainhand().chance()) {
            ItemStack weapon = plan.mainhand().roll(random, access);
            if (!weapon.isEmpty()) {
                mob.setItemSlot(EquipmentSlot.MAINHAND, weapon);
            }
        }
        if (mob.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty() && random.nextFloat() < plan.offhand().chance()) {
            ItemStack offhand = plan.offhand().roll(random, access);
            if (!offhand.isEmpty()) {
                mob.setItemSlot(EquipmentSlot.OFFHAND, offhand);
            }
        }
        if (!plan.armor().isEmpty() && random.nextFloat() < plan.armor().chance()) {
            // one set for the whole mob, so plate and travelers pieces are never mixed
            MobGearRule.ArmorSet set = plan.armor().roll(random);
            if (set != MobGearRule.ArmorSet.NONE) {
                equipArmor(mob, set, random, access);
            }
        }
    }

    private static void equipArmor(Mob mob, MobGearRule.ArmorSet set, RandomSource random, RegistryAccess access) {
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            if (type == ArmorItem.Type.BODY) {
                continue;
            }
            EquipmentSlot slot = type.getSlot();
            if (!mob.getItemBySlot(slot).isEmpty() || random.nextFloat() > 0.85F) {
                continue;
            }
            Item item = MobGearRule.Armor.item(set, type);
            if (item != null) {
                ItemStack stack = ToolFactory.build(item, random, access);
                if (!stack.isEmpty()) {
                    mob.setItemSlot(slot, stack);
                }
            }
        }
    }

    /** Attaches the ranged AI when the mob is carrying a Tinkers' launcher or spear. */
    private static void addRangedGoal(Mob mob) {
        TinkersRangedAttackGoal.Ranged mode = rangedMode(mob.getMainHandItem());
        if (mode == TinkersRangedAttackGoal.Ranged.NONE) {
            return;
        }
        // mobs with a vanilla crossbow AI keep their own goal, it is patched to drive Tinkers' crossbows as well
        if (mode == TinkersRangedAttackGoal.Ranged.CROSSBOW && mob instanceof CrossbowAttackMob) {
            return;
        }
        for (WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof TinkersRangedAttackGoal) {
                return;
            }
        }
        int interval = switch (mode) {
            case CROSSBOW -> 30;
            case SPEAR -> 40;
            default -> 20;
        };
        float radius = mode == TinkersRangedAttackGoal.Ranged.BOW ? 15.0F : 12.0F;
        mob.goalSelector.addGoal(1, new TinkersRangedAttackGoal(mob, mode, 1.0D, interval, radius));
    }

    private static TinkersRangedAttackGoal.Ranged rangedMode(ItemStack stack) {
        if (stack.getItem() instanceof ModifiableBowItem) {
            return TinkersRangedAttackGoal.Ranged.BOW;
        }
        if (stack.getItem() instanceof ModifiableCrossbowItem) {
            return TinkersRangedAttackGoal.Ranged.CROSSBOW;
        }
        if (TinkersSpearAi.isSpear(stack)) {
            return TinkersRangedAttackGoal.Ranged.SPEAR;
        }
        return TinkersRangedAttackGoal.Ranged.NONE;
    }
}
