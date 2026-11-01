package dev.rhaaaaaast1.fabrimechanus.tinkers;

import net.minecraft.util.RandomSource;

/** Relative tier weights for one tool or one armor piece. */
public record MaterialWeights(int tier1, int tier2, int tier3, int tier4) {
    public static final MaterialWeights DEFAULT = new MaterialWeights(55, 35, 8, 2);

    public boolean isEmpty() {
        return tier1 <= 0 && tier2 <= 0 && tier3 <= 0 && tier4 <= 0;
    }

    /** Returns a tier 1-4, or 0 when all weights are zero. */
    public int rollTier(RandomSource random) {
        int total = positive(tier1) + positive(tier2) + positive(tier3) + positive(tier4);
        if (total <= 0) {
            return 0;
        }
        int roll = random.nextInt(total);
        roll -= positive(tier1);
        if (roll < 0) {
            return 1;
        }
        roll -= positive(tier2);
        if (roll < 0) {
            return 2;
        }
        roll -= positive(tier3);
        if (roll < 0) {
            return 3;
        }
        return 4;
    }

    private static int positive(int value) {
        return Math.max(0, value);
    }
}
