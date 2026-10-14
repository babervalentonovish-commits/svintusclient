package com.lowcode.svintusclient.client.hud.widget;

import com.lowcode.svintusclient.client.gui.Theme;
import com.lowcode.svintusclient.client.hud.HudDraw;
import com.lowcode.svintusclient.client.hud.HudWidget;
import com.lowcode.svintusclient.client.module.ModuleManager;
import com.lowcode.svintusclient.client.module.setting.BooleanSetting;
import com.lowcode.svintusclient.client.render.PigHead;
import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.render.font.Fonts;
import com.lowcode.svintusclient.client.render.icon.Icon;
import com.lowcode.svintusclient.client.render.icon.Icons;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WatermarkWidget extends HudWidget {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final float H = 20f, TITLE = 7.4f, VALUE = 6.4f, UNIT = 5.6f;

    private final BooleanSetting nick = option("Ник", true);
    private final BooleanSetting fps = option("FPS", true);
    private final BooleanSetting ping = option("Пинг", true);
    private final BooleanSetting time = option("Время", true);

    private record Segment(Icon icon, String value, String unit) {
    }

    private final List<Segment> segments = new ArrayList<>();

    public WatermarkWidget() {
        super("watermark", "Ватермарка", 0f, 0f);
    }

    @Override
    public boolean isVisible() {
        return ModuleManager.HUD.watermark.get();
    }

    @Override
    public void setVisible(boolean visible) {
        ModuleManager.HUD.watermark.set(visible);
    }

    @Override
    protected void layout(boolean editing) {
        MinecraftClient client = MinecraftClient.getInstance();
        segments.clear();
        if (nick.get()) segments.add(new Segment(Icons.get("player"), client.getSession().getUsername(), ""));
        if (fps.get()) segments.add(new Segment(Icons.get("activity"), String.valueOf(client.getCurrentFps()), " fps"));
        if (ping.get()) segments.add(new Segment(Icons.get("signal"), String.valueOf(ping(client)), " ms"));
        if (time.get()) segments.add(new Segment(Icons.get("clock"), LocalTime.now().format(TIME), ""));

        float width = 6f + 13f + 5f + Fonts.SEMIBOLD.width("EFASHHVH", TITLE) + 7f;
        for (Segment s : segments) {
            width += 8f + 7f + 3.5f + Fonts.SEMIBOLD.width(s.value, VALUE) + Fonts.REGULAR.width(s.unit, UNIT);
        }
        w = width;
        h = H;
    }

    private static int ping(MinecraftClient client) {
        if (client.getNetworkHandler() == null || client.player == null) return 0;
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        return entry == null ? 0 : entry.getLatency();
    }

    @Override
    protected void draw(DrawContext ctx, boolean editing) {
        HudDraw.glass(ctx, x, y, w, h, 7f);
        Render2D.gradientH(ctx, x, y, 40f, h, 7f, Theme.accent(0.16f), Theme.accent(0f));

        float cy = y + h / 2f;
        Render2D.shadow(ctx, x + 6f, y + 3.5f, 13f, 13f, 3f, 7f, Theme.accent(0.45f));
        PigHead.draw(ctx, x + 6f, y + 3.5f, 13f);

        float tx = x + 24f;
        tx += Fonts.SEMIBOLD.drawGradient(ctx, "EFASHHVH", tx, cy, TITLE, Theme.accent2(), Theme.accent()) + 3f;

        for (Segment s : segments) {
            tx += 4f;
            Render2D.rect(ctx, tx, y + 6f, 0.8f, h - 12f, 0.4f, 0x1EFFFFFF);
            tx += 4f;
            s.icon.drawCentered(ctx, tx + 3.5f, cy, 7f, Theme.accent());
            tx += 7f + 3.5f;
            tx += Fonts.SEMIBOLD.drawCentered(ctx, s.value, tx, cy, VALUE, Theme.TEXT);
            if (!s.unit.isEmpty()) tx += Fonts.REGULAR.drawCentered(ctx, s.unit, tx, cy, UNIT, Theme.TEXT_DIM);
        }
    }
}