package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyMessagesController extends ToolController {

    @FXML
    private VBox conversationList;
    @FXML
    private javafx.scene.control.Button refreshButton;

    private static final String DEFAULT_AVATAR_URL = "https://img.phb123.com/uploads/allimg/220607/810-22060G55A40-L.jpeg";

    @FXML
    public void initialize() {
        loadConversations();
    }
    
    @FXML
    private void onRefresh() {
        loadConversations();
    }

    private void loadConversations() {
        conversationList.getChildren().clear();
        Label loadingLabel = new Label("加载中...");
        loadingLabel.setStyle("-fx-text-fill: #999; -fx-padding: 40;");
        conversationList.getChildren().add(loadingLabel);

        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() {
                return HttpRequestUtil.getConversationList();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                conversationList.getChildren().clear();
                List<Map<String, Object>> conversations = task.getValue();
                if (conversations != null) {
                    if (conversations.isEmpty()) {
                        Label emptyLabel = new Label("您还没有任何私信");
                        emptyLabel.setStyle("-fx-text-fill: #999; -fx-padding: 40;");
                        conversationList.getChildren().add(emptyLabel);
                    } else {
                        for (Map<String, Object> conv : conversations) {
                            addConversationCard(conv);
                        }
                    }
                } else {
                    Label errorLabel = new Label("加载失败");
                    errorLabel.setStyle("-fx-text-fill: #ff4d4f; -fx-padding: 40;");
                    conversationList.getChildren().add(errorLabel);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                conversationList.getChildren().clear();
                Label errorLabel = new Label("加载失败: " + event.getSource().getException().getMessage());
                errorLabel.setStyle("-fx-text-fill: #ff4d4f; -fx-padding: 40;");
                conversationList.getChildren().add(errorLabel);
            });
        });

        new Thread(task).start();
    }

    private void addConversationCard(Map<String, Object> conv) {
        VBox card = new VBox();
        card.setSpacing(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-cursor: hand;");

        HBox topRow = new HBox();
        topRow.setSpacing(12);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Map<String, Object> otherUser = (Map<String, Object>) conv.get("otherUser");
        String nickname = otherUser != null ? String.valueOf(otherUser.getOrDefault("nickname", "未知用户")) : "未知用户";
        String avatarUrl = otherUser != null ? String.valueOf(otherUser.getOrDefault("avatarUrl", DEFAULT_AVATAR_URL)) : DEFAULT_AVATAR_URL;

        ImageView avatar = new ImageView();
        avatar.setFitWidth(50);
        avatar.setFitHeight(50);
        avatar.setPreserveRatio(true);
        avatar.setStyle("-fx-background-radius: 25;");

        try {
            String fullUrl = avatarUrl.startsWith("http") ? avatarUrl : HttpRequestUtil.serverUrl + avatarUrl;
            Image image = new Image(fullUrl, true);
            avatar.setImage(image);
        } catch (Exception e) {
            avatar.setImage(new Image(DEFAULT_AVATAR_URL));
        }

        VBox infoBox = new VBox();
        infoBox.setSpacing(4);

        Label nameLabel = new Label(nickname);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Object unreadObj = conv.get("unreadCount");
        int unreadCount = unreadObj instanceof Number ? ((Number) unreadObj).intValue() : 0;

        Map<String, Object> lastMessage = (Map<String, Object>) conv.get("lastMessage");
        String lastMsgContent = "";
        if (lastMessage != null) {
            String type = String.valueOf(lastMessage.getOrDefault("messageType", "text"));
            if ("image".equals(type)) {
                lastMsgContent = "[图片]";
            } else {
                lastMsgContent = String.valueOf(lastMessage.getOrDefault("content", ""));
            }
            if (lastMsgContent.length() > 30) {
                lastMsgContent = lastMsgContent.substring(0, 30) + "...";
            }
        }

        Label lastMsgLabel = new Label(lastMsgContent);
        lastMsgLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

        infoBox.getChildren().addAll(nameLabel, lastMsgLabel);

        VBox rightBox = new VBox();
        rightBox.setSpacing(4);
        rightBox.setAlignment(javafx.geometry.Pos.TOP_RIGHT);

        String lastTime = conv.get("lastMessageTime") != null ? formatTime(String.valueOf(conv.get("lastMessageTime"))) : "";
        Label timeLabel = new Label(lastTime);
        timeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");

        if (unreadCount > 0) {
            Label unreadBadge = new Label(String.valueOf(unreadCount));
            unreadBadge.setStyle("-fx-background-color: #ff4d4f; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 2 8; -fx-background-radius: 10;");
            rightBox.getChildren().addAll(timeLabel, unreadBadge);
        } else {
            rightBox.getChildren().add(timeLabel);
        }

        topRow.getChildren().addAll(avatar, infoBox, rightBox);
        card.getChildren().add(topRow);

        Long conversationId = conv.get("conversationId") instanceof Number ? ((Number) conv.get("conversationId")).longValue() : null;
        if (conversationId != null) {
            final Long finalConversationId = conversationId;
            card.setOnMouseClicked(event -> openChat(finalConversationId, otherUser));
        }

        conversationList.getChildren().add(card);
    }

    private String formatTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return "";
        }
        try {
            if (timeStr.contains(" ")) {
                String[] parts = timeStr.split(" ");
                if (parts.length >= 2) {
                    return parts[1].substring(0, 5);
                }
            }
            return timeStr.substring(0, Math.min(16, timeStr.length()));
        } catch (Exception e) {
            return timeStr;
        }
    }

    private void openChat(Long conversationId, Map<String, Object> otherUser) {
        if (AppStore.getMainFrameController() != null) {
            try {
                Integer otherUserId = otherUser.get("userId") instanceof Number ? ((Number) otherUser.get("userId")).intValue() : null;
                AppStore.setSelectedUserId(otherUserId);
                AppStore.setSelectedConversationId(conversationId);
                
                javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(com.teach.javafx.MainApplication.class.getResource("chat-view.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 800, 600);
                ChatViewController controller = fxmlLoader.getController();
                controller.setConversation(conversationId, otherUser);
                
                String tabName = "chat-" + conversationId;
                AppStore.getMainFrameController().changeContentWithScene(tabName, "与" + otherUser.get("nickname") + "聊天", scene, controller);
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText("打开聊天页面失败");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }
}
