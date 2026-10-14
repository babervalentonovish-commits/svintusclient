package com.lowcode.svintusclient.client.render.font;

public final class Fonts {

    private static final String DIR = "/assets/svintusclient/fonts/";

    public static final FontRenderer REGULAR = new FontRenderer("svintus_regular",
            DIR + "inter-latin-400.ttf", DIR + "inter-cyrillic-400.ttf");

    public static final FontRenderer SEMIBOLD = new FontRenderer("svintus_semibold",
            DIR + "inter-latin-600.ttf", DIR + "inter-cyrillic-600.ttf");

    private Fonts() {
    }
}