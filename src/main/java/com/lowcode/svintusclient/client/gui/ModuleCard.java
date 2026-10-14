package com.lowcode.svintusclient.client.gui;

import com.lowcode.svintusclient.client.gui.component.BindComponent;
import com.lowcode.svintusclient.client.gui.component.BooleanComponent;
import com.lowcode.svintusclient.client.gui.component.ModeComponent;
import com.lowcode.svintusclient.client.gui.component.SettingComponent;
import com.lowcode.svintusclient.client.gui.component.SliderComponent;
import com.lowcode.svintusclient.client.module.Module;
import com.lowcode.svintusclient.client.module.setting.BindSetting;
import com.lowcode.svintusclient.client.module.setting.BooleanSetting;
import com.lowcode.svintusclient.client.module.setting.ModeSetting;
import com.lowcode.svintusclient.client.module.setting.NumberSetting;
import com.lowcode.svintusclient.client.module.setting.Setting;
import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.render.font.Fonts;
import com.lowcode.svintusclient.client.render.icon.Icons;
import com.lowcode.svintusclient.client.util.Anim;
import com.lowcode.svintusclient.client.util.Colors;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

final class ModuleCard {

    static final float HEADER_H = 32f;
    private static final float RADIUS = 9f;
    private static final float PAD = 10f;
    private static final float INSET = 6f;
    private static final float ROW_GAP = 4f;

    private record Row(Setting<?> setting, SettingComponent component) {
    }

    final Module module;
    private final List<Row> rows = new ArrayList<>();
    private final Anim enabled;
    private final Anim hover = new Anim(14f);
    private final Anim expand = new Anim(12f);
    private final Anim chevronHover = new Anim(16f);
    private boolean expanded;

    float x, y, w, h;

    ModuleCard(Module module) {
        this.module = module;
        this.enabled = new Anim(module.isEnabled() ? 1f : 0f, 14f);
        for (Setting<?> setting : module.getSettings()) {
            SettingComponent component = switch (setting) {
                case BooleanSetting b -> new BooleanComponent(b);
                case NumberSetting n -> new SliderComponent(n);
                case ModeSetting m -> new ModeComponent(m);
                case BindSetting b -> new BindComponent(b.getName(), b::get, b::set);
                default -> null;
            };
            if (component != null) rows.add(new Row(setting, component));
        }
        if (module.isToggleable()) rows.add(new Row(null, new BindComponent("Бинд", module::getKey, module::setKey)));
    }

    boolean hasSettings() {
        return !rows.isEmpty();
    }

    private List<Row> visibleRows() {
        List<Row> list = new ArrayList<>(rows.size());
        for (Row row : rows) if (row.setting == null || row.setting.isVisible()) list.add(row);
        return list;
    }

    private float rowsHeight() {
        float total = 0;
        for (Row row : visibleRows()) total += row.component.height() + ROW_GAP;
        return total;
    }

    private float expandProgress() {
        return Anim.easeOutCubic(expand.to(expanded).get());
    }

    float height() {
        float e = expandProgress();
        return e <= 0.001f ? HEADER_H : HEADER_H + (rowsHeight() + INSET - ROW_GAP + 2f) * e;
    }

    private float switchX() {
        return x + w - PAD - Widgets.SWITCH_W;
    }

    private float chevronX() {
        return module.isToggleable() ? switchX() - 12f : x + w - PAD - 8f;
    }

    private boolean overChevron(float mx, float my) {
        return hasSettings() && Widgets.inside(mx, my, chevronX() - 4f, y + 6f, 16f, HEADER_H - 12f);
    }

    boolean isOverHeader(float mx, float my) {
        return Widgets.inside(mx, my, x, y, w, HEADER_H);
    }

    void render(DrawContext ctx, float x, float y, float w, float mx, float my, boolean interactive) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = height();

        float e = enabled.to(module.isEnabled()).get();
        float hv = hover.to(interactive && isOverHeader(mx, my)).get();
        float ex = expandProgress();

        if (e > 0.01f) Render2D.shadow(ctx, x, y, w, h, RADIUS, 10f, Theme.accent(0.12f * e));
        Render2D.rect(ctx, x, y, w, h, RADIUS, Colors.lerp(0x0CFFFFFF, 0x14FFFFFF, Math.max(hv, ex * 0.4f)));
        if (e > 0.01f) {
            Render2D.gradientH(ctx, x, y, w, h, RADIUS, Theme.accent(0.13f * e), Theme.accent(0.02f * e));
            Render2D.outlineGradientH(ctx, x, y, w, h, RADIUS, 1f,
                    Colors.withAlpha(Theme.accent(), 0.75f * e), Colors.withAlpha(Theme.accent2(), 0.30f * e));
        } else {
            Render2D.outlineGradientV(ctx, x, y, w, h, RADIUS, 0.8f, 0x14FFFFFF, 0x06FFFFFF);
        }

        float right = (hasSettings() ? chevronX() : module.isToggleable() ? switchX() : x + w - PAD) - 6f;
        if (module.getKey() != GLFW.GLFW_KEY_UNKNOWN) {
            String key = BindComponent.keyName(module.getKey());
            float kw = Fonts.REGULAR.width(key, 5.4f) + 8f;
            right -= kw;
            Render2D.rect(ctx, right, y + 7f, kw, 9f, 3f, 0x14FFFFFF);
            Fonts.REGULAR.drawCentered(ctx, key, right + 4f, y + 11.5f, 5.4f, Theme.TEXT_DIM);
            right -= 4f;
        }

        String name = Fonts.SEMIBOLD.trim(module.getName(), 7.4f, right - (x + PAD));
        Fonts.SEMIBOLD.drawCentered(ctx, name, x + PAD, y + 11.5f, 7.4f,
                Colors.lerp(Theme.TEXT_DIM, Theme.TEXT, Math.max(e, Math.max(hv * 0.6f, module.isToggleable() ? 0f : 0.8f))));
        String desc = Fonts.REGULAR.trim(module.getDescription(), 5.8f, (hasSettings() ? chevronX() - 6f : right) - (x + PAD));
        Fonts.REGULAR.drawCentered(ctx, desc, x + PAD, y + 22f, 5.8f, Colors.lerp(Theme.TEXT_MUTE, Theme.TEXT_DIM, Math.max(e, hv * 0.5f)));

        if (module.isToggleable()) Widgets.toggle(ctx, switchX(), y + (HEADER_H - Widgets.SWITCH_H) / 2f, e, hv);

        if (hasSettings()) {
            float ch = chevronHover.to(interactive && overChevron(mx, my)).get();
            float cx = chevronX() + 4f, cy = y + HEADER_H / 2f;
            ctx.getMatrices().pushMatrix();
            ctx.getMatrices().translate(cx, cy);
            ctx.getMatrices().rotate((float) Math.PI * ex);
            Icons.CHEVRON.drawCentered(ctx, 0, 0, 8f, Colors.lerp(Colors.lerp(Theme.TEXT_MUTE, Theme.TEXT, ch), Theme.accent(), ex));
            ctx.getMatrices().popMatrix();
        }

        if (ex > 0.001f) {
            float prev = Render2D.alpha();
            Render2D.setAlpha(prev * ex);
            Render2D.rect(ctx, x + PAD, y + HEADER_H - 0.5f, w - PAD * 2f, 0.7f, 0.35f, 0x10FFFFFF);
            Render2D.pushScissor(ctx, x, y + HEADER_H, w, h - HEADER_H);
            boolean rowsInteractive = interactive && expanded && ex > 0.9f;
            float rmx = rowsInteractive ? mx : -1e4f, rmy = rowsInteractive ? my : -1e4f;
            float yy = y + HEADER_H + INSET - 2f;
            for (Row row : visibleRows()) {
                row.component.setBounds(x + INSET, yy, w - INSET * 2f);
                row.component.render(ctx, rmx, rmy);
                yy += row.component.height() + ROW_GAP;
            }
            Render2D.popScissor(ctx);
            Render2D.setAlpha(prev);
        }
    }

    void renderOverlays(DrawContext ctx, float mx, float my) {
        if (!expanded) return;
        for (Row row : visibleRows()) row.component.renderOverlay(ctx, mx, my);
    }

    boolean hasOverlay() {
        for (Row row : rows) if (row.component.hasOverlay()) return true;
        return false;
    }

    boolean overlayClicked(float mx, float my, int button) {
        boolean consumed = false;
        for (Row row : rows) {
            if (row.component.hasOverlay() && row.component.overlayClicked(mx, my, button)) consumed = true;
        }
        return consumed;
    }

    boolean isPointerOver(float mx, float my) {
        if (isOverHeader(mx, my)) return true;
        if (!expanded) return false;
        for (Row row : visibleRows()) if (row.component.isHovered(mx, my)) return true;
        return false;
    }

    boolean mouseClicked(float mx, float my, int button) {
        if (isOverHeader(mx, my)) {
            boolean expandClick = button == 1 || (button == 0 && (overChevron(mx, my) || !module.isToggleable()));
            if (expandClick && hasSettings()) expanded = !expanded;
            else if (button == 0) module.toggle();
            return true;
        }
        if (expanded && Widgets.inside(mx, my, x, y, w, h)) {
            for (Row row : visibleRows()) {
                if (row.component.mouseClicked(mx, my, button)) return true;
            }
            return true;
        }
        for (Row row : rows) row.component.mouseClicked(-1e4f, -1e4f, button);
        return false;
    }

    void mouseReleased() {
        for (Row row : rows) row.component.mouseReleased();
    }

    boolean keyPressed(int key) {
        for (Row row : rows) if (row.component.keyPressed(key)) return true;
        return false;
    }
}