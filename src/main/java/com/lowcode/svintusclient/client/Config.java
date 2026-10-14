package com.lowcode.svintusclient.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowcode.svintusclient.client.gui.Theme;
import com.lowcode.svintusclient.client.hud.HudManager;
import com.lowcode.svintusclient.client.hud.HudWidget;
import com.lowcode.svintusclient.client.module.Module;
import com.lowcode.svintusclient.client.module.ModuleManager;
import com.lowcode.svintusclient.client.module.setting.BindSetting;
import com.lowcode.svintusclient.client.module.setting.BooleanSetting;
import com.lowcode.svintusclient.client.module.setting.ModeSetting;
import com.lowcode.svintusclient.client.module.setting.NumberSetting;
import com.lowcode.svintusclient.client.module.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("efashhvhclient.json");

    private Config() {
    }

    public static void save() {
        JsonObject root = new JsonObject();
        root.addProperty("theme", Theme.current().name());

        JsonObject modules = new JsonObject();
        for (Module module : ModuleManager.all()) {
            JsonObject m = new JsonObject();
            m.addProperty("enabled", module.isEnabled());
            m.addProperty("key", module.getKey());
            m.add("settings", writeSettings(module.getSettings()));
            modules.add(module.getName(), m);
        }
        root.add("modules", modules);

        JsonObject hud = new JsonObject();
        for (HudWidget widget : HudManager.widgets()) {
            JsonObject w = new JsonObject();
            w.addProperty("x", widget.getFx());
            w.addProperty("y", widget.getFy());
            w.addProperty("scale", widget.getScale());
            w.add("options", writeSettings(widget.options()));
            hud.add(widget.id, w);
        }
        root.add("hud", hud);

        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(root));
        } catch (Exception e) {
            SvintusclientClient.LOGGER.error("Failed to save config", e);
        }
    }

    public static void load() {
        if (!Files.exists(FILE)) return;
        try {
            JsonObject root = GSON.fromJson(Files.readString(FILE), JsonObject.class);
            if (root == null) return;
            if (root.has("theme")) {
                try {
                    Theme.setInstant(Theme.Accent.valueOf(root.get("theme").getAsString()));
                } catch (IllegalArgumentException ignored) {
                }
            }
            JsonObject modules = root.getAsJsonObject("modules");
            if (modules != null) {
                for (Module module : ModuleManager.all()) {
                    JsonObject m = modules.getAsJsonObject(module.getName());
                    if (m == null) continue;
                    if (m.has("enabled")) module.restoreEnabled(m.get("enabled").getAsBoolean());
                    if (m.has("key")) module.setKey(m.get("key").getAsInt());
                    readSettings(module.getSettings(), m.getAsJsonObject("settings"));
                }
            }
            JsonObject hud = root.getAsJsonObject("hud");
            if (hud != null) {
                for (HudWidget widget : HudManager.widgets()) {
                    JsonObject w = hud.getAsJsonObject(widget.id);
                    if (w == null) continue;
                    widget.setPosition(w.get("x").getAsFloat(), w.get("y").getAsFloat());
                    if (w.has("scale")) widget.setScale(w.get("scale").getAsFloat());
                    readSettings(widget.options(), w.getAsJsonObject("options"));
                }
            }
        } catch (Exception e) {
            SvintusclientClient.LOGGER.error("Failed to load config", e);
        }
    }

    private static JsonObject writeSettings(List<? extends Setting<?>> settings) {
        JsonObject out = new JsonObject();
        for (Setting<?> setting : settings) {
            Object value = setting.get();
            if (value instanceof Boolean b) out.addProperty(setting.getName(), b);
            else if (value instanceof Number n) out.addProperty(setting.getName(), n);
            else if (value != null) out.addProperty(setting.getName(), value.toString());
        }
        return out;
    }

    private static void readSettings(List<? extends Setting<?>> settings, JsonObject in) {
        if (in == null) return;
        for (Setting<?> setting : settings) {
            JsonElement e = in.get(setting.getName());
            if (e == null) continue;
            try {
                switch (setting) {
                    case BooleanSetting b -> b.set(e.getAsBoolean());
                    case NumberSetting n -> n.set(e.getAsFloat());
                    case ModeSetting m -> {
                        if (m.getModes().contains(e.getAsString())) m.set(e.getAsString());
                    }
                    case BindSetting k -> k.set(e.getAsInt());
                    default -> {
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }
}