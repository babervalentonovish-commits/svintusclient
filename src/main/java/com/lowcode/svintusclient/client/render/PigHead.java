package com.lowcode.svintusclient.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;

public final class PigHead {

    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/pig/temperate_pig.png");
    private static final float TEX = 64f;

    private PigHead() {
    }

    public static void draw(DrawContext ctx, float x, float y, float size) {
        AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(TEXTURE);
        TextureSetup setup = TextureSetup.of(texture.getGlTextureView(), RenderSystem.getSamplerCache().get(FilterMode.NEAREST));

        float ps = Render2D.pixelScale();
        float px = Math.max(8f, Math.round(size * ps / 8f) * 8f);
        float s = px / ps;
        float ox = Render2D.snap(x + (size - s) / 2f), oy = Render2D.snap(y + (size - s) / 2f);
        float texel = s / 8f;

        Render2D.texture(ctx, setup, ox, oy, s, s, 8 / TEX, 8 / TEX, 16 / TEX, 16 / TEX, 0xFFFFFFFF);
        Render2D.texture(ctx, setup, ox + texel * 2f, oy + texel * 4f, texel * 4f, texel * 3f,
                17 / TEX, 17 / TEX, 21 / TEX, 20 / TEX, 0xFFFFFFFF);
    }
}