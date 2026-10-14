package com.lowcode.svintusclient.client.module.client;

import com.lowcode.svintusclient.client.module.Category;
import com.lowcode.svintusclient.client.module.Module;
import com.lowcode.svintusclient.client.module.setting.BooleanSetting;
import com.lowcode.svintusclient.client.module.setting.NumberSetting;

public class ClickGuiModule extends Module {

    public final BooleanSetting blur = add(new BooleanSetting("Размытие окна", true));
    public final NumberSetting glass = add(new NumberSetting("Плотность стекла", 0.72f, 0.5f, 0.95f, 0.05f));
    public final NumberSetting dim = add(new NumberSetting("Затемнение фона", 0.3f, 0f, 0.8f, 0.05f));
    public final NumberSetting scale = add(new NumberSetting("Масштаб", 1f, 0.75f, 1.35f, 0.05f));

    public ClickGuiModule() {
        super("Click GUI", "Внешний вид этого меню", Category.CLIENT);
        setToggleable(false);
    }
}