package com.lowcode.svintusclient.client.module.setting;

import java.util.function.BooleanSupplier;

public abstract class Setting<T> {

    private final String name;
    protected T value;
    private BooleanSupplier visibility = () -> true;

    protected Setting(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public <S extends Setting<T>> S visibleIf(BooleanSupplier condition) {
        this.visibility = condition;
        return (S) this;
    }

    public boolean isVisible() {
        return visibility.getAsBoolean();
    }
}