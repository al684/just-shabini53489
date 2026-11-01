package dev.bedrockfeel.mixin;

import dev.bedrockfeel.BedrockConstants;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bedrock elytra poses from elytra.animation.json.
 *
 * Gliding already matches: Java uses the same lerp (15 -> 20 pitch, -15 -> -90 roll),
 * so it is left alone. The idle and sneaking poses differ, and Java also smooths the
 * wing angles over time for the local player while Bedrock sets them instantly.
 */
@Mixin(ElytraEntityModel.class)
public abstract class ElytraModelMixin {

    @Shadow @Final private ModelPart leftWing;
    @Shadow @Final private ModelPart rightWing;

    @Inject(
        method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
        at = @At("TAIL")
    )
    private void bedrockfeel$poses(LivingEntity entity, float limbAngle, float limbDistance,
                                   float animationProgress, float headYaw, float headPitch,
                                   CallbackInfo ci) {
        if (entity.isFallFlying()) {
            return;
        }

        float yawSign = BedrockConstants.YAW_SIGN;

        if (entity.isInSneakingPose()) {
            float pitch = (float) Math.toRadians(BedrockConstants.ELYTRA_SNEAK_PITCH_DEG);
            float yaw = (float) Math.toRadians(BedrockConstants.ELYTRA_SNEAK_YAW_DEG);
            float roll = (float) Math.toRadians(BedrockConstants.ELYTRA_SNEAK_ROLL_DEG);
            this.leftWing.pitch = pitch;
            this.rightWing.pitch = pitch;
            this.leftWing.yaw = -yaw * yawSign;
            this.rightWing.yaw = yaw * yawSign;
            this.leftWing.roll = -roll;
            this.rightWing.roll = roll;
        } else {
            float pitch = (float) Math.toRadians(BedrockConstants.ELYTRA_DEFAULT_PITCH_DEG);
            float roll = (float) Math.toRadians(BedrockConstants.ELYTRA_DEFAULT_ROLL_DEG);
            this.leftWing.pitch = pitch;
            this.rightWing.pitch = pitch;
            this.leftWing.yaw = 0.0f;
            this.rightWing.yaw = 0.0f;
            this.leftWing.roll = -roll;
            this.rightWing.roll = roll;
        }
    }
}
