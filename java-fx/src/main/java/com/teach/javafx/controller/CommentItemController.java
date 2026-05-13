package com.teach.javafx.controller;

import com.teach.javafx.models.Comment;
import com.teach.javafx.models.AttachmentInfo;
import com.teach.javafx.models.User;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.util.AttachmentUtil;
import com.teach.javafx.util.FollowStateManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CommentItemController {
    @FXML
    private HBox authorBox;
    @FXML
    private ImageView avatarImageView;
    @FXML
    private Label authorLabel;
    @FXML
    private Button followButton;
    @FXML
    private Label contentLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Button likeButton;
    @FXML
    private Button replyButton;
    @FXML
    private Button reportButton;
    @FXML
    private VBox replyVBox;
    @FXML
    private FlowPane commentImagesVBox;
    @FXML
    private VBox commentAttachmentsVBox;

    private Comment comment;
    private User currentUser;
    private Long postId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Consumer<Comment> onReplyCallback;
    private Runnable onRefreshCallback;
    private boolean isReplyItem = false;
    private boolean isLiked = false;

    public void setData(Comment comment, User currentUser, Long postId, Consumer<Comment> onReplyCallback, Runnable onRefreshCallback, boolean isReplyItem) {
        this.comment = comment;
        this.currentUser = currentUser;
        this.postId = postId;
        this.onReplyCallback = onReplyCallback;
        this.onRefreshCallback = onRefreshCallback;
        this.isReplyItem = isReplyItem;
        initializeView();
    }

    private void initializeView() {
        String avatarUrl = comment.getAuthorAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            try {
                String fullAvatarUrl = avatarUrl.startsWith("/") ? 
                    HttpRequestUtil.serverUrl + avatarUrl : avatarUrl;
                Image image = new Image(fullAvatarUrl, true);
                avatarImageView.setImage(image);
            } catch (Exception e) {
                avatarImageView.setImage(null);
            }
        }
        
        // 头像点击事件
        avatarImageView.setStyle("-fx-cursor: hand;");
        avatarImageView.setOnMouseClicked(event -> {
            if (comment.getAuthorId() != null) {
                openUserHome(comment.getAuthorId().intValue(), comment.getAuthorNickname());
            }
        });

        String authorText;
        if (comment.getReplyToUserNickname() != null && !comment.getReplyToUserNickname().isBlank()) {
            authorText = (comment.getAuthorNickname() != null ? comment.getAuthorNickname() : "未知") + " 回复 " + comment.getReplyToUserNickname();
        } else {
            authorText = comment.getAuthorNickname() != null ? comment.getAuthorNickname() : "未知";
        }
        authorLabel.setText(authorText);

        contentLabel.setText(comment.getContent());

        timeLabel.setText(comment.getCreateTime() != null ? dateFormat.format(comment.getCreateTime()) : "");

        boolean isLoggedIn = currentUser != null;
        boolean isBanned = isLoggedIn && Boolean.TRUE.equals(currentUser.getIsBanned());
        boolean isCommentAuthor = isLoggedIn && comment.getAuthorId() != null && 
            currentUser.getPersonId() != null && 
            currentUser.getPersonId().longValue() == comment.getAuthorId().longValue();

        followButton.setVisible(isLoggedIn && !isBanned && !isCommentAuthor);
        if (isLoggedIn && !isBanned && !isCommentAuthor) {
            setupCommentFollowButton(followButton, comment.getAuthorId());
        }

        likeButton.setVisible(isLoggedIn && !isBanned);
        likeButton.setOnAction(e -> toggleLike());
        if (isLoggedIn && !isBanned) {
            loadLikeStatus();
        }
        updateLikeButtonText();

        replyButton.setOnAction(e -> openReplyDialog());
        reportButton.setOnAction(e -> openCommentReportDialog());
        reportButton.setVisible(isLoggedIn && !isBanned && !isCommentAuthor);

        if (isReplyItem) {
            avatarImageView.setFitWidth(20);
            avatarImageView.setFitHeight(20);
            ((VBox) authorBox.getParent()).getStyleClass().add("reply-comment-card");
        }

        displayCommentImages();
        displayCommentAttachments();

        if (!isReplyItem && comment.getReplyList() != null && !comment.getReplyList().isEmpty()) {
            replyVBox.setStyle("-fx-padding: 10 0 0 30;");
            for (Comment reply : comment.getReplyList()) {
                addReplyComment(reply);
            }
        }
    }

    private void displayCommentImages() {
        commentImagesVBox.getChildren().clear();

        String imagesStr = comment.getImageUrls();
        if (imagesStr == null || imagesStr.isBlank()) {
            return;
        }

        String[] imageUrlArray = imagesStr.split(",");
        java.util.List<String> fullUrlList = new java.util.ArrayList<>();

        for (String imageUrl : imageUrlArray) {
            String trimmedUrl = imageUrl.trim();
            if (trimmedUrl.isBlank()) {
                continue;
            }

            String fullUrl;
            if (trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")) {
                fullUrl = trimmedUrl;
            } else if (trimmedUrl.startsWith("/")) {
                fullUrl = "http://localhost:22223" + trimmedUrl;
            } else {
                fullUrl = "http://localhost:22223/" + trimmedUrl;
            }
            fullUrlList.add(fullUrl);
        }

        for (int i = 0; i < fullUrlList.size(); i++) {
            final int index = i;
            String fullUrl = fullUrlList.get(i);

            try {
                ImageView imageView = new ImageView();
                imageView.getStyleClass().add("comment-image");
                imageView.setFitWidth(isReplyItem ? 120 : 150);
                imageView.setFitHeight(isReplyItem ? 120 : 150);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setStyle("-fx-cursor: hand;");

                Image image = new Image(fullUrl, true);
                imageView.setImage(image);

                image.errorProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        imageView.setImage(null);
                    }
                });

                final java.util.List<String> finalUrlList = fullUrlList;
                imageView.setOnMouseClicked(e -> openImagePreview(finalUrlList, index));

                commentImagesVBox.getChildren().add(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openImagePreview(java.util.List<String> imageUrls, int startIndex) {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                com.teach.javafx.MainApplication.class.getResource("image-preview.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 900, 700);

            ImagePreviewController controller = fxmlLoader.getController();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("图片预览");
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            controller.setStage(stage);
            controller.setImageUrls(imageUrls, startIndex);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayCommentAttachments() {
        commentAttachmentsVBox.getChildren().clear();
        List<AttachmentInfo> attachments = AttachmentUtil.parse(comment.getAttachmentInfos());
        if (attachments.isEmpty()) {
            commentAttachmentsVBox.setVisible(false);
            commentAttachmentsVBox.setManaged(false);
            return;
        }

        commentAttachmentsVBox.setVisible(true);
        commentAttachmentsVBox.setManaged(true);

        for (AttachmentInfo attachment : attachments) {
            HBox row = new HBox(8);
            row.getStyleClass().add("attachment-row");

            String name = attachment.getName() != null ? attachment.getName() : "未命名附件";
            String size = AttachmentUtil.formatSize(attachment.getSize());
            Label nameLabel = new Label(name + (size.isBlank() ? "" : " (" + size + ")"));
            nameLabel.getStyleClass().add("attachment-name");
            nameLabel.setOnMouseClicked(event -> openAttachment(attachment));

            Button downloadButton = new Button("下载");
            downloadButton.getStyleClass().add("text-button");
            downloadButton.setOnAction(event -> openAttachment(attachment));

            row.getChildren().addAll(nameLabel, downloadButton);
            commentAttachmentsVBox.getChildren().add(row);
        }
    }

    private void openAttachment(AttachmentInfo attachment) {
        if (attachment == null || attachment.getUrl() == null || attachment.getUrl().isBlank()) {
            showError("附件地址为空");
            return;
        }
        try {
            String fullUrl = AttachmentUtil.fullUrl(attachment.getUrl());
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(fullUrl));
            } else {
                showInfo(fullUrl);
            }
        } catch (Exception e) {
            showError("打开附件失败");
        }
    }

    private void addReplyComment(Comment reply) {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                com.teach.javafx.MainApplication.class.getResource("comment-item.fxml"));
            VBox replyNode = fxmlLoader.load();
            
            CommentItemController replyController = fxmlLoader.getController();
            replyController.setData(reply, currentUser, postId, onReplyCallback, onRefreshCallback, true);
            
            replyVBox.getChildren().add(replyNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openReplyDialog() {
        if (onReplyCallback != null) {
            onReplyCallback.accept(comment);
        } else {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("回复评论");
            dialog.setHeaderText("回复 @" + (comment.getAuthorNickname() != null ? comment.getAuthorNickname() : "未知"));
            dialog.setContentText("请输入回复内容：");
            
            dialog.showAndWait().ifPresent(content -> {
                if (content.trim().isEmpty()) {
                    showError("回复内容不能为空");
                    return;
                }
                
                Task<Comment> task = new Task<Comment>() {
                    @Override
                    protected Comment call() {
                        return HttpRequestUtil.publishComment(postId, content.trim(), comment.getId());
                    }
                };
                
                task.setOnSucceeded(event -> {
                    Platform.runLater(() -> {
                        Comment result = task.getValue();
                        if (result != null) {
                            showInfo("回复成功！");
                            if (onRefreshCallback != null) {
                                onRefreshCallback.run();
                            }
                        } else {
                            showError("回复失败，请稍后重试");
                        }
                    });
                });
                
                task.setOnFailed(event -> {
                    Platform.runLater(() -> showError("回复失败，请稍后重试"));
                });
                
                new Thread(task).start();
            });
        }
    }

    private void openCommentReportDialog() {
        String content = comment.getContent() != null ? comment.getContent() : "";
        String preview = content.length() > 50 ? content.substring(0, 50) + "..." : content;
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("举报评论");
        dialog.setHeaderText("举报评论：" + preview);
        dialog.setContentText("请输入举报原因：");
        
        dialog.showAndWait().ifPresent(reason -> {
            if (reason.trim().isEmpty()) {
                showError("举报原因不能为空");
                return;
            }
            
            Task<com.teach.javafx.models.Report> task = new Task<com.teach.javafx.models.Report>() {
                @Override
                protected com.teach.javafx.models.Report call() {
                    return HttpRequestUtil.submitReport(2, comment.getId(), reason.trim());
                }
            };
            
            task.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    com.teach.javafx.models.Report result = task.getValue();
                    if (result != null) {
                        showInfo("举报成功！管理员会尽快处理。");
                    } else {
                        showError("举报失败，请稍后重试");
                    }
                });
            });
            
            task.setOnFailed(event -> {
                Platform.runLater(() -> showError("举报失败，请稍后重试"));
            });
            
            new Thread(task).start();
        });
    }

    private void setupCommentFollowButton(Button followButton, Long userId) {
        if (userId == null) {
            followButton.setVisible(false);
            return;
        }

        Boolean cachedState = FollowStateManager.getInstance().getFollowState(userId);
        
        if (cachedState != null) {
            updateCommentFollowButtonText(followButton, cachedState);
            FollowStateManager.getInstance().registerListener(userId, (followed) -> {
                Platform.runLater(() -> {
                    updateCommentFollowButtonText(followButton, followed);
                });
            });
        } else {
            Task<Map<String, Object>> checkTask = new Task<Map<String, Object>>() {
                @Override
                protected Map<String, Object> call() {
                    return HttpRequestUtil.checkFollowStatus(userId);
                }
            };

            checkTask.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    Map<String, Object> result = checkTask.getValue();
                    if (result != null) {
                        Boolean followed = (Boolean) result.get("followed");
                        FollowStateManager.getInstance().setFollowState(userId, followed != null && followed);
                        updateCommentFollowButtonText(followButton, followed != null && followed);
                        FollowStateManager.getInstance().registerListener(userId, (newFollowed) -> {
                            Platform.runLater(() -> {
                                updateCommentFollowButtonText(followButton, newFollowed);
                            });
                        });
                    }
                });
            });

            new Thread(checkTask).start();
        }

        followButton.setOnAction(e -> {
            followButton.setDisable(true);
            
            Task<Map<String, Object>> toggleTask = new Task<Map<String, Object>>() {
                @Override
                protected Map<String, Object> call() {
                    return HttpRequestUtil.toggleFollow(userId);
                }
            };

            toggleTask.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    Map<String, Object> result = toggleTask.getValue();
                    if (result != null) {
                        Boolean followed = (Boolean) result.get("followed");
                        FollowStateManager.getInstance().setFollowState(userId, followed != null && followed);
                    }
                    followButton.setDisable(false);
                });
            });

            toggleTask.setOnFailed(event -> {
                Platform.runLater(() -> {
                    showError("操作失败，请稍后重试");
                    followButton.setDisable(false);
                });
            });

            new Thread(toggleTask).start();
        });
    }

    private void updateCommentFollowButtonText(Button button, boolean isFollowed) {
        String fontSize = isReplyItem ? "10px" : "11px";
        if (isFollowed) {
            button.setText("已关注");
            button.setStyle("-fx-font-size: " + fontSize + "; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        } else {
            button.setText("关注");
            button.setStyle("-fx-font-size: " + fontSize + ";");
        }
    }

    private void toggleLike() {
        if (comment == null || comment.getId() == null) {
            return;
        }

        likeButton.setDisable(true);

        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.toggleCommentLike(comment.getId());
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> result = task.getValue();
                if (result != null) {
                    Boolean liked = (Boolean) result.get("liked");
                    Double likeCount = ((Number) result.get("likeCount")).doubleValue();

                    isLiked = liked != null && liked;
                    comment.setLikeCount(likeCount.intValue());

                    updateLikeButtonText();
                }
                likeButton.setDisable(false);
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                showError("操作失败，请稍后重试");
                likeButton.setDisable(false);
            });
        });

        new Thread(task).start();
    }

    private void loadLikeStatus() {
        if (comment == null || comment.getId() == null) {
            return;
        }

        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getCommentLikeStatus(comment.getId());
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> result = task.getValue();
                if (result != null) {
                    Boolean liked = (Boolean) result.get("liked");
                    Double likeCount = ((Number) result.get("likeCount")).doubleValue();

                    isLiked = liked != null && liked;
                    if (comment != null) {
                        comment.setLikeCount(likeCount.intValue());
                    }

                    updateLikeButtonText();
                }
            });
        });

        new Thread(task).start();
    }

    private void updateLikeButtonText() {
        if (comment != null) {
            int count = comment.getLikeCount() != null ? comment.getLikeCount() : 0;
            String fontSize = isReplyItem ? "10px" : "11px";
            if (isLiked) {
                likeButton.setText("已赞 (" + count + ")");
                likeButton.setStyle("-fx-font-size: " + fontSize + "; -fx-background-color: #4CAF50; -fx-text-fill: white;");
            } else {
                likeButton.setText("点赞 (" + count + ")");
                likeButton.setStyle("-fx-font-size: " + fontSize + ";");
            }
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void openUserHome(Integer userId, String nickname) {
        if (com.teach.javafx.AppStore.getMainFrameController() != null) {
            com.teach.javafx.AppStore.getMainFrameController().openUserHome(userId, nickname);
        }
    }
}
