package com.lowcode.svintusclient.client.module;

import com.lowcode.svintusclient.client.render.icon.Icon;
import com.lowcode.svintusclient.client.render.icon.Icons;

public enum Category {
    COMBAT("Combat", "combat"),
    MOVEMENT("Movement", "movement"),
    PLAYER("Player", "player"),
    MISC("Misc", "misc"),
    RENDER("Render", "render"),
    CLIENT("Client", "client");

    public final String title;
    private final String iconName;

    Category(String title, String iconName) {
        this.title = title;
        this.iconName = iconName;
    }

    public Icon icon() {
        return Icons.get(iconName);
    }
}