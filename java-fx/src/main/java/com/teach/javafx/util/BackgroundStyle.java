package com.teach.javafx.util;

public final class BackgroundStyle {
    public static final String BACKGROUND_STYLE_CLASS = "app-background";

    private BackgroundStyle() {
    }

    public static String appBackground() {
        return "-fx-background-color: #eef2f7;";
    }
}
