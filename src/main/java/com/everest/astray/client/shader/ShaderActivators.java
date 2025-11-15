package com.everest.astray.client.shader;

import com.everest.astray.Astray;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.post.PostProcessingManager;
import foundry.veil.mixin.pipeline.client.PipelinePoseStackMixin;
import net.minecraft.client.gui.screen.option.CreditsAndAttributionScreen;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;

public class ShaderActivators {
    public static void blackHoleShaderActivator(Vec3d pos, float timer) {
        try {
            PostProcessingManager postProcessingManager = VeilRenderSystem.renderer().getPostProcessingManager();
            PostPipeline postPipeline = postProcessingManager.getPipeline(Identifier.of(Astray.MODID, "black_hole"));
            assert postPipeline != null;
            postPipeline.getUniformSafe("pos").setVector((float) pos.x, (float) pos.y, (float) pos.z);
            postPipeline.getUniformSafe("timer").setFloat(timer);
            postProcessingManager.runPipeline(postPipeline);
        } catch (Exception ignored) {}
    }
}
