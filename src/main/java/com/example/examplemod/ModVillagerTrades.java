
package com.example.examplemod;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(
        modid = ExampleMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class ModVillagerTrades {

    private ModVillagerTrades() {
    }

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.LIBRARIAN) {
            return;
        }

        List<VillagerTrades.ItemListing> masterTrades =
                event.getTrades().get(5);

        if (masterTrades == null) {
            return;
        }

        for (int level = 1; level <= 3; level++) {
            ItemStack enchantedBook = EnchantedBookItem.createForEnchantment(
                    new EnchantmentInstance(ModEnchantments.LUNGE.get(), level)
            );

            int price = 15 + level * 5;

            masterTrades.add((trader, random) ->
                    new MerchantOffer(
                            new ItemStack(Items.EMERALD, price),
                            enchantedBook.copy(),
                            12,
                            30,
                            0.05F
                    )
            );
        }
    }
}
