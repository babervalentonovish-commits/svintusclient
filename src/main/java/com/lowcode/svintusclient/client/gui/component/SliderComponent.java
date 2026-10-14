package com.lowcode.svintusclient.client.gui.component;

import com.lowcode.svintusclient.client.gui.Theme;
import com.lowcode.svintusclient.client.module.setting.NumberSetting;
import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.render.font.Fonts;
import com.lowcode.svintusclient.client.util.Anim;
import com.lowcode.svintusclient.client.util.Colors;
import net.minecraft.client.gui.DrawContext;

public class SliderComponent extends SettingComponent {

    private static final float VALUE_SIZE = 6.2f;
    private static final float TRACK_H = 3.5f;

    private final NumberSetting setting;
    private final Anim fill;
    private final Anim knobHover = new Anim(16f);
    private boolean dragging;

    public SliderComponent(NumberSetting setting) {
        this.setting = setting;
        this.fill = new Anim(setting.progress(), 18f);
    }

    @Override
    public float height() {
        return 31f;
    }

    private float trackX() {
        return x + PAD;
    }

    private float trackW() {
        return w - PAD * 2f;
    }

    private float trackY() {
        return y + 21f;
    }

    @Override
    public void render(DrawContext ctx, float mx, float my) {
        if (dragging) setting.setProgress((mx - trackX()) / trackW());

        float h = drawCard(ctx, mx, my);
        Fonts.REGULAR.drawCentered(ctx, setting.getName(), x + PAD, y + 10.5f, LABEL_SIZE,
                Colors.lerp(Theme.TEXT_DIM, Theme.TEXT, Math.max(h * 0.7f, dragging ? 1f : 0f)));

        String max = " / " + setting.format(setting.getMax());
        String value = setting.format(setting.get());
        float maxW = Fonts.REGULAR.width(max, VALUE_SIZE);
        float valueW = Fonts.SEMIBOLD.width(value, VALUE_SIZE);
        float right = x + w - PAD;
        Fonts.REGULAR.drawCentered(ctx, max, right - maxW, y + 10.5f, VALUE_SIZE, Theme.TEXT_MUTE);
        Fonts.SEMIBOLD.drawCentered(ctx, value, right - maxW - valueW, y + 10.5f, VALUE_SIZE, Theme.accent());

        float t = fill.to(setting.progress()).get();
        float tx = trackX(), ty = trackY(), tw = trackW();
        Render2D.rect(ctx, tx, ty, tw, TRACK_H, TRACK_H / 2f, Theme.TRACK);
        float fw = Math.max(TRACK_H, tw * t);
        Render2D.shadow(ctx, tx, ty, fw, TRACK_H, TRACK_H / 2f, 5f, Theme.accent(0.25f));
        Render2D.gradientH(ctx, tx, ty, fw, TRACK_H, TRACK_H / 2f, Theme.accent2(), Theme.accent());

        float k = knobHover.to(h > 0.5f || dragging).get();
        float kx = tx + tw * t, ky = ty + TRACK_H / 2f;
        float r = 3.6f + k * 0.9f;
        Render2D.shadow(ctx, kx - r, ky - r, r * 2, r * 2, r, 4f + k * 3f, Theme.accent(0.35f + k * 0.25f));
        Render2D.circle(ctx, kx, ky, r, 0xFFFFFFFF);
        Render2D.circle(ctx, kx, ky, r * 0.42f, Theme.accent());
    }

    @Override
    public boolean mouseClicked(float mx, float my, int button) {
        if (!isHovered(mx, my) || button != 0) return false;
        dragging = true;
        setting.setProgress((mx - trackX()) / trackW());
        return true;
    }

    @Override
    public void mouseReleased() {
        dragging = false;
    }
}