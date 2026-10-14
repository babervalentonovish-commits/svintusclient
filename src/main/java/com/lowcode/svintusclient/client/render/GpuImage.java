package com.lowcode.svintusclient.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureSetup;

import java.awt.image.BufferedImage;

public final class GpuImage extends NativeImageBackedTexture {

    private boolean dirty;

    public GpuImage(String name, NativeImage image) {
        super(() -> name, image);
        this.sampler = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
    }

    public static NativeImage blank(int width, int height) {
        NativeImage image = new NativeImage(width, height, false);
        image.fillRect(0, 0, width, height, 0x00FFFFFF);
        return image;
    }

    public static void blitAlpha(BufferedImage src, NativeImage dst, int dstX, int dstY) {
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int a = src.getRGB(x, y) >>> 24;
                dst.setColorArgb(dstX + x, dstY + y, (a << 24) | 0xFFFFFF);
            }
        }
    }

    public void markDirty() {
        dirty = true;
    }

    public TextureSetup setup() {
        if (dirty) {
            upload();
            dirty = false;
        }
        return TextureSetup.of(getGlTextureView(), getSampler());
    }
}