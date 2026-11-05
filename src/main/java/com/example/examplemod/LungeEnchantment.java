
package com.example.examplemod;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class LungeEnchantment extends Enchantment {

    public LungeEnchantment() {
        super(
                Rarity.UNCOMMON,
                EnchantmentCategory.WEAPON,
                new EquipmentSlot[] {
                        EquipmentSlot.MAINHAND
                }
        );
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 9;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof SpearItem
                || stack.is(Items.BOOK)
                || super.canEnchant(stack);
    }
}
