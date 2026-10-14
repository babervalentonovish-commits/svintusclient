package com.lowcode.svintusclient.client.gui;

import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.util.Colors;
import net.minecraft.client.gui.DrawContext;

public final class Widgets {

    public static final float SWITCH_W = 20f;
    public static final float SWITCH_H = 11f;

    private Widgets() {
    }

    public static void toggle(DrawContext ctx, float x, float y, float t, float hover) {
        float r = SWITCH_H / 2f;
        Render2D.rect(ctx, x, y, SWITCH_W, SWITCH_H, r, Colors.lerp(0xFF25262E, 0xFF2D2E38, hover));
        if (t > 0.01f) {
            Render2D.shadow(ctx, x, y, SWITCH_W, SWITCH_H, r, 6f, Theme.accent(0.30f * t));
            Render2D.gradientH(ctx, x, y, SWITCH_W, SWITCH_H, r, Colors.fade(Theme.accent2(), t), Colors.fade(Theme.accent(), t));
        }
        float knob = SWITCH_H - 4f;
        float kx = x + 2f + (SWITCH_W - 4f - knob) * t;
        Render2D.shadow(ctx, kx, y + 2f, knob, knob, knob / 2f, 2.5f, 0x55000000);
        Render2D.circle(ctx, kx + knob / 2f, y + SWITCH_H / 2f, knob / 2f, Colors.lerp(0xFF6C6E7C, 0xFFFFFFFF, t));
    }

    public static boolean inside(float mx, float my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}