package dev.bedrockfeel;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Client entrypoint.
 *
 * Also contains the flight recorder used to calibrate elytra + firework physics:
 * press J to start/stop recording. While gliding it writes one CSV row per tick to
 * <game dir>/bedrockfeel-flight.csv. Record the same flight in Java and in Bedrock
 * (with a coordinate display), compare the speed column, then we tune against the numbers.
 */
public class BedrockFeelClient implements ClientModInitializer {

    private static KeyBinding recordKey;
    private static boolean recording = false;
    private static Path csv;
    private static long tickCounter = 0;

    @Override
    public void onInitializeClient() {
        recordKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.bedrockfeel.record", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.misc"));
        csv = FabricLoader.getInstance().getGameDir().resolve("bedrockfeel-flight.csv");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (recordKey.wasPressed()) {
                toggle(client);
            }
            if (recording) {
                record(client);
            }
        });
    }

    private static void toggle(MinecraftClient client) {
        recording = !recording;
        if (recording) {
            tickCounter = 0;
            try {
                Files.writeString(csv, "tick,gliding,boosted,vx,vy,vz,horizontal_speed,total_speed,yaw,pitch\n",
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                recording = false;
            }
        }
        if (client.player != null) {
            client.player.sendMessage(Text.literal(recording
                ? "[BedrockFeel] recording flight to " + csv.getFileName()
                : "[BedrockFeel] recording stopped"), true);
        }
    }

    private static void record(MinecraftClient client) {
        if (client.player == null || client.world == null || !client.player.isFallFlying()) {
            return;
        }
        Vec3d v = client.player.getVelocity();
        Box box = client.player.getBoundingBox().expand(8.0);
        List<FireworkRocketEntity> rockets = client.world.getEntitiesByClass(
            FireworkRocketEntity.class, box, r -> r.getOwner() == client.player);
        boolean boosted = !rockets.isEmpty();
        double horizontal = Math.sqrt(v.x * v.x + v.z * v.z);
        String row = String.format("%d,%d,%d,%.5f,%.5f,%.5f,%.5f,%.5f,%.2f,%.2f%n",
            tickCounter++, 1, boosted ? 1 : 0, v.x, v.y, v.z, horizontal, v.length(),
            client.player.getYaw(), client.player.getPitch());
        try {
            Files.writeString(csv, row, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }
}
