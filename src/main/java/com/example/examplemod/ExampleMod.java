
package com.example.examplemod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ExampleMod.MODID)
public class ExampleMod {

    public static final String MODID = "faithfulbackport";

    public ExampleMod() {
        var modEventBus =
                FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        ModEnchantments.ENCHANTMENTS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        ModNetwork.register();
    }

    public static net.minecraft.resources.ResourceLocation id(
            String path
    ) {
        return new net.minecraft.resources.ResourceLocation(
                MODID,
                path
        );
    }
}
