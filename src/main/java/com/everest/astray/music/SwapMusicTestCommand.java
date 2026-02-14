package com.everest.astray.music;

import com.everest.astray.ui.ToastHelper;
import de.keksuccino.melody.resources.audio.SimpleAudioFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class SwapMusicTestCommand {

    private static DynamicMusicHandler trackA = new DynamicMusicHandler();
    private static DynamicMusicHandler trackB = new DynamicMusicHandler();
    private static boolean usingA = true;
    private static boolean loaded = false;

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("swapmusic")
                    .executes(context -> {
                        MinecraftClient client = MinecraftClient.getInstance();

                        client.execute(() -> {
                            try {
                                if (!loaded) {
                                    trackA = MusicLoader.getHandler("astray:music/obstructed-vision-chill.ogg");
                                    trackB = MusicLoader.getHandler("astray:music/obstructed-vision-battle.ogg");
                                    loaded = true;
                                }

                                if (!trackA.isPlaying() && !trackB.isPlaying()) {
                                    trackA.play(0f, true);
                                    usingA = true;
                                    ToastHelper.showMusicToast("Now Playing", "Obstructed Vision (Chill) - Bashful");
                                    return;
                                }

                                DynamicMusicHandler current = usingA ? trackA : trackB;
                                DynamicMusicHandler other = usingA ? trackB : trackA;
                                float time = current.getTime();

                                other.setVolume(0f);
                                other.play(time, true);

                                new Thread(() -> {
                                    int steps = 30;
                                    float fadeDuration = 1.5f;
                                    for (int i = 0; i <= steps; i++) {
                                        float t = i / (float) steps;
                                        current.setVolume(1f - t);
                                        other.setVolume(t);
                                        try { Thread.sleep((long)(fadeDuration * 1000 / steps)); } catch (InterruptedException ignored) {}
                                    }
                                    current.stop();
                                    usingA = !usingA;
                                }, "Melody-Crossfade").start();

                                ToastHelper.showMusicToast(
                                        "Now Playing",
                                        (usingA ? "Obstructed Vision (Battle)" : "Obstructed Vision (Chill)") + " - Bashful"
                                );

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                        return 1;
                    })
            );
        });
    }
}