package com.teach.javafx.util;

import com.teach.javafx.request.HttpRequestUtil;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LevelIconUtil {

    public static Image loadLevelIcon(String iconPath) {
        if (iconPath == null || iconPath.isEmpty()) {
            return null;
        }
        try {
            String fullUrl = iconPath.startsWith("/") ? HttpRequestUtil.serverUrl + iconPath : iconPath;
            return new Image(fullUrl, true);
        } catch (Exception e) {
            return null;
        }
    }

    public static void setLevelIcon(ImageView imageView, String iconPath) {
        if (imageView == null) {
            return;
        }
        Image image = loadLevelIcon(iconPath);
        if (image != null) {
            imageView.setImage(image);
        } else {
            imageView.setImage(null);
        }
    }

    public static void setLevelIcon(ImageView imageView, String iconPath, double fitWidth, double fitHeight) {
        if (imageView != null) {
            imageView.setFitWidth(fitWidth);
            imageView.setFitHeight(fitHeight);
            imageView.setPreserveRatio(true);
        }
        setLevelIcon(imageView, iconPath);
    }
}
