package com.lowcode.svintusclient.client;

import com.lowcode.svintusclient.client.gui.ClickGuiScreen;
import com.lowcode.svintusclient.client.hud.HudManager;
import com.lowcode.svintusclient.client.module.Module;
import com.lowcode.svintusclient.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class SvintusclientClient implements ClientModInitializer {

    public static final String MOD_ID = "svintusclient";
    public static final Logger LOGGER = LoggerFactory.getLogger("EFASHHVH CLIENT");

    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
    private static final Set<Integer> HELD_KEYS = new HashSet<>();
    private static KeyBinding clickGuiKey;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        HudManager.register();
        Config.load();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> Config.save());

        clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.svintusclient.clickgui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (clickGuiKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new ClickGuiScreen());
            }
            handleModuleBinds(client);
        });
    }

    private static void handleModuleBinds(MinecraftClient client) {
        for (Module module : ModuleManager.all()) {
            int key = module.getKey();
            if (key == GLFW.GLFW_KEY_UNKNOWN) continue;
            boolean down = InputUtil.isKeyPressed(client.getWindow(), key);
            if (down && HELD_KEYS.add(key)) {
                if (client.currentScreen == null) module.toggle();
            } else if (!down) {
                HELD_KEYS.remove(key);
            }
        }
    }

    public static int clickGuiKey() {
        return KeyBindingHelper.getBoundKeyOf(clickGuiKey).getCode();
    }
}