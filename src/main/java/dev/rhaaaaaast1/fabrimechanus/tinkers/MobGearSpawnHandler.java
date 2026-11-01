package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableBowItem;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableCrossbowItem;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.EnumMap;
import java.util.Map;

/**
 * Replaces vanilla equipment slot-for-slot with Tinkers' gear.
 *
 * <p>Vanilla items are removed first, but the handler remembers which slots had gear. A replacement is only built for
 * a slot that actually contained vanilla gear, so the result keeps the vanilla distribution (including partial armor)
 * without leaving the removed vanilla item behind.</p>
 */
public final class MobGearSpawnHandler {
    private static final String PROCESSED_TAG = "fabrimechanus:tinkers_gear";
    private static final MaterialId GOLD_MATERIAL = MaterialId.tryParse("tconstruct:gold");

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final EquipmentSlot[] STRIP_SLOTS = {
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
            addRangedGoal(mob);
            return;
        }
        data.putBoolean(PROCESSED_TAG, true);

        Map<EquipmentSlot, ItemStack> removed = stripVanillaGear(mob);
        if (plan != null) {
            equip(mob, plan, removed);
        }
        addRangedGoal(mob);
    }

    /** Removes vanilla tools/armor and records what was removed so each slot can be replaced one-for-one. */
    private static Map<EquipmentSlot, ItemStack> stripVanillaGear(Mob mob) {
        Map<EquipmentSlot, ItemStack> removed = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : STRIP_SLOTS) {
            ItemStack stack = mob.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            // Drowned keep their trident; no Tinkers replacement is desired for it.
            if (slot == EquipmentSlot.MAINHAND && stack.is(Items.TRIDENT)) {
                continue;
            }
            if (VanillaGear.isBlocked(stack)) {
                removed.put(slot, stack.copy());
                mob.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
        return removed;
    }

    private static void equip(Mob mob, MobGearRule plan, Map<EquipmentSlot, ItemStack> removed) {
        RandomSource random = mob.getRandom();
        RegistryAccess access = mob.level().registryAccess();

        ItemStack oldMainhand = removed.get(EquipmentSlot.MAINHAND);
        if (oldMainhand != null && mob.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            ItemStack replacement;
            if (oldMainhand.is(Items.FISHING_ROD)) {
                // Drowned normally drop the rod for the Tinkers rod, with a small configured spear chance.
                replacement = plan.mainhand().isEmpty()
                        ? ToolFactory.build(TinkerTools.fishingRod.get(), random, access, plan.mainhand().materials())
                        : plan.mainhand().roll(random, access);
            } else {
                replacement = plan.mainhand().roll(random, access);
            }
            if (!replacement.isEmpty()) {
                mob.setItemSlot(EquipmentSlot.MAINHAND, replacement);
            }
        }

        if (plan.armor().isEmpty()) {
            return;
        }

        boolean piglinGold = mob instanceof Piglin piglin && piglin.isAdult();
        MobGearRule.ArmorSet set = piglinGold ? MobGearRule.ArmorSet.PLATE : plan.armor().roll(random);
        int armorTier = plan.armor().materials().rollTier(random);
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!removed.containsKey(slot) || !mob.getItemBySlot(slot).isEmpty()) {
                continue;
            }
            ArmorItem.Type type = switch (slot) {
                case HEAD -> ArmorItem.Type.HELMET;
                case CHEST -> ArmorItem.Type.CHESTPLATE;
                case LEGS -> ArmorItem.Type.LEGGINGS;
                default -> ArmorItem.Type.BOOTS;
            };
            Item item = MobGearRule.Armor.item(set, type);
            if (item == null) {
                continue;
            }
            ItemStack stack = piglinGold
                    ? ToolFactory.buildWithMaterial(item, GOLD_MATERIAL, access)
                    : ToolFactory.build(item, random, access, armorTier);
            if (!stack.isEmpty()) {
                mob.setItemSlot(slot, stack);
            }
        }
    }

    private static void addRangedGoal(Mob mob) {
        TinkersRangedAttackGoal.Ranged mode = rangedMode(mob.getMainHandItem());
        if (mode == TinkersRangedAttackGoal.Ranged.NONE) {
            return;
        }
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
