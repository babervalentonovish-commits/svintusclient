package com.lowcode.svintusclient.client.gui.component;

import com.lowcode.svintusclient.client.gui.Theme;
import com.lowcode.svintusclient.client.gui.Widgets;
import com.lowcode.svintusclient.client.module.setting.BooleanSetting;
import com.lowcode.svintusclient.client.render.font.Fonts;
import com.lowcode.svintusclient.client.util.Anim;
import com.lowcode.svintusclient.client.util.Colors;
import net.minecraft.client.gui.DrawContext;

public class BooleanComponent extends SettingComponent {

    private final BooleanSetting setting;
    private final Anim state;

    public BooleanComponent(BooleanSetting setting) {
        this.setting = setting;
        this.state = new Anim(setting.get() ? 1f : 0f, 16f);
    }

    @Override
    public float height() {
        return 24f;
    }

    @Override
    public void render(DrawContext ctx, float mx, float my) {
        float h = drawCard(ctx, mx, my);
        float t = state.to(setting.get()).get();
        Fonts.REGULAR.drawCentered(ctx, setting.getName(), x + PAD, y + height() / 2f, LABEL_SIZE,
                Colors.lerp(Theme.TEXT_DIM, Theme.TEXT, Math.max(t, h * 0.6f)));
        Widgets.toggle(ctx, x + w - PAD - Widgets.SWITCH_W, y + (height() - Widgets.SWITCH_H) / 2f, t, h);
    }

    @Override
    public boolean mouseClicked(float mx, float my, int button) {
        if (!isHovered(mx, my) || button != 0) return false;
        setting.toggle();
        return true;
    }
}