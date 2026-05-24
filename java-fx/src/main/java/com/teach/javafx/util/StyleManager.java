package com.teach.javafx.util;

import com.teach.javafx.models.AppSettings;
import javafx.scene.Scene;

import java.util.List;
import java.util.ArrayList;

public class StyleManager {

    private static final String FONT_SIZE_STYLE_KEY = "font-size-style";

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

        // 不要清空所有样式表！只是移除之前添加的字体样式表，然后添加新的
        List<String> stylesheets = new ArrayList<>(scene.getStylesheets());
        // 移除旧的字体样式（通过 data:text/css 前缀识别）
        stylesheets.removeIf(stylesheet -> stylesheet.startsWith("data:text/css,"));
        // 添加新的字体样式
        stylesheets.add("data:text/css," + css.replace(" ", "%20").replace("\n", ""));
        // 重新设置样式表
        scene.getStylesheets().setAll(stylesheets);
    }

    public static void applyFontSizeToScene(Scene scene) {
        AppSettings settings = SettingsManager.getCurrentSettings();
        applyFontSize(scene, settings.getFontSize());
    }
}
