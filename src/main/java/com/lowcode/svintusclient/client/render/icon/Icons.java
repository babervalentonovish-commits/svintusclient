package com.lowcode.svintusclient.client.render.icon;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class Icons {

    private static final Map<String, Icon> ICONS = new HashMap<>();

    public static final Icon SEARCH = get("search");
    public static final Icon CHEVRON = get("chevron");
    public static final Icon KEYBOARD = get("keyboard");
    public static final Icon SETTINGS = get("settings");

    private Icons() {
    }

    public static Icon get(String name) {
        return ICONS.computeIfAbsent(name, n -> {
            String path = "/assets/svintusclient/icons/" + n + ".svg";
            try (InputStream in = Icons.class.getResourceAsStream(path)) {
                if (in == null) throw new IllegalArgumentException("Missing icon " + path);
                return new Icon(n, Svg.parse(in));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load icon " + n, e);
            }
        });
    }
}