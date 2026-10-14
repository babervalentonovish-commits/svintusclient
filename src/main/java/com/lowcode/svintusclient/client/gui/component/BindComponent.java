package com.lowcode.svintusclient.client.gui.component;

import com.lowcode.svintusclient.client.gui.Theme;
import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.render.font.Fonts;
import com.lowcode.svintusclient.client.render.icon.Icons;
import com.lowcode.svintusclient.client.util.Anim;
import com.lowcode.svintusclient.client.util.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class BindComponent extends SettingComponent {

    private static final float PILL_H = 15f;
    private static final float TEXT_SIZE = 6.2f;

    private final String name;
    private final IntSupplier getter;
    private final IntConsumer setter;
    private final Anim listen = new Anim(14f);
    private boolean listening;

    public BindComponent(String name, IntSupplier getter, IntConsumer setter) {
        this.name = name;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public float height() {
        return 24f;
    }

    public static String keyName(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return "Нет";
        String name = InputUtil.Type.KEYSYM.createFromCode(key).getLocalizedText().getString();
        return name.length() <= 2 ? name.toUpperCase(Locale.ROOT) : name;
    }

    @Override
    public void render(DrawContext ctx, float mx, float my) {
        float h = drawCard(ctx, mx, my);
        float l = listen.to(listening).get();

        Icons.KEYBOARD.drawCentered(ctx, x + PAD + 4f, y + height() / 2f, 9f, Colors.lerp(Theme.TEXT_MUTE, Theme.accent(), l));
        Fonts.REGULAR.drawCentered(ctx, name, x + PAD + 13f, y + height() / 2f, LABEL_SIZE,
                Colors.lerp(Theme.TEXT_DIM, Theme.TEXT, Math.max(h * 0.7f, l)));

        int key = getter.getAsInt();
        String text = listening ? "Нажми клавишу…" : keyName(key);
        float tw = Fonts.REGULAR.width(text, TEXT_SIZE);
        float pw = Math.max(40f, tw + 16f);
        float px = x + w - PAD + 3f - pw, py = y + (height() - PILL_H) / 2f;

        float pulse = listening ? 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 160.0) : 0f;
        Render2D.rect(ctx, px, py, pw, PILL_H, 5f, Colors.lerp(Theme.SURFACE_RAISED, Theme.tint(Theme.SURFACE_RAISED, 0.12f), l));
        Render2D.outline(ctx, px, py, pw, PILL_H, 5f, 0.8f, Colors.lerp(0x0DFFFFFF, Theme.accent(0.4f + 0.4f * pulse), l));
        int color = listening ? Theme.accent() : key == GLFW.GLFW_KEY_UNKNOWN ? Theme.TEXT_MUTE : Theme.TEXT;
        Fonts.REGULAR.drawCentered(ctx, text, px + (pw - tw) / 2f, py + PILL_H / 2f, TEXT_SIZE, color);
    }

    @Override
    public boolean mouseClicked(float mx, float my, int button) {
        if (!isHovered(mx, my)) {
            listening = false;
            return false;
        }
        if (button == 0) {
            listening = !listening;
        } else if (button == 1) {
            setter.accept(GLFW.GLFW_KEY_UNKNOWN);
            listening = false;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int key) {
        if (!listening) return false;
        if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) setter.accept(GLFW.GLFW_KEY_UNKNOWN);
        else if (key != GLFW.GLFW_KEY_ESCAPE) setter.accept(key);
        listening = false;
        return true;
    }
}