package dev.bedrockfeel.mixin;

import dev.bedrockfeel.BedrockConstants;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the Java hand-swing with the Bedrock formulas from animation.player.attack.rotations.
 *
 * Plan: at HEAD we stash handSwingProgress and set it to 0 so vanilla applies no swing.
 * Right after the super.setAngles call (and before the sleeve/pants overlay copy) we restore
 * the value and apply the Bedrock swing, so jacket/sleeves follow the new pose.
 */
@Mixin(PlayerEntityModel.class)
public abstract class PlayerModelMixin<T extends LivingEntity> extends BipedEntityModel<T> {

    @Unique private float bedrockfeel$savedSwing;

    protected PlayerModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(
        method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
        at = @At("HEAD")
    )
    private void bedrockfeel$stashSwing(LivingEntity entity, float limbAngle, float limbDistance,
                                        float animationProgress, float headYaw, float headPitch,
                                        CallbackInfo ci) {
        this.bedrockfeel$savedSwing = this.handSwingProgress;
        this.handSwingProgress = 0.0f;
    }

    @Inject(
        method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
            shift = At.Shift.AFTER
        )
    )
    private void bedrockfeel$applySwing(LivingEntity entity, float limbAngle, float limbDistance,
                                        float animationProgress, float headYaw, float headPitch,
                                        CallbackInfo ci) {
        float t = this.bedrockfeel$savedSwing;
        this.handSwingProgress = t;
        if (t <= 0.0f) {
            return;
        }

        boolean rightHanded = entity.getMainArm() == Arm.RIGHT;
        float side = rightHanded ? 1.0f : -1.0f;
        float yawSign = BedrockConstants.YAW_SIGN * side;

        // f = 1 - (1 - t)^4 ; s = sin(f * 180deg)
        float inv = 1.0f - t;
        float f = 1.0f - inv * inv * inv * inv;
        float s = MathHelper.sin(f * (float) Math.PI);

        // body Y rotation: sin(360 * sqrt(t)) * 5 degrees
        float bodyYawDeg = MathHelper.sin(MathHelper.sqrt(t) * (float) Math.PI * 2.0f)
                * BedrockConstants.ATTACK_BODY_YAW_DEG;
        this.body.yaw = (float) Math.toRadians(bodyYawDeg) * yawSign;

        // shared pitch term: -(s * 1.2 + sin(t * 180deg))
        float pitchTerm = -(s * 1.2f + MathHelper.sin(t * (float) Math.PI));

        ModelPart mainArm = rightHanded ? this.rightArm : this.leftArm;
        ModelPart offArm = rightHanded ? this.leftArm : this.rightArm;

        mainArm.pitch += (float) Math.toRadians(pitchTerm * BedrockConstants.ATTACK_MAIN_ARM_PITCH_DEG);
        offArm.pitch += (float) Math.toRadians(pitchTerm * BedrockConstants.ATTACK_OFF_ARM_PITCH_DEG);

        // main arm Y: s != 0 ? 90*s - 30 : 0
        if (s != 0.0f) {
            float yawDeg = BedrockConstants.ATTACK_MAIN_ARM_YAW_AMP_DEG * s
                    - BedrockConstants.ATTACK_MAIN_ARM_YAW_OFFSET_DEG;
            mainArm.yaw += (float) Math.toRadians(yawDeg) * yawSign;
        }

        // restore the real value; the renderer rewrites it every frame anyway
        this.handSwingProgress = t;
    }
}
