package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.world.entity.EntityType;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.HashMap;
import java.util.Map;

/**
 * Per mob type gear table: which Tinkers' weapons and armor a mob may spawn with.
 *
 * <p>Weapons follow vanilla exactly: a mob that spawns holding a bow gets the Tinkers' longbow, a mob holding an axe gets
 * a hand axe or broad axe, and so on. Mobs that spawn empty handed in vanilla (zombie, husk, zombie villager, evoker,
 * witch, ravager) never receive a weapon here.</p>
 *
 * <p>Armor is deliberately more generous than vanilla: every humanoid monster whose model can render armor may spawn
 * with a random piece of Tinkers' plate or travelers' gear, which is the main source of the armor variety this feature
 * adds. Illagers and vexes are left out because their models have no armor layer, so players would see invisible gear.</p>
 *
 * <p>Only regular Tinkers' tools are listed here. The "ancient" tool set (battlesign, swasher, war pick, melting pan,
 * minotaur axe) is reserved for Tinkers' own mob equipment tables and is deliberately never handed out by this mod.</p>
 */
public final class MobGearPlans {
    private static final Map<EntityType<?>, MobGearPlan> PLANS = new HashMap<>();

    static {
        // --- bow users: skeletons get the Tinkers' longbow ------------------------------------------------
        MobGearPlan bowUser = MobGearPlan.builder()
            .weapon(TinkerTools.longbow::get, 1)
            .weaponChance(1.0f)
            .armor(0.3f, MobGearPlan.ArmorSet.PLATE)
            .build();
        PLANS.put(EntityType.SKELETON, bowUser);
        PLANS.put(EntityType.STRAY, bowUser);
        PLANS.put(EntityType.BOGGED, bowUser);
        // the illusioner also spawns with a bow, but its model has no armor layer, so it gets no armor
        PLANS.put(EntityType.ILLUSIONER, MobGearPlan.builder()
            .weapon(TinkerTools.longbow::get, 1)
            .weaponChance(1.0f)
            .build());

        // --- wither skeletons: vanilla stone sword, so a sword or cleaver ----------------------------------
        PLANS.put(EntityType.WITHER_SKELETON, MobGearPlan.builder()
            .weapon(TinkerTools.sword::get, 1)
            .weapon(TinkerTools.cleaver::get, 1)
            .weaponChance(1.0f)
            .armor(0.5f, MobGearPlan.ArmorSet.PLATE)
            .build());

        // --- mobs that spawn empty handed in vanilla only get armor ---------------------------------------
        MobGearPlan zombie = MobGearPlan.builder()
            .weaponChance(0.0f)
            .armor(0.25f, MobGearPlan.ArmorSet.TRAVELERS)
            .build();
        PLANS.put(EntityType.ZOMBIE, zombie);
        PLANS.put(EntityType.HUSK, zombie);
        PLANS.put(EntityType.ZOMBIE_VILLAGER, MobGearPlan.builder()
            .weaponChance(0.0f)
            .armor(0.3f, MobGearPlan.ArmorSet.PLATE)
            .build());

        // --- drowned keep their trident or fishing rod, so only armor is added ------------------------------
        PLANS.put(EntityType.DROWNED, MobGearPlan.builder()
            .weaponChance(0.0f)
            .armor(0.25f, MobGearPlan.ArmorSet.TRAVELERS)
            .build());

        // --- illagers -------------------------------------------------------------------------------------
        PLANS.put(EntityType.PILLAGER, MobGearPlan.builder()
            .weapon(TinkerTools.crossbow::get, 1)
            .weaponChance(1.0f)
            .build());
        PLANS.put(EntityType.VINDICATOR, MobGearPlan.builder()
            .weapon(TinkerTools.handAxe::get, 5)
            .weapon(TinkerTools.broadAxe::get, 4)
            .weaponChance(1.0f)
            .build());
        PLANS.put(EntityType.VEX, MobGearPlan.builder()
            .weapon(TinkerTools.dagger::get, 1)
            .weapon(TinkerTools.sword::get, 1)
            .weaponChance(1.0f)
            .build());

        // --- piglins --------------------------------------------------------------------------------------
        PLANS.put(EntityType.PIGLIN, MobGearPlan.builder()
            .weapon(TinkerTools.crossbow::get, 1)
            .weapon(TinkerTools.sword::get, 1)
            .weaponChance(1.0f)
            .armor(0.35f, MobGearPlan.ArmorSet.PLATE)
            .build());
        PLANS.put(EntityType.ZOMBIFIED_PIGLIN, MobGearPlan.builder()
            .weapon(TinkerTools.sword::get, 1)
            .weaponChance(1.0f)
            .armor(0.25f, MobGearPlan.ArmorSet.PLATE)
            .build());
        PLANS.put(EntityType.PIGLIN_BRUTE, MobGearPlan.builder()
            .weapon(TinkerTools.handAxe::get, 5)
            .weapon(TinkerTools.broadAxe::get, 3)
            .weaponChance(1.0f)
            .armor(0.2f, MobGearPlan.ArmorSet.PLATE)
            .build());
    }

    private MobGearPlans() {
    }

    public static MobGearPlan get(EntityType<?> type) {
        return PLANS.get(type);
    }

}
