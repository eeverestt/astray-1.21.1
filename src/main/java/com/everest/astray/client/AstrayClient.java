package com.everest.astray.client;

import com.everest.astray.Astray;
import com.everest.astray.client.render.entity.RiftEntityModel;
import com.everest.astray.client.render.entity.RiftEntityRenderer;
import com.everest.astray.init.AstrayEntities;
import com.everest.astray.music.SwapMusicTestCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;

public class AstrayClient implements ClientModInitializer {
    public static final EntityModelLayer MODEL_TEST_LAYER = new EntityModelLayer(Astray.id("test"), "main");

    @Override
    public void onInitializeClient() {
        SwapMusicTestCommand.register();
        EntityRendererRegistry.register(AstrayEntities.RIFT_ENTITY_TYPE, RiftEntityRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(MODEL_TEST_LAYER, RiftEntityModel::getTexturedModelData);
    }
}
