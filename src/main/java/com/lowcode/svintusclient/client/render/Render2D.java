package com.lowcode.svintusclient.client.render;

import com.lowcode.svintusclient.client.util.Colors;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

public final class Render2D {

    private static float alpha = 1f;
    private static float pixelScale = 2f;

    private Render2D() {
    }

    public static void setAlpha(float a) {
        alpha = Math.max(0f, Math.min(1f, a));
    }

    public static float alpha() {
        return alpha;
    }

    public static void setPixelScale(float scale) {
        pixelScale = Math.max(0.5f, scale);
    }

    public static float pixelScale() {
        return pixelScale;
    }

    public static int applyAlpha(int color) {
        return alpha >= 1f ? color : Colors.fade(color, alpha);
    }

    public static void rect(DrawContext ctx, float x, float y, float w, float h, float radius, int color) {
        shape(ctx, Pipelines.ROUND_RECT, TextureSetup.empty(), x, y, w, h, 0, radius, 0, color, color, color, color, true);
    }

    public static void rect(DrawContext ctx, float x, float y, float w, float h, float radius,
                            int topLeft, int topRight, int bottomRight, int bottomLeft) {
        shape(ctx, Pipelines.ROUND_RECT, TextureSetup.empty(), x, y, w, h, 0, radius, 0, topLeft, topRight, bottomRight, bottomLeft, true);
    }

    public static void gradientH(DrawContext ctx, float x, float y, float w, float h, float radius, int left, int right) {
        rect(ctx, x, y, w, h, radius, left, right, right, left);
    }

    public static void gradientV(DrawContext ctx, float x, float y, float w, float h, float radius, int top, int bottom) {
        rect(ctx, x, y, w, h, radius, top, top, bottom, bottom);
    }

    public static void outline(DrawContext ctx, float x, float y, float w, float h, float radius, float thickness, int color) {
        outline(ctx, x, y, w, h, radius, thickness, color, color, color, color);
    }

    public static void outline(DrawContext ctx, float x, float y, float w, float h, float radius, float thickness,
                               int topLeft, int topRight, int bottomRight, int bottomLeft) {
        shape(ctx, Pipelines.ROUND_RECT, TextureSetup.empty(), x, y, w, h, 0, radius, thickness, topLeft, topRight, bottomRight, bottomLeft, true);
    }

    public static void outlineGradientH(DrawContext ctx, float x, float y, float w, float h, float radius, float thickness,
                                        int left, int right) {
        outline(ctx, x, y, w, h, radius, thickness, left, right, right, left);
    }

    public static void outlineGradientV(DrawContext ctx, float x, float y, float w, float h, float radius, float thickness,
                                        int top, int bottom) {
        outline(ctx, x, y, w, h, radius, thickness, top, top, bottom, bottom);
    }

    public static void shadow(DrawContext ctx, float x, float y, float w, float h, float radius, float softness, int color) {
        shape(ctx, Pipelines.ROUND_SHADOW, TextureSetup.empty(), x, y, w, h, softness, radius, softness, color, color, color, color, true);
    }

    public static void circle(DrawContext ctx, float cx, float cy, float r, int color) {
        rect(ctx, cx - r, cy - r, r * 2, r * 2, r, color);
    }

    public static void circleOutline(DrawContext ctx, float cx, float cy, float r, float thickness, int color) {
        outline(ctx, cx - r, cy - r, r * 2, r * 2, r, thickness, color);
    }

    public static boolean blur(DrawContext ctx, float x, float y, float w, float h, float radius, int tint) {
        TextureSetup setup = BlurEffect.setup();
        if (setup == null) return false;
        shape(ctx, Pipelines.BLUR_RECT, setup, x, y, w, h, 0, radius, alpha, tint, tint, tint, tint, false);
        return true;
    }

    private static void shape(DrawContext ctx, RenderPipeline pipeline, TextureSetup setup, float x, float y, float w, float h,
                              float expand, float radius, float param, int tl, int tr, int br, int bl, boolean fade) {
        if (w <= 0 || h <= 0) return;
        if (fade) {
            tl = applyAlpha(tl);
            tr = applyAlpha(tr);
            br = applyAlpha(br);
            bl = applyAlpha(bl);
            if ((tl | tr | br | bl) >>> 24 == 0) return;
        } else if (param <= 0.001f) {
            return;
        }

        Matrix3x2f pose = new Matrix3x2f(ctx.getMatrices());
        ScreenRect scissor = ctx.scissorStack.peekLast();
        ScreenRect bounds = bounds(pose, x - expand, y - expand, x + w + expand, y + h + expand, scissor);
        if (bounds == null) return;

        ctx.state.addSimpleElement(new ShapeRenderState(pipeline, setup, pose, x, y, w, h, expand, radius, param,
                tl, tr, br, bl, scissor, bounds));
    }

    public static void texture(DrawContext ctx, TextureSetup setup, float x, float y, float w, float h,
                               float u0, float v0, float u1, float v1, int color) {
        texturedQuads(ctx, setup, new float[]{x, y, x + w, y + h, u0, v0, u1, v1}, 1, color);
    }

    public static void texturedQuads(DrawContext ctx, TextureSetup setup, float[] quads, int count, int color) {
        if (count == 0) return;
        color = applyAlpha(color);
        if (color >>> 24 == 0) return;

        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            int o = i * 8;
            minX = Math.min(minX, quads[o]);
            minY = Math.min(minY, quads[o + 1]);
            maxX = Math.max(maxX, quads[o + 2]);
            maxY = Math.max(maxY, quads[o + 3]);
        }

        Matrix3x2f pose = new Matrix3x2f(ctx.getMatrices());
        ScreenRect scissor = ctx.scissorStack.peekLast();
        ScreenRect bounds = bounds(pose, minX, minY, maxX, maxY, scissor);
        if (bounds == null) return;

        ctx.state.addSimpleElement(new TexturedRenderState(RenderPipelines.GUI_TEXTURED, setup, pose,
                quads, count, color, scissor, bounds));
    }

    public static void pushScissor(DrawContext ctx, float x, float y, float w, float h) {
        ctx.enableScissor((int) Math.floor(x), (int) Math.floor(y), (int) Math.ceil(x + w), (int) Math.ceil(y + h));
    }

    public static void popScissor(DrawContext ctx) {
        ctx.disableScissor();
    }

    public static float snap(float v) {
        return Math.round(v * pixelScale) / pixelScale;
    }

    @Nullable
    private static ScreenRect bounds(Matrix3x2f pose, float x0, float y0, float x1, float y1, @Nullable ScreenRect scissor) {
        Vector2f a = pose.transformPosition(x0, y0, new Vector2f());
        Vector2f b = pose.transformPosition(x1, y0, new Vector2f());
        Vector2f c = pose.transformPosition(x1, y1, new Vector2f());
        Vector2f d = pose.transformPosition(x0, y1, new Vector2f());
        int minX = (int) Math.floor(Math.min(Math.min(a.x, b.x), Math.min(c.x, d.x)));
        int minY = (int) Math.floor(Math.min(Math.min(a.y, b.y), Math.min(c.y, d.y)));
        int maxX = (int) Math.ceil(Math.max(Math.max(a.x, b.x), Math.max(c.x, d.x)));
        int maxY = (int) Math.ceil(Math.max(Math.max(a.y, b.y), Math.max(c.y, d.y)));
        ScreenRect rect = new ScreenRect(minX, minY, Math.max(1, maxX - minX), Math.max(1, maxY - minY));
        return scissor != null ? scissor.intersection(rect) : rect;
    }
}