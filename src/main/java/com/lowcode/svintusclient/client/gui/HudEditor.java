package com.lowcode.svintusclient.client.hud;

import com.lowcode.svintusclient.client.Config;
import com.lowcode.svintusclient.client.gui.Theme;
import com.lowcode.svintusclient.client.gui.Widgets;
import com.lowcode.svintusclient.client.module.setting.BooleanSetting;
import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.render.font.Fonts;
import com.lowcode.svintusclient.client.util.Anim;
import com.lowcode.svintusclient.client.util.Colors;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HudEditor {

    private static final float SNAP = 7f;

    private static HudWidget dragging;
    private static float grabX, grabY;
    private static ContextMenu menu;

    private static final Anim guideXAlpha = new Anim(16f);
    private static final Anim guideYAlpha = new Anim(16f);
    private static float guideX = -1f, guideY = -1f;
    private static final Anim banner = new Anim(10f);

    private HudEditor() {
    }

    public static boolean isEditing() {
        return MinecraftClient.getInstance().currentScreen instanceof ChatScreen;
    }

    static boolean isDragging(HudWidget widget) {
        return dragging == widget;
    }

    static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof ChatScreen)) return;
            banner.snap(0f).to(1f);
            ScreenMouseEvents.allowMouseClick(screen).register((s, click) -> !mouseClicked(click));
            ScreenMouseEvents.allowMouseRelease(screen).register((s, click) -> !mouseReleased());
            ScreenMouseEvents.allowMouseScroll(screen).register((s, mx, my, h, v) -> !mouseScrolled((float) mx, (float) my, v));
            ScreenEvents.afterRender(screen).register((s, ctx, mx, my, delta) -> render(ctx, mx, my));
            ScreenEvents.remove(screen).register(s -> {
                if (dragging != null || menu != null) Config.save();
                dragging = null;
                menu = null;
            });
        });
    }

    private static List<HudWidget> active() {
        List<HudWidget> list = new ArrayList<>();
        for (HudWidget widget : HudManager.widgets()) if (HudManager.isActive(widget)) list.add(widget);
        return list;
    }

    private static HudWidget widgetAt(float mx, float my) {
        List<HudWidget> list = active();
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).contains(mx, my)) return list.get(i);
        }
        return null;
    }

    private static boolean mouseClicked(Click click) {
        float mx = (float) click.x(), my = (float) click.y();
        if (menu != null) {
            boolean inside = menu.contains(mx, my);
            if (inside) menu.click(mx, my);
            else menu = null;
            return true;
        }
        HudWidget widget = widgetAt(mx, my);
        if (widget == null) return false;
        if (click.button() == 0) {
            dragging = widget;
            grabX = mx - widget.x;
            grabY = my - widget.y;
            return true;
        }
        if (click.button() == 1) {
            menu = new ContextMenu(widget, mx, my);
            return true;
        }
        return false;
    }

    private static boolean mouseScrolled(float mx, float my, double amount) {
        if (menu != null || amount == 0) return false;
        HudWidget widget = dragging != null ? dragging : widgetAt(mx, my);
        if (widget == null) return false;
        var window = MinecraftClient.getInstance().getWindow();
        float sw = window.getScaledWidth(), sh = window.getScaledHeight();
        float cx = widget.x + widget.scaledW() / 2f, cy = widget.y + widget.scaledH() / 2f;
        widget.setScale(widget.scale + (amount > 0 ? 0.05f : -0.05f));
        if (dragging != widget) widget.moveTo(cx - widget.scaledW() / 2f, cy - widget.scaledH() / 2f, sw, sh);
        widget.scaleFlash.snap(1f).to(0f);
        Config.save();
        return true;
    }

    private static boolean mouseReleased() {
        if (dragging == null) return false;
        dragging = null;
        Config.save();
        return true;
    }

    private static boolean ctrlDown() {
        var window = MinecraftClient.getInstance().getWindow();
        return InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    private static void render(DrawContext ctx, int mouseX, int mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        Render2D.setPixelScale(client.getWindow().getScaleFactor());
        Render2D.setAlpha(1f);
        float sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        float mx = mouseX, my = mouseY;

        boolean snappedX = false, snappedY = false;
        if (dragging != null) {
            float nx = mx - grabX, ny = my - grabY;
            if (ctrlDown()) {
                float[] sx = snap(nx, dragging.scaledW(), sw, true);
                float[] sy = snap(ny, dragging.scaledH(), sh, false);
                if (sx != null) {
                    nx = sx[0];
                    guideX = sx[1];
                    snappedX = true;
                }
                if (sy != null) {
                    ny = sy[0];
                    guideY = sy[1];
                    snappedY = true;
                }
            }
            dragging.moveTo(nx, ny, sw, sh);
            ctx.setCursor(StandardCursors.RESIZE_ALL);
        }

        float gx = guideXAlpha.to(snappedX).get(), gy = guideYAlpha.to(snappedY).get();
        if (gx > 0.01f && guideX >= 0) {
            Render2D.rect(ctx, guideX - 0.4f, 0, 0.8f, sh, 0, Theme.accent(0.75f * gx));
            Render2D.shadow(ctx, guideX - 0.4f, 0, 0.8f, sh, 0, 4f, Theme.accent(0.35f * gx));
        }
        if (gy > 0.01f && guideY >= 0) {
            Render2D.rect(ctx, 0, guideY - 0.4f, sw, 0.8f, 0, Theme.accent(0.75f * gy));
            Render2D.shadow(ctx, 0, guideY - 0.4f, sw, 0.8f, 0, 4f, Theme.accent(0.35f * gy));
        }

        HudWidget hovered = menu == null && dragging == null ? widgetAt(mx, my) : null;
        for (HudWidget widget : active()) {
            float hv = widget.editHover.to(widget == hovered || widget == dragging).get();
            float pad = 2.5f;
            float flash = widget.scaleFlash.get();
            hv = Math.max(hv, flash);
            Render2D.outline(ctx, widget.x - pad, widget.y - pad, widget.scaledW() + pad * 2f, widget.scaledH() + pad * 2f, 6f, 0.8f,
                    Colors.lerp(0x22FFFFFF, Theme.accent(0.85f), hv));
            if (hv > 0.01f) {
                float prev = Render2D.alpha();
                Render2D.setAlpha(hv);
                String title = widget.title + "  ·  " + Math.round(widget.scale * 100f) + "%";
                float tw = Fonts.SEMIBOLD.width(title, 5.8f) + 10f;
                float ty = widget.y - pad - 13f;
                if (ty < 2f) ty = widget.y + widget.scaledH() + pad + 2f;
                Render2D.rect(ctx, widget.x - pad, ty, tw, 11f, 4f, Theme.accent(0.9f));
                Fonts.SEMIBOLD.drawCentered(ctx, title, widget.x - pad + 5f, ty + 5.5f, 5.8f, 0xFF0B0C10);
                Render2D.setAlpha(prev);
            }
        }
        if (hovered != null) ctx.setCursor(StandardCursors.POINTING_HAND);

        drawBanner(ctx, sw);
        if (menu != null) menu.render(ctx, mx, my, sw, sh);
    }

    private static void drawBanner(DrawContext ctx, float sw) {
        float a = Anim.easeOutCubic(banner.get());
        String main = "Редактор HUD";
        String hint = "ЛКМ — двигать   Колесо — размер   ПКМ — настройки   Ctrl — прилипание";
        float w = Fonts.SEMIBOLD.width(main, 6.4f) + 10f + Fonts.REGULAR.width(hint, 5.8f) + 20f, h = 16f;
        float x = (sw - w) / 2f, y = 4f - (1f - a) * 10f;
        float prev = Render2D.alpha();
        Render2D.setAlpha(a);
        HudDraw.glass(ctx, x, y, w, h, 6f);
        float tx = x + 10f;
        tx += Fonts.SEMIBOLD.drawCentered(ctx, main, tx, y + h / 2f, 6.4f, Theme.accent()) + 10f;
        Fonts.REGULAR.drawCentered(ctx, hint, tx, y + h / 2f, 5.8f, Theme.TEXT_DIM);
        Render2D.setAlpha(prev);
    }

    private static float[] snap(float pos, float size, float screen, boolean horizontal) {
        List<float[]> candidates = new ArrayList<>();
        float m = HudWidget.MARGIN;
        candidates.add(new float[]{m, 0.5f});
        candidates.add(new float[]{screen - size - m, screen - 0.5f});
        candidates.add(new float[]{(screen - size) / 2f, screen / 2f});
        for (HudWidget other : active()) {
            if (other == dragging) continue;
            float o = horizontal ? other.x : other.y;
            float os = horizontal ? other.scaledW() : other.scaledH();
            candidates.add(new float[]{o, o});
            candidates.add(new float[]{o + os - size, o + os});
            candidates.add(new float[]{o + os + 3f, o + os + 1.5f});
            candidates.add(new float[]{o - size - 3f, o - 1.5f});
        }
        float[] best = null;
        float bestDist = SNAP;
        for (float[] c : candidates) {
            float d = Math.abs(pos - c[0]);
            if (d < bestDist) {
                bestDist = d;
                best = c;
            }
        }
        return best;
    }

    private static final class ContextMenu {
        private static final float ITEM_H = 15f, TITLE_H = 17f, W_MIN = 110f;

        final HudWidget widget;
        final float x, y, w, h;
        final Anim open = new Anim(0f, 16f).to(1f);
        final Map<BooleanSetting, Anim> states = new HashMap<>();
        final Map<Integer, Anim> hovers = new HashMap<>();

        ContextMenu(HudWidget widget, float mx, float my) {
            this.widget = widget;
            float width = Fonts.SEMIBOLD.width(widget.title, 6.4f) + 20f;
            for (BooleanSetting option : widget.options()) {
                width = Math.max(width, Fonts.REGULAR.width(option.getName(), 6.2f) + Widgets.SWITCH_W + 26f);
            }
            w = Math.max(W_MIN, width);
            h = TITLE_H + (widget.options().size() + 1) * ITEM_H + 6f;
            MinecraftClient client = MinecraftClient.getInstance();
            float sw = client.getWindow().getScaledWidth(), sh = client.getWindow().getScaledHeight();
            x = Math.min(mx, sw - w - 4f);
            y = Math.min(my, sh - h - 4f);
        }

        boolean contains(float mx, float my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }

        private int indexAt(float mx, float my) {
            if (!contains(mx, my) || my < y + TITLE_H) return -1;
            int i = (int) ((my - y - TITLE_H) / ITEM_H);
            return i <= widget.options().size() ? i : -1;
        }

        void click(float mx, float my) {
            int i = indexAt(mx, my);
            if (i < 0) return;
            if (i < widget.options().size()) {
                widget.options().get(i).toggle();
            } else {
                widget.setVisible(false);
                menu = null;
            }
            Config.save();
        }

        void render(DrawContext ctx, float mx, float my, float sw, float sh) {
            float a = Anim.easeOutCubic(open.get());
            float prev = Render2D.alpha();
            Render2D.setAlpha(a);
            float yy = y - (1f - a) * 4f;

            HudDraw.glass(ctx, x, yy, w, h, 7f);
            Render2D.outline(ctx, x, yy, w, h, 7f, 0.8f, Theme.accent(0.35f));
            Fonts.SEMIBOLD.drawCentered(ctx, widget.title, x + 9f, yy + TITLE_H / 2f + 1f, 6.4f, Theme.accent());
            Render2D.rect(ctx, x + 8f, yy + TITLE_H - 0.5f, w - 16f, 0.7f, 0.35f, 0x14FFFFFF);

            int hoverIndex = indexAt(mx, my);
            List<BooleanSetting> options = widget.options();
            for (int i = 0; i <= options.size(); i++) {
                float iy = yy + TITLE_H + 2f + i * ITEM_H;
                float hv = hovers.computeIfAbsent(i, k -> new Anim(16f)).to(i == hoverIndex).get();
                if (hv > 0.01f) Render2D.rect(ctx, x + 3f, iy, w - 6f, ITEM_H - 1f, 5f, Colors.fade(0x16FFFFFF, hv));
                float cy = iy + (ITEM_H - 1f) / 2f;
                if (i < options.size()) {
                    BooleanSetting option = options.get(i);
                    float t = states.computeIfAbsent(option, o -> new Anim(o.get() ? 1f : 0f, 16f)).to(option.get()).get();
                    Fonts.REGULAR.drawCentered(ctx, option.getName(), x + 9f, cy, 6.2f, Colors.lerp(Theme.TEXT_DIM, Theme.TEXT, Math.max(t, hv)));
                    Widgets.toggle(ctx, x + w - 9f - Widgets.SWITCH_W, cy - Widgets.SWITCH_H / 2f, t, hv);
                } else {
                    Fonts.REGULAR.drawCentered(ctx, "Скрыть элемент", x + 9f, cy, 6.2f, Colors.lerp(0xFFE0707A, 0xFFFF8A94, hv));
                }
            }
            if (hoverIndex >= 0) ctx.setCursor(StandardCursors.POINTING_HAND);
            Render2D.setAlpha(prev);
        }
    }
}