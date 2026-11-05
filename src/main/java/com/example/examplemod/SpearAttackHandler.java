
package com.example.examplemod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class SpearAttackHandler {

    private static final String BRACING_KEY =
            "faithfulbackport_bracing";

    private SpearAttackHandler() {
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();

        if (player.level().isClientSide
                || !(player instanceof ServerPlayer)) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();

        boolean isSpear =
                heldItem.is(ModItems.DIAMOND_SPEAR.get())
                        || heldItem.is(ModItems.NETHERITE_SPEAR.get());

        if (isSpear
                && player.getPersistentData().getBoolean(BRACING_KEY)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.getMainHandItem().getItem() instanceof SpearItem) {
            SpearChargeTracker.clearStoredCharge(player);
        }
    }

    public static void clearChargeAfterAttack(ServerPlayer player) {
        SpearChargeTracker.clearStoredCharge(player);
    }
}
