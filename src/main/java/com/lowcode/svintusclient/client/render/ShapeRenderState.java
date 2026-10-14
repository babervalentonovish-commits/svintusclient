package com.lowcode.svintusclient.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record ShapeRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        float x, float y, float width, float height,
        float expand, float radius, float param,
        int colorTopLeft, int colorTopRight, int colorBottomRight, int colorBottomLeft,
        @Nullable ScreenRect scissorArea,
        @Nullable ScreenRect bounds
) implements SimpleGuiElementRenderState {

    @Override
    public void setupVertices(VertexConsumer vc) {
        float x0 = x - expand, y0 = y - expand;
        float x1 = x + width + expand, y1 = y + height + expand;
        float l0 = -expand, l1x = width + expand, l1y = height + expand;

        int sw = pack(width), sh = pack(height);
        int r = pack(radius), p = pack(param);

        vc.vertex(pose, x0, y0).color(colorTopLeft).texture(l0, l0).overlay(sw, sh).light(r, p);
        vc.vertex(pose, x0, y1).color(colorBottomLeft).texture(l0, l1y).overlay(sw, sh).light(r, p);
        vc.vertex(pose, x1, y1).color(colorBottomRight).texture(l1x, l1y).overlay(sw, sh).light(r, p);
        vc.vertex(pose, x1, y0).color(colorTopRight).texture(l1x, l0).overlay(sw, sh).light(r, p);
    }

    private static int pack(float value) {
        return Math.max(0, Math.min(Short.MAX_VALUE, Math.round(value * 8f)));
    }
}