package dev.rhaaaaaast1.fabrimechanus.primitive;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.rhaaaaaast1.fabrimechanus.Fabrimechanus;
import dev.rhaaaaaast1.fabrimechanus.tinkers.TinkersIntegration;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Replaces vanilla chest gear after loot generation. */
public final class PrimitiveLoot {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Fabrimechanus.MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ChestGearModifier>> CHEST_GEAR =
            SERIALIZERS.register("tconstruct_chest_gear", () -> ChestGearModifier.CODEC);

    private PrimitiveLoot() {
    }

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
    }

    public static final class ChestGearModifier extends LootModifier {
        public static final MapCodec<ChestGearModifier> CODEC =
                RecordCodecBuilder.mapCodec(instance -> codecStart(instance)
                        .apply(instance, ChestGearModifier::new));

        public ChestGearModifier(LootItemCondition[] conditions) {
            super(conditions);
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
            if (!TinkersIntegration.isLoaded()) {
                return loot;
            }
            RegistryAccess access = context.getLevel().registryAccess();
            RandomSource random = context.getRandom();
            for (int i = 0; i < loot.size(); i++) {
                ItemStack stack = loot.get(i);
                if (stack.isEmpty() || !PrimitiveTargets.contains(stack.getItem())) {
                    continue;
                }
                ItemStack replacement = PrimitiveLootReplacer.replace(stack, random, access);
                if (replacement.isEmpty()) {
                    loot.remove(i);
                    i--;
                } else {
                    loot.set(i, replacement);
                }
            }
            return loot;
        }

        @Override
        public MapCodec<? extends IGlobalLootModifier> codec() {
            return CODEC;
        }
    }
}
