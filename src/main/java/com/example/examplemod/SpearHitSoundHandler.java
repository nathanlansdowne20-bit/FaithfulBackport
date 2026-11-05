package com.example.examplemod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class SpearHitSoundHandler {

    private SpearHitSoundHandler() {
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.getMainHandItem().getItem() instanceof SpearItem)) {
            return;
        }

        LivingEntity target = event.getEntity();

        player.level().playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                ModSounds.SPEAR_HIT.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }
}