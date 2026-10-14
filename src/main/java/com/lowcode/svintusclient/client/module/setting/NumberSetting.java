package com.lowcode.svintusclient.client.module.setting;

public class NumberSetting extends Setting<Float> {

    private final float min, max, step;

    public NumberSetting(String name, float value, float min, float max, float step) {
        super(name, value);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public void set(Float v) {
        float clamped = Math.max(min, Math.min(max, v));
        value = Math.round(clamped / step) * step;
    }

    public float progress() {
        return (value - min) / (max - min);
    }

    public void setProgress(float t) {
        set(min + (max - min) * t);
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public String format(float v) {
        if (step >= 1f) return String.valueOf(Math.round(v));
        int decimals = step >= 0.1f ? 1 : 2;
        return String.format(java.util.Locale.ROOT, "%." + decimals + "f", v);
    }
}