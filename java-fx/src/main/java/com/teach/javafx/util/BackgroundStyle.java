package com.teach.javafx.util;

public final class BackgroundStyle {
    public static final String BACKGROUND_STYLE_CLASS = "app-background";

    private static final String APP_BACKGROUND_IMAGE = "/shanda1.jpg";

    private BackgroundStyle() {
    }

    public static String appBackground() {
        return "-fx-background-image: url('" + APP_BACKGROUND_IMAGE + "'); "
                + "-fx-background-repeat: no-repeat; "
                + "-fx-background-size: cover; "
                + "-fx-background-position: center center;";
    }
}
