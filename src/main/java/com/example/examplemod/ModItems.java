
package com.example.examplemod;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(
                    ForgeRegistries.ITEMS,
                    ExampleMod.MODID
            );

    public static final RegistryObject<Item> DIAMOND_SPEAR =
            ITEMS.register("diamond_spear", () ->
                    new SpearItem(
                            Tiers.DIAMOND,
                            1,
                            -3.05F,
                            new Item.Properties().stacksTo(1)
                    )
            );

    public static final RegistryObject<Item> NETHERITE_SPEAR =
            ITEMS.register("netherite_spear", () ->
                    new SpearItem(
                            Tiers.NETHERITE,
                            1,
                            -3.13F,
                            new Item.Properties()
                                    .stacksTo(1)
                                    .fireResistant()
                    )
            );
}
