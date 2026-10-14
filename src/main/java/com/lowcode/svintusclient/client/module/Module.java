package com.lowcode.svintusclient.client.module;

import com.lowcode.svintusclient.client.hud.Notifications;
import com.lowcode.svintusclient.client.module.setting.Setting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Module {

    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean toggleable = true;
    private boolean enabled;
    private int key = GLFW.GLFW_KEY_UNKNOWN;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (!toggleable || this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
        Notifications.onToggle(this);
    }

    public void restoreEnabled(boolean enabled) {
        if (toggleable) this.enabled = enabled;
    }

    protected void enableByDefault() {
        this.enabled = true;
    }

    protected void setToggleable(boolean toggleable) {
        this.toggleable = toggleable;
    }

    protected <T extends Setting<?>> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    @SafeVarargs
    protected final void add(Setting<?>... settings) {
        this.settings.addAll(Arrays.asList(settings));
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    public boolean isToggleable() {
        return toggleable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}