package com.teach.javafx;

import com.teach.javafx.models.AppSettings;
import com.teach.javafx.util.BackgroundStyle;
import com.teach.javafx.util.SettingsManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    private static Stage mainStage;
    private static double stageWidth = -1;
    private static double stageHeight = -1;

    private static boolean canClose = true;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("base/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 800);
        applyTheme(scene);
        stage.setTitle("学生交流社区");
        stage.setScene(scene);
        stage.setWidth(600);
        stage.setHeight(800);
        stage.centerOnScreen();
        stage.show();
        stage.setOnCloseRequest(event -> {
            if (canClose) {
                closeHttpRequestUtil();
            } else {
                event.consume();
            }
        });
        mainStage = stage;
    }

    private static void applyTheme(Scene scene) {
        Parent root = scene.getRoot();
        if (root.getStyleClass().contains(BackgroundStyle.BACKGROUND_STYLE_CLASS)) {
            if (root.getStyle() == null || !root.getStyle().contains("-fx-background-image")) {
                root.setStyle(BackgroundStyle.appBackground());
            }
            return;
        }

        AppSettings settings = SettingsManager.getCurrentSettings();
        String theme = settings.getTheme();
        
        if ("深色主题".equals(theme)) {
            root.setStyle("-fx-base: #1e1e1e; -fx-background: #1e1e1e; -fx-control-inner-background: #2d2d2d; -fx-text-fill: #d4d4d4;");
        } else {
            root.setStyle("");
        }
    }

    public static void applyCurrentTheme() {
        if (mainStage != null && mainStage.getScene() != null) {
            applyTheme(mainStage.getScene());
        }
    }

    public static void resetStage(String name, Scene scene) {
        resetStage(name, scene, true);
    }

    public static void resetStage(String name, Scene scene, boolean maximize) {
        if (stageWidth > 0) {
            mainStage.setWidth(stageWidth);
            mainStage.setHeight(stageHeight);
            mainStage.setX(0);
            mainStage.setY(0);
        }
        mainStage.setTitle(name);
        applyTheme(scene);
        mainStage.setScene(scene);
        if (maximize) {
            mainStage.setMaximized(true);
        } else {
            mainStage.setWidth(600);
            mainStage.setHeight(800);
            mainStage.centerOnScreen();
        }
        mainStage.show();
    }

    public static void loginStage(String name, Scene scene) {
        stageWidth = mainStage.getWidth();
        stageHeight = mainStage.getHeight();
        mainStage.setTitle(name);
        applyTheme(scene);
        mainStage.setScene(scene);
        mainStage.setWidth(600);
        mainStage.setHeight(800);
        mainStage.centerOnScreen();
        mainStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static Scene getMainScene() {
        if (mainStage != null) {
            return mainStage.getScene();
        }
        return null;
    }

    public static void setCanClose(boolean canClose) {
        MainApplication.canClose = canClose;
    }

    private static void closeHttpRequestUtil() {
        try {
            Class<?> requestUtilClass = Class.forName("com.teach.javafx.request.HttpRequestUtil");
            requestUtilClass.getMethod("close").invoke(null);
        } catch (ReflectiveOperationException e) {
            System.err.println("HttpRequestUtil close skipped: " + e.getMessage());
        }
    }
}
