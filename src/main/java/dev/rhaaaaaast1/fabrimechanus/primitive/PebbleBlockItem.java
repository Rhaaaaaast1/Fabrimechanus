package dev.rhaaaaaast1.fabrimechanus.primitive;

import dev.rhaaaaaast1.fabrimechanus.tinkers.FabriTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/** Block item that can knap a pebble into a shard by sneak-right-clicking stone. */
public final class PebbleBlockItem extends BlockItem {
    private final Supplier<Item> shard;

    public PebbleBlockItem(Block block, Properties properties, Supplier<Item> shard) {
        super(block, properties);
        this.shard = shard;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (player != null && player.isShiftKeyDown() && state.is(FabriTags.PEBBLE_KNAPPING_BLOCKS)) {
            if (!level.isClientSide()) {
                ItemStack stack = context.getItemInHand();
                stack.shrink(1);
                ItemStack result = new ItemStack(shard.get());
                if (!player.getInventory().add(result)) {
                    player.drop(result, false);
                }
                level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F);
                if (level instanceof ServerLevel server) {
                    server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                            pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                            12, 0.25D, 0.15D, 0.25D, 0.05D);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useOn(context);
    }
}
