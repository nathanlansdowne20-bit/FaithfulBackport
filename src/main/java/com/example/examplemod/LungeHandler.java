
package com.example.examplemod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class LungeHandler {

    private static final float FULL_CHARGE_THRESHOLD = 0.99F;

    private LungeHandler() {
    }

    @SubscribeEvent
    public static void onSpearAttack(AttackEntityEvent event) {
        if (event.isCanceled()
                || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack spear = getHeldSpear(player);

        if (spear.isEmpty()) {
            return;
        }

        int level = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.LUNGE.get(),
                spear
        );

        if (level <= 0) {
            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ModSounds.SPEAR_ATTACK.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }
    }

    public static void tryLunge(ServerPlayer player, float clientCharge) {
        if (player.isRemoved() || player.isSpectator()) {
            return;
        }

        SpearLocation location = findSpear(player);

        if (location == null) {
            System.out.println(
                    "[FaithfulBackport] Air Lunge rejected: no spear in hands or hotbar. "
                            + "mainItem=" + player.getMainHandItem().getItem()
                            + ", selectedSlot=" + player.getInventory().selected
            );
            return;
        }

        ItemStack spear = location.stack();

        int level = EnchantmentHelper.getItemEnchantmentLevel(
                ModEnchantments.LUNGE.get(),
                spear
        );

        if (level <= 0) {
            System.out.println(
                    "[FaithfulBackport] Air Lunge rejected: spear has no Lunge enchantment."
            );
            return;
        }

        if (player.isInWater()
                || player.isFallFlying()
                || player.isPassenger()) {
            return;
        }

        boolean storedChargeValid =
                ModNetwork.hasValidChargeWindow(player);

        boolean spearFullyCharged =
                Float.isFinite(clientCharge)
                        && clientCharge >= FULL_CHARGE_THRESHOLD;

        if (!storedChargeValid && !spearFullyCharged) {
            System.out.println(
                    "[FaithfulBackport] Air Lunge rejected: charge="
                            + clientCharge
                            + ", storedWindow="
                            + storedChargeValid
            );
            return;
        }

        if (player.getFoodData().getFoodLevel() < 6) {
            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ModSounds.SPEAR_LUNGE_FAIL.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );

            ModNetwork.clearChargeWindow(player);
            return;
        }

        var look = player.getLookAngle();

        double horizontalLength =
                Math.sqrt(look.x * look.x + look.z * look.z);

        if (horizontalLength < 0.001D) {
            return;
        }

        double strength = 0.8D + level * 0.4D;
        var current = player.getDeltaMovement();

        player.setDeltaMovement(
                current.x + look.x / horizontalLength * strength,
                current.y,
                current.z + look.z / horizontalLength * strength
        );

        player.hurtMarked = true;

        if (location.hand() != null) {
            spear.hurtAndBreak(
                    1,
                    player,
                    p -> p.broadcastBreakEvent(location.hand())
            );
        } else {
            spear.hurtAndBreak(1, player, p -> {
            });
            player.getInventory().setChanged();
        }

        player.causeFoodExhaustion(4.0F * level);
        player.resetAttackStrengthTicker();

        ModNetwork.clearChargeWindow(player);

        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSounds.SPEAR_LUNGE_SUCCESS.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );

        System.out.println(
                "[FaithfulBackport] Air Lunge succeeded: spearSlot="
                        + location.slot()
                        + ", held="
                        + (location.hand() != null)
        );
    }

    private static ItemStack getHeldSpear(Player player) {
        SpearLocation location = findHeldSpear(player);

        return location == null
                ? ItemStack.EMPTY
                : location.stack();
    }

    private static SpearLocation findHeldSpear(Player player) {
        if (player.getMainHandItem().getItem() instanceof SpearItem) {
            return new SpearLocation(
                    player.getMainHandItem(),
                    InteractionHand.MAIN_HAND,
                    player.getInventory().selected
            );
        }

        if (player.getOffhandItem().getItem() instanceof SpearItem) {
            return new SpearLocation(
                    player.getOffhandItem(),
                    InteractionHand.OFF_HAND,
                    -1
            );
        }

        return null;
    }

    private static SpearLocation findSpear(ServerPlayer player) {
        SpearLocation held = findHeldSpear(player);

        if (held != null) {
            return held;
        }

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);

            if (stack.getItem() instanceof SpearItem) {
                return new SpearLocation(stack, null, slot);
            }
        }

        return null;
    }

    private record SpearLocation(
            ItemStack stack,
            InteractionHand hand,
            int slot
    ) {
    }
}
