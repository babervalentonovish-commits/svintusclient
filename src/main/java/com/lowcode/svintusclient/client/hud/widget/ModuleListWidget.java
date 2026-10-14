package com.lowcode.svintusclient.client.hud.widget;

import com.lowcode.svintusclient.client.gui.Theme;
import com.lowcode.svintusclient.client.hud.HudDraw;
import com.lowcode.svintusclient.client.hud.HudWidget;
import com.lowcode.svintusclient.client.module.Module;
import com.lowcode.svintusclient.client.module.ModuleManager;
import com.lowcode.svintusclient.client.module.setting.BooleanSetting;
import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.render.font.Fonts;
import com.lowcode.svintusclient.client.util.Anim;
import com.lowcode.svintusclient.client.util.Colors;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleListWidget extends HudWidget {

    private static final float SIZE = 6.4f, LINE = 13f, GAP = 2f;

    private final BooleanSetting glow = option("Свечение", true);
    private final Map<Module, Anim> anims = new HashMap<>();
    private final List<Module> visible = new ArrayList<>();

    public ModuleListWidget() {
        super("modules", "Список модулей", 1f, 0f);
    }

    @Override
    public boolean isVisible() {
        return ModuleManager.HUD.moduleList.get();
    }

    @Override
    public void setVisible(boolean v) {
        ModuleManager.HUD.moduleList.set(v);
    }

    private float lineWidth(Module module) {
        return Fonts.REGULAR.width(module.getName(), SIZE) + 15f;
    }

    @Override
    protected void layout(boolean editing) {
        visible.clear();
        for (Module module : ModuleManager.all()) {
            if (!module.isToggleable()) continue;
            Anim anim = anims.computeIfAbsent(module, m -> new Anim(m.isEnabled() ? 1f : 0f, 12f));
            anim.to(module.isEnabled());
            if (anim.get() > 0.01f) visible.add(module);
        }
        visible.sort(Comparator.comparingDouble(this::lineWidth).reversed());

        float width = 60f, height = 0;
        for (Module module : visible) {
            width = Math.max(width, lineWidth(module));
            height += (LINE + GAP) * Anim.easeOutCubic(anims.get(module).get());
        }
        w = width;
        h = Math.max(LINE, height - GAP);
    }

    @Override
    protected void draw(DrawContext ctx, boolean editing) {
        if (visible.isEmpty()) {
            if (editing) {
                HudDraw.glass(ctx, x, y, w, LINE, 4f);
                Fonts.REGULAR.drawCentered(ctx, "Нет модулей", x + 7f, y + LINE / 2f, SIZE, Theme.TEXT_MUTE);
            }
            return;
        }
        boolean right = alignRight();
        float yy = alignBottom() ? y + h : y;
        float prev = Render2D.alpha();
        for (int i = 0; i < visible.size(); i++) {
            Module module = visible.get(i);
            float a = Anim.easeOutCubic(anims.get(module).get());
            float lw = lineWidth(module);
            float lx = right ? x + w - lw + (1f - a) * (lw + 10f) : x - (1f - a) * (lw + 10f);
            float ly = alignBottom() ? yy - LINE : yy;
            int accent = Colors.lerp(Theme.accent(), Theme.accent2(), visible.size() <= 1 ? 0f : i / (float) (visible.size() - 1));

            Render2D.setAlpha(prev * a);
            HudDraw.glass(ctx, lx, ly, lw, LINE, 4f);
            float bx = right ? lx + lw - 4f : lx + 2.5f;
            if (glow.get()) Render2D.shadow(ctx, bx, ly + 3f, 1.5f, LINE - 6f, 0.75f, 5f, Colors.withAlpha(accent, 0.6f));
            Render2D.rect(ctx, bx, ly + 3f, 1.5f, LINE - 6f, 0.75f, accent);
            Fonts.REGULAR.drawCentered(ctx, module.getName(), right ? lx + 6f : lx + 8f, ly + LINE / 2f, SIZE, Theme.TEXT);

            float step = (LINE + GAP) * a;
            yy += alignBottom() ? -step : step;
        }
        Render2D.setAlpha(prev);
    }
}