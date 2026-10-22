package dev.rhaaaaaast1.fabrimechanus.tinkers;

import dev.rhaaaaaast1.fabrimechanus.Config;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Picks a random material for every single tool part, following the tier distribution
 * requested for natural mob spawns:
 * <ul>
 *   <li>tier 0 + tier 1: 50%</li>
 *   <li>tier 2: 30%</li>
 *   <li>tier 3: 17%</li>
 *   <li>tier 4: 3%</li>
 * </ul>
 * Every part rolls independently, so a single tool can mix materials of different tiers.
 */
public final class RandomTierMaterials {
    /** Highest tier defined by Tinkers' Construct (tier 5 is a single material, blood). */
    public static final int MAX_TIER = 5;

    /** Materials usable for a stat type, indexed by tier. */
    private static Map<MaterialStatsId, List<List<IMaterial>>> byTierCache;
    private static IMaterialRegistry cachedRegistry;

    private RandomTierMaterials() {
    }

    /** Drops the material cache, which must happen whenever the material registry is rebuilt (datapack reload). */
    public static void invalidate() {
        byTierCache = null;
        cachedRegistry = null;
    }

    /** Builds a full material list for the given tool definition, one independent roll per part. */
    public static MaterialNBT buildMaterials(ToolDefinition definition, RandomSource random) {
        MaterialNBT.Builder builder = MaterialNBT.builder();
        for (MaterialStatsId statType : definition.getHook(ToolHooks.TOOL_MATERIALS).getStatTypes(definition)) {
            IMaterial material = pick(statType, random);
            builder.add(material == null ? MaterialRegistry.firstWithStatType(statType) : material);
        }
        return builder.build();
    }

    /** Rolls a single material that can actually be used by the given stat type. */
    private static IMaterial pick(MaterialStatsId statType, RandomSource random) {
        List<List<IMaterial>> tiers = tiersFor(statType);
        int preferred = rollTier(random);
        // prefer the rolled tier, then fall outwards until something usable is found
        for (int distance = 0; distance <= MAX_TIER; distance++) {
            int higher = preferred + distance;
            if (higher <= MAX_TIER) {
                IMaterial found = pickFrom(tiers.get(higher), random);
                if (found != null) {
                    return found;
                }
            }
            int lower = preferred - distance;
            if (distance > 0 && lower >= 0) {
                IMaterial found = pickFrom(tiers.get(lower), random);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static IMaterial pickFrom(List<IMaterial> materials, RandomSource random) {
        if (materials.isEmpty()) {
            return null;
        }
        return materials.get(random.nextInt(materials.size()));
    }

    /** Rolls the material tier using the tier weights from the config file. */
    private static int rollTier(RandomSource random) {
        float[] weights = Config.tierWeights();
        float roll = random.nextFloat();
        float cumulative = 0.0F;
        for (int tier = 0; tier < weights.length; tier++) {
            cumulative += weights[tier];
            if (roll < cumulative) {
                return tier;
            }
        }
        // floating point rounding can leave a tiny gap at the top of the range
        return weights.length - 1;
    }
    private static List<List<IMaterial>> tiersFor(MaterialStatsId statType) {
        IMaterialRegistry registry = MaterialRegistry.getInstance();
        if (byTierCache == null || cachedRegistry != registry) {
            byTierCache = new HashMap<>();
            cachedRegistry = registry;
        }
        return byTierCache.computeIfAbsent(statType, stat -> {
            List<List<IMaterial>> tiers = new ArrayList<>(MAX_TIER + 1);
            for (int tier = 0; tier <= MAX_TIER; tier++) {
                tiers.add(new ArrayList<>());
            }
            for (IMaterial material : registry.getVisibleMaterials()) {
                // skip anything that cannot actually be applied to this part
                if (registry.getMaterialStats(material.getIdentifier(), stat).isEmpty()) {
                    continue;
                }
                tiers.get(Mth.clamp(material.getTier(), 0, MAX_TIER)).add(material);
            }
            return tiers;
        });
    }
}
