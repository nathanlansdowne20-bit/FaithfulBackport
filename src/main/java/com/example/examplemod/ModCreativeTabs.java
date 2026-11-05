
package com.example.examplemod;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(
                    Registries.CREATIVE_MODE_TAB,
                    ExampleMod.MODID
            );

    public static final RegistryObject<CreativeModeTab> MAIN_TAB =
            CREATIVE_TABS.register("main",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.faithfulbackport"))
                            .icon(() -> new ItemStack(ModItems.DIAMOND_SPEAR.get()))
                            .displayItems((parameters, output) -> {
                                output.accept(ModItems.DIAMOND_SPEAR.get());
                                output.accept(ModItems.NETHERITE_SPEAR.get());
                            })
                            .build());
}
