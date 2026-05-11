package com.teach.javafx.controller;

import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.ToolController;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PersonalCenterController extends ToolController {

    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    @FXML
    private StackPane contentStackPane;

    @FXML
    private Button profileButton;

    @FXML
    private Button myPostsButton;

    @FXML
    private Button myFavoritesButton;

    @FXML
    private Button myFollowingButton;

    @FXML
    private Button myFollowersButton;

    @FXML
    private Button statisticsButton;

    @FXML
    private Button changePasswordButton;

    private final List<Button> navButtons = new ArrayList<>();

    @FXML
    public void initialize() {
        navButtons.add(profileButton);
        navButtons.add(myPostsButton);
        navButtons.add(myFavoritesButton);
        navButtons.add(myFollowingButton);
        navButtons.add(myFollowersButton);
        navButtons.add(statisticsButton);
        navButtons.add(changePasswordButton);

        loadPage("personal-profile.fxml", profileButton);
    }

    @FXML
    private void onProfile() {
        loadPage("personal-profile.fxml", profileButton);
    }

    @FXML
    private void onMyPosts() {
        loadPage("my-posts.fxml", myPostsButton);
    }

    @FXML
    private void onMyFavorites() {
        loadPage("my-favorites.fxml", myFavoritesButton);
    }

    @FXML
    private void onMyFollowing() {
        loadPage("my-followers.fxml", myFollowingButton);
    }

    @FXML
    private void onMyFollowers() {
        loadPage("my-followers.fxml", myFollowersButton);
    }

    @FXML
    private void onStatistics() {
        loadPage("user-statistics.fxml", statisticsButton);
    }

    @FXML
    private void onPasswordChange() {
        loadPage("password-change.fxml", changePasswordButton);
    }

    private void loadPage(String fxmlFile, Button selectedButton) {
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

            for (Button btn : navButtons) {
                btn.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
            }
            selectedButton.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
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
}
