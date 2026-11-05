
package com.example.examplemod;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(
        modid = ExampleMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ModItemProperties {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    ModItems.DIAMOND_SPEAR.get(),
                    new ResourceLocation(ExampleMod.MODID, "in_gui"),
                    (stack, level, entity, seed) -> 0.0F
            );

            ItemProperties.register(
                    ModItems.NETHERITE_SPEAR.get(),
                    new ResourceLocation(ExampleMod.MODID, "in_gui"),
                    (stack, level, entity, seed) -> 0.0F
            );
        });
    }
}
