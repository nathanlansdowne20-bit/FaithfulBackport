
package com.example.examplemod.mixin;

import com.example.examplemod.SpearItem;
import com.example.examplemod.SpearChargeTracker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class ClientAttackConsumeMixin {

    @Inject(method = "attack", at = @At("RETURN"))
    private void faithfulbackport$consumeChargeAfterAttack(
            Player player,
            Entity target,
            CallbackInfo ci
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (player != null
                && player == minecraft.player
                && player.getMainHandItem().getItem() instanceof SpearItem) {
            SpearChargeTracker.clearStoredCharge(player);
        }
    }
}
