package com.teach.javafx.util;

import javafx.scene.control.Label;

/**
 * 昵称样式工具类
 * 根据等级特权中的 nicknameStyle 值统一应用昵称样式
 */
public class NicknameStyleUtil {

    /**
     * 获取昵称样式的CSS字符串
     */
    public static String getStyle(String nicknameStyle) {
        if (nicknameStyle == null || "normal".equals(nicknameStyle)) {
            return "";
        } else if ("bold".equals(nicknameStyle)) {
            return "-fx-font-weight: bold;";
        } else if ("bold_red".equals(nicknameStyle)) {
            return "-fx-font-weight: bold; -fx-text-fill: #e53935;";
        }
        return "";
    }

    /**
     * 将昵称样式应用到Label上（保留原有样式）
     */
    public static void applyStyle(Label label, String nicknameStyle) {
        if (label == null) return;
        String style = getStyle(nicknameStyle);
        if (style.isEmpty()) {
            label.setStyle("");
        } else {
            label.setStyle(style);
        }
    }

    /**
     * 将昵称样式合并到现有样式中
     */
    public static String mergeStyle(String existingStyle, String nicknameStyle) {
        String nicknameCss = getStyle(nicknameStyle);
        if (nicknameCss.isEmpty()) {
            return existingStyle != null ? existingStyle : "";
        }
        if (existingStyle == null || existingStyle.isEmpty()) {
            return nicknameCss;
        }
        return existingStyle + ";" + nicknameCss;
    }
}
