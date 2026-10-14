package com.lowcode.svintusclient.client.gui.component;

import com.lowcode.svintusclient.client.gui.Theme;
import com.lowcode.svintusclient.client.gui.Widgets;
import com.lowcode.svintusclient.client.module.setting.ModeSetting;
import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.render.font.Fonts;
import com.lowcode.svintusclient.client.render.icon.Icons;
import com.lowcode.svintusclient.client.util.Anim;
import com.lowcode.svintusclient.client.util.Colors;
import net.minecraft.client.gui.DrawContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModeComponent extends SettingComponent {

    private static final float PILL_H = 15f;
    private static final float ITEM_H = 14f;
    private static final float TEXT_SIZE = 6.2f;

    private final ModeSetting setting;
    private final Anim open = new Anim(18f);
    private final Anim pillHover = new Anim(16f);
    private final Map<String, Anim> itemHover = new HashMap<>();
    private boolean expanded;

    public ModeComponent(ModeSetting setting) {
        this.setting = setting;
    }

    @Override
    public float height() {
        return 24f;
    }

    private float pillW() {
        float max = 0;
        for (String mode : setting.getModes()) max = Math.max(max, Fonts.REGULAR.width(mode, TEXT_SIZE));
        return Math.max(64f, max + 26f);
    }

    private float pillX() {
        return x + w - PAD + 3f - pillW();
    }

    private float pillY() {
        return y + (height() - PILL_H) / 2f;
    }

    @Override
    public void render(DrawContext ctx, float mx, float my) {
        float h = drawCard(ctx, mx, my);
        float o = open.to(expanded).get();
        Fonts.REGULAR.drawCentered(ctx, setting.getName(), x + PAD, y + height() / 2f, LABEL_SIZE,
                Colors.lerp(Theme.TEXT_DIM, Theme.TEXT, Math.max(h * 0.7f, o)));

        float px = pillX(), py = pillY(), pw = pillW();
        float ph = pillHover.to(Widgets.inside(mx, my, px, py, pw, PILL_H)).get();
        Render2D.rect(ctx, px, py, pw, PILL_H, 5f, Colors.lerp(Theme.SURFACE_RAISED, 0xFF24252E, ph));
        Render2D.outline(ctx, px, py, pw, PILL_H, 5f, 0.8f, Colors.lerp(0x0DFFFFFF, Theme.accent(0.6f), o));

        String text = setting.get();
        float tw = Fonts.REGULAR.width(text, TEXT_SIZE);
        Fonts.REGULAR.drawCentered(ctx, text, px + (pw - 10f - tw) / 2f, py + PILL_H / 2f, TEXT_SIZE,
                Colors.lerp(Theme.TEXT_DIM, Theme.TEXT, Math.max(ph, o)));

        float cx = px + pw - 8.5f, cy = py + PILL_H / 2f;
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(cx, cy);
        ctx.getMatrices().rotate((float) Math.PI * o);
        Icons.CHEVRON.drawCentered(ctx, 0, 0, 7f, Colors.lerp(Theme.TEXT_MUTE, Theme.accent(), o));
        ctx.getMatrices().popMatrix();
    }

    @Override
    public boolean hasOverlay() {
        return expanded;
    }

    private float listH() {
        return setting.getModes().size() * ITEM_H + 4f;
    }

    private float listY() {
        return pillY() + PILL_H + 3f;
    }

    @Override
    public void renderOverlay(DrawContext ctx, float mx, float my) {
        float o = open.get();
        if (o <= 0.01f) return;

        float prevAlpha = Render2D.alpha();
        Render2D.setAlpha(prevAlpha * o);

        float lx = pillX(), ly = listY() - 4f * (1f - o), lw = pillW(), lh = listH();
        Render2D.shadow(ctx, lx, ly, lw, lh, 6f, 10f, 0x90000000);
        Render2D.rect(ctx, lx, ly, lw, lh, 6f, Theme.SURFACE_RAISED);
        Render2D.outline(ctx, lx, ly, lw, lh, 6f, 0.8f, 0x14FFFFFF);

        List<String> modes = setting.getModes();
        for (int i = 0; i < modes.size(); i++) {
            String mode = modes.get(i);
            float iy = ly + 2f + i * ITEM_H;
            boolean selected = setting.is(mode);
            float ih = itemHover.computeIfAbsent(mode, m -> new Anim(18f))
                    .to(expanded && Widgets.inside(mx, my, lx, iy, lw, ITEM_H)).get();
            if (ih > 0.01f) Render2D.rect(ctx, lx + 2f, iy, lw - 4f, ITEM_H, 4.5f, Colors.fade(0x14FFFFFF, ih));
            if (selected) {
                Render2D.rect(ctx, lx + 2f, iy, lw - 4f, ITEM_H, 4.5f, Theme.accent(0.10f));
                Render2D.circle(ctx, lx + 8f, iy + ITEM_H / 2f, 1.6f, Theme.accent());
            }
            Fonts.REGULAR.drawCentered(ctx, mode, lx + 14f, iy + ITEM_H / 2f, TEXT_SIZE,
                    selected ? Theme.accent() : Colors.lerp(Theme.TEXT_DIM, Theme.TEXT, ih));
        }

        Render2D.setAlpha(prevAlpha);
    }

    @Override
    public boolean overlayClicked(float mx, float my, int button) {
        if (!expanded) return false;
        expanded = false;
        float ly = listY();
        if (Widgets.inside(mx, my, pillX(), ly, pillW(), listH())) {
            int index = (int) ((my - ly - 2f) / ITEM_H);
            List<String> modes = setting.getModes();
            if (index >= 0 && index < modes.size()) setting.set(modes.get(index));
            return true;
        }
        return isHovered(mx, my);
    }

    @Override
    public boolean mouseClicked(float mx, float my, int button) {
        if (!isHovered(mx, my)) return false;
        if (button == 0) {
            expanded = !expanded;
        } else if (button == 1) {
            List<String> modes = setting.getModes();
            setting.set(modes.get((modes.indexOf(setting.get()) + 1) % modes.size()));
        }
        return true;
    }
}