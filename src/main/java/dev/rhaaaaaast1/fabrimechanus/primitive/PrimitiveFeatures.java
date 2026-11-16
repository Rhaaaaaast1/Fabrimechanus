package dev.rhaaaaaast1.fabrimechanus.primitive;

import dev.rhaaaaaast1.fabrimechanus.Fabrimechanus;
import dev.rhaaaaaast1.fabrimechanus.tinkers.FabriTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Surface-only loose rock generation. */
public final class PrimitiveFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, Fabrimechanus.MODID);

    public static final DeferredHolder<Feature<?>, LooseRockFeature> SMALL_PEBBLE =
            FEATURES.register("small_pebble", () -> new LooseRockFeature(
                    PrimitiveRegistry.SMALL_PEBBLE.get(), FabriTags.PEBBLE_SUPPORT));
    public static final DeferredHolder<Feature<?>, LooseRockFeature> SMALL_FLINT =
            FEATURES.register("small_flint", () -> new LooseRockFeature(
                    PrimitiveRegistry.SMALL_FLINT.get(), FabriTags.FLINT_SUPPORT));

    private PrimitiveFeatures() {
    }

    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
    }

    public static final class LooseRockFeature extends Feature<NoneFeatureConfiguration> {
        private final Block block;
        private final TagKey<Block> support;

        public LooseRockFeature(Block block, TagKey<Block> support) {
            super(NoneFeatureConfiguration.CODEC);
            this.block = block;
            this.support = support;
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            WorldGenLevel level = context.level();
            BlockPos pos = context.origin();
            RandomSource random = context.random();

            boolean pebble = block == PrimitiveRegistry.SMALL_PEBBLE.get();
            boolean enabled = pebble ? PrimitiveConfig.pebbleEnabled() : PrimitiveConfig.flintEnabled();
            int desired = pebble ? PrimitiveConfig.pebbleCount() : PrimitiveConfig.flintCount();
            int max = pebble ? PrimitiveConfig.maxPebbleTries() : PrimitiveConfig.maxFlintTries();
            if (!enabled || desired <= 0 || random.nextFloat() >= (float) desired / max) {
                return false;
            }

            BlockState below = level.getBlockState(pos.below());
            BlockState current = level.getBlockState(pos);

            // Snowy biomes put the heightmap on top of snow. Replace the top
            // snow directly; never place a rock above the snow layer.
            BlockPos placement = pos;
            boolean replacingSnow = false;
            if (current.is(Blocks.SNOW) || current.is(Blocks.SNOW_BLOCK)) {
                replacingSnow = true;
            } else if (below.is(Blocks.SNOW) || below.is(Blocks.SNOW_BLOCK)) {
                placement = pos.below();
                replacingSnow = true;
            }

            if (!level.getBlockState(placement.below()).is(support)) {
                return false;
            }

            BlockState target = level.getBlockState(placement);
            boolean water = target.is(Blocks.WATER);
            if (!replacingSnow && !target.isAir() && !water) {
                return false;
            }

            BlockState state = block.defaultBlockState();
            if (state.hasProperty(LooseRockBlock.WATERLOGGED)) {
                state = state.setValue(LooseRockBlock.WATERLOGGED, water);
            }
            // A single setBlock replaces the snow layer/block; no air pass.
            return level.setBlock(placement, state, 2);
        }
    }
}
