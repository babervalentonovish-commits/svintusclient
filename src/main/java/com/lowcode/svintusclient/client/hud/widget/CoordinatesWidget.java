package com.lowcode.svintusclient.client.hud.widget;

import com.lowcode.svintusclient.client.gui.Theme;
import com.lowcode.svintusclient.client.hud.HudDraw;
import com.lowcode.svintusclient.client.hud.HudWidget;
import com.lowcode.svintusclient.client.module.ModuleManager;
import com.lowcode.svintusclient.client.module.setting.BooleanSetting;
import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.render.font.Fonts;
import com.lowcode.svintusclient.client.render.icon.Icons;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class CoordinatesWidget extends HudWidget {

    private static final float SIZE = 6.2f, H = 15f;

    private final BooleanSetting direction = option("Направление", true);
    private final BooleanSetting nether = option("Незер / Верхний мир", false);

    private String coords = "";
    private String dir = "";
    private String other = "";

    public CoordinatesWidget() {
        super("coordinates", "Координаты", 0f, 1f);
    }

    @Override
    public boolean isVisible() {
        return ModuleManager.HUD.coordinates.get();
    }

    @Override
    public void setVisible(boolean v) {
        ModuleManager.HUD.coordinates.set(v);
    }

    @Override
    protected void layout(boolean editing) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            coords = "0, 0, 0";
            dir = "";
            other = "";
        } else {
            int px = (int) Math.floor(player.getX()), py = (int) Math.floor(player.getY()), pz = (int) Math.floor(player.getZ());
            coords = px + ", " + py + ", " + pz;
            dir = direction.get() ? player.getHorizontalFacing().asString().substring(0, 1).toUpperCase() : "";
            boolean inNether = player.getEntityWorld().getRegistryKey() == World.NETHER;
            other = nether.get() ? (inNether ? (px * 8) + ", " + (pz * 8) : (px / 8) + ", " + (pz / 8)) : "";
        }
        float width = 7f + Fonts.SEMIBOLD.width("XYZ", SIZE) + 5f + Fonts.REGULAR.width(coords, SIZE) + 7f;
        if (!dir.isEmpty()) width += 21f + Fonts.SEMIBOLD.width(dir, SIZE);
        if (!other.isEmpty()) width += 10f + Fonts.REGULAR.width(other, SIZE - 0.6f);
        w = width;
        h = H;
    }

    @Override
    protected void draw(DrawContext ctx, boolean editing) {
        HudDraw.glass(ctx, x, y, w, h, 5f);
        float cy = y + h / 2f, tx = x + 7f;
        tx += Fonts.SEMIBOLD.drawCentered(ctx, "XYZ", tx, cy, SIZE, Theme.accent()) + 5f;
        tx += Fonts.REGULAR.drawCentered(ctx, coords, tx, cy, SIZE, Theme.TEXT);
        if (!other.isEmpty()) {
            tx += 5f;
            Render2D.rect(ctx, tx, y + 4.5f, 0.8f, h - 9f, 0.4f, 0x1EFFFFFF);
            tx += 5f;
            tx += Fonts.REGULAR.drawCentered(ctx, other, tx, cy, SIZE - 0.6f, Theme.TEXT_DIM);
        }
        if (!dir.isEmpty()) {
            tx += 6f;
            Render2D.rect(ctx, tx, y + 4.5f, 0.8f, h - 9f, 0.4f, 0x1EFFFFFF);
            tx += 6f;
            Icons.get("compass").drawCentered(ctx, tx + 3f, cy, 7f, Theme.accent());
            Fonts.SEMIBOLD.drawCentered(ctx, dir, tx + 9f, cy, SIZE, Theme.TEXT);
        }
    }
}