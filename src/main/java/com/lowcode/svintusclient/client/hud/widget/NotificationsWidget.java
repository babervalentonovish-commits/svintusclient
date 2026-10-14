package com.lowcode.svintusclient.client.hud.widget;

import com.lowcode.svintusclient.client.gui.Theme;
import com.lowcode.svintusclient.client.hud.HudWidget;
import com.lowcode.svintusclient.client.hud.Notifications;
import com.lowcode.svintusclient.client.module.ModuleManager;
import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.render.font.Fonts;
import net.minecraft.client.gui.DrawContext;

public class NotificationsWidget extends HudWidget {

    public NotificationsWidget() {
        super("notifications", "Уведомления", 1f, 0.9f);
    }

    @Override
    public boolean isVisible() {
        return ModuleManager.NOTIFICATIONS.isEnabled();
    }

    @Override
    public void setVisible(boolean v) {
        ModuleManager.NOTIFICATIONS.setEnabled(v);
    }

    @Override
    protected void layout(boolean editing) {
        w = Notifications.CARD_W;
        h = Notifications.CARD_H;
    }

    @Override
    protected void draw(DrawContext ctx, boolean editing) {
        if (editing && Notifications.isEmpty()) {
            Render2D.rect(ctx, x, y, w, h, 7f, 0x10FFFFFF);
            Render2D.outline(ctx, x, y, w, h, 7f, 0.8f, Theme.accent(0.35f));
            String text = "Здесь появятся уведомления";
            Fonts.REGULAR.drawCentered(ctx, text, x + (w - Fonts.REGULAR.width(text, 6f)) / 2f, y + h / 2f, 6f, Theme.TEXT_DIM);
        }
        Notifications.render(ctx, x, y, alignRight(), alignBottom());
    }
}