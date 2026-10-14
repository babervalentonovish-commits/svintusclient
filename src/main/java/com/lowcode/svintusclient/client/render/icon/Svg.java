package com.lowcode.svintusclient.client.render.icon;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Svg {

    private record Part(Shape shape, float fillAlpha, float strokeAlpha, float strokeWidth, int cap, int join) {
    }

    private static final class Style {
        boolean fill = true;
        boolean stroke = false;
        float strokeWidth = 1f;
        int cap = BasicStroke.CAP_BUTT;
        int join = BasicStroke.JOIN_MITER;
        float opacity = 1f;
        float fillOpacity = 1f;
        float strokeOpacity = 1f;
        boolean evenOdd = false;

        Style copy() {
            Style s = new Style();
            s.fill = fill;
            s.stroke = stroke;
            s.strokeWidth = strokeWidth;
            s.cap = cap;
            s.join = join;
            s.opacity = 1f;
            s.fillOpacity = fillOpacity;
            s.strokeOpacity = strokeOpacity;
            s.evenOdd = evenOdd;
            return s;
        }
    }

    private final float vbX, vbY, vbW, vbH;
    private final List<Part> parts;

    private Svg(float vbX, float vbY, float vbW, float vbH, List<Part> parts) {
        this.vbX = vbX;
        this.vbY = vbY;
        this.vbW = vbW;
        this.vbH = vbH;
        this.parts = parts;
    }

    public static Svg parse(InputStream in) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Element root = factory.newDocumentBuilder().parse(in).getDocumentElement();

            float x = 0, y = 0, w = 24, h = 24;
            String viewBox = root.getAttribute("viewBox");
            if (!viewBox.isEmpty()) {
                String[] v = viewBox.trim().split("[\\s,]+");
                x = Float.parseFloat(v[0]);
                y = Float.parseFloat(v[1]);
                w = Float.parseFloat(v[2]);
                h = Float.parseFloat(v[3]);
            } else if (!root.getAttribute("width").isEmpty()) {
                w = parseLength(root.getAttribute("width"));
                h = parseLength(root.getAttribute("height"));
            }

            List<Part> parts = new ArrayList<>();
            walk(root, new Style(), 1f, parts);
            return new Svg(x, y, w, h, parts);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse SVG", e);
        }
    }

    private static void walk(Element el, Style parent, float parentOpacity, List<Part> out) {
        Style style = parent.copy();
        applyAttributes(el, style);
        float opacity = parentOpacity * style.opacity;

        Shape shape = switch (el.getTagName()) {
            case "path" -> PathParser.parse(el.getAttribute("d"), style.evenOdd);
            case "circle" -> {
                float cx = num(el, "cx"), cy = num(el, "cy"), r = num(el, "r");
                yield new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2);
            }
            case "ellipse" -> {
                float cx = num(el, "cx"), cy = num(el, "cy"), rx = num(el, "rx"), ry = num(el, "ry");
                yield new Ellipse2D.Float(cx - rx, cy - ry, rx * 2, ry * 2);
            }
            case "rect" -> {
                float rx = num(el, "rx"), ry = num(el, "ry");
                if (!el.hasAttribute("ry")) ry = rx;
                if (!el.hasAttribute("rx")) rx = ry;
                yield new RoundRectangle2D.Float(num(el, "x"), num(el, "y"), num(el, "width"), num(el, "height"), rx * 2, ry * 2);
            }
            case "line" -> new Line2D.Float(num(el, "x1"), num(el, "y1"), num(el, "x2"), num(el, "y2"));
            case "polyline", "polygon" -> {
                String[] p = el.getAttribute("points").trim().split("[\\s,]+");
                Path2D.Float path = new Path2D.Float();
                for (int i = 0; i + 1 < p.length; i += 2) {
                    float px = Float.parseFloat(p[i]), py = Float.parseFloat(p[i + 1]);
                    if (i == 0) path.moveTo(px, py);
                    else path.lineTo(px, py);
                }
                if (el.getTagName().equals("polygon")) path.closePath();
                yield path;
            }
            default -> null;
        };

        if (shape != null) {
            float fillAlpha = style.fill ? opacity * style.fillOpacity : 0f;
            float strokeAlpha = style.stroke ? opacity * style.strokeOpacity : 0f;
            out.add(new Part(shape, fillAlpha, strokeAlpha, style.strokeWidth, style.cap, style.join));
        }

        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element e) walk(e, style, opacity, out);
        }
    }

    private static void applyAttributes(Element el, Style s) {
        if (el.hasAttribute("fill")) s.fill = !el.getAttribute("fill").trim().equals("none");
        if (el.hasAttribute("stroke")) s.stroke = !el.getAttribute("stroke").trim().equals("none");
        if (el.hasAttribute("stroke-width")) s.strokeWidth = parseLength(el.getAttribute("stroke-width"));
        if (el.hasAttribute("opacity")) s.opacity = Float.parseFloat(el.getAttribute("opacity"));
        if (el.hasAttribute("fill-opacity")) s.fillOpacity = Float.parseFloat(el.getAttribute("fill-opacity"));
        if (el.hasAttribute("stroke-opacity")) s.strokeOpacity = Float.parseFloat(el.getAttribute("stroke-opacity"));
        if (el.hasAttribute("fill-rule")) s.evenOdd = el.getAttribute("fill-rule").trim().equals("evenodd");
        if (el.hasAttribute("stroke-linecap")) {
            s.cap = switch (el.getAttribute("stroke-linecap").trim()) {
                case "round" -> BasicStroke.CAP_ROUND;
                case "square" -> BasicStroke.CAP_SQUARE;
                default -> BasicStroke.CAP_BUTT;
            };
        }
        if (el.hasAttribute("stroke-linejoin")) {
            s.join = switch (el.getAttribute("stroke-linejoin").trim()) {
                case "round" -> BasicStroke.JOIN_ROUND;
                case "bevel" -> BasicStroke.JOIN_BEVEL;
                default -> BasicStroke.JOIN_MITER;
            };
        }
    }

    private static float num(Element el, String attr) {
        String v = el.getAttribute(attr);
        return v.isEmpty() ? 0f : parseLength(v);
    }

    private static float parseLength(String v) {
        return Float.parseFloat(v.trim().toLowerCase(Locale.ROOT).replace("px", ""));
    }

    public BufferedImage rasterize(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        float scale = size / Math.max(vbW, vbH);
        g.translate((size - vbW * scale) / 2.0, (size - vbH * scale) / 2.0);
        g.scale(scale, scale);
        g.translate(-vbX, -vbY);

        for (Part part : parts) {
            if (part.fillAlpha > 0 && !(part.shape instanceof Line2D)) {
                g.setColor(new Color(1f, 1f, 1f, Math.min(1f, part.fillAlpha)));
                g.fill(part.shape);
            }
            if (part.strokeAlpha > 0) {
                g.setColor(new Color(1f, 1f, 1f, Math.min(1f, part.strokeAlpha)));
                g.setStroke(new BasicStroke(part.strokeWidth, part.cap, part.join, 4f));
                g.draw(part.shape);
            }
        }
        g.dispose();
        return img;
    }

    static final class PathParser {
        private final String s;
        private int pos;

        private PathParser(String s) {
            this.s = s;
        }

        static Path2D.Double parse(String d, boolean evenOdd) {
            Path2D.Double path = new Path2D.Double(evenOdd ? Path2D.WIND_EVEN_ODD : Path2D.WIND_NON_ZERO);
            if (d != null && !d.isEmpty()) new PathParser(d).run(path);
            return path;
        }

        private void run(Path2D.Double p) {
            char cmd = 0;
            char prev = 0;
            double cx = 0, cy = 0, startX = 0, startY = 0, ctrlX = 0, ctrlY = 0;

            while (true) {
                skipSeparators();
                if (pos >= s.length()) break;
                char c = s.charAt(pos);
                if (isCommand(c)) {
                    cmd = c;
                    pos++;
                } else if (cmd == 0 || cmd == 'Z' || cmd == 'z') {
                    break;
                }

                boolean rel = Character.isLowerCase(cmd);
                double ox = rel ? cx : 0, oy = rel ? cy : 0;
                char type = Character.toUpperCase(cmd);

                switch (type) {
                    case 'M' -> {
                        cx = num() + ox;
                        cy = num() + oy;
                        p.moveTo(cx, cy);
                        startX = cx;
                        startY = cy;
                        cmd = rel ? 'l' : 'L';
                    }
                    case 'L' -> {
                        cx = num() + ox;
                        cy = num() + oy;
                        p.lineTo(cx, cy);
                    }
                    case 'H' -> {
                        cx = num() + ox;
                        p.lineTo(cx, cy);
                    }
                    case 'V' -> {
                        cy = num() + oy;
                        p.lineTo(cx, cy);
                    }
                    case 'C' -> {
                        double x1 = num() + ox, y1 = num() + oy;
                        double x2 = num() + ox, y2 = num() + oy;
                        cx = num() + ox;
                        cy = num() + oy;
                        p.curveTo(x1, y1, x2, y2, cx, cy);
                        ctrlX = x2;
                        ctrlY = y2;
                    }
                    case 'S' -> {
                        double x1 = (prev == 'C' || prev == 'S') ? 2 * cx - ctrlX : cx;
                        double y1 = (prev == 'C' || prev == 'S') ? 2 * cy - ctrlY : cy;
                        double x2 = num() + ox, y2 = num() + oy;
                        cx = num() + ox;
                        cy = num() + oy;
                        p.curveTo(x1, y1, x2, y2, cx, cy);
                        ctrlX = x2;
                        ctrlY = y2;
                    }
                    case 'Q' -> {
                        double x1 = num() + ox, y1 = num() + oy;
                        cx = num() + ox;
                        cy = num() + oy;
                        p.quadTo(x1, y1, cx, cy);
                        ctrlX = x1;
                        ctrlY = y1;
                    }
                    case 'T' -> {
                        double x1 = (prev == 'Q' || prev == 'T') ? 2 * cx - ctrlX : cx;
                        double y1 = (prev == 'Q' || prev == 'T') ? 2 * cy - ctrlY : cy;
                        cx = num() + ox;
                        cy = num() + oy;
                        p.quadTo(x1, y1, cx, cy);
                        ctrlX = x1;
                        ctrlY = y1;
                    }
                    case 'A' -> {
                        double rx = num(), ry = num(), angle = num();
                        boolean large = flag(), sweep = flag();
                        double x = num() + ox, y = num() + oy;
                        arcTo(p, cx, cy, rx, ry, angle, large, sweep, x, y);
                        cx = x;
                        cy = y;
                    }
                    case 'Z' -> {
                        p.closePath();
                        cx = startX;
                        cy = startY;
                    }
                    default -> {
                        return;
                    }
                }
                prev = type;
            }
        }

        private static boolean isCommand(char c) {
            return "MmLlHhVvCcSsQqTtAaZz".indexOf(c) >= 0;
        }

        private void skipSeparators() {
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if (c == ',' || Character.isWhitespace(c)) pos++;
                else break;
            }
        }

        private boolean flag() {
            skipSeparators();
            char c = s.charAt(pos++);
            return c == '1';
        }

        private double num() {
            skipSeparators();
            int start = pos;
            if (pos < s.length() && (s.charAt(pos) == '-' || s.charAt(pos) == '+')) pos++;
            boolean dot = false;
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if (Character.isDigit(c)) {
                    pos++;
                } else if (c == '.' && !dot) {
                    dot = true;
                    pos++;
                } else {
                    break;
                }
            }
            if (pos < s.length() && (s.charAt(pos) == 'e' || s.charAt(pos) == 'E')) {
                pos++;
                if (pos < s.length() && (s.charAt(pos) == '-' || s.charAt(pos) == '+')) pos++;
                while (pos < s.length() && Character.isDigit(s.charAt(pos))) pos++;
            }
            if (start == pos) throw new IllegalArgumentException("Number expected at " + pos + ": " + s);
            return Double.parseDouble(s.substring(start, pos));
        }

        private static void arcTo(Path2D.Double p, double x0, double y0, double rx, double ry, double angle,
                                  boolean largeArc, boolean sweep, double x, double y) {
            if (x0 == x && y0 == y) return;
            if (rx == 0 || ry == 0) {
                p.lineTo(x, y);
                return;
            }
            rx = Math.abs(rx);
            ry = Math.abs(ry);

            double dx2 = (x0 - x) / 2.0, dy2 = (y0 - y) / 2.0;
            double a = Math.toRadians(angle % 360.0);
            double cos = Math.cos(a), sin = Math.sin(a);
            double x1 = cos * dx2 + sin * dy2;
            double y1 = -sin * dx2 + cos * dy2;

            double rx2 = rx * rx, ry2 = ry * ry, x12 = x1 * x1, y12 = y1 * y1;
            double check = x12 / rx2 + y12 / ry2;
            if (check > 1) {
                rx *= Math.sqrt(check);
                ry *= Math.sqrt(check);
                rx2 = rx * rx;
                ry2 = ry * ry;
            }

            double sign = (largeArc == sweep) ? -1 : 1;
            double sq = ((rx2 * ry2) - (rx2 * y12) - (ry2 * x12)) / ((rx2 * y12) + (ry2 * x12));
            double coef = sign * Math.sqrt(Math.max(0, sq));
            double cx1 = coef * ((rx * y1) / ry);
            double cy1 = coef * -((ry * x1) / rx);

            double cx = (x0 + x) / 2.0 + (cos * cx1 - sin * cy1);
            double cy = (y0 + y) / 2.0 + (sin * cx1 + cos * cy1);

            double ux = (x1 - cx1) / rx, uy = (y1 - cy1) / ry;
            double vx = (-x1 - cx1) / rx, vy = (-y1 - cy1) / ry;

            double n = Math.sqrt(ux * ux + uy * uy);
            double start = Math.toDegrees((uy < 0 ? -1 : 1) * Math.acos(Math.max(-1, Math.min(1, ux / n))));

            n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
            double dot = ux * vx + uy * vy;
            double extent = Math.toDegrees((ux * vy - uy * vx < 0 ? -1 : 1) * Math.acos(Math.max(-1, Math.min(1, dot / n))));
            if (!sweep && extent > 0) extent -= 360;
            else if (sweep && extent < 0) extent += 360;

            Arc2D.Double arc = new Arc2D.Double(cx - rx, cy - ry, rx * 2, ry * 2, -start, -extent, Arc2D.OPEN);
            Shape shape = AffineTransform.getRotateInstance(a, cx, cy).createTransformedShape(arc);
            p.append(shape, true);
        }
    }
}