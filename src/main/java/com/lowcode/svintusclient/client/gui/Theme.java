package com.lowcode.svintusclient.client.gui;

import com.lowcode.svintusclient.client.util.Anim;
import com.lowcode.svintusclient.client.util.Colors;

public final class Theme {

    public enum Accent {
        PERIWINKLE("Перивинкл", 0xFF8C9EFF, 0xFFB69CFF),
        ROSE("Роза", 0xFFE5737F, 0xFFF29E7C),
        VIOLET("Фиалка", 0xFF8A72F2, 0xFFCB7CF4),
        MINT("Мята", 0xFF5DD39E, 0xFF5BC0E8),
        SILVER("Серебро", 0xFFC2C6D0, 0xFF8D93A3);

        public final String title;
        public final int primary;
        public final int secondary;

        Accent(String title, int primary, int secondary) {
            this.title = title;
            this.primary = primary;
            this.secondary = secondary;
        }
    }

    public static final int SURFACE_RAISED = 0xFF1E1F27;
    public static final int TRACK = 0xFF23242C;
    public static final int TEXT = 0xFFE9EAF1;
    public static final int TEXT_DIM = 0xFF8D8F9B;
    public static final int TEXT_MUTE = 0xFF555762;

    private static Accent current = Accent.PERIWINKLE;
    private static int fromPrimary = current.primary;
    private static int fromSecondary = current.secondary;
    private static final Anim BLEND = new Anim(1f, 8f);

    private Theme() {
    }

    public static Accent current() {
        return current;
    }

    public static void setInstant(Accent accent) {
        current = accent;
        fromPrimary = accent.primary;
        fromSecondary = accent.secondary;
        BLEND.snap(1f);
    }

    public static void set(Accent accent) {
        if (accent == current) return;
        fromPrimary = accent();
        fromSecondary = accent2();
        current = accent;
        BLEND.snap(0f).to(1f);
    }

    public static int accent() {
        return Colors.lerp(fromPrimary, current.primary, BLEND.get());
    }

    public static int accent2() {
        return Colors.lerp(fromSecondary, current.secondary, BLEND.get());
    }

    public static int accent(float alpha) {
        return Colors.withAlpha(accent(), alpha);
    }

    public static int tint(int base, float amount) {
        return Colors.lerp(base, accent() | 0xFF000000, amount) & 0x00FFFFFF | (base & 0xFF000000);
    }
}