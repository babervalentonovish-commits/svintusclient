package com.lowcode.svintusclient.client.util;

public final class Anim {

    private final float speed;
    private float value;
    private float target;
    private long last = System.nanoTime();

    public Anim(float value, float speed) {
        this.value = value;
        this.target = value;
        this.speed = speed;
    }

    public Anim(float speed) {
        this(0f, speed);
    }

    public float get() {
        long now = System.nanoTime();
        float dt = Math.min((now - last) / 1_000_000_000f, 0.1f);
        last = now;
        if (value != target) {
            value += (target - value) * (1f - (float) Math.exp(-speed * dt));
            if (Math.abs(target - value) < 0.0005f) value = target;
        }
        return value;
    }

    public Anim to(float target) {
        get();
        this.target = target;
        return this;
    }

    public Anim to(boolean on) {
        return to(on ? 1f : 0f);
    }

    public Anim snap(float v) {
        value = v;
        target = v;
        last = System.nanoTime();
        return this;
    }

    public float target() {
        return target;
    }

    public static float easeOutCubic(float t) {
        t = Math.max(0f, Math.min(1f, t));
        float f = 1f - t;
        return 1f - f * f * f;
    }
}