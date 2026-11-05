
package com.example.examplemod.mixin;

import com.example.examplemod.SpearClientInput;
import com.example.examplemod.SpearItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandRenderer.class)
public abstract class SpearFirstPersonAttackMixin {

    @Redirect(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmAttackTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V"
            )
    )
    private void faithfulbackport$replaceSpearSwing(
            ItemInHandRenderer renderer,
            PoseStack poseStack,
            HumanoidArm arm,
            float swingProgress
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null) {
            faithfulbackport$vanillaSwing(poseStack, arm, swingProgress);
            return;
        }

        InteractionHand hand = arm == player.getMainArm()
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;

        ItemStack stack = player.getItemInHand(hand);

        if (!(stack.getItem() instanceof SpearItem)) {
            faithfulbackport$vanillaSwing(poseStack, arm, swingProgress);
            return;
        }

        float progress =
                SpearClientInput.getSpearAttackAnimationProgress(
                        minecraft.getFrameTime()
                );

        if (progress < 0.0F) {
            faithfulbackport$vanillaSwing(poseStack, arm, swingProgress);
            return;
        }

        float forward;
        float pitchDegrees;

        if (progress < 0.24F) {
            // Draw straight back, with almost no rotation.
            float t = smoothstep(progress / 0.24F);
            forward = lerp(0.0F, 0.22F, t);
            pitchDegrees = lerp(0.0F, 2.0F, t);
        } else if (progress < 0.46F) {
            // Drive the spear forward, tip-first.
            float t = smoothstep((progress - 0.24F) / 0.22F);
            forward = lerp(0.22F, -0.72F, t);
            pitchDegrees = lerp(2.0F, -2.0F, t);
        } else if (progress < 0.56F) {
            // Short extension hold.
            forward = -0.72F;
            pitchDegrees = -2.0F;
        } else {
            // Return to neutral.
            float t = smoothstep((progress - 0.56F) / 0.44F);
            forward = lerp(-0.72F, 0.0F, t);
            pitchDegrees = lerp(-2.0F, 0.0F, t);
        }

        // No yaw or roll: avoid the sideways sweep of a sword.
        poseStack.translate(0.0F, 0.0F, forward);
        poseStack.mulPose(Axis.XP.rotationDegrees(pitchDegrees));
    }

    private static float smoothstep(float value) {
        float t = Mth.clamp(value, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static float lerp(float start, float end, float amount) {
        return start + (end - start) * amount;
    }

    private static void faithfulbackport$vanillaSwing(
            PoseStack poseStack,
            HumanoidArm arm,
            float swingProgress
    ) {
        int direction = arm == HumanoidArm.RIGHT ? 1 : -1;

        float f = Mth.sin(
                swingProgress * swingProgress * (float) Math.PI
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        direction * (45.0F - 20.0F * f)
                )
        );

        float f1 = Mth.sin(
                Mth.sqrt(swingProgress) * (float) Math.PI
        );

        poseStack.mulPose(
                Axis.ZP.rotationDegrees(direction * f1 * -20.0F)
        );

        poseStack.mulPose(
                Axis.XP.rotationDegrees(f1 * -80.0F)
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(direction * -45.0F)
        );
    }
}
