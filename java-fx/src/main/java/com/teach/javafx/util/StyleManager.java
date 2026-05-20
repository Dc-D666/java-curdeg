package com.teach.javafx.util;

import com.teach.javafx.models.AppSettings;
import javafx.scene.Scene;

public class StyleManager {

    public static void applyFontSize(Scene scene, String fontSize) {
        double fontSizeValue;
        switch (fontSize) {
            case "小":
                fontSizeValue = 12;
                break;
            case "大":
                fontSizeValue = 16;
                break;
            case "中（默认）":
            default:
                fontSizeValue = 14;
                break;
        }

        String css = String.format(
            ".root { -fx-font-size: %.0fpx; }",
            fontSizeValue
        );

        scene.getStylesheets().clear();
        scene.getStylesheets().add("data:text/css," + css.replace(" ", "%20").replace("\n", ""));
    }

    public static void applyFontSizeToScene(Scene scene) {
        AppSettings settings = SettingsManager.getCurrentSettings();
        applyFontSize(scene, settings.getFontSize());
    }
}
