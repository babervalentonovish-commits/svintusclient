package com.lowcode.svintusclient.client.module.setting;

import java.util.List;

public class ModeSetting extends Setting<String> {

    private final List<String> modes;

    public ModeSetting(String name, String... modes) {
        super(name, modes[0]);
        this.modes = List.of(modes);
    }

    public List<String> getModes() {
        return modes;
    }

    public boolean is(String mode) {
        return value.equals(mode);
    }
}