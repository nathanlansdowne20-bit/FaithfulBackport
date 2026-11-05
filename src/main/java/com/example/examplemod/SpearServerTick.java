
package com.example.examplemod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(
        modid = ExampleMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class SpearServerTick {

    private static final String BRACING_KEY =
            "faithfulbackport_bracing";
    private static final String COOLDOWN_KEY =
            "faithfulbackport_joust_hits";

    private static final double MIN_SPEED = 0.12D;
    private static final double MAX_SPEED = 1.50D;
    private static final double REACH = 4.0D;
    private static final int HIT_COOLDOWN_TICKS = 10;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.player.level().isClientSide
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        CompoundTag data = player.getPersistentData();

        if (!data.getBoolean(BRACING_KEY)) {
            return;
        }

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        boolean hasSpear =
                main.is(ModItems.DIAMOND_SPEAR.get())
                        || main.is(ModItems.NETHERITE_SPEAR.get())
                        || off.is(ModItems.DIAMOND_SPEAR.get())
                        || off.is(ModItems.NETHERITE_SPEAR.get());

        if (!hasSpear) {
            data.putBoolean(BRACING_KEY, false);
            return;
        }

        Entity movingEntity = player.getVehicle() != null
                ? player.getVehicle()
                : player;

        Vec3 velocity = movingEntity.getDeltaMovement();
        double speed = velocity.length();

        if (speed < MIN_SPEED) {
            return;
        }

        Vec3 look = player.getLookAngle().normalize();

        if (velocity.normalize().dot(look) < 0.35D) {
            return;
        }

        double progress = Math.min(
                1.0D,
                (speed - MIN_SPEED) / (MAX_SPEED - MIN_SPEED)
        );

        float damage = (float) (
                player.getAttributeValue(Attributes.ATTACK_DAMAGE)
                        * (0.25D + progress * 3.75D)
        );

        AABB searchBox = player.getBoundingBox()
                .expandTowards(look.scale(REACH))
                .inflate(0.65D);

        List<Entity> targets = player.level().getEntities(
                player,
                searchBox,
                target -> target.isAlive()
                        && target.isPickable()
                        && !(target instanceof Player)
                        && (!(target instanceof LivingEntity living)
                        || !living.isAlliedTo(player))
        );

        Entity target = targets.stream()
                .filter(candidate -> {
                    Vec3 toTarget = candidate.getBoundingBox()
                            .getCenter()
                            .subtract(player.getEyePosition());

                    return toTarget.length() <= REACH + 1.0D
                            && toTarget.length() > 0.001D
                            && toTarget.normalize().dot(look) > 0.55D;
                })
                .min(Comparator.comparingDouble(
                        candidate -> candidate.distanceToSqr(player)
                ))
                .orElse(null);

        if (target == null) {
            return;
        }

        CompoundTag cooldowns = data.getCompound(COOLDOWN_KEY);
        String targetKey = target.getUUID().toString();

        long gameTime = player.level().getGameTime();
        long lastHit = cooldowns.getLong(targetKey);

        if (gameTime - lastHit < HIT_COOLDOWN_TICKS) {
            return;
        }

        double knockback = 0.3D + progress * 0.7D;

        // Armor stands register a jousting hit but take no damage,
        // so the spear cannot break them.
        if (target instanceof ArmorStand) {
            target.push(
                    -look.x * knockback,
                    0.10D,
                    -look.z * knockback
            );

            cooldowns.putLong(targetKey, gameTime);
            data.put(COOLDOWN_KEY, cooldowns);
            return;
        }

        DamageSource source =
                player.level().damageSources().playerAttack(player);

        if (target.hurt(source, damage)) {
            cooldowns.putLong(targetKey, gameTime);
            data.put(COOLDOWN_KEY, cooldowns);

            if (target instanceof LivingEntity living) {
                living.knockback(
                        knockback,
                        -look.x,
                        -look.z
                );
            } else {
                target.push(
                        -look.x * knockback,
                        0.15D,
                        -look.z * knockback
                );
            }
        }
    }

    private SpearServerTick() {
    }
}
