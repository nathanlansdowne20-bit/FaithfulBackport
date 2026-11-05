
package com.example.examplemod;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = ExampleMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class SpearInteractionHandler {

    private static boolean hasSpear(Player player) {
        return isSpear(player.getMainHandItem())
                || isSpear(player.getOffhandItem());
    }

    private static boolean isSpear(ItemStack stack) {
        return stack.is(ModItems.DIAMOND_SPEAR.get())
                || stack.is(ModItems.NETHERITE_SPEAR.get());
    }

    @SubscribeEvent
    public static void onRightClickBlock(
            PlayerInteractEvent.RightClickBlock event
    ) {
        if (hasSpear(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(
            PlayerInteractEvent.RightClickItem event
    ) {
        if (hasSpear(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickEntity(
            PlayerInteractEvent.EntityInteract event
    ) {
        if (hasSpear(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickEntitySpecific(
            PlayerInteractEvent.EntityInteractSpecific event
    ) {
        if (hasSpear(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private SpearInteractionHandler() {
    }
}
