package dev.rhaaaaaast1.fabrimechanus.primitive;

import dev.rhaaaaaast1.fabrimechanus.Fabrimechanus;
import dev.rhaaaaaast1.fabrimechanus.tinkers.FabriTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
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
                    PrimitiveRegistry.SMALL_PEBBLE.get(), FabriTags.PEBBLE_SUPPORT, true));
    public static final DeferredHolder<Feature<?>, LooseRockFeature> SMALL_FLINT =
            FEATURES.register("small_flint", () -> new LooseRockFeature(
                    PrimitiveRegistry.SMALL_FLINT.get(), FabriTags.FLINT_SUPPORT, false));

    private PrimitiveFeatures() {
    }

    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
    }

    public static final class LooseRockFeature extends Feature<NoneFeatureConfiguration> {
        private final Block block;
        private final TagKey<Block> support;
        private final boolean requireStoneBelow;

        public LooseRockFeature(Block block, TagKey<Block> support, boolean requireStoneBelow) {
            super(NoneFeatureConfiguration.CODEC);
            this.block = block;
            this.support = support;
            this.requireStoneBelow = requireStoneBelow;
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            WorldGenLevel level = context.level();
            BlockPos pos = context.origin();

            if (!level.canSeeSky(pos)) {
                return false;
            }

            BlockState below = level.getBlockState(pos.below());
            if (!below.is(support)) {
                return false;
            }
            if (requireStoneBelow && !hasStoneBelow(level, pos)) {
                return false;
            }

            BlockState current = level.getBlockState(pos);
            boolean water = current.is(Blocks.WATER);
            if (!current.isAir() && !water) {
                return false;
            }

            BlockState state = block.defaultBlockState();
            if (state.hasProperty(LooseRockBlock.WATERLOGGED)) {
                state = state.setValue(LooseRockBlock.WATERLOGGED, water);
            }
            return level.setBlock(pos, state, 2);
        }

        private static boolean hasStoneBelow(WorldGenLevel level, BlockPos pos) {
            for (int i = 1; i <= 8; i++) {
                if (level.getBlockState(pos.below(i)).is(BlockTags.BASE_STONE_OVERWORLD)) {
                    return true;
                }
            }
            return false;
        }
    }
}
