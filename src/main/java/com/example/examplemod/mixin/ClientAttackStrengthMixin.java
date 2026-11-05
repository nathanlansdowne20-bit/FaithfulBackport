
package com.example.examplemod.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public abstract class ClientAttackStrengthMixin {
    // Intentionally empty.
    // Do not force the client cooldown to full for spears.
}
