
package com.example.examplemod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class SpearItem extends SwordItem {

    public SpearItem(
            Tier tier,
            int attackDamageModifier,
            float attackSpeedModifier,
            Properties properties
    ) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            level.playLocalSound(
                    player.getX(), player.getY(), player.getZ(),
                    ModSounds.SPEAR_USE.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F, false
            );
        }

        return InteractionResultHolder.sidedSuccess(
                stack, level.isClientSide
        );
    }

    @Override
    public void initializeClient(
            Consumer<IClientItemExtensions> consumer
    ) {
        consumer.accept(new IClientItemExtensions() {

            @Override
            public boolean applyForgeHandTransform(
                    PoseStack poseStack,
                    LocalPlayer player,
                    HumanoidArm arm,
                    ItemStack stack,
                    float partialTick,
                    float equipProcess,
                    float swingProcess
            ) {
                float brace = smooth(
                        SpearClientInput.getBraceProgress(partialTick)
                );

                if (brace > 0.0F) {
                    float movement =
                            brace * brace * (3.0F - 2.0F * brace);

                    poseStack.translate(
                            0.5F * movement,
                            -0.5F * movement,
                            -1.7F * movement
                    );

                    poseStack.mulPose(
                            Axis.XP.rotationDegrees(-90.0F * movement)
                    );

                    poseStack.mulPose(
                            Axis.YP.rotationDegrees(90.0F * movement)
                    );

                    poseStack.scale(
                            1.0F + 0.8F * brace,
                            1.0F + 0.5F * brace,
                            1.0F + 0.5F * brace
                    );

                    return true;
                }

                // Attack animation
                float progress =
                        SpearClientInput.getSpearAttackAnimationProgress(
                                partialTick
                        );

                if (progress < 0.0F) {
                    return false;
                }

                float forward;

                if (progress < 0.24F) {
                    float t = smooth(progress / 0.24F);
                    forward = lerp(0.0F, 0.20F, t);
                } else if (progress < 0.46F) {
                    float t = smooth((progress - 0.24F) / 0.22F);
                    forward = lerp(0.20F, -0.85F, t);
                } else if (progress < 0.56F) {
                    forward = -0.85F;
                } else {
                    float t = smooth((progress - 0.56F) / 0.44F);
                    forward = lerp(-0.85F, 0.0F, t);
                }

                poseStack.translate(0.40F, -0.27F, forward);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(13.0F));

                return true;
            }

            private float smooth(float value) {
                float t = Math.max(0.0F, Math.min(1.0F, value));
                return t * t * (3.0F - 2.0F * t);
            }

            private float lerp(float start, float end, float t) {
                return start + (end - start) * t;
            }
        });
    }
}
