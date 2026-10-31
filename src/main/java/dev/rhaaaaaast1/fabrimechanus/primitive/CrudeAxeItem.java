package dev.rhaaaaaast1.fabrimechanus.primitive;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Crude axe that can saw stripped wood into two planks with a charged use. */
public class CrudeAxeItem extends AxeItem {
    private static final int SAW_TICKS = 20;

    public CrudeAxeItem(Tier tier, Item.Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        if (isStrippedWood(state) && context.getPlayer() != null) {
            context.getPlayer().startUsingItem(context.getHand());
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useOn(context);
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
        if (!isStrippedWood(state)) {
            return stack;
        }

        Block planks = planksFor(state);
        if (planks == Blocks.AIR) {
            return stack;
        }

        level.removeBlock(pos, false);
        Block.popResource(level, pos, new ItemStack(planks, 2));
        level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 0.8F, 1.0F);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        player.swing(InteractionHand.MAIN_HAND, true);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return SAW_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    private static boolean isStrippedWood(BlockState state) {
        if (!state.is(BlockTags.LOGS)) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return id != null && id.getPath().startsWith("stripped_");
    }

    private static Block planksFor(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (id == null) {
            return Blocks.AIR;
        }
        String path = id.getPath()
                .replaceFirst("^stripped_", "")
                .replaceFirst("_log$", "")
                .replaceFirst("_wood$", "")
                .replaceFirst("_stem$", "")
                .replaceFirst("_hyphae$", "")
                .replaceFirst("_block$", "");
        ResourceLocation planks = ResourceLocation.fromNamespaceAndPath("minecraft", path + "_planks");
        return BuiltInRegistries.BLOCK.get(planks);
    }
}
