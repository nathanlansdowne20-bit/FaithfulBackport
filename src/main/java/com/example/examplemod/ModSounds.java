
package com.example.examplemod;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(
                    ForgeRegistries.SOUND_EVENTS,
                    ExampleMod.MODID
            );

    public static final RegistryObject<SoundEvent> SPEAR_ATTACK =
            register("spear_attack");

    public static final RegistryObject<SoundEvent> SPEAR_HIT =
            register("spear_hit");

    public static final RegistryObject<SoundEvent> SPEAR_LUNGE_SUCCESS =
            register("spear_lunge_success");

    public static final RegistryObject<SoundEvent> SPEAR_LUNGE_FAIL =
            register("spear_lunge_fail");

    public static final RegistryObject<SoundEvent> SPEAR_USE =
            register("spear_use");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(
                name,
                () -> SoundEvent.createVariableRangeEvent(
                        ExampleMod.id(name)
                )
        );
    }

    private ModSounds() {
    }
}
