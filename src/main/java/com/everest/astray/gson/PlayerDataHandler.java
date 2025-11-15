package com.everest.astray.gson;

import com.everest.astray.ui.ToastHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

public class PlayerDataHandler {

    private static MinecraftServer server;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID uuid = player.getUuid();
            ensurePlayerDirectories(uuid);
        });
    }

    private static void ensurePlayerDirectories(UUID uuid) {
        File playbackFile = getPlayerPlaybackFile(uuid);
        File skillsDir = getPlayerSkillsDir(uuid);

        if (!skillsDir.exists()) skillsDir.mkdirs();
        if (!playbackFile.exists()) createDefaultPlaybackFile(playbackFile);
    }

    public static File getPlayerPlaybackFile(UUID uuid) {
        Path base = getPlayerBasePath(uuid);
        File musicDir = base.resolve("music").toFile();
        if (!musicDir.exists()) musicDir.mkdirs();
        return new File(musicDir, "playback-data.json");
    }

    public static File getPlayerSkillsDir(UUID uuid) {
        Path base = getPlayerBasePath(uuid);
        return base.resolve("skills").toFile();
    }

    private static Path getPlayerBasePath(UUID uuid) {
        if (server == null) throw new IllegalStateException("Server instance not available!");
        Path worldDir = server.getSavePath(WorldSavePath.ROOT);
        return worldDir.resolve("astray").resolve(uuid.toString());
    }

    private static void createDefaultPlaybackFile(File file) {
        JsonObject json = new JsonObject();
        json.addProperty("lastTrack", "");
        json.addProperty("time", 0.0);

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            ToastHelper.showErrorToast("A fatal error occurred", "Failed to create playback-data.json");
            System.err.println("[Astray] Failed to create playback-data.json: " + e.getMessage());
        }
    }

    public static JsonObject readPlaybackData(UUID uuid) {
        File file = getPlayerPlaybackFile(uuid);
        if (!file.exists()) {
            createDefaultPlaybackFile(file);
        }

        try (FileReader reader = new FileReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            ToastHelper.showErrorToast("A fatal error occurred", "Failed to read playback-data.json");
            System.err.println("[Astray] Failed to read playback-data.json: " + e.getMessage());
            return new JsonObject();
        }
    }

    public static void savePlaybackData(UUID uuid, String lastTrack, double time) {
        JsonObject json = new JsonObject();
        json.addProperty("lastTrack", lastTrack);
        json.addProperty("time", time);

        File file = getPlayerPlaybackFile(uuid);
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            ToastHelper.showErrorToast("A fatal error occurred", "Failed to save playback-data.json");
            System.err.println("[Astray] Failed to save playback-data.json: " + e.getMessage());
        }
    }
}
