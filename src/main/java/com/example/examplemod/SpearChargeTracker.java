
package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = ExampleMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class SpearChargeTracker {

    private static final int CHARGE_WINDOW_TICKS = 20;
    private static final float FULL_CHARGE_THRESHOLD = 0.99F;

    private static float clientStoredCharge = 0.0F;
    private static int clientExpiryTick = -1;

    private static boolean sourceWasFullyCharged = false;
    private static boolean wasNonSpear = false;
    private static boolean windowStarted = false;

    private SpearChargeTracker() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        if (player.level().isClientSide
                && player == Minecraft.getInstance().player) {
            trackClient(player);
        }
    }

    private static void trackClient(Player player) {
        ItemStack main = player.getMainHandItem();
        boolean spearHeld = main.getItem() instanceof SpearItem;

        if (!spearHeld) {
            float charge = player.getAttackStrengthScale(0.0F);

            if (charge >= FULL_CHARGE_THRESHOLD) {
                clientStoredCharge = 1.0F;
                sourceWasFullyCharged = true;
            } else if (!sourceWasFullyCharged) {
                clientStoredCharge = Math.max(
                        clientStoredCharge,
                        charge
                );
            }

            wasNonSpear = true;
            return;
        }

        if (wasNonSpear
                && sourceWasFullyCharged
                && !windowStarted) {

            clientStoredCharge = 1.0F;
            clientExpiryTick =
                    player.tickCount + CHARGE_WINDOW_TICKS;

            windowStarted = true;
            sourceWasFullyCharged = false;

            ModNetwork.sendChargeReady(true);

            System.out.println(
                    "[FaithfulBackport] Full item/empty-hand charge "
                            + "transferred to spear; expiry tick="
                            + clientExpiryTick
            );
        }

        wasNonSpear = false;

        if (windowStarted
                && player.tickCount > clientExpiryTick) {
            clearClientCharge(true);
        }
    }

    public static float getStoredCharge(Player player) {
        if (player != Minecraft.getInstance().player) {
            return 0.0F;
        }

        if (windowStarted
                && clientExpiryTick >= 0
                && player.tickCount > clientExpiryTick) {
            clearClientCharge(true);
            return 0.0F;
        }

        return clientStoredCharge;
    }

    public static boolean hadFullEmptyHandCharge(Player player) {
        return getStoredCharge(player) >= 1.0F;
    }

    public static boolean hasStoredCharge(Player player) {
        return getStoredCharge(player) > 0.0F;
    }

    public static void clearStoredCharge(Player player) {
        if (player == Minecraft.getInstance().player) {
            clearClientCharge(true);
            wasNonSpear = false;
        }
    }

    public static void clearStoredChargeLocally(Player player) {
        if (player == Minecraft.getInstance().player) {
            clearClientCharge(false);
            wasNonSpear = false;
        }
    }

    private static void clearClientCharge(boolean notifyServer) {
        clientStoredCharge = 0.0F;
        clientExpiryTick = -1;
        sourceWasFullyCharged = false;
        windowStarted = false;

        if (notifyServer) {
            ModNetwork.sendChargeReady(false);
        }
    }
}
