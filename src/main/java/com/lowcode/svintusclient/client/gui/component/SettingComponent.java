package com.lowcode.svintusclient.client.gui.component;

import com.lowcode.svintusclient.client.gui.Widgets;
import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.util.Anim;
import com.lowcode.svintusclient.client.util.Colors;
import net.minecraft.client.gui.DrawContext;

public abstract class SettingComponent {

    public static final float LABEL_SIZE = 6.6f;
    public static final float PAD = 10f;

    protected float x, y, w;
    protected final Anim hover = new Anim(14f);

    public void setBounds(float x, float y, float w) {
        this.x = x;
        this.y = y;
        this.w = w;
    }

    public abstract float height();

    public abstract void render(DrawContext ctx, float mx, float my);

    public boolean mouseClicked(float mx, float my, int button) {
        return false;
    }

    public void mouseReleased() {
    }

    public boolean keyPressed(int key) {
        return false;
    }

    public void renderOverlay(DrawContext ctx, float mx, float my) {
    }

    public boolean overlayClicked(float mx, float my, int button) {
        return false;
    }

    public boolean hasOverlay() {
        return false;
    }

    public boolean isHovered(float mx, float my) {
        return Widgets.inside(mx, my, x, y, w, height());
    }

    protected float drawCard(DrawContext ctx, float mx, float my) {
        float h = hover.to(isHovered(mx, my)).get();
        Render2D.rect(ctx, x, y, w, height(), 7f, Colors.lerp(0x0BFFFFFF, 0x14FFFFFF, h));
        return h;
    }
}