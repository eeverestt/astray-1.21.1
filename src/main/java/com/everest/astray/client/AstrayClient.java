package com.everest.astray.client;

import com.everest.astray.Astray;
import com.everest.astray.client.render.entity.RiftEntityModel;
import com.everest.astray.client.render.entity.RiftEntityRenderer;
import com.everest.astray.init.AstrayEntities;
import com.everest.astray.music.MusicLoader;
import com.everest.astray.music.SwapMusicTestCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.EntityModelLayer;

public class AstrayClient implements ClientModInitializer {
    public static final EntityModelLayer MODEL_TEST_LAYER = new EntityModelLayer(Astray.id("test"), "main");
    private static boolean musicLoaded = false;

    @Override
    public void onInitializeClient() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!musicLoaded) {
                if (MinecraftClient.getInstance().getSoundManager() != null) {
                    musicLoaded = true;
                    MusicLoader.loadAllMusic().thenRun(() ->
                            System.out.println("[Astray] All music loaded and OpenAL ready!")
                    );
                }
            }
        });


        SwapMusicTestCommand.register();
        EntityRendererRegistry.register(AstrayEntities.RIFT_ENTITY_TYPE, RiftEntityRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(MODEL_TEST_LAYER, RiftEntityModel::getTexturedModelData);
    }
}
