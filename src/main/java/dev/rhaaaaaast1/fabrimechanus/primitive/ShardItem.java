package dev.rhaaaaaast1.fabrimechanus.primitive;

import dev.rhaaaaaast1.fabrimechanus.tinkers.FabriTags;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Sharp stone/flint shard: hold use on grass to cut plant fiber. */
public class ShardItem extends Item {
    private static final int CUT_TICKS = 20;

    public ShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !context.getLevel().getBlockState(context.getClickedPos()).is(FabriTags.PLANT_FIBER_SOURCES)) {
            return InteractionResult.PASS;
        }
        player.startUsingItem(context.getHand());
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player) || level.isClientSide()) {
            return super.finishUsingItem(stack, level, entity);
        }

        HitResult hit = player.pick(4.0D, 0.0F, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return stack;
        }
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(FabriTags.PLANT_FIBER_SOURCES)) {
            return stack;
        }

        level.destroyBlock(pos, false);
        int count = 1 + level.random.nextInt(2);
        Block.popResource(level, pos, new ItemStack(PrimitiveRegistry.PLANT_FIBER.get(), count));
        level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 0.7F, 1.2F);
        player.swing(InteractionHand.MAIN_HAND, true);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return CUT_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }
}
