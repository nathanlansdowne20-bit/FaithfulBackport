
package com.example.examplemod;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEnchantments {

    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(
                    ForgeRegistries.ENCHANTMENTS,
                    ExampleMod.MODID
            );

    public static final RegistryObject<Enchantment> LUNGE =
            ENCHANTMENTS.register(
                    "lunge",
                    LungeEnchantment::new
            );

    private ModEnchantments() {
    }
}
