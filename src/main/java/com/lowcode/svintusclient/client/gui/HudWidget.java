package com.lowcode.svintusclient.client.hud;

import com.lowcode.svintusclient.client.module.setting.BooleanSetting;
import com.lowcode.svintusclient.client.util.Anim;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public abstract class HudWidget {

    public static final float MARGIN = 4f;

    public final String id;
    public final String title;
    private final List<BooleanSetting> options = new ArrayList<>();
    final Anim editHover = new Anim(14f);
    final Anim scaleFlash = new Anim(0f, 2.5f);

    float fx, fy;
    float scale = 1f;
    protected float x, y, w, h;

    protected HudWidget(String id, String title, float fx, float fy) {
        this.id = id;
        this.title = title;
        this.fx = fx;
        this.fy = fy;
    }

    protected BooleanSetting option(String name, boolean value) {
        BooleanSetting setting = new BooleanSetting(name, value);
        options.add(setting);
        return setting;
    }

    public List<BooleanSetting> options() {
        return options;
    }

    public abstract boolean isVisible();

    public abstract void setVisible(boolean visible);

    protected abstract void layout(boolean editing);

    protected abstract void draw(DrawContext ctx, boolean editing);

    protected boolean hasContent(boolean editing) {
        return true;
    }

    float scaledW() {
        return w * scale;
    }

    float scaledH() {
        return h * scale;
    }

    void place(float screenW, float screenH) {
        x = MARGIN + fx * Math.max(0f, screenW - scaledW() - MARGIN * 2f);
        y = MARGIN + fy * Math.max(0f, screenH - scaledH() - MARGIN * 2f);
    }

    void moveTo(float nx, float ny, float screenW, float screenH) {
        float rangeX = screenW - scaledW() - MARGIN * 2f, rangeY = screenH - scaledH() - MARGIN * 2f;
        fx = rangeX <= 0 ? 0f : Math.max(0f, Math.min(1f, (nx - MARGIN) / rangeX));
        fy = rangeY <= 0 ? 0f : Math.max(0f, Math.min(1f, (ny - MARGIN) / rangeY));
        place(screenW, screenH);
    }

    protected boolean alignRight() {
        return fx > 0.5f;
    }

    protected boolean alignBottom() {
        return fy > 0.5f;
    }

    boolean contains(float mx, float my) {
        return mx >= x && mx <= x + scaledW() && my >= y && my <= y + scaledH();
    }

    public float getFx() {
        return fx;
    }

    public float getFy() {
        return fy;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = Math.max(0.5f, Math.min(2f, Math.round(scale * 20f) / 20f));
    }

    public void setPosition(float fx, float fy) {
        this.fx = Math.max(0f, Math.min(1f, fx));
        this.fy = Math.max(0f, Math.min(1f, fy));
    }
}