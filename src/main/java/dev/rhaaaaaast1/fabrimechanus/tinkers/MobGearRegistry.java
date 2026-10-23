package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.world.entity.Mob;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * Lookup table of all mob gear rules, rebuilt every time datapacks reload.
 *
 * <p>Rules are sorted by {@code priority} (highest first), and the first matching rule wins. That makes it possible for
 * a pack to override a single mob without copying the whole entry: a rule targeting {@code minecraft:wither_skeleton}
 * with {@code priority 10} beats the shipped {@code #minecraft:skeletons} rule with {@code priority 0}.</p>
 */
public final class MobGearRegistry {
    private static volatile List<MobGearRule> rules = List.of();

    private MobGearRegistry() {
    }

    /** Replaces the rule list. Called by {@link MobGearReloadListener} after every datapack reload. */
    public static void reload(List<MobGearRule> loaded) {
        rules = loaded.stream()
                .sorted(Comparator.comparingInt(MobGearRule::priority).reversed())
                .toList();
    }

    /** The highest priority rule matching this mob, or null when no datapack entry covers it. */
    @Nullable
    public static MobGearRule find(Mob mob) {
        for (MobGearRule rule : rules) {
            if (rule.matches(mob)) {
                return rule;
            }
        }
        return null;
    }

    public static List<MobGearRule> rules() {
        return rules;
    }
}
