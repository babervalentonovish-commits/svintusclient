package com.lowcode.svintusclient.client.module;

import com.lowcode.svintusclient.client.module.client.ClickGuiModule;
import com.lowcode.svintusclient.client.module.client.HudModule;
import com.lowcode.svintusclient.client.module.client.NotificationsModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {

    private static final List<Module> MODULES = new ArrayList<>();

    public static final ClickGuiModule CLICK_GUI = register(new ClickGuiModule());
    public static final HudModule HUD = register(new HudModule());
    public static final NotificationsModule NOTIFICATIONS = register(new NotificationsModule());

    private ModuleManager() {
    }

    public static void init() {
    }

    public static <T extends Module> T register(T module) {
        MODULES.add(module);
        return module;
    }

    public static List<Module> all() {
        return Collections.unmodifiableList(MODULES);
    }

    public static List<Module> byCategory(Category category) {
        return MODULES.stream().filter(m -> m.getCategory() == category).toList();
    }
}