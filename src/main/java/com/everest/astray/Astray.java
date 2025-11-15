package com.everest.astray;

import com.everest.astray.entity.RiftEntity;
import com.everest.astray.gson.PlayerDataHandler;
import com.everest.astray.init.AstrayEntities;
import com.everest.astray.world.IslandChunkGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Astray implements ModInitializer {
    public static final String MODID = "astray";

    @Override
    public void onInitialize() {
        PlayerDataHandler.init();

        AstrayEntities.init();
        FabricDefaultAttributeRegistry.register(AstrayEntities.RIFT_ENTITY_TYPE, RiftEntity.setAttributes());

        //Registry.register(Registries.CHUNK_GENERATOR, id("islands"), IslandChunkGenerator.CODEC);
    }

    public static Identifier id(String s) {
        return Identifier.of(MODID, s);
    }
}
