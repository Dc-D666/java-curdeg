package com.teach.javafx.controller;

import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.ToolController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class PersonalCenterController extends ToolController {

    @FXML
    private StackPane contentStackPane;

    @FXML
    public void initialize() {
        loadPage("personal-profile.fxml");
    }

    @FXML
    private void onProfile() {
        loadPage("personal-profile.fxml");
    }

    @FXML
    private void onMyPosts() {
        loadPage("my-posts.fxml");
    }

    @FXML
    private void onMyFavorites() {
        loadPage("my-favorites.fxml");
    }

    @FXML
    private void onMyFollowing() {
        loadPage("my-followers.fxml");
    }

    @FXML
    private void onMyFollowers() {
        loadPage("my-followers.fxml");
    }

    @FXML
    private void onStatistics() {
        loadPage("user-statistics.fxml");
    }

    @FXML
    private void onPasswordChange() {
        loadPage("password-change.fxml");
    }

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(fxmlFile));
            contentStackPane.getChildren().clear();
            contentStackPane.getChildren().add(fxmlLoader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}