package com.lowcode.svintusclient.client.gui;

import com.lowcode.svintusclient.client.Config;
import com.lowcode.svintusclient.client.SvintusclientClient;
import com.lowcode.svintusclient.client.module.Category;
import com.lowcode.svintusclient.client.module.Module;
import com.lowcode.svintusclient.client.module.ModuleManager;
import com.lowcode.svintusclient.client.module.client.ClickGuiModule;
import com.lowcode.svintusclient.client.render.BlurEffect;
import com.lowcode.svintusclient.client.render.PigHead;
import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.render.font.Fonts;
import com.lowcode.svintusclient.client.render.icon.Icons;
import com.lowcode.svintusclient.client.util.Anim;
import com.lowcode.svintusclient.client.util.Colors;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ClickGuiScreen extends Screen {

    private static final float W = 440f, H = 296f, RADIUS = 14f, MARGIN = 14f;
    private static final float HEADER_Y = 12f, LOGO = 26f;
    private static final float TABS_Y = 46f, TABS_H = 22f;
    private static final float BODY_Y = 78f, FOOTER_H = 26f;
    private static final float SEARCH_W = 124f, SEARCH_H = 20f;
    private static final float COL_GAP = 8f, CARD_GAP = 6f;
    private static final float DOT_GAP = 13f;

    private static Category category = Category.CLIENT;
    private static float offsetX, offsetY;

    private final Anim open = new Anim(0f, 10f);
    private boolean closing;

    private float uiScale = 1f, appliedScale = -1f, animScale = 1f, panelX, panelY;
    private float rawMouseX, rawMouseY;
    private boolean mouseDown, pointer, ibeam, swallowChar;

    private final Anim tabIndicator = new Anim(16f);
    private boolean tabIndicatorPlaced;
    private final Map<Category, Anim> tabHover = new EnumMap<>(Category.class);
    private final Map<Category, Anim> tabActive = new EnumMap<>(Category.class);

    private final Map<Theme.Accent, Anim> accentHover = new EnumMap<>(Theme.Accent.class);
    private final Map<Theme.Accent, Anim> accentActive = new EnumMap<>(Theme.Accent.class);

    private final Map<Module, ModuleCard> cards = new HashMap<>();
    private final Anim scroll = new Anim(16f);
    private final Anim content = new Anim(0f, 9f);
    private float contentH;
    private List<ModuleCard> visibleCards = List.of();

    private String search = "";
    private boolean searchFocused;
    private final Anim searchFocus = new Anim(14f);
    private final Anim searchHover = new Anim(14f);

    private boolean dragging;
    private float dragStartX, dragStartY, dragOffsetX, dragOffsetY;

    public ClickGuiScreen() {
        super(Text.literal("Click GUI"));
        open.to(1f);
        content.to(1f);
    }

    private static ClickGuiModule settings() {
        return ModuleManager.CLICK_GUI;
    }

    private float bodyX() {
        return panelX + MARGIN;
    }

    private float bodyW() {
        return W - MARGIN * 2f;
    }

    private float bodyY() {
        return panelY + BODY_Y;
    }

    private float bodyH() {
        return H - BODY_Y - FOOTER_H - 4f;
    }

    private float columnW() {
        return (bodyW() - COL_GAP) / 2f;
    }

    private float searchX() {
        return panelX + W - MARGIN - SEARCH_W;
    }

    private float searchY() {
        return panelY + HEADER_Y + (LOGO - SEARCH_H) / 2f;
    }

    private float tabsX() {
        return panelX + MARGIN;
    }

    private float tabsY() {
        return panelY + TABS_Y;
    }

    private float tabW() {
        return (bodyW() - 4f) / Category.values().length;
    }

    private float footerY() {
        return panelY + H - FOOTER_H;
    }

    private float dotX(int i) {
        int n = Theme.Accent.values().length;
        return panelX + W - MARGIN - 5f - (n - 1 - i) * DOT_GAP;
    }

    private float dotY() {
        return footerY() + FOOTER_H / 2f;
    }

    private void updateLayout() {
        if (!mouseDown || appliedScale < 0) appliedScale = settings().scale.get();
        Window window = client.getWindow();
        float guiScale = window.getScaleFactor();
        float desired = Math.max(1f, window.getFramebufferHeight() / 1080f * 2f) * appliedScale;
        float fit = Math.min((width - 12f) / W, (height - 12f) / H);
        uiScale = Math.min(desired / guiScale, fit);
        Render2D.setPixelScale(guiScale * uiScale);

        float vw = width / uiScale, vh = height / uiScale;
        float baseX = (vw - W) / 2f, baseY = (vh - H) / 2f;
        offsetX = clamp(offsetX, 4f - baseX, vw - W - 4f - baseX);
        offsetY = clamp(offsetY, 4f - baseY, vh - H - 4f - baseY);
        panelX = Render2D.snap(baseX + offsetX);
        panelY = Render2D.snap(baseY + offsetY);
    }

    private float toVirtualX(double x) {
        float cx = panelX + W / 2f;
        return ((float) x / uiScale - cx) / animScale + cx;
    }

    private float toVirtualY(double y) {
        float cy = panelY + H / 2f;
        return ((float) y / uiScale - cy) / animScale + cy;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (client.world == null) renderPanoramaBackground(ctx, delta);
        float dim = settings().dim.get() * Anim.easeOutCubic(open.get());
        if (dim > 0.005f) {
            float prev = Render2D.alpha();
            Render2D.setAlpha(1f);
            Render2D.gradientV(ctx, 0, 0, width, height, 0, Colors.withAlpha(0xFF05060A, dim * 0.5f), Colors.withAlpha(0xFF05060A, dim));
            Render2D.setAlpha(prev);
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float openT = open.get();
        if (closing && openT < 0.02f) {
            super.close();
            return;
        }
        float ease = Anim.easeOutCubic(openT);

        updateLayout();
        if (settings().blur.get()) BlurEffect.capture(ctx);

        animScale = 0.94f + 0.06f * ease;
        rawMouseX = mouseX / uiScale;
        rawMouseY = mouseY / uiScale;
        float mx = toVirtualX(mouseX), my = toVirtualY(mouseY);
        pointer = false;
        ibeam = false;

        if (dragging) {
            offsetX = dragOffsetX + (rawMouseX - dragStartX);
            offsetY = dragOffsetY + (rawMouseY - dragStartY);
        }

        Matrix3x2fStack m = ctx.getMatrices();
        m.pushMatrix();
        m.scale(uiScale, uiScale);
        float cx = panelX + W / 2f, cy = panelY + H / 2f;
        m.translate(cx, cy);
        m.scale(animScale, animScale);
        m.translate(-cx, -cy);
        Render2D.setAlpha(ease);

        drawWindow(ctx);
        drawHeader(ctx, mx, my);
        drawTabs(ctx, mx, my);
        drawBody(ctx, mx, my);
        drawFooter(ctx, mx, my);
        for (ModuleCard card : visibleCards) card.renderOverlays(ctx, mx, my);
        if (anyOverlay()) pointer = true;

        Render2D.setAlpha(1f);
        m.popMatrix();

        if (dragging) ctx.setCursor(StandardCursors.RESIZE_ALL);
        else if (ibeam) ctx.setCursor(StandardCursors.IBEAM);
        else if (pointer) ctx.setCursor(StandardCursors.POINTING_HAND);
    }

    private void drawWindow(DrawContext ctx) {
        float x = panelX, y = panelY;
        Render2D.shadow(ctx, x, y, W, H, RADIUS, 40f, 0xA0000000);
        Render2D.shadow(ctx, x, y, W, H, RADIUS, 90f, Theme.accent(0.07f));

        boolean glass = settings().blur.get()
                && Render2D.blur(ctx, x, y, W, H, RADIUS, Colors.withAlpha(0xFF08090E, settings().glass.get()));
        if (!glass) Render2D.gradientV(ctx, x, y, W, H, RADIUS, 0xF5111218, 0xF50B0C10);

        Render2D.rect(ctx, x, y, W, H, RADIUS, Theme.accent(0.08f), Theme.accent(0.02f), 0x00000000, Theme.accent(0.03f));
        Render2D.gradientV(ctx, x, y, W, 60f, RADIUS, 0x0CFFFFFF, 0x00FFFFFF);
        Render2D.outline(ctx, x, y, W, H, RADIUS, 1f, 0x2AFFFFFF, 0x1CFFFFFF, 0x0AFFFFFF, 0x0EFFFFFF);
    }

    private void drawHeader(DrawContext ctx, float mx, float my) {
        float lx = panelX + MARGIN, ly = panelY + HEADER_Y;

        Render2D.shadow(ctx, lx, ly, LOGO, LOGO, 8f, 10f, Theme.accent(0.30f));
        Render2D.rect(ctx, lx, ly, LOGO, LOGO, 8f, Theme.accent(0.32f), Theme.accent(0.16f), Colors.withAlpha(Theme.accent2(), 0.12f), Colors.withAlpha(Theme.accent2(), 0.22f));
        Render2D.outline(ctx, lx, ly, LOGO, LOGO, 8f, 1f, Theme.accent(0.55f));
        PigHead.draw(ctx, lx + 5f, ly + 5f, LOGO - 10f);

        float tx = lx + LOGO + 9f;
        float w = Fonts.SEMIBOLD.drawCentered(ctx, "EFASHHVH", tx, ly + 9f, 9f, Theme.TEXT);
        Fonts.SEMIBOLD.drawCentered(ctx, "CLIENT", tx + w + 3f, ly + 9f, 9f, Theme.accent());
        float by = tx + Fonts.REGULAR.drawCentered(ctx, "v1.0  ·  by ", tx, ly + 20f, 5.8f, Theme.TEXT_DIM);
        Fonts.SEMIBOLD.drawGradient(ctx, "lowcode.dll", by, ly + 20f, 5.8f, Theme.accent2(), Theme.accent());

        float sx = searchX(), sy = searchY();
        boolean over = Widgets.inside(mx, my, sx, sy, SEARCH_W, SEARCH_H);
        if (over) ibeam = true;
        float f = searchFocus.to(searchFocused).get();
        float hv = searchHover.to(over).get();
        float r = SEARCH_H / 2f;

        Render2D.rect(ctx, sx, sy, SEARCH_W, SEARCH_H, r, Colors.lerp(0x0DFFFFFF, 0x16FFFFFF, Math.max(hv, f)));
        if (f > 0.01f) Render2D.shadow(ctx, sx, sy, SEARCH_W, SEARCH_H, r, 8f, Theme.accent(0.18f * f));
        Render2D.outline(ctx, sx, sy, SEARCH_W, SEARCH_H, r, 1f, Colors.lerp(0x0CFFFFFF, Theme.accent(0.6f), f));
        Icons.SEARCH.drawCentered(ctx, sx + 11f, sy + r, 8f, Colors.lerp(Theme.TEXT_MUTE, Theme.accent(), f));

        float textX = sx + 20f, maxW = SEARCH_W - 28f;
        if (search.isEmpty()) {
            Fonts.REGULAR.drawCentered(ctx, "Поиск", textX, sy + r, 6.2f, Theme.TEXT_MUTE);
        } else {
            Fonts.REGULAR.drawCentered(ctx, Fonts.REGULAR.trim(search, 6.2f, maxW), textX, sy + r, 6.2f, Theme.TEXT);
        }
        if (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
            float caret = textX + Math.min(Fonts.REGULAR.width(search, 6.2f), maxW) + 0.8f;
            Render2D.rect(ctx, caret, sy + r - 4f, 0.8f, 8f, 0.4f, Theme.accent());
        }
    }

    private void drawTabs(DrawContext ctx, float mx, float my) {
        float x = tabsX(), y = tabsY(), w = bodyW(), h = TABS_H, slot = tabW();
        Render2D.rect(ctx, x, y, w, h, h / 2f, 0x0AFFFFFF);
        Render2D.outline(ctx, x, y, w, h, h / 2f, 0.8f, 0x0CFFFFFF);

        float target = x + 2f + category.ordinal() * slot;
        if (!tabIndicatorPlaced) {
            tabIndicator.snap(target);
            tabIndicatorPlaced = true;
        }
        float ix = tabIndicator.to(target).get();
        float searching = search.isBlank() ? 1f : 0.35f;
        float ih = h - 4f;
        Render2D.shadow(ctx, ix, y + 2f, slot, ih, ih / 2f, 9f, Theme.accent(0.25f * searching));
        Render2D.gradientH(ctx, ix, y + 2f, slot, ih, ih / 2f, Theme.accent(0.30f * searching), Colors.withAlpha(Theme.accent2(), 0.20f * searching));
        Render2D.outline(ctx, ix, y + 2f, slot, ih, ih / 2f, 0.8f, Theme.accent(0.55f * searching));

        for (Category cat : Category.values()) {
            float sx = x + 2f + cat.ordinal() * slot;
            boolean over = Widgets.inside(mx, my, sx, y, slot, h);
            if (over) pointer = true;
            float hv = tabHover.computeIfAbsent(cat, c -> new Anim(14f)).to(over).get();
            float a = tabActive.computeIfAbsent(cat, c -> new Anim(c == category ? 1f : 0f, 12f)).to(cat == category).get() * searching;

            float textW = Fonts.SEMIBOLD.width(cat.title, 6.2f);
            float contentW = 9f + 4f + textW;
            float cx = sx + (slot - contentW) / 2f, cy = y + h / 2f;
            int text = Colors.lerp(Colors.lerp(Theme.TEXT_MUTE, Theme.TEXT_DIM, hv), Theme.TEXT, a);
            cat.icon().drawCentered(ctx, cx + 4.5f, cy, 9f, Colors.lerp(Colors.lerp(Theme.TEXT_MUTE, Theme.TEXT_DIM, hv), Theme.accent(), a));
            Fonts.SEMIBOLD.drawCentered(ctx, cat.title, cx + 13f, cy, 6.2f, text);
        }
    }

    private List<Module> filteredModules() {
        if (search.isBlank()) return ModuleManager.byCategory(category);
        String q = search.toLowerCase(Locale.ROOT).trim();
        return ModuleManager.all().stream()
                .filter(mod -> mod.getName().toLowerCase(Locale.ROOT).contains(q)
                        || mod.getDescription().toLowerCase(Locale.ROOT).contains(q))
                .toList();
    }

    private void drawBody(DrawContext ctx, float mx, float my) {
        float x = bodyX(), y = bodyY(), w = bodyW(), h = bodyH(), colW = columnW();
        boolean inBody = Widgets.inside(mx, my, x, y, w, h) && !dragging && !anyOverlay();

        List<Module> modules = filteredModules();
        if (modules.isEmpty()) {
            visibleCards = List.of();
            contentH = 0;
            drawEmpty(ctx, x, y, w, h);
            return;
        }

        float c = Anim.easeOutCubic(content.get());
        float prev = Render2D.alpha();
        Render2D.setAlpha(prev * c);
        Render2D.pushScissor(ctx, x - 8f, y, w + 16f, h);

        float top = y - scroll.get() + (1f - c) * 10f;
        float[] columns = {top, top};
        List<ModuleCard> visible = new ArrayList<>(modules.size());
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            ModuleCard card = cards.computeIfAbsent(module, ModuleCard::new);
            int col = i % 2;
            float cardX = x + col * (colW + COL_GAP);
            card.render(ctx, cardX, columns[col], colW, mx, my, inBody);
            if (inBody && card.isPointerOver(mx, my)) pointer = true;
            columns[col] += card.h + CARD_GAP;
            visible.add(card);
        }
        visibleCards = visible;
        contentH = Math.max(columns[0], columns[1]) - top - CARD_GAP;

        Render2D.popScissor(ctx);
        Render2D.setAlpha(prev);

        float max = Math.max(0f, contentH - h);
        if (scroll.target() > max) scroll.to(max);
        if (max > 0.5f) {
            float trackH = h - 8f;
            float thumbH = Math.max(18f, trackH * h / contentH);
            float thumbY = y + 4f + (trackH - thumbH) * (scroll.get() / max);
            Render2D.rect(ctx, x + w + 5f, thumbY, 2f, thumbH, 1f, 0x22FFFFFF);
        }
    }

    private void drawEmpty(DrawContext ctx, float x, float y, float w, float h) {
        boolean searching = !search.isBlank();
        float cx = x + w / 2f, cy = y + h / 2f - 12f;
        Render2D.circle(ctx, cx, cy - 4f, 18f, 0x0AFFFFFF);
        Render2D.circleOutline(ctx, cx, cy - 4f, 18f, 0.8f, 0x10FFFFFF);
        (searching ? Icons.SEARCH : category.icon()).drawCentered(ctx, cx, cy - 4f, 16f, Theme.TEXT_MUTE);

        String title = searching ? "Ничего не найдено" : "Здесь пока пусто";
        String sub = searching ? "Попробуй другой запрос" : "Модули категории " + category.title + " появятся здесь";
        Fonts.SEMIBOLD.drawCentered(ctx, title, cx - Fonts.SEMIBOLD.width(title, 7.4f) / 2f, cy + 24f, 7.4f, Theme.TEXT_DIM);
        Fonts.REGULAR.drawCentered(ctx, sub, cx - Fonts.REGULAR.width(sub, 6f) / 2f, cy + 35f, 6f, Theme.TEXT_MUTE);
    }

    private void drawFooter(DrawContext ctx, float mx, float my) {
        float y = footerY(), cy = y + FOOTER_H / 2f;
        Render2D.rect(ctx, panelX + MARGIN, y, bodyW(), 0.7f, 0.35f, 0x0EFFFFFF);

        float hx = panelX + MARGIN;
        hx = hint(ctx, hx, cy, "ЛКМ", "вкл / выкл");
        hint(ctx, hx + 8f, cy, "ПКМ", "настройки");

        Theme.Accent[] accents = Theme.Accent.values();
        for (int i = 0; i < accents.length; i++) {
            Theme.Accent accent = accents[i];
            float dx = dotX(i), dy = dotY();
            boolean over = Widgets.inside(mx, my, dx - 6f, dy - 6f, 12f, 12f);
            if (over) pointer = true;
            float h = accentHover.computeIfAbsent(accent, a -> new Anim(16f)).to(over).get();
            float a = accentActive.computeIfAbsent(accent, a2 -> new Anim(Theme.current() == accent ? 1f : 0f, 12f))
                    .to(Theme.current() == accent).get();
            if (a > 0.01f) {
                Render2D.shadow(ctx, dx - 4f, dy - 4f, 8f, 8f, 4f, 6f, Colors.withAlpha(accent.primary, 0.5f * a));
                Render2D.circleOutline(ctx, dx, dy, 6f, 1f, Colors.withAlpha(accent.primary, 0.85f * a));
            }
            Render2D.circle(ctx, dx, dy, 3.4f + h * 0.6f - a * 0.3f, Colors.lerp(Colors.lerp(accent.primary, 0xFF000000, 0.2f), accent.primary, Math.max(a, h)));
        }
    }

    private float hint(DrawContext ctx, float x, float cy, String key, String text) {
        float kw = Fonts.SEMIBOLD.width(key, 5.2f) + 8f;
        Render2D.rect(ctx, x, cy - 5f, kw, 10f, 3f, 0x10FFFFFF);
        Fonts.SEMIBOLD.drawCentered(ctx, key, x + 4f, cy, 5.2f, Theme.TEXT_DIM);
        return x + kw + 4f + Fonts.REGULAR.drawCentered(ctx, text, x + kw + 4f, cy, 5.6f, Theme.TEXT_MUTE);
    }

    private boolean anyOverlay() {
        for (ModuleCard card : visibleCards) if (card.hasOverlay()) return true;
        return false;
    }

    private void setCategory(Category cat) {
        if (cat == category && search.isBlank()) return;
        category = cat;
        search = "";
        searchFocused = false;
        scroll.snap(0f);
        content.snap(0f).to(1f);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (closing) return true;
        mouseDown = true;
        float mx = toVirtualX(click.x()), my = toVirtualY(click.y());
        int button = click.button();

        if (anyOverlay()) {
            boolean consumed = false;
            for (ModuleCard card : visibleCards) if (card.overlayClicked(mx, my, button)) consumed = true;
            if (consumed) return true;
        }

        Theme.Accent[] accents = Theme.Accent.values();
        for (int i = 0; i < accents.length; i++) {
            if (Widgets.inside(mx, my, dotX(i) - 6f, dotY() - 6f, 12f, 12f)) {
                Theme.set(accents[i]);
                return true;
            }
        }

        for (Category cat : Category.values()) {
            if (Widgets.inside(mx, my, tabsX() + 2f + cat.ordinal() * tabW(), tabsY(), tabW(), TABS_H)) {
                setCategory(cat);
                return true;
            }
        }

        searchFocused = Widgets.inside(mx, my, searchX(), searchY(), SEARCH_W, SEARCH_H);
        if (searchFocused) return true;

        boolean inBody = Widgets.inside(mx, my, bodyX(), bodyY(), bodyW(), bodyH());
        boolean handled = false;
        for (ModuleCard card : visibleCards) {
            if (!handled && inBody && card.mouseClicked(mx, my, button)) handled = true;
            else card.mouseClicked(-1e4f, -1e4f, button);
        }
        if (handled) return true;

        boolean header = Widgets.inside(mx, my, panelX, panelY, W, TABS_Y - 2f);
        boolean footer = Widgets.inside(mx, my, panelX, footerY(), W, FOOTER_H);
        if (button == 0 && (header || footer)) {
            dragging = true;
            dragStartX = rawMouseX;
            dragStartY = rawMouseY;
            dragOffsetX = offsetX;
            dragOffsetY = offsetY;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        mouseDown = false;
        dragging = false;
        for (ModuleCard card : cards.values()) card.mouseReleased();
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (anyOverlay()) return true;
        float mx = toVirtualX(mouseX), my = toVirtualY(mouseY);
        if (Widgets.inside(mx, my, bodyX(), bodyY(), bodyW(), bodyH())) {
            float max = Math.max(0f, contentH - bodyH());
            scroll.to(clamp(scroll.target() - (float) vertical * 28f, 0f, max));
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        swallowChar = false;
        for (ModuleCard card : visibleCards) {
            if (card.keyPressed(key)) {
                swallowChar = true;
                return true;
            }
        }

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (anyOverlay()) {
                for (ModuleCard card : visibleCards) card.overlayClicked(-1e4f, -1e4f, 0);
            } else if (!search.isEmpty()) {
                search = "";
                searchFocused = false;
            } else {
                close();
            }
            return true;
        }
        if (key == SvintusclientClient.clickGuiKey() && !searchFocused) {
            close();
            return true;
        }
        if (key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            boolean ctrl = (input.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0;
            search = ctrl ? "" : search.substring(0, search.offsetByCodePoints(search.length(), -1));
            scroll.snap(0f);
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER && searchFocused) {
            searchFocused = false;
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (swallowChar) {
            swallowChar = false;
            return true;
        }
        if (!input.isValidChar()) return false;
        if (search.length() >= 24) return true;
        search += input.asString();
        searchFocused = true;
        scroll.snap(0f);
        return true;
    }

    @Override
    public void close() {
        if (closing) return;
        Config.save();
        closing = true;
        dragging = false;
        open.to(0f);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static float clamp(float v, float min, float max) {
        return max < min ? min : Math.max(min, Math.min(max, v));
    }
}