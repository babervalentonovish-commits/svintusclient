package com.lowcode.svintusclient.client.hud;

import com.lowcode.svintusclient.client.module.ModuleManager;
import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.util.Colors;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.joml.Vector2f;

public final class HudDraw {

    private HudDraw() {
    }

    public static void glass(DrawContext ctx, float x, float y, float w, float h, float r) {
        Render2D.shadow(ctx, x, y, w, h, r, 10f, 0x60000000);
        boolean blurred = ModuleManager.HUD.blur.get()
                && Render2D.blur(ctx, x, y, w, h, r, Colors.withAlpha(0xFF08090E, ModuleManager.HUD.glass.get()));
        if (!blurred) Render2D.rect(ctx, x, y, w, h, r, 0xE00E0F14);
        Render2D.gradientV(ctx, x, y, w, Math.min(h, 14f), r, 0x0AFFFFFF, 0x00FFFFFF);
        Render2D.outlineGradientV(ctx, x, y, w, h, r, 0.8f, 0x24FFFFFF, 0x08FFFFFF);
    }

    public static void item(DrawContext ctx, ItemStack stack, float x, float y, float size) {
        if (stack.isEmpty()) return;
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(x, y);
        ctx.getMatrices().scale(size / 16f, size / 16f);
        ctx.drawItem(stack, 0, 0);
        ctx.getMatrices().popMatrix();
    }

    public static void durability(DrawContext ctx, ItemStack stack, float x, float y, float w) {
        if (stack.isEmpty() || !stack.isItemBarVisible()) return;
        float t = stack.getItemBarStep() / 13f;
        int color = 0xFF000000 | stack.getItemBarColor();
        Render2D.rect(ctx, x, y, w, 1.6f, 0.8f, 0x50000000);
        Render2D.rect(ctx, x, y, Math.max(1.6f, w * t), 1.6f, 0.8f, color);
    }

    public static int durabilityPercent(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageable() || stack.getMaxDamage() <= 0) return -1;
        return Math.round(100f * (stack.getMaxDamage() - stack.getDamage()) / stack.getMaxDamage());
    }

    public static void head(DrawContext ctx, LivingEntity entity, float x, float y, float size) {
        if (entity instanceof AbstractClientPlayerEntity player) {
            Identifier skin = player.getSkin().body().texturePath();
            AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(skin);
            TextureSetup setup = TextureSetup.of(texture.getGlTextureView(), RenderSystem.getSamplerCache().get(FilterMode.NEAREST));
            float t = 1f / 64f;
            Render2D.texture(ctx, setup, x, y, size, size, 8 * t, 8 * t, 16 * t, 16 * t, 0xFFFFFFFF);
            float pad = size * 0.04f;
            Render2D.texture(ctx, setup, x - pad, y - pad, size + pad * 2f, size + pad * 2f, 40 * t, 8 * t, 48 * t, 16 * t, 0xFFFFFFFF);
            return;
        }
        Vector2f p = ctx.getMatrices().transformPosition(x, y, new Vector2f());
        float k = ctx.getMatrices().m00();
        int x1 = Math.round(p.x), y1 = Math.round(p.y), s = Math.round(size * k);
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().identity();
        float extent = Math.max(entity.getHeight(), entity.getWidth());
        int scale = Math.max(4, Math.round(s * 0.9f / Math.max(0.6f, extent)));
        InventoryScreen.drawEntity(ctx, x1, y1, x1 + s, y1 + s, scale, 0.0625f, x1 + s / 2f + 6f, y1 + s * 0.35f, entity);
        ctx.getMatrices().popMatrix();
    }
}