package dev.rhaaaaaast1.fabrimechanus.primitive;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.module.material.ToolMaterialHook;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.utils.ItemStackUtil;
import slimeknights.tconstruct.tools.TinkerTools;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Builds a Tinkers' tool/armor replacement for a vanilla gear item. */
public final class PrimitiveLootReplacer {
    private PrimitiveLootReplacer() {
    }

    public static ItemStack replace(ItemStack vanilla, RandomSource random, RegistryAccess access) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(vanilla.getItem());
        if (id == null) {
            return ItemStack.EMPTY;
        }
        String path = id.getPath();
        Item target = targetItem(path);
        if (target == null || target == Items.AIR || !(target instanceof IModifiable modifiable)) {
            return ItemStack.EMPTY;
        }

        ToolDefinition definition = modifiable.getToolDefinition();
        if (!definition.hasMaterials()) {
            return new ItemStack(target);
        }

        IMaterial material = pickMaterial(definition, desiredTier(path), random);
        if (material == null) {
            return ItemStack.EMPTY;
        }

        MaterialNBT.Builder builder = MaterialNBT.builder();
        for (MaterialStatsId stat : ToolMaterialHook.stats(definition)) {
            builder.add(material);
        }
        ItemStack result = ToolStack.createTool(target, definition, builder.build()).createStack();
        ItemStackUtil.ensurePacketSafe(result, access);
        return result;
    }

    @Nullable
    private static Item targetItem(String path) {
        if (path.endsWith("_sword")) return TinkerTools.sword.get();
        if (path.endsWith("_pickaxe")) return TinkerTools.pickaxe.get();
        if (path.endsWith("_axe")) return TinkerTools.handAxe.get();
        if (path.endsWith("_shovel")) return TinkerTools.mattock.get();
        if (path.endsWith("_hoe")) return TinkerTools.kama.get();

        if (path.endsWith("_helmet") || path.endsWith("_chestplate")
                || path.endsWith("_leggings") || path.endsWith("_boots")) {
            ArmorItem.Type type = switch (path.substring(path.lastIndexOf('_') + 1)) {
                case "helmet" -> ArmorItem.Type.HELMET;
                case "chestplate" -> ArmorItem.Type.CHESTPLATE;
                case "leggings" -> ArmorItem.Type.LEGGINGS;
                default -> ArmorItem.Type.BOOTS;
            };
            boolean leather = path.startsWith("leather_");
            return leather ? TinkerTools.travelersGear.get(type) : TinkerTools.plateArmor.get(type);
        }

        return switch (path) {
            case "bow" -> TinkerTools.longbow.get();
            case "crossbow" -> TinkerTools.crossbow.get();
            case "shield" -> TinkerTools.plateShield.get();
            default -> null;
        };
    }

    private static int desiredTier(String path) {
        if (path.startsWith("wooden") || path.startsWith("stone") || path.startsWith("leather")) {
            return 1;
        }
        if (path.startsWith("iron") || path.startsWith("golden")) {
            return 2;
        }
        if (path.startsWith("diamond")) {
            return 3;
        }
        if (path.startsWith("netherite")) {
            return 4;
        }
        return 2;
    }

    @Nullable
    private static IMaterial pickMaterial(ToolDefinition definition, int tier, RandomSource random) {
        IMaterialRegistry registry = MaterialRegistry.getInstance();
        List<MaterialStatsId> stats = ToolMaterialHook.stats(definition);
        List<IMaterial> candidates = new ArrayList<>();
        for (IMaterial material : registry.getVisibleMaterials()) {
            if (material.getTier() != tier) {
                continue;
            }
            boolean supportsAll = true;
            for (MaterialStatsId stat : stats) {
                if (registry.getMaterialStats(material.getIdentifier(), stat).isEmpty()) {
                    supportsAll = false;
                    break;
                }
            }
            if (supportsAll) {
                candidates.add(material);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(random.nextInt(candidates.size()));
    }
}
