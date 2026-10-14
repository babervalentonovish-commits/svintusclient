package com.lowcode.svintusclient.client.module.client;

import com.lowcode.svintusclient.client.module.Category;
import com.lowcode.svintusclient.client.module.Module;
import com.lowcode.svintusclient.client.module.setting.NumberSetting;

public class NotificationsModule extends Module {

    public final NumberSetting duration = add(new NumberSetting("Время показа", 2.5f, 1f, 6f, 0.5f));

    public NotificationsModule() {
        super("Notifications", "Уведомления о включении модулей", Category.CLIENT);
        enableByDefault();
    }
}