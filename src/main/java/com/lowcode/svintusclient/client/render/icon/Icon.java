package com.lowcode.svintusclient.client.render.icon;

import com.lowcode.svintusclient.client.render.GpuImage;
import com.lowcode.svintusclient.client.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;

import java.util.HashMap;
import java.util.Map;

public final class Icon {

    private final String name;
    private final Svg svg;
    private final Map<Integer, GpuImage> cache = new HashMap<>();

    Icon(String name, Svg svg) {
        this.name = name;
        this.svg = svg;
    }

    public void draw(DrawContext ctx, float x, float y, float size, int color) {
        int px = Math.max(1, Math.round(size * Render2D.pixelScale()));
        GpuImage image = cache.computeIfAbsent(px, p -> {
            NativeImage pixels = GpuImage.blank(p, p);
            GpuImage.blitAlpha(svg.rasterize(p), pixels, 0, 0);
            return new GpuImage("svintus_icon_" + name + "_" + p, pixels);
        });
        float s = px / Render2D.pixelScale();
        Render2D.texture(ctx, image.setup(), Render2D.snap(x), Render2D.snap(y), s, s, 0, 0, 1, 1, color);
    }

    public void drawCentered(DrawContext ctx, float cx, float cy, float size, int color) {
        draw(ctx, cx - size / 2f, cy - size / 2f, size, color);
    }
}