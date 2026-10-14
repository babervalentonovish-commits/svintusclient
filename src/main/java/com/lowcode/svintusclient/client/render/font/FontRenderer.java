package com.lowcode.svintusclient.client.render.font;

import com.lowcode.svintusclient.client.render.GpuImage;
import com.lowcode.svintusclient.client.render.Render2D;
import com.lowcode.svintusclient.client.util.Colors;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class FontRenderer {

    private static final int PAGE_SIZE = 1024;
    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

    private final String name;
    private final Font[] faces;
    private final Map<Integer, Size> sizes = new HashMap<>();
    private final List<Page> pages = new ArrayList<>();

    public FontRenderer(String name, String... resources) {
        this.name = name;
        List<Font> loaded = new ArrayList<>();
        for (String resource : resources) {
            try (InputStream in = FontRenderer.class.getResourceAsStream(resource)) {
                if (in != null) {
                    loaded.add(Font.createFont(Font.TRUETYPE_FONT, in));
                }
            } catch (Exception ignored) {
            }
        }
        if (loaded.isEmpty()) {
            loaded.add(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        }
        this.faces = loaded.toArray(new Font[0]);
    }

    public float draw(DrawContext ctx, String text, float x, float y, float size, int color) {
        if (text.isEmpty()) return 0;
        Size sz = size(size);
        float ps = Render2D.pixelScale();
        float originX = Render2D.snap(x);
        float baseline = Render2D.snap(y + sz.ascent / ps);

        Map<Page, float[]> batches = new IdentityHashMap<>();
        Map<Page, int[]> counts = new IdentityHashMap<>();
        float pen = 0;

        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            Glyph g = sz.glyph(cp);
            if (g.page != null) {
                float gx = originX + (Math.round(pen) + g.xOff) / ps;
                float gy = baseline + g.yOff / ps;
                int[] count = counts.computeIfAbsent(g.page, p -> new int[1]);
                float[] quads = batches.get(g.page);
                if (quads == null || quads.length < (count[0] + 1) * 8) {
                    float[] grown = new float[Math.max(64, (count[0] + 1) * 16)];
                    if (quads != null) System.arraycopy(quads, 0, grown, 0, count[0] * 8);
                    batches.put(g.page, quads = grown);
                }
                int o = count[0]++ * 8;
                quads[o] = gx;
                quads[o + 1] = gy;
                quads[o + 2] = gx + g.w / ps;
                quads[o + 3] = gy + g.h / ps;
                quads[o + 4] = g.u / (float) PAGE_SIZE;
                quads[o + 5] = g.v / (float) PAGE_SIZE;
                quads[o + 6] = (g.u + g.w) / (float) PAGE_SIZE;
                quads[o + 7] = (g.v + g.h) / (float) PAGE_SIZE;
            }
            pen += g.advance;
        }

        for (Map.Entry<Page, float[]> e : batches.entrySet()) {
            Render2D.texturedQuads(ctx, e.getKey().image.setup(), e.getValue(), counts.get(e.getKey())[0], color);
        }
        return pen / ps;
    }

    public float drawCentered(DrawContext ctx, String text, float x, float centerY, float size, int color) {
        Size sz = size(size);
        float ps = Render2D.pixelScale();
        return draw(ctx, text, x, centerY - (sz.ascent - sz.capHeight / 2f) / ps, size, color);
    }

    public float drawGradient(DrawContext ctx, String text, float x, float centerY, float size, int from, int to) {
        float total = width(text, size);
        float pen = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            String ch = new String(Character.toChars(cp));
            i += Character.charCount(cp);
            float t = total <= 0 ? 0 : (pen + width(ch, size) / 2f) / total;
            drawCentered(ctx, ch, x + pen, centerY, size, Colors.lerp(from, to, t));
            pen += width(ch, size);
        }
        return total;
    }

    public float width(String text, float size) {
        Size sz = size(size);
        float pen = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            pen += sz.glyph(cp).advance;
        }
        return pen / Render2D.pixelScale();
    }

    public float height(float size) {
        Size sz = size(size);
        return (sz.ascent + sz.descent) / Render2D.pixelScale();
    }

    public String trim(String text, float size, float maxWidth) {
        if (width(text, size) <= maxWidth) return text;
        String dots = "…";
        float dotsW = width(dots, size);
        int end = text.length();
        while (end > 0 && width(text.substring(0, end), size) + dotsW > maxWidth) end--;
        return text.substring(0, end).stripTrailing() + dots;
    }

    public List<String> wrap(String text, float size, float maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (width(candidate, size) <= maxWidth || line.isEmpty()) {
                line.setLength(0);
                line.append(candidate);
            } else {
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            }
        }
        if (!line.isEmpty()) lines.add(line.toString());
        return lines;
    }

    private Size size(float units) {
        float px = units * Render2D.pixelScale();
        int key = Math.max(4, Math.round(px * 4f));
        return sizes.computeIfAbsent(key, k -> new Size(k / 4f));
    }

    private record Glyph(Page page, int u, int v, int w, int h, int xOff, int yOff, float advance) {
    }

    private final class Size {
        final Font[] derived;
        final float ascent, descent, capHeight;
        final Int2ObjectMap<Glyph> glyphs = new Int2ObjectOpenHashMap<>();

        Size(float px) {
            derived = new Font[faces.length];
            for (int i = 0; i < faces.length; i++) derived[i] = faces[i].deriveFont(px);
            LineMetrics lm = derived[0].getLineMetrics("Hg", FRC);
            ascent = lm.getAscent();
            descent = lm.getDescent();
            capHeight = (float) -derived[0].createGlyphVector(FRC, "H").getVisualBounds().getY();
        }

        Glyph glyph(int cp) {
            Glyph g = glyphs.get(cp);
            if (g == null) {
                g = rasterize(cp);
                glyphs.put(cp, g);
            }
            return g;
        }

        private Glyph rasterize(int cp) {
            Font font = derived[0];
            for (Font f : derived) {
                if (f.canDisplay(cp)) {
                    font = f;
                    break;
                }
            }
            GlyphVector gv = font.createGlyphVector(FRC, new String(Character.toChars(cp)));
            float advance = (float) gv.getGlyphMetrics(0).getAdvanceX();
            Rectangle pb = gv.getPixelBounds(FRC, 0, 0);
            if (pb.width <= 0 || pb.height <= 0) return new Glyph(null, 0, 0, 0, 0, 0, 0, advance);

            int w = pb.width + 2, h = pb.height + 2;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setColor(Color.WHITE);
            g.drawGlyphVector(gv, -pb.x + 1, -pb.y + 1);
            g.dispose();

            int[] slot = allocate(w, h);
            Page page = pages.get(slot[0]);
            GpuImage.blitAlpha(img, page.pixels, slot[1], slot[2]);
            page.image.markDirty();
            return new Glyph(page, slot[1], slot[2], w, h, pb.x - 1, pb.y - 1, advance);
        }
    }

    private static final class Page {
        final NativeImage pixels;
        final GpuImage image;
        int cursorX = 1, cursorY = 1, rowHeight = 0;

        Page(String name) {
            pixels = GpuImage.blank(PAGE_SIZE, PAGE_SIZE);
            image = new GpuImage(name, pixels);
        }
    }

    private int[] allocate(int w, int h) {
        if (pages.isEmpty()) pages.add(new Page(name + "_page0"));
        Page page = pages.get(pages.size() - 1);
        if (page.cursorX + w + 1 > PAGE_SIZE) {
            page.cursorX = 1;
            page.cursorY += page.rowHeight + 1;
            page.rowHeight = 0;
        }
        if (page.cursorY + h + 1 > PAGE_SIZE) {
            page = new Page(name + "_page" + pages.size());
            pages.add(page);
        }
        int[] slot = {pages.size() - 1, page.cursorX, page.cursorY};
        page.cursorX += w + 1;
        page.rowHeight = Math.max(page.rowHeight, h);
        return slot;
    }
}