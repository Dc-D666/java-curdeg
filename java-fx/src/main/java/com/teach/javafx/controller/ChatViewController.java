package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ChatViewController extends ToolController {

    private static final String DEFAULT_AVATAR_URL = "https://img.phb123.com/uploads/allimg/220607/810-22060G55A40-L.jpeg";

    @FXML
    private BorderPane rootPane;

    @FXML
    private ImageView avatarImageView;

    @FXML
    private Label nicknameLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private ScrollPane messageScrollPane;

    @FXML
    private VBox messageList;

    @FXML
    private TextArea messageTextArea;

    @FXML
    private Button sendButton;

    @FXML
    private Button imageButton;

    @FXML
    private HBox warningBox;

    @FXML
    private Label warningLabel;

    private Long conversationId;
    private Integer otherUserId;
    private String otherNickname;
    private String otherAvatarUrl;
    private boolean isMutualFollow = false;
    private String selectedImagePath;

    @FXML
    public void initialize() {
        messageList.heightProperty().addListener((observable, oldValue, newValue) -> {
            messageScrollPane.setVvalue(1.0);
        });
    }

    public void setConversation(Long conversationId, Map<String, Object> otherUser) {
        this.conversationId = conversationId;
        if (otherUser != null) {
            this.otherUserId = otherUser.get("userId") instanceof Number ? ((Number) otherUser.get("userId")).intValue() : null;
            this.otherNickname = String.valueOf(otherUser.getOrDefault("nickname", "未知用户"));
            this.otherAvatarUrl = String.valueOf(otherUser.getOrDefault("avatarUrl", DEFAULT_AVATAR_URL));

            nicknameLabel.setText(this.otherNickname);

            try {
                String fullUrl = this.otherAvatarUrl.startsWith("http") ? this.otherAvatarUrl : HttpRequestUtil.serverUrl + this.otherAvatarUrl;
                Image image = new Image(fullUrl, true);
                avatarImageView.setImage(image);
            } catch (Exception e) {
                avatarImageView.setImage(new Image(DEFAULT_AVATAR_URL));
            }
            
            avatarImageView.setStyle("-fx-cursor: hand;");
            avatarImageView.setOnMouseClicked(event -> openUserProfile(otherUserId, otherNickname));
        }

        loadMessages();
        markAsRead();
    }
    
    private void openUserProfile(Integer targetUserId, String targetNickname) {
        if (targetUserId != null && AppStore.getMainFrameController() != null) {
            try {
                AppStore.setSelectedUserId(targetUserId);
                javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(com.teach.javafx.MainApplication.class.getResource("user-home.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load());
                UserHomeController controller = fxmlLoader.getController();
                controller.setUserId(targetUserId);
                
                String tabName = "user-home-" + targetUserId;
                String displayName = targetNickname != null ? targetNickname : "用户";
                AppStore.getMainFrameController().changeContentWithScene(tabName, displayName + "的主页", scene, controller);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMessages() {
        messageList.getChildren().clear();

        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getMessageHistory(conversationId);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> data = task.getValue();
                if (data != null) {
                    isMutualFollow = Boolean.TRUE.equals(data.get("isMutualFollow"));
                    updateMutualFollowStatus();

                    Object messagesObj = data.get("messages");
                    if (messagesObj instanceof List) {
                        List<?> messages = (List<?>) messagesObj;
                        for (Object msgObj : messages) {
                            if (msgObj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> message = (Map<String, Object>) msgObj;
                                addMessageBubble(message);
                            }
                        }
                    }
                } else {
                    showError("加载消息失败");
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("加载消息失败: " + event.getSource().getException().getMessage()));
        });

        new Thread(task).start();
    }

    private void updateMutualFollowStatus() {
        if (isMutualFollow) {
            statusLabel.setText("互相关注");
            statusLabel.setStyle("-fx-text-fill: #52c41a; -fx-font-size: 12px;");
            warningBox.setVisible(false);
        } else {
            statusLabel.setText("单向关注");
            statusLabel.setStyle("-fx-text-fill: #faad14; -fx-font-size: 12px;");
            warningBox.setVisible(true);
            warningLabel.setText("你们还没有互关，你只能发送一条消息");
        }
    }

    private void addMessageBubble(Map<String, Object> message) {
        boolean isOwn = Boolean.TRUE.equals(message.get("isOwnMessage"));
        String type = String.valueOf(message.getOrDefault("messageType", "text"));
        String content = String.valueOf(message.getOrDefault("content", ""));
        String time = formatTime(String.valueOf(message.getOrDefault("createTime", "")));

        HBox bubbleBox = new HBox();
        bubbleBox.setSpacing(8);
        bubbleBox.setAlignment(isOwn ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        if (!isOwn) {
            ImageView smallAvatar = new ImageView();
            smallAvatar.setFitWidth(40);
            smallAvatar.setFitHeight(40);
            smallAvatar.setPreserveRatio(true);
            smallAvatar.setStyle("-fx-cursor: hand;");
            smallAvatar.setOnMouseClicked(event -> openUserProfile(otherUserId, otherNickname));
            try {
                String fullUrl = otherAvatarUrl.startsWith("http") ? otherAvatarUrl : HttpRequestUtil.serverUrl + otherAvatarUrl;
                smallAvatar.setImage(new Image(fullUrl));
            } catch (Exception e) {
                smallAvatar.setImage(new Image(DEFAULT_AVATAR_URL));
            }
            bubbleBox.getChildren().add(smallAvatar);
        }

        VBox messageBox = new VBox();
        messageBox.setSpacing(4);

        Label contentLabel = new Label();
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(400);

        if ("image".equals(type)) {
            ImageView imgView = new ImageView();
            imgView.setFitWidth(200);
            imgView.setPreserveRatio(true);
            try {
                String imgUrl = content.startsWith("http") ? content : HttpRequestUtil.serverUrl + content;
                imgView.setImage(new Image(imgUrl, true));
                contentLabel.setGraphic(imgView);
                contentLabel.setText("");
            } catch (Exception e) {
                contentLabel.setText("[图片加载失败]");
            }
        } else {
            contentLabel.setText(content);
        }

        if (isOwn) {
            contentLabel.setStyle("-fx-background-color: #95ec69; -fx-text-fill: #333; -fx-padding: 10 14; -fx-background-radius: 8; -fx-font-size: 14px;");
        } else {
            contentLabel.setStyle("-fx-background-color: white; -fx-text-fill: #333; -fx-padding: 10 14; -fx-background-radius: 8; -fx-font-size: 14px; -fx-border-color: #e8e8e8; -fx-border-radius: 8;");
        }

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 10px;");

        if (isOwn) {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
        } else {
            messageBox.setAlignment(Pos.CENTER_LEFT);
        }

        messageBox.getChildren().add(contentLabel);
        messageBox.getChildren().add(timeLabel);
        bubbleBox.getChildren().add(messageBox);

        if (isOwn) {
            ImageView ownAvatar = new ImageView();
            ownAvatar.setFitWidth(40);
            ownAvatar.setFitHeight(40);
            ownAvatar.setPreserveRatio(true);
            ownAvatar.setStyle("-fx-cursor: hand;");
            
            Integer currentUserId = AppStore.getCurrentUser() != null ? AppStore.getCurrentUser().getPersonId() : null;
            String currentUserNickname = AppStore.getCurrentUser() != null ? AppStore.getCurrentUser().getNickname() : "我";
            String currentUserAvatar = AppStore.getCurrentUser() != null ? AppStore.getCurrentUser().getAvatarUrl() : null;
            
            ownAvatar.setOnMouseClicked(event -> {
                if (currentUserId != null) {
                    openUserProfile(currentUserId, currentUserNickname);
                }
            });
            
            try {
                if (currentUserAvatar != null && !currentUserAvatar.isEmpty()) {
                    String fullUrl = currentUserAvatar.startsWith("http") ? currentUserAvatar : HttpRequestUtil.serverUrl + currentUserAvatar;
                    ownAvatar.setImage(new Image(fullUrl));
                } else {
                    ownAvatar.setImage(new Image(DEFAULT_AVATAR_URL));
                }
            } catch (Exception e) {
                ownAvatar.setImage(new Image(DEFAULT_AVATAR_URL));
            }
            bubbleBox.getChildren().add(ownAvatar);
        }

        messageList.getChildren().add(bubbleBox);
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

    private void markAsRead() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.markMessagesAsRead(conversationId);
            }
        };
        new Thread(task).start();
    }



    @FXML
    private void onSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择图片");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(imageButton.getScene().getWindow());
        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            messageTextArea.appendText("[已选择图片: " + file.getName() + "]\n");
        }
    }

    @FXML
    private void onSendMessage() {
        String text = messageTextArea.getText();
        if (text == null || text.trim().isEmpty()) {
            if (selectedImagePath == null) {
                showError("请输入消息内容");
                return;
            }
        }

        // 如果有选择图片，先上传
        String finalContent = text != null ? text.trim() : null;
        String finalMessageType = "text";
        
        if (selectedImagePath != null) {
            imageButton.setDisable(true);
            sendButton.setDisable(true);
            
            Task<String> uploadTask = new Task<>() {
                @Override
                protected String call() {
                    return HttpRequestUtil.uploadImage(selectedImagePath);
                }
            };
            
            uploadTask.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    imageButton.setDisable(false);
                    String uploadedUrl = uploadTask.getValue();
                    if (uploadedUrl != null) {
                        sendMessageWithContent(uploadedUrl, "image");
                    } else {
                        sendButton.setDisable(false);
                        showError("图片上传失败");
                    }
                });
            });
            
            uploadTask.setOnFailed(event -> {
                Platform.runLater(() -> {
                    imageButton.setDisable(false);
                    sendButton.setDisable(false);
                    showError("图片上传失败");
                });
            });
            
            new Thread(uploadTask).start();
        } else {
            sendMessageWithContent(finalContent, finalMessageType);
        }
    }
    
    private void sendMessageWithContent(String content, String messageType) {
        sendButton.setDisable(true);

        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.sendMessage(conversationId, messageType, content);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                sendButton.setDisable(false);
                Map<String, Object> data = task.getValue();
                if (data != null) {
                    Boolean canSend = (Boolean) data.getOrDefault("canSend", true);
                    if (canSend) {
                        messageTextArea.clear();
                        selectedImagePath = null;
                        loadMessages();
                    } else {
                        String message = String.valueOf(data.getOrDefault("message", "发送失败"));
                        showError(message);
                    }
                } else {
                    showError("发送失败");
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                sendButton.setDisable(false);
                showError("发送失败: " + event.getSource().getException().getMessage());
            });
        });

        new Thread(task).start();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(message);
        alert.showAndWait();
    }
}
