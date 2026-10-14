package com.lowcode.svintusclient.client.hud.widget;

import com.lowcode.svintusclient.client.gui.Theme;
import com.lowcode.svintusclient.client.hud.HudDraw;
import com.lowcode.svintusclient.client.hud.HudWidget;
import com.lowcode.svintusclient.client.module.ModuleManager;
import com.lowcode.svintusclient.client.module.setting.BooleanSetting;
import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.render.font.Fonts;
import com.lowcode.svintusclient.client.render.icon.Icons;
import com.lowcode.svintusclient.client.util.Anim;
import com.lowcode.svintusclient.client.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.util.Locale;

public class TargetHudWidget extends HudWidget {

    private static final float W = 150f, HEAD = 30f, PAD = 7f;
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final long LINGER = 1500L;

    private final BooleanSetting items = option("Предметы и броня", true);
    private final BooleanSetting distance = option("Дистанция", true);

    private final Anim show = new Anim(0f, 10f);
    private final Anim health = new Anim(0f, 8f);
    private final Anim absorption = new Anim(0f, 8f);
    private final Anim hurt = new Anim(0f, 14f);
    private LivingEntity target;
    private long lastSeen;

    public TargetHudWidget() {
        super("target", "Таргет HUD", 0.5f, 0.66f);
    }

    @Override
    public boolean isVisible() {
        return ModuleManager.HUD.targetHud.get();
    }

    @Override
    public void setVisible(boolean v) {
        ModuleManager.HUD.targetHud.set(v);
    }

    private void updateTarget(boolean editing) {
        MinecraftClient client = MinecraftClient.getInstance();
        Entity looked = client.targetedEntity;
        long now = System.currentTimeMillis();
        if (looked instanceof LivingEntity living && living.isAlive()) {
            if (target != living) {
                health.snap(living.getHealth() / living.getMaxHealth());
                absorption.snap(0f);
            }
            target = living;
            lastSeen = now;
        }
        boolean alive = target != null && target.isAlive() && !target.isRemoved();
        boolean active = alive && now - lastSeen < LINGER;
        if (!active && editing && client.player != null) {
            if (target != client.player) health.snap(client.player.getHealth() / client.player.getMaxHealth());
            target = client.player;
            active = true;
        }
        show.to(active);
        if (!active && show.get() < 0.01f) target = null;
    }

    @Override
    protected boolean hasContent(boolean editing) {
        return target != null;
    }

    @Override
    protected void layout(boolean editing) {
        updateTarget(editing);
        w = W;
        h = PAD * 2f + HEAD;
    }

    @Override
    protected void draw(DrawContext ctx, boolean editing) {
        if (target == null) return;
        float a = Anim.easeOutCubic(show.get());
        if (a <= 0.01f) return;

        float prev = Render2D.alpha();
        Render2D.setAlpha(prev * a);
        float cx = x + w / 2f, cy = y + h / 2f, s = 0.92f + 0.08f * a;
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(cx, cy);
        ctx.getMatrices().scale(s, s);
        ctx.getMatrices().translate(-cx, -cy);

        HudDraw.glass(ctx, x, y, w, h, 8f);

        float hx = x + PAD, hy = y + PAD;
        float hurtT = hurt.to(target.hurtTime > 0).get();
        Render2D.shadow(ctx, hx, hy, HEAD, HEAD, 6f, 8f, Colors.lerp(Theme.accent(0.30f), 0x80FF3B3B, hurtT));
        Render2D.rect(ctx, hx, hy, HEAD, HEAD, 6f, 0x30000000);
        HudDraw.head(ctx, target, hx + 2f, hy + 2f, HEAD - 4f);
        if (hurtT > 0.01f) Render2D.rect(ctx, hx + 2f, hy + 2f, HEAD - 4f, HEAD - 4f, 2f, Colors.withAlpha(0xFFFF3030, 0.45f * hurtT));
        Render2D.outline(ctx, hx, hy, HEAD, HEAD, 6f, 0.8f, Theme.accent(0.5f));

        float tx = hx + HEAD + 7f, right = x + w - PAD, rowW = right - tx;
        String name = target.getName().getString();
        String dist = "";
        MinecraftClient client = MinecraftClient.getInstance();
        if (distance.get() && client.player != null && target != client.player) {
            dist = String.format(Locale.ROOT, "%.1fm", client.player.distanceTo(target));
        }
        float distW = dist.isEmpty() ? 0f : Fonts.REGULAR.width(dist, 5.6f) + 4f;
        Fonts.SEMIBOLD.drawCentered(ctx, Fonts.SEMIBOLD.trim(name, 7.2f, rowW - distW), tx, hy + 4.5f, 7.2f, Theme.TEXT);
        if (!dist.isEmpty()) Fonts.REGULAR.drawCentered(ctx, dist, right - distW + 4f, hy + 4.5f, 5.6f, Theme.TEXT_MUTE);

        float max = Math.max(1f, target.getMaxHealth());
        float hp = health.to(Math.min(1f, target.getHealth() / max)).get();
        float abs = absorption.to(Math.min(1f, target.getAbsorptionAmount() / max)).get();
        float barY = hy + 11.5f, barH = 3.5f;
        Render2D.rect(ctx, tx, barY, rowW - 26f, barH, barH / 2f, 0x30FFFFFF);
        float bw = rowW - 26f;
        if (hp > 0.001f) {
            Render2D.shadow(ctx, tx, barY, bw * hp, barH, barH / 2f, 5f, Theme.accent(0.35f));
            Render2D.gradientH(ctx, tx, barY, Math.max(barH, bw * hp), barH, barH / 2f, Theme.accent2(), Theme.accent());
        }
        if (abs > 0.001f) Render2D.rect(ctx, tx, barY, Math.max(barH, bw * abs), barH, barH / 2f, 0xD0F5C542);
        String hpText = String.format(Locale.ROOT, "%.1f", target.getHealth() + target.getAbsorptionAmount());
        Icons.get("heart").drawCentered(ctx, right - 22f, barY + barH / 2f, 6f, 0xFFFF5A6A);
        Fonts.SEMIBOLD.drawCentered(ctx, hpText, right - 17.5f, barY + barH / 2f, 5.8f, Theme.TEXT);

        if (items.get()) {
            float iy = hy + 18f, size = 10f, step = 13f;
            float ix = tx;
            ix = slot(ctx, target.getMainHandStack(), ix, iy, size) + step;
            ix = slot(ctx, target.getOffHandStack(), ix, iy, size) + step + 3f;
            for (EquipmentSlot slot : ARMOR) {
                ix = slot(ctx, target.getEquippedStack(slot), ix, iy, size) + step;
            }
        }

        ctx.getMatrices().popMatrix();
        Render2D.setAlpha(prev);
    }

    private static float slot(DrawContext ctx, ItemStack stack, float x, float y, float size) {
        Render2D.rect(ctx, x - 1f, y - 1f, size + 2f, size + 2f, 2.5f, 0x14FFFFFF);
        HudDraw.item(ctx, stack, x, y, size);
        HudDraw.durability(ctx, stack, x, y + size + 1.2f, size);
        return x;
    }
}