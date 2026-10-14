package com.lowcode.svintusclient.client.hud.widget;

import com.lowcode.svintusclient.client.gui.Theme;
import com.lowcode.svintusclient.client.hud.HudDraw;
import com.lowcode.svintusclient.client.hud.HudWidget;
import com.lowcode.svintusclient.client.module.ModuleManager;
import com.lowcode.svintusclient.client.module.setting.BooleanSetting;
import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.render.font.Fonts;
import com.lowcode.svintusclient.client.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ArmorHudWidget extends HudWidget {

    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final float CELL = 18f, ITEM = 14f, GAP = 3f, PAD = 4f;

    private final BooleanSetting hands = option("Предметы в руках", true);
    private final BooleanSetting percent = option("Прочность в %", true);

    private final List<ItemStack> stacks = new ArrayList<>();
    private int armorCount;

    public ArmorHudWidget() {
        super("armor", "Броня", 0.5f, 0.8f);
    }

    @Override
    public boolean isVisible() {
        return ModuleManager.HUD.armorHud.get();
    }

    @Override
    public void setVisible(boolean v) {
        ModuleManager.HUD.armorHud.set(v);
    }

    @Override
    protected void layout(boolean editing) {
        stacks.clear();
        PlayerEntity player = MinecraftClient.getInstance().player;
        for (EquipmentSlot slot : ARMOR) stacks.add(player == null ? ItemStack.EMPTY : player.getEquippedStack(slot));
        armorCount = stacks.size();
        if (hands.get()) {
            stacks.add(player == null ? ItemStack.EMPTY : player.getMainHandStack());
            stacks.add(player == null ? ItemStack.EMPTY : player.getOffHandStack());
        }
        int n = stacks.size();
        w = PAD * 2f + n * CELL + (n - 1) * GAP + (hands.get() ? 4f : 0f);
        h = PAD * 2f + CELL + (percent.get() ? 7f : 0f);
    }

    @Override
    protected boolean hasContent(boolean editing) {
        if (editing) return true;
        for (ItemStack stack : stacks) if (!stack.isEmpty()) return true;
        return false;
    }

    @Override
    protected void draw(DrawContext ctx, boolean editing) {
        if (!hasContent(editing)) return;
        HudDraw.glass(ctx, x, y, w, h, 7f);

        float cx = x + PAD, cy = y + PAD;
        for (int i = 0; i < stacks.size(); i++) {
            if (i == armorCount) {
                Render2D.rect(ctx, cx + 0.5f, cy + 3f, 0.8f, CELL - 6f, 0.4f, 0x1EFFFFFF);
                cx += 4f;
            }
            ItemStack stack = stacks.get(i);
            int pct = HudDraw.durabilityPercent(stack);
            int tint = pct < 0 ? 0x10FFFFFF : pct <= 20 ? 0x30FF4040 : 0x14FFFFFF;
            Render2D.rect(ctx, cx, cy, CELL, CELL, 4f, tint);
            if (stack.isEmpty()) {
                Render2D.circle(ctx, cx + CELL / 2f, cy + CELL / 2f, 1.2f, 0x22FFFFFF);
            } else {
                HudDraw.item(ctx, stack, cx + (CELL - ITEM) / 2f, cy + (CELL - ITEM) / 2f - 0.5f, ITEM);
                HudDraw.durability(ctx, stack, cx + 3f, cy + CELL - 2.6f, CELL - 6f);
            }
            if (percent.get()) {
                String text = pct < 0 ? (stack.getCount() > 1 ? "x" + stack.getCount() : "") : pct + "%";
                if (!text.isEmpty()) {
                    int color = pct < 0 ? Theme.TEXT_DIM : Colors.lerp(0xFFFF5A5A, Theme.TEXT, Math.min(1f, pct / 60f));
                    float tw = Fonts.SEMIBOLD.width(text, 5f);
                    Fonts.SEMIBOLD.drawCentered(ctx, text, cx + (CELL - tw) / 2f, cy + CELL + 4f, 5f, color);
                }
            }
            cx += CELL + GAP;
        }
    }
}