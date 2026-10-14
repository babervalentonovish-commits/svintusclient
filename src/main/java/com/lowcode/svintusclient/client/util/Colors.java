package com.lowcode.svintusclient.client.util;

public final class Colors {

    private Colors() {
    }

    public static int alpha(int c) {
        return c >>> 24;
    }

    public static int withAlpha(int c, float alpha) {
        int a = Math.round(Math.max(0f, Math.min(1f, alpha)) * 255f);
        return (a << 24) | (c & 0xFFFFFF);
    }

    public static int fade(int c, float factor) {
        int a = Math.round(alpha(c) * Math.max(0f, Math.min(1f, factor)));
        return (a << 24) | (c & 0xFFFFFF);
    }

    public static int lerp(int from, int to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = (int) (alpha(from) + (alpha(to) - alpha(from)) * t);
        int r = (int) (((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t);
        int g = (int) (((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}