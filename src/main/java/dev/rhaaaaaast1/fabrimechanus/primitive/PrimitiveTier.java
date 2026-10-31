package dev.rhaaaaaast1.fabrimechanus.primitive;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** Low-tier tool material used by the crude stone/flint axes. */
public enum PrimitiveTier implements Tier {
    STONE(10, 2.0F, 0.0F, 15, ItemTags.STONE_TOOL_MATERIALS),
    FLINT(10, 2.2F, 0.5F, 15, ItemTags.STONE_TOOL_MATERIALS);

    private final int uses;
    private final float speed;
    private final float attackDamage;
    private final int enchantmentValue;
    private final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> repairTag;

    PrimitiveTier(int uses, float speed, float attackDamage, int enchantmentValue,
                  net.minecraft.tags.TagKey<net.minecraft.world.item.Item> repairTag) {
        this.uses = uses;
        this.speed = speed;
        this.attackDamage = attackDamage;
        this.enchantmentValue = enchantmentValue;
        this.repairTag = repairTag;
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return attackDamage;
    }

    @Override
    public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_WOODEN_TOOL;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(repairTag);
    }
}
