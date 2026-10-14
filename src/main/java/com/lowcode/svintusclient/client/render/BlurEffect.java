package com.lowcode.svintusclient.client.render;

import com.lowcode.svintusclient.client.SvintusclientClient;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.ClosableFactory;
import net.minecraft.client.util.memory.ObjectAllocator;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public final class BlurEffect {

    private static final Identifier EFFECT = Identifier.of(SvintusclientClient.MOD_ID, "gui_blur");
    private static final Pool POOL = new Pool();

    private static SimpleFramebuffer buffer;
    private static boolean ready;
    private static WeakReference<DrawContext> lastContext = new WeakReference<>(null);

    private BlurEffect() {
    }

    public static void capture(DrawContext ctx) {
        if (lastContext.get() == ctx) return;
        lastContext = new WeakReference<>(ctx);
        ready = false;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        Framebuffer main = client.getFramebuffer();
        int w = Math.max(1, main.textureWidth / 2), h = Math.max(1, main.textureHeight / 2);
        if (buffer == null) buffer = new SimpleFramebuffer("svintus_blur", w, h, false);
        else if (buffer.textureWidth != w || buffer.textureHeight != h) buffer.resize(w, h);

        PostEffectProcessor processor = client.getShaderLoader().loadPostEffect(EFFECT, DefaultFramebufferSet.MAIN_ONLY);
        if (processor == null) return;

        main.drawBlit(buffer.getColorAttachmentView());
        processor.render(buffer, POOL);
        ready = true;
    }

    public static boolean isReady() {
        return ready;
    }

    @Nullable
    public static TextureSetup setup() {
        if (!ready) return null;
        return TextureSetup.of(buffer.getColorAttachmentView(), RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
    }

    private static final class Pool implements ObjectAllocator {

        private final Map<ClosableFactory<?>, Object> cache = new HashMap<>();

        @Override
        @SuppressWarnings("unchecked")
        public <T> T acquire(ClosableFactory<T> factory) {
            Object cached = cache.remove(factory);
            T value = cached != null ? (T) cached : factory.create();
            factory.prepare(value);
            return value;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> void release(ClosableFactory<T> factory, T value) {
            Object previous = cache.put(factory, value);
            if (previous != null && previous != value) factory.close((T) previous);
        }
    }
}