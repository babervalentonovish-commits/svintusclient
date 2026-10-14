package com.lowcode.svintusclient.client.module.client;

import com.lowcode.svintusclient.client.module.Category;
import com.lowcode.svintusclient.client.module.Module;
import com.lowcode.svintusclient.client.module.setting.BooleanSetting;
import com.lowcode.svintusclient.client.module.setting.NumberSetting;

public class HudModule extends Module {

    public final BooleanSetting blur = add(new BooleanSetting("Размытие", true));
    public final NumberSetting glass = add(new NumberSetting("Плотность стекла", 0.62f, 0.45f, 0.95f, 0.05f));
    public final BooleanSetting watermark = add(new BooleanSetting("Ватермарка", true));
    public final BooleanSetting moduleList = add(new BooleanSetting("Список модулей", true));
    public final BooleanSetting coordinates = add(new BooleanSetting("Координаты", true));
    public final BooleanSetting targetHud = add(new BooleanSetting("Таргет HUD", true));
    public final BooleanSetting armorHud = add(new BooleanSetting("Броня", true));

    public HudModule() {
        super("HUD", "Элементы на экране. Открой чат, чтобы двигать", Category.CLIENT);
        enableByDefault();
    }
}