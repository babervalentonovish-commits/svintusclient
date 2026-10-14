package com.lowcode.svintusclient.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record TexturedRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        float[] quads,
        int quadCount,
        int color,
        @Nullable ScreenRect scissorArea,
        @Nullable ScreenRect bounds
) implements SimpleGuiElementRenderState {

    @Override
    public void setupVertices(VertexConsumer vc) {
        for (int i = 0; i < quadCount; i++) {
            int o = i * 8;
            float x0 = quads[o], y0 = quads[o + 1], x1 = quads[o + 2], y1 = quads[o + 3];
            float u0 = quads[o + 4], v0 = quads[o + 5], u1 = quads[o + 6], v1 = quads[o + 7];
            vc.vertex(pose, x0, y0).texture(u0, v0).color(color);
            vc.vertex(pose, x0, y1).texture(u0, v1).color(color);
            vc.vertex(pose, x1, y1).texture(u1, v1).color(color);
            vc.vertex(pose, x1, y0).texture(u1, v0).color(color);
        }
    }
}