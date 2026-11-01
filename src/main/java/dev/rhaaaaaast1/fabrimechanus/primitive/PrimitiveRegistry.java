package dev.rhaaaaaast1.fabrimechanus.primitive;

import dev.rhaaaaaast1.fabrimechanus.Fabrimechanus;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registration for the primitive survival items and loose rock blocks. */
public final class PrimitiveRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Fabrimechanus.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Fabrimechanus.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Fabrimechanus.MODID);

    public static final DeferredHolder<Block, LooseRockBlock> SMALL_PEBBLE = BLOCKS.register("small_pebble",
            () -> new LooseRockBlock(rockProperties()));
    public static final DeferredHolder<Block, LooseRockBlock> SMALL_FLINT = BLOCKS.register("small_flint",
            () -> new LooseRockBlock(rockProperties()));

    public static final DeferredItem<ShardItem> STONE_SHARD =
            ITEMS.register("stone_shard", () -> new ShardItem(new Item.Properties()));
    public static final DeferredItem<ShardItem> FLINT_SHARD =
            ITEMS.register("flint_shard", () -> new ShardItem(new Item.Properties()));

    public static final DeferredItem<BlockItem> SMALL_PEBBLE_ITEM = ITEMS.register("small_pebble",
            () -> new PebbleBlockItem(SMALL_PEBBLE.get(), new Item.Properties(), () -> STONE_SHARD.get()));
    public static final DeferredItem<BlockItem> SMALL_FLINT_ITEM = ITEMS.register("small_flint",
            () -> new PebbleBlockItem(SMALL_FLINT.get(), new Item.Properties(), () -> FLINT_SHARD.get()));

    public static final DeferredItem<Item> PLANT_FIBER =
            ITEMS.registerSimpleItem("plant_fiber", new Item.Properties());
    public static final DeferredItem<Item> PLANT_CORD =
            ITEMS.registerSimpleItem("plant_cord", new Item.Properties());

    public static final DeferredItem<CrudeAxeItem> STONE_AXE = ITEMS.register("stone_axe",
            () -> new CrudeAxeItem(PrimitiveTier.STONE, axeProperties(PrimitiveTier.STONE)));
    public static final DeferredItem<CrudeAxeItem> FLINT_AXE = ITEMS.register("flint_axe",
            () -> new CrudeAxeItem(PrimitiveTier.FLINT, axeProperties(PrimitiveTier.FLINT)));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("primitive",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.fabrimechanus.primitive"))
                    .icon(() -> new ItemStack(SMALL_PEBBLE_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(SMALL_PEBBLE_ITEM.get());
                        output.accept(SMALL_FLINT_ITEM.get());
                        output.accept(STONE_SHARD.get());
                        output.accept(FLINT_SHARD.get());
                        output.accept(PLANT_FIBER.get());
                        output.accept(PLANT_CORD.get());
                        output.accept(STONE_AXE.get());
                        output.accept(FLINT_AXE.get());
                    })
                    .build());

    private PrimitiveRegistry() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
    }

    private static BlockBehaviour.Properties rockProperties() {
        return BlockBehaviour.Properties.of()
                .strength(0.15F)
                .sound(SoundType.STONE)
                .noCollission()
                .noOcclusion();
    }

    private static Item.Properties axeProperties(PrimitiveTier tier) {
        return new Item.Properties()
                .durability(tier.getUses())
                .attributes(AxeItem.createAttributes(tier, 2.5F, -3.2F));
    }
}
