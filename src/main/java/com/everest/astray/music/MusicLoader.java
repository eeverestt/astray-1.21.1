package com.everest.astray.music;

import com.everest.astray.Astray;
import de.keksuccino.melody.resources.audio.SimpleAudioFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MusicLoader {
    private static final Map<String, DynamicMusicHandler> HANDLERS = new HashMap<>();
    private static final List<String> TRACK_PATHS = new ArrayList<>();
    private static final String MUSIC_DIR = "music";

    public static CompletableFuture<Void> loadAllMusic() {
        try {
            Collection<Identifier> found =
                    MinecraftClient.getInstance()
                            .getResourceManager()
                            .findResources(MUSIC_DIR, path -> path.getPath().endsWith(".ogg")).keySet();

            if (found.isEmpty()) {
                Astray.LOGGER.severe("[Astray] No .ogg files found in assets/astray/music/");
                return CompletableFuture.completedFuture(null);
            }

            Astray.LOGGER.info("[Astray] Found " + found.size() + " music tracks.");

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (Identifier id : found) {
                String path = id.toString();
                TRACK_PATHS.add(path);

                if (HANDLERS.containsKey(path)) continue;

                DynamicMusicHandler handler = new DynamicMusicHandler();
                HANDLERS.put(path, handler);

                Astray.LOGGER.info("[Astray] Loading track: " + path);

                futures.add(handler.load(path, SimpleAudioFactory.SourceType.RESOURCE_LOCATION));
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(MusicLoader::playComa);

        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }
    }

    private static void playComa() {
        Optional<String> comaId = TRACK_PATHS.stream()
                .filter(p -> p.endsWith("coma.ogg"))
                .findFirst();

        if (comaId.isEmpty()) {
            Astray.LOGGER.severe("[Astray] coma.ogg not found!");
            return;
        }

        String path = comaId.get();
        Astray.LOGGER.info("[Astray] Playing coma.ogg: " + path);

        DynamicMusicHandler handler = HANDLERS.get(path);
        if (handler != null) {
            handler.fadeIn(3f, true);
        } else {
            Astray.LOGGER.severe("[Astray] Handler for coma.ogg not found!");
        }
    }

    public static DynamicMusicHandler getHandler(String trackPath) {
        return HANDLERS.get(trackPath);
    }

    public static List<String> getAllTracks() {
        return new ArrayList<>(TRACK_PATHS);
    }
}
