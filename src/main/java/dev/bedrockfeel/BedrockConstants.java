package dev.bedrockfeel;

/**
 * Values taken from Mojang/bedrock-samples (resource_pack/animations/player.animation.json
 * and elytra.animation.json). Angles are in DEGREES here; convert with Math.toRadians.
 *
 * Sign convention: the walk cycle in both editions has identical X-rotation signs
 * (left arm = +cos, right arm = -cos, left leg = -1.4*cos), so X rotations are applied as-is.
 * Y rotation is the one thing I could not confirm without running the game. If the swing
 * twists the wrong way in your test, flip YAW_SIGN to -1 and rebuild. That is the only change.
 */
public final class BedrockConstants {
    private BedrockConstants() {}

    public static final float YAW_SIGN = 1.0f;

    // animation.player.attack.rotations
    public static final float ATTACK_BODY_YAW_DEG = 5.0f;        // sin(360*sqrt(t)) * 5
    public static final float ATTACK_MAIN_ARM_PITCH_DEG = 30.0f; // -(sin(f*180)*1.2 + sin(t*180)) * 30
    public static final float ATTACK_OFF_ARM_PITCH_DEG = 10.0f;  // -(same) * 10
    public static final float ATTACK_MAIN_ARM_YAW_AMP_DEG = 90.0f;   // 90*sin(f*180) - 30
    public static final float ATTACK_MAIN_ARM_YAW_OFFSET_DEG = 30.0f;

    // elytra.animation.json
    public static final float ELYTRA_DEFAULT_PITCH_DEG = 15.0f;
    public static final float ELYTRA_DEFAULT_ROLL_DEG = 13.0f;   // left wing = -13, right = +13
    public static final float ELYTRA_SNEAK_PITCH_DEG = 20.0f;
    public static final float ELYTRA_SNEAK_YAW_DEG = 15.0f;
    public static final float ELYTRA_SNEAK_ROLL_DEG = 45.0f;
    // gliding: pitch lerp(15,20,ratio), roll lerp(-15,-90,ratio). Java vanilla already uses
    // the exact same numbers, so the mod leaves gliding untouched.

    // Reference only (not implemented yet, next stage):
    // animation.player.swim / crawl root: pos Y = (sin(pitch)*17 + 5)*swim_amount,
    //   pos Z = cos(pitch)*17*swim_amount, rot X = swim_amount*(90 + pitch)
    // animation.player.sneaking: root rot X = 28 - this, arms -5.7, legs -28 (almost identical to Java 1.20.1)
    // animation.player.bob: arms Z = +-(cos(life_time*103.2)*2.865 + 2.865) (identical to Java idle sway)
}
