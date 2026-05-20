package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.ToolController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class PersonalCenterController extends ToolController {

    @FXML
    private StackPane contentStackPane;

    @FXML
    public void initialize() {
        // 注册控制器到 AppStore
        AppStore.setPersonalCenterController(this);
        // 默认加载个人资料页面
        loadPage("personal-profile.fxml");
    }

    /**
     * Programmatic navigation by page name, used by child controllers
     */
    public void navigateByPage(String pageName, String title) {
        // 直接使用 pageName 作为 FXML 文件名
        String fxmlFile = pageName + ".fxml";
        loadPage(fxmlFile);
    }

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(fxmlFile));
            Node content = fxmlLoader.load();
            contentStackPane.getChildren().clear();

            if (content instanceof ScrollPane scrollPane) {
                configureScrollPane(scrollPane);
                contentStackPane.getChildren().add(scrollPane);
            } else {
                ScrollPane scrollPane = new ScrollPane(content);
                configureScrollPane(scrollPane);
                scrollPane.getStyleClass().add("profile-scroll-pane");
                contentStackPane.getChildren().add(scrollPane);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configureScrollPane(ScrollPane scrollPane) {
        scrollPane.setMinSize(0, 0);
        scrollPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    public void onClose() {
        // 清理 AppStore 中的引用，防止使用过期的控制器
        if (AppStore.getPersonalCenterController() == this) {
            AppStore.setPersonalCenterController(null);
        }
    }
    
    /**
     * 当个人中心页面被重新加载时调用，重新注册控制器
     */
    public void onReopen() {
        AppStore.setPersonalCenterController(this);
    }
}
