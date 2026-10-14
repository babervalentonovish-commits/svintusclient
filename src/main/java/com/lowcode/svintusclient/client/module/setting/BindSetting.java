package com.lowcode.svintusclient.client.module.setting;

import org.lwjgl.glfw.GLFW;

public class BindSetting extends Setting<Integer> {

    public BindSetting(String name) {
        super(name, GLFW.GLFW_KEY_UNKNOWN);
    }
}