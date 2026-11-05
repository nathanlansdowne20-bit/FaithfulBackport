
package com.example.examplemod.mixin;

import com.example.examplemod.ModNetwork;
import com.example.examplemod.SpearChargeTracker;
import com.example.examplemod.SpearItem;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class AttackStrengthMixin {

    @Shadow
    private ItemStack lastItemInMainHand;

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"
            )
    )
    private void faithfulbackport$preserveEmptyHandCharge(Player player) {
        ItemStack currentItem = player.getMainHandItem();

        boolean switchingEmptyHandToSpear =
                this.lastItemInMainHand.isEmpty()
                        && currentItem.getItem() instanceof SpearItem;

        if (!switchingEmptyHandToSpear) {
            player.resetAttackStrengthTicker();
        }
    }

    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getAttackStrengthScale(F)F"
            )
    )
    private float faithfulbackport$useStoredCharge(
            Player player,
            float partialTick
    ) {
        if (!(player.getMainHandItem().getItem() instanceof SpearItem)) {
            return player.getAttackStrengthScale(partialTick);
        }

        // Client: use the charge captured while the hand was empty.
        if (player.level().isClientSide) {
            float storedCharge =
                    SpearChargeTracker.getStoredCharge(player);

            if (storedCharge >= 1.0F) {
                return 1.0F;
            }
        }

        // Server: use the charge window received from the client.
        if (player instanceof ServerPlayer serverPlayer
                && ModNetwork.hasValidChargeWindow(serverPlayer)) {
            return 1.0F;
        }

        return player.getAttackStrengthScale(partialTick);
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void faithfulbackport$consumeStoredCharge(
            Entity target,
            CallbackInfo ci
    ) {
        Player player = (Player) (Object) this;

        if (!(player.getMainHandItem().getItem() instanceof SpearItem)) {
            return;
        }

        if (player.level().isClientSide) {
            SpearChargeTracker.clearStoredCharge(player);
        } else if (player instanceof ServerPlayer serverPlayer) {
            ModNetwork.clearChargeWindow(serverPlayer);
        }
    }
}
