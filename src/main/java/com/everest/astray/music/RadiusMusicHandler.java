package com.everest.astray.music;

import com.everest.astray.ui.ToastHelper;
import de.keksuccino.melody.resources.audio.SimpleAudioFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class RadiusMusicHandler {

    private static DynamicMusicHandler currentTrack = null;
    private static DynamicMusicHandler previousTrack = null;
    private static DynamicMusicHandler comaTrack = new DynamicMusicHandler();
    private static boolean loaded = false;
    private static final float FADE_DURATION = 2f;

    public static void init() {
        if (loaded) return;

        MinecraftClient.getInstance().execute(() -> {
            try {
                comaTrack = MusicLoader.getHandler("astray:music/high.ogg");
                loaded = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void playComaTrack() {
        MinecraftClient.getInstance().execute(() -> {
            if (!loaded) return;

            previousTrack = currentTrack;

            if (comaTrack.isPlaying()) return;

            if (currentTrack != null && currentTrack.isPlaying()) {
                currentTrack.pauseFadeOut(FADE_DURATION, () -> comaTrack.fadeIn(FADE_DURATION, true));
            } else {
                comaTrack.fadeIn(FADE_DURATION, true);
            }

            currentTrack = comaTrack;
            ToastHelper.showMusicToast("Now Playing", "High - Bashful");
        });
    }

    public static void restorePreviousTrack() {
        MinecraftClient.getInstance().execute(() -> {
            if (previousTrack == null) {
                // Stop coma track if no previous track
                if (currentTrack != null && currentTrack.isPlaying()) {
                    currentTrack.fadeOut(FADE_DURATION);
                    currentTrack = null;
                }
                return;
            }

            if (currentTrack != null && currentTrack.isPlaying()) {
                currentTrack.pauseFadeOut(FADE_DURATION, () -> previousTrack.fadeIn(FADE_DURATION, true));
            } else {
                previousTrack.fadeIn(FADE_DURATION, true);
            }

            currentTrack = previousTrack;
            previousTrack = null;
            ToastHelper.showMusicToast("Now Playing", "Restored Track");
        });
    }

    public static DynamicMusicHandler getCurrentTrack() {
        return currentTrack;
    }

    public static void stopCurrent() {
        MinecraftClient.getInstance().execute(() -> {
            if (currentTrack != null && currentTrack.isPlaying()) {
                currentTrack.fadeOut(FADE_DURATION);
                currentTrack = null;
            }
        });
    }
}
