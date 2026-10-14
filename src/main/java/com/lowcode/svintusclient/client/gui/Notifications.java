package com.lowcode.svintusclient.client.hud;

import com.lowcode.svintusclient.client.gui.Theme;
import com.lowcode.svintusclient.client.gui.Widgets;
import com.lowcode.svintusclient.client.module.Module;
import com.lowcode.svintusclient.client.module.ModuleManager;
import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.render.font.Fonts;
import com.lowcode.svintusclient.client.util.Anim;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Notifications {

    public static final float CARD_W = 150f;
    public static final float CARD_H = 27f;
    private static final float GAP = 4f;
    private static final int MAX = 5;
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private static final class Entry {
        final String title;
        final boolean enabled;
        final long start = System.currentTimeMillis();
        final Anim show = new Anim(0f, 9f).to(1f);
        final Anim offset = new Anim(14f);
        final Anim toggle;
        boolean placed;
        boolean leaving;

        Entry(String title, boolean enabled) {
            this.title = title;
            this.enabled = enabled;
            this.toggle = new Anim(enabled ? 0f : 1f, 7f).to(enabled);
        }

        void leave() {
            if (leaving) return;
            leaving = true;
            show.to(0f);
        }
    }

    private Notifications() {
    }

    public static void onToggle(Module module) {
        if (!ModuleManager.NOTIFICATIONS.isEnabled()) return;
        push(module.getName(), module.isEnabled());
    }

    public static void push(String title, boolean enabled) {
        for (Entry entry : ENTRIES) {
            if (!entry.leaving && entry.title.equals(title)) entry.leave();
        }
        ENTRIES.add(0, new Entry(title, enabled));
        int active = 0;
        for (Entry entry : ENTRIES) {
            if (!entry.leaving && ++active > MAX) entry.leave();
        }
    }

    public static boolean isEmpty() {
        return ENTRIES.isEmpty();
    }

    public static void render(DrawContext ctx, float ax, float ay, boolean right, boolean bottom) {
        long now = System.currentTimeMillis();
        long duration = (long) (ModuleManager.NOTIFICATIONS.duration.get() * 1000f);
        float stack = 0f;
        float prev = Render2D.alpha();

        Iterator<Entry> it = ENTRIES.iterator();
        while (it.hasNext()) {
            Entry entry = it.next();
            if (now - entry.start > duration) entry.leave();
            float a = entry.show.get();
            if (entry.leaving && a < 0.01f) {
                it.remove();
                continue;
            }
            float e = Anim.easeOutCubic(a);

            if (!entry.placed) {
                entry.offset.snap(stack);
                entry.placed = true;
            }
            float off = entry.offset.to(stack).get();
            stack += (CARD_H + GAP) * e;

            float cy = bottom ? ay - off : ay + off;
            float cx = ax + (1f - e) * 28f * (right ? 1f : -1f);

            Render2D.setAlpha(prev * e);
            HudDraw.glass(ctx, cx, cy, CARD_W, CARD_H, 7f);
            if (entry.enabled) Render2D.gradientH(ctx, cx, cy, 70f, CARD_H, 7f, Theme.accent(0.16f), Theme.accent(0f));

            Widgets.toggle(ctx, cx + 8f, cy + (CARD_H - Widgets.SWITCH_H) / 2f - 1f, entry.toggle.get(), 0f);

            float tx = cx + 8f + Widgets.SWITCH_W + 7f;
            Fonts.SEMIBOLD.drawCentered(ctx, Fonts.SEMIBOLD.trim(entry.title, 6.8f, CARD_W - (tx - cx) - 8f), tx, cy + 9f, 6.8f, Theme.TEXT);
            Fonts.REGULAR.drawCentered(ctx, entry.enabled ? "Модуль включён" : "Модуль выключен", tx, cy + 17.5f, 5.8f,
                    entry.enabled ? Theme.accent() : Theme.TEXT_DIM);

            float left = 1f - Math.min(1f, (now - entry.start) / (float) duration);
            float barW = CARD_W - 16f;
            Render2D.rect(ctx, cx + 8f, cy + CARD_H - 3.5f, barW, 1.2f, 0.6f, 0x12FFFFFF);
            Render2D.gradientH(ctx, cx + 8f, cy + CARD_H - 3.5f, Math.max(1.2f, barW * left), 1.2f, 0.6f, Theme.accent2(), Theme.accent());
        }
        Render2D.setAlpha(prev);
    }
}