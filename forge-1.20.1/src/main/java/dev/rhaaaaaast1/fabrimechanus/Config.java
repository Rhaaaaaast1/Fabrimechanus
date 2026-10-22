package dev.rhaaaaaast1.fabrimechanus;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Config of the Tinkers' Construct integration.
 *
 * <p>The material tier weights are exposed so packs and players can tune how strong the randomly generated monster
 * gear is. Weights are relative, they do not have to add up to 100: {@code 25/25/30/17/3} is the same as
 * {@code 0.25/0.25/0.30/0.17/0.03}.</p>
 *
 * <p>The option names shown in the in-game config screen are translated through the mod's language files, see
 * {@code assets/fabrimechanus/lang}. Only the name and the tooltip provided by the translation key are translated,
 * the comment written into the generated toml file itself stays in English.</p>
 */
@EventBusSubscriber(modid = Fabrimechanus.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class Config {
    /** Tier 0 + tier 1 share 50% of all rolls by default, tier 2 30%, tier 3 17% and tier 4 3%. */
    private static final float[] DEFAULT_TIER_WEIGHTS = {25.0F, 25.0F, 30.0F, 17.0F, 3.0F};

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.DoubleValue TIER_0_WEIGHT = tierWeight("tier0", 25.0D);
    private static final ForgeConfigSpec.DoubleValue TIER_1_WEIGHT = tierWeight("tier1", 25.0D);
    private static final ForgeConfigSpec.DoubleValue TIER_2_WEIGHT = tierWeight("tier2", 30.0D);
    private static final ForgeConfigSpec.DoubleValue TIER_3_WEIGHT = tierWeight("tier3", 17.0D);
    private static final ForgeConfigSpec.DoubleValue TIER_4_WEIGHT = tierWeight("tier4", 3.0D);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    /** Normalised weights, index 0 is tier 0. Read on every roll, so editing the config applies immediately. */
    private static volatile float[] tierWeights = DEFAULT_TIER_WEIGHTS.clone();

    private Config() {
    }

    private static ForgeConfigSpec.DoubleValue tierWeight(String tier, double defaultValue) {
        return BUILDER
                .translation("fabrimechanus.configuration.material_tier_weights." + tier)
                .comment("Relative weight of tier " + tier.substring(4) + " materials when rolling the material of a tool part.")
                .comment("Weights are relative, they do not need to add up to 100.")
                .comment("Defaults: tier 0 = 25, tier 1 = 25 (together 50%), tier 2 = 30, tier 3 = 17, tier 4 = 3.")
                .defineInRange("materialTierWeights." + tier, defaultValue, 0.0D, 10_000.0D);
    }

    /** Normalised tier weights, index 0 is tier 0. The array is never mutated after it is published. */
    public static float[] tierWeights() {
        return tierWeights;
    }

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            refresh();
        }
    }

    private static void refresh() {
        float[] weights = {
                (float) (double) TIER_0_WEIGHT.get(),
                (float) (double) TIER_1_WEIGHT.get(),
                (float) (double) TIER_2_WEIGHT.get(),
                (float) (double) TIER_3_WEIGHT.get(),
                (float) (double) TIER_4_WEIGHT.get()
        };
        float total = 0.0F;
        for (float weight : weights) {
            total += Math.max(0.0F, weight);
        }
        if (total <= 0.0F) {
            // all weights set to zero is not a meaningful distribution, keep the requested defaults instead
            tierWeights = DEFAULT_TIER_WEIGHTS.clone();
            return;
        }
        float[] normalised = new float[weights.length];
        for (int i = 0; i < weights.length; i++) {
            normalised[i] = Math.max(0.0F, weights[i]) / total;
        }
        tierWeights = normalised;
    }
}
