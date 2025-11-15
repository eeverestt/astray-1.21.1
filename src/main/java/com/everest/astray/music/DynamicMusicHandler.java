package com.everest.astray.music;

import de.keksuccino.melody.resources.audio.MelodyAudioException;
import de.keksuccino.melody.resources.audio.SimpleAudioFactory;
import de.keksuccino.melody.resources.audio.SimpleAudioFactory.SourceType;
import de.keksuccino.melody.resources.audio.openal.ALAudioClip;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

public class DynamicMusicHandler {
    public static DynamicMusicHandler active;

    public DynamicMusicHandler() {
        active = this;
    }

    private static Field sourceField;
    private ALAudioClip clip;
    private int sourceId;
    private boolean fading;
    private float volume = 1f;
    private float pausedPosition = 0f;
    private boolean paused = false;
    private String currentTrackPath = "";

    static {
        try {
            sourceField = ALAudioClip.class.getDeclaredField("source");
            sourceField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Void> load(String path, SourceType type) throws MelodyAudioException {
        this.currentTrackPath = path;
        return SimpleAudioFactory.ogg(path, type).thenAccept(c -> {
            this.clip = c;
            try {
                this.sourceId = (int) sourceField.get(c);
            } catch (Exception e) {
                throw new RuntimeException("Failed to access OpenAL source", e);
            }
        });
    }


    public void play(float startSeconds, boolean loop) {
        if (clip == null) return;
        AL10.alSourcef(sourceId, AL11.AL_SEC_OFFSET, startSeconds);
        AL10.alSourcei(sourceId, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
        AL10.alSourcePlay(sourceId);
        DynamicMusicHandler.active = this;
    }

    public void stop() {
        if (clip != null)
            AL10.alSourceStop(sourceId);
    }

    public void fadeOut(float durationSeconds) {
        if (clip == null || fading) return;
        fading = true;
        new Thread(() -> {
            float startVol = volume;
            int steps = 30;
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                setVolume(startVol * (1f - t));
                try { Thread.sleep((long) (durationSeconds * 1000f / steps)); } catch (InterruptedException ignored) {}
            }
            stop();
            setVolume(startVol);
            fading = false;
        }, "Melody-FadeOut").start();
    }

    public void pauseFadeOut(float durationSeconds, Runnable afterFade) {
        if (clip == null || fading) return;
        fading = true;
        new Thread(() -> {
            float startVol = volume;
            int steps = 30;
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                setVolume(startVol * (1f - t));
                try { Thread.sleep((long) (durationSeconds * 1000f / steps)); } catch (InterruptedException ignored) {}
            }
            pausedPosition = getTime();
            AL10.alSourcePause(sourceId);
            setVolume(startVol);
            fading = false;
            paused = true;

            if (afterFade != null) afterFade.run();
        }, "Melody-FadeOut").start();
    }


    public void fadeIn(float durationSeconds, boolean loop) {
        if (clip == null || fading) return;
        fading = true;
        setVolume(0f);
        play(0f, loop);
        new Thread(() -> {
            int steps = 30;
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                setVolume(t);
                try { Thread.sleep((long) (durationSeconds * 1000f / steps)); } catch (InterruptedException ignored) {}
            }
            setVolume(1f);
            fading = false;
        }, "Melody-FadeIn").start();
    }

    public void pauseFadeIn(float durationSeconds, boolean loop) {
        if (clip == null || fading) return;
        fading = true;
        setVolume(0f);
        resume(loop);
        new Thread(() -> {
            int steps = 30;
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                setVolume(t);
                try { Thread.sleep((long) (durationSeconds * 1000f / steps)); } catch (InterruptedException ignored) {}
            }
            setVolume(1f);
            fading = false;
        }, "Melody-FadeIn").start();
    }

    public void seek(float seconds) {
        if (clip != null)
            AL10.alSourcef(sourceId, AL11.AL_SEC_OFFSET, seconds);
    }

    public float getTime() {
        if (clip == null) return 0f;
        return AL10.alGetSourcef(sourceId, AL11.AL_SEC_OFFSET);
    }

    public boolean isPlaying() {
        if (clip == null) return false;
        int state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
        return state == AL10.AL_PLAYING;
    }

    public void setVolume(float vol) {
        if (clip == null) return;
        this.volume = Math.max(0f, Math.min(1f, vol));
        AL10.alSourcef(sourceId, AL10.AL_GAIN, this.volume);
    }

    public float getVolume() {
        return volume;
    }

    public void close() {
        if (clip != null) {
            clip.close();
            clip = null;
        }
    }

    public void pause() {
        if (clip == null || !isPlaying()) return;
        pausedPosition = getTime();
        AL10.alSourcePause(sourceId);
        paused = true;
    }

    public void resume(boolean loop) {
        if (clip == null || !paused) return;
        play(pausedPosition, loop);
        paused = false;
    }

    public String getCurrentTrackPath() {
        return currentTrackPath;
    }
}

