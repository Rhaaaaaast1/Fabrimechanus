package dev.rhaaaaaast1.fabrimechanus.mixin;

import dev.rhaaaaaast1.fabrimechanus.compat.ComposterOutputContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Replaces the bone meal read and exposes the configured output to hoppers. */
@Mixin(ComposterBlock.class)
public abstract class ComposterBlockMixin {
    @Unique
    private static volatile Item fabrimechanus$output = Items.BONE_MEAL;

    @Unique
    public static void fabrimechanus$setOutput(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(rl);
        if (item != Items.AIR) {
            fabrimechanus$output = item;
        }
    }

    @Unique
    public static Item fabrimechanus$getOutput() {
        return fabrimechanus$output;
    }

    @Redirect(
            method = "extractProduce",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/item/Items;BONE_MEAL:Lnet/minecraft/world/item/Item;"
            )
    )
    private static Item fabrimechanus$composterOutput() {
        return fabrimechanus$getOutput();
    }

    @Inject(method = "getContainer", at = @At("HEAD"), cancellable = true)
    private void fabrimechanus$composterContainer(BlockState state, LevelAccessor level, BlockPos pos,
                                                   CallbackInfoReturnable<WorldlyContainer> cir) {
        if (state.getValue(ComposterBlock.LEVEL) == 8) {
            cir.setReturnValue(new ComposterOutputContainer(state, level, pos));
        }
    }
}
