
package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(
        modid = ExampleMod.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class SpearClientInput {

    private static boolean bracing = false;
    private static boolean wasRightPressed = false;

    private static float braceProgress = 0.0F;
    private static float previousBraceProgress = 0.0F;
    private static final float BRACE_ANIMATION_SPEED = 0.125F;

    private static boolean attackAnimationActive = false;
    private static int attackAnimationStartTick = -1;

    private static final float ATTACK_ANIMATION_DURATION = 12.0F;

    public static boolean isBracing() {
        return bracing;
    }

    public static float getBraceProgress(float partialTick) {
        return previousBraceProgress
                + (braceProgress - previousBraceProgress) * partialTick;
    }

    public static void startSpearAttackAnimation() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        attackAnimationStartTick = minecraft.player.tickCount;
        attackAnimationActive = true;
    }

    /**
     * Returns normalized animation progress from 0 to 1.
     * Returns -1 when no custom spear attack animation is active.
     */
    public static float getSpearAttackAnimationProgress(float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!attackAnimationActive || minecraft.player == null) {
            return -1.0F;
        }

        float elapsed = minecraft.player.tickCount
                - attackAnimationStartTick
                + partialTick;

        float progress = elapsed / ATTACK_ANIMATION_DURATION;

        if (progress >= 1.0F) {
            attackAnimationActive = false;
            return -1.0F;
        }

        return Math.max(0.0F, progress);
    }

    public static float getEmptyHandCharge() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return 0.0F;
        }

        return SpearChargeTracker.getStoredCharge(minecraft.player);
    }

    public static void consumeStoredCharge() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player != null) {
            SpearChargeTracker.clearStoredCharge(minecraft.player);
        }
    }

    private static void playUseSound() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player != null && minecraft.level != null) {
            minecraft.level.playLocalSound(
                    minecraft.player.getX(),
                    minecraft.player.getY(),
                    minecraft.player.getZ(),
                    ModSounds.SPEAR_USE.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F,
                    false
            );
        }
    }

    @SubscribeEvent
    public static void onInteractionInput(
            InputEvent.InteractionKeyMappingTriggered event
    ) {
        if (!event.isAttack()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null
                || minecraft.level == null
                || minecraft.screen != null) {
            return;
        }

        ItemStack main = minecraft.player.getMainHandItem();

        if (!(main.getItem() instanceof SpearItem)) {
            return;
        }

        float spearCharge =
                minecraft.player.getAttackStrengthScale(0.0F);

        float storedCharge =
                SpearChargeTracker.getStoredCharge(minecraft.player);

        float charge = Math.max(spearCharge, storedCharge);
        charge = Math.max(0.0F, Math.min(1.0F, charge));

        if (minecraft.hitResult instanceof EntityHitResult entityHit) {
            System.out.println(
                    "[FaithfulBackport] TARGET CLICK:"
                            + " target=" + entityHit.getEntity().getName().getString()
                            + ", spearCharge=" + spearCharge
                            + ", storedCharge=" + storedCharge
                            + ", sendingCharge=" + charge
                            + ", tick=" + minecraft.player.tickCount
            );

            if (charge >= 0.99F) {
                startSpearAttackAnimation();

                ModNetwork.sendChargeTransferAttack(
                        entityHit.getEntity().getId(),
                        charge
                );

                event.setCanceled(true);
                event.setSwingHand(false);

                minecraft.player.resetAttackStrengthTicker();

                SpearChargeTracker.clearStoredChargeLocally(
                        minecraft.player
                );

                System.out.println(
                        "[FaithfulBackport] TARGET CLICK: attack packet sent."
                );
            } else {
                System.out.println(
                        "[FaithfulBackport] TARGET CLICK: charge not full; "
                                + "attack packet not sent."
                );
            }

            return;
        }

        System.out.println(
                "[FaithfulBackport] Air-swing click:"
                        + " spearCharge=" + spearCharge
                        + ", storedCharge=" + storedCharge
                        + ", sending=" + charge
                        + ", tick=" + minecraft.player.tickCount
        );

        System.out.println(
                "[FaithfulBackport] Sending lunge:"
                        + " clientMain=" + main.getItem()
                        + ", clientOff="
                        + minecraft.player.getOffhandItem().getItem()
                        + ", selectedSlot="
                        + minecraft.player.getInventory().selected
                        + ", tick=" + minecraft.player.tickCount
        );

        startSpearAttackAnimation();

        ModNetwork.sendLungeRequest(charge);
        minecraft.player.resetAttackStrengthTicker();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        previousBraceProgress = braceProgress;

        if (minecraft.player == null || minecraft.level == null) {
            if (wasRightPressed) {
                ModNetwork.sendBraceState(false);
            }

            wasRightPressed = false;
            bracing = false;
            braceProgress = 0.0F;
            previousBraceProgress = 0.0F;
            attackAnimationActive = false;
            attackAnimationStartTick = -1;
            return;
        }

        ItemStack main = minecraft.player.getMainHandItem();
        ItemStack off = minecraft.player.getOffhandItem();

        boolean holdingSpear =
                main.getItem() instanceof SpearItem
                        || off.getItem() instanceof SpearItem;

        boolean rightPressed = holdingSpear
                && minecraft.screen == null
                && GLFW.glfwGetMouseButton(
                minecraft.getWindow().getWindow(),
                GLFW.GLFW_MOUSE_BUTTON_RIGHT
        ) == GLFW.GLFW_PRESS;

        if (rightPressed && !wasRightPressed) {
            playUseSound();
        }

        bracing = rightPressed;

        if (rightPressed != wasRightPressed) {
            ModNetwork.sendBraceState(rightPressed);
        }

        wasRightPressed = rightPressed;

        float target = rightPressed ? 1.0F : 0.0F;

        if (braceProgress < target) {
            braceProgress = Math.min(
                    target,
                    braceProgress + BRACE_ANIMATION_SPEED
            );
        } else if (braceProgress > target) {
            braceProgress = Math.max(
                    target,
                    braceProgress - BRACE_ANIMATION_SPEED
            );
        }
    }

    private SpearClientInput() {
    }
}
