package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.Comment;
import com.teach.javafx.models.Post;
import com.teach.javafx.models.User;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class PostDetailController extends ToolController {
    @FXML
    private Label titleLabel;
    @FXML
    private ImageView authorImageView;
    @FXML
    private Label authorLabel;
    @FXML
    private Label createTimeLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private TextArea contentTextArea;
    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private VBox contentVBox;
    @FXML
    private VBox commentVBox;
    @FXML
    private TextArea commentTextArea;
    @FXML
    private Button submitCommentButton;
    @FXML
    private Button likeButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button topButton;
    @FXML
    private Button featureButton;
    @FXML
    private Button reportButton;

    private Long postId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private User currentUser;
    private Post currentPost;
    private boolean isLiked = false;

    @FXML
    public void initialize() {
        submitCommentButton.setOnAction(event -> publishComment());
        likeButton.setOnAction(event -> toggleLike());
        editButton.setOnAction(event -> openEditDialog());
        deleteButton.setOnAction(event -> deletePost());
        topButton.setOnAction(event -> toggleTop());
        featureButton.setOnAction(event -> toggleFeature());
        reportButton.setOnAction(event -> openReportDialog());
        
        editButton.setVisible(false);
        deleteButton.setVisible(false);
        topButton.setVisible(false);
        featureButton.setVisible(false);
        submitCommentButton.setVisible(false);
        reportButton.setVisible(false);
        
        mainScrollPane.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
            Object target = event.getTarget();
            if (target == mainScrollPane || target == contentVBox || target == commentVBox) {
                event.consume();
            }
        });
        
        contentVBox.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getTarget() == contentVBox) {
                event.consume();
            }
        });
        
        commentVBox.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getTarget() == commentVBox) {
                event.consume();
            }
        });
    }

    public void setPostId(Long postId) {
        this.postId = postId;
        loadCurrentUser();
    }
    
    private void loadCurrentUser() {
        Task<User> task = new Task<User>() {
            @Override
            protected User call() {
                return HttpRequestUtil.getCurrentUser();
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                currentUser = task.getValue();
                loadPostDetail();
                loadCommentList();
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                loadPostDetail();
                loadCommentList();
            });
        });
        
        new Thread(task).start();
    }

    private void loadPostDetail() {
        Task<Post> task = new Task<Post>() {
            @Override
            protected Post call() {
                return HttpRequestUtil.getPostDetail(postId);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                currentPost = task.getValue();
                if (currentPost != null) {
                    titleLabel.setText(currentPost.getTitle());
                    String avatarUrl = currentPost.getAuthorAvatarUrl();
                    if (avatarUrl != null && !avatarUrl.isBlank()) {
                        try {
                            Image image = new Image(avatarUrl, true);
                            authorImageView.setImage(image);
                        } catch (Exception e) {
                            authorImageView.setImage(null);
                        }
                    } else {
                        authorImageView.setImage(null);
                    }
                    authorLabel.setText("作者：" + (currentPost.getAuthorNickname() != null ? currentPost.getAuthorNickname() : "未知"));
                    if (currentPost.getCreateTime() != null) {
                        createTimeLabel.setText("发布时间：" + dateFormat.format(currentPost.getCreateTime()));
                    }
                    statusLabel.setText(currentPost.getStatusText());
                    contentTextArea.setText(currentPost.getContent());
                    
                    updateButtonVisibility();
                    loadLikeStatus();
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("加载帖子详情失败"));
        });

        new Thread(task).start();
    }

    private void loadCommentList() {
        Task<List<Comment>> task = new Task<List<Comment>>() {
            @Override
            protected List<Comment> call() {
                return HttpRequestUtil.getCommentList(postId);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                List<Comment> comments = task.getValue();
                commentVBox.getChildren().clear();
                if (comments != null && !comments.isEmpty()) {
                    for (Comment comment : comments) {
                        VBox commentBox = new VBox(5);
                        HBox authorBox = new HBox(5);
                        ImageView avatarImageView = new ImageView();
                        avatarImageView.setFitWidth(24);
                        avatarImageView.setFitHeight(24);
                        avatarImageView.setPreserveRatio(true);
                        
                        String avatarUrl = comment.getAuthorAvatarUrl();
                        if (avatarUrl != null && !avatarUrl.isBlank()) {
                            try {
                                Image image = new Image(avatarUrl, true);
                                avatarImageView.setImage(image);
                            } catch (Exception e) {
                                avatarImageView.setImage(null);
                            }
                        }
                        
                        Label authorLabel;
                        if (comment.getReplyToUserNickname() != null && !comment.getReplyToUserNickname().isBlank()) {
                            authorLabel = new Label((comment.getAuthorNickname() != null ? comment.getAuthorNickname() : "未知") + " 回复 " + comment.getReplyToUserNickname());
                        } else {
                            authorLabel = new Label(comment.getAuthorNickname() != null ? comment.getAuthorNickname() : "未知");
                        }
                        authorLabel.setStyle("-fx-font-weight: bold;");
                        authorBox.getChildren().addAll(avatarImageView, authorLabel);
                        
                        Label contentLabel = new Label(comment.getContent());
                        contentLabel.setWrapText(true);
                        Label timeLabel = new Label();
                        if (comment.getCreateTime() != null) {
                            timeLabel.setText("时间：" + dateFormat.format(comment.getCreateTime()));
                        }
                        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");
                        
                        Button replyButton = new Button("回复");
                        replyButton.setStyle("-fx-font-size: 11px;");
                        replyButton.setOnAction(e -> openReplyDialog(comment));
                        
                        Button reportButton = new Button("举报");
                        reportButton.setStyle("-fx-font-size: 11px;");
                        reportButton.setOnAction(e -> openCommentReportDialog(comment));
                        
                        boolean isLoggedIn = currentUser != null;
                        boolean isBanned = isLoggedIn && Boolean.TRUE.equals(currentUser.getIsBanned());
                        boolean isCommentAuthor = isLoggedIn && comment.getAuthorId() != null && 
                            currentUser.getPersonId() != null && 
                            currentUser.getPersonId().longValue() == comment.getAuthorId().longValue();
                        reportButton.setVisible(isLoggedIn && !isBanned && !isCommentAuthor);
                        
                        HBox bottomBox = new HBox(10);
                        bottomBox.getChildren().addAll(timeLabel, replyButton, reportButton);
                        
                        VBox replyVBox = new VBox(5);
                        replyVBox.setStyle("-fx-padding: 10 0 0 30;");
                        
                        commentBox.getChildren().addAll(authorBox, contentLabel, bottomBox, replyVBox);
                        commentBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 5;");
                        
                        if (comment.getReplyList() != null && !comment.getReplyList().isEmpty()) {
                            for (Comment reply : comment.getReplyList()) {
                                VBox replyBox = new VBox(5);
                                HBox replyAuthorBox = new HBox(5);
                                ImageView replyAvatarImageView = new ImageView();
                                replyAvatarImageView.setFitWidth(20);
                                replyAvatarImageView.setFitHeight(20);
                                replyAvatarImageView.setPreserveRatio(true);
                                
                                String replyAvatarUrl = reply.getAuthorAvatarUrl();
                                if (replyAvatarUrl != null && !replyAvatarUrl.isBlank()) {
                                    try {
                                        Image image = new Image(replyAvatarUrl, true);
                                        replyAvatarImageView.setImage(image);
                                    } catch (Exception e) {
                                        replyAvatarImageView.setImage(null);
                                    }
                                }
                                
                                Label replyAuthorLabel;
                                if (reply.getReplyToUserNickname() != null && !reply.getReplyToUserNickname().isBlank()) {
                                    replyAuthorLabel = new Label((reply.getAuthorNickname() != null ? reply.getAuthorNickname() : "未知") + " 回复 " + reply.getReplyToUserNickname());
                                } else {
                                    replyAuthorLabel = new Label(reply.getAuthorNickname() != null ? reply.getAuthorNickname() : "未知");
                                }
                                replyAuthorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                                replyAuthorBox.getChildren().addAll(replyAvatarImageView, replyAuthorLabel);
                                
                                Label replyContentLabel = new Label(reply.getContent());
                                replyContentLabel.setWrapText(true);
                                replyContentLabel.setStyle("-fx-font-size: 12px;");
                                Label replyTimeLabel = new Label();
                                if (reply.getCreateTime() != null) {
                                    replyTimeLabel.setText("时间：" + dateFormat.format(reply.getCreateTime()));
                                }
                                replyTimeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
                                
                                Button replyReplyButton = new Button("回复");
                                replyReplyButton.setStyle("-fx-font-size: 10px;");
                                replyReplyButton.setOnAction(e -> openReplyDialog(reply));
                                
                                Button replyReportButton = new Button("举报");
                                replyReportButton.setStyle("-fx-font-size: 10px;");
                                replyReportButton.setOnAction(e -> openCommentReportDialog(reply));
                                
                                boolean replyIsLoggedIn = currentUser != null;
                                boolean replyIsBanned = replyIsLoggedIn && Boolean.TRUE.equals(currentUser.getIsBanned());
                                boolean isReplyAuthor = replyIsLoggedIn && reply.getAuthorId() != null && 
                                    currentUser.getPersonId() != null && 
                                    currentUser.getPersonId().longValue() == reply.getAuthorId().longValue();
                                replyReportButton.setVisible(replyIsLoggedIn && !replyIsBanned && !isReplyAuthor);
                                
                                HBox replyBottomBox = new HBox(10);
                                replyBottomBox.getChildren().addAll(replyTimeLabel, replyReplyButton, replyReportButton);
                                
                                replyBox.getChildren().addAll(replyAuthorBox, replyContentLabel, replyBottomBox);
                                replyBox.setStyle("-fx-background-color: #e8e8e8; -fx-padding: 8; -fx-background-radius: 3;");
                                replyVBox.getChildren().add(replyBox);
                            }
                        }
                        
                        commentVBox.getChildren().add(commentBox);
                    }
                } else {
                    Label noCommentLabel = new Label("暂无评论");
                    noCommentLabel.setStyle("-fx-text-fill: gray;");
                    commentVBox.getChildren().add(noCommentLabel);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("加载评论列表失败"));
        });

        new Thread(task).start();
    }

    private void openReplyDialog(Comment parentComment) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("回复评论");
        dialog.setHeaderText("回复 @" + (parentComment.getAuthorNickname() != null ? parentComment.getAuthorNickname() : "未知"));
        dialog.setContentText("请输入回复内容：");
        
        dialog.showAndWait().ifPresent(content -> {
            if (content.trim().isEmpty()) {
                showError("回复内容不能为空");
                return;
            }
            
            Task<Comment> task = new Task<Comment>() {
                @Override
                protected Comment call() {
                    return HttpRequestUtil.publishComment(postId, content.trim(), parentComment.getId());
                }
            };
            
            task.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    Comment result = task.getValue();
                    if (result != null) {
                        showInfo("回复成功！");
                        loadCommentList();
                        loadPostDetail();
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
    
    private void updateButtonVisibility() {
        boolean isLoggedIn = currentUser != null;
        boolean isBanned = isLoggedIn && Boolean.TRUE.equals(currentUser.getIsBanned());
        boolean isAdmin = isLoggedIn && 
            ("ROLE_ADMIN".equals(currentUser.getAuthority()) || "ROLE_SUPER".equals(currentUser.getAuthority()));
        boolean isAuthor = isLoggedIn && currentPost != null && 
            currentUser.getPersonId() != null && currentPost.getUserId() != null &&
            currentUser.getPersonId().longValue() == currentPost.getUserId().longValue();
        
        submitCommentButton.setVisible(isLoggedIn && !isBanned);
        reportButton.setVisible(isLoggedIn && !isBanned && !isAuthor);
        
        boolean canEditDelete = isLoggedIn && !isBanned && (isAuthor || isAdmin);
        editButton.setVisible(canEditDelete);
        deleteButton.setVisible(canEditDelete);
        
        topButton.setVisible(isAdmin);
        featureButton.setVisible(isAdmin);
    }
    
    private void openReportDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("举报");
        dialog.setHeaderText("举报帖子：" + (currentPost != null ? currentPost.getTitle() : ""));
        dialog.setContentText("请输入举报原因：");
        
        dialog.showAndWait().ifPresent(reason -> {
            if (reason.trim().isEmpty()) {
                showError("举报原因不能为空");
                return;
            }
            
            Task<com.teach.javafx.models.Report> task = new Task<com.teach.javafx.models.Report>() {
                @Override
                protected com.teach.javafx.models.Report call() {
                    return HttpRequestUtil.submitReport(1, postId, reason.trim());
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
    
    private void openCommentReportDialog(Comment comment) {
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

    private void publishComment() {
        String content = commentTextArea.getText().trim();
        if (content.isEmpty()) {
            showError("评论内容不能为空");
            return;
        }
        
        Task<Comment> task = new Task<Comment>() {
            @Override
            protected Comment call() {
                return HttpRequestUtil.publishComment(postId, content);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Comment result = task.getValue();
                if (result != null) {
                    showInfo("评论发表成功！");
                    commentTextArea.clear();
                    loadCommentList();
                    loadPostDetail();
                } else {
                    showError("评论发表失败，请稍后重试");
                }
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("评论发表失败，请稍后重试"));
        });
        
        new Thread(task).start();
    }
    
    private void deletePost() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除这个帖子吗？");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Task<Boolean> task = new Task<Boolean>() {
                    @Override
                    protected Boolean call() {
                        return HttpRequestUtil.deletePost(postId);
                    }
                };
                
                task.setOnSucceeded(event -> {
                    Platform.runLater(() -> {
                        Boolean result = task.getValue();
                        if (Boolean.TRUE.equals(result)) {
                            showInfo("删除成功！");
                            if (AppStore.getMainFrameController() != null) {
                                AppStore.getMainFrameController().closeCurrentTab();
                                com.teach.javafx.controller.PostListController plc = 
                                    AppStore.getMainFrameController().getPostListController();
                                if (plc != null) {
                                    plc.loadPostList();
                                }
                            }
                        } else {
                            showError("删除失败，请稍后重试");
                        }
                    });
                });
                
                task.setOnFailed(event -> {
                    Platform.runLater(() -> showError("删除失败，请稍后重试"));
                });
                
                new Thread(task).start();
            }
        });
    }
    
    private void toggleTop() {
        Task<Post> task = new Task<Post>() {
            @Override
            protected Post call() {
                return HttpRequestUtil.toggleTop(postId);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Post result = task.getValue();
                if (result != null) {
                    loadPostDetail();
                } else {
                    showError("操作失败，请稍后重试");
                }
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("操作失败，请稍后重试"));
        });
        
        new Thread(task).start();
    }
    
    private void toggleFeature() {
        Task<Post> task = new Task<Post>() {
            @Override
            protected Post call() {
                return HttpRequestUtil.toggleFeature(postId);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Post result = task.getValue();
                if (result != null) {
                    loadPostDetail();
                } else {
                    showError("操作失败，请稍后重试");
                }
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("操作失败，请稍后重试"));
        });
        
        new Thread(task).start();
    }

    private void toggleLike() {
        if (currentPost == null || postId == null) {
            return;
        }
        
        likeButton.setDisable(true);
        
        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.toggleLike(postId);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> result = task.getValue();
                if (result != null) {
                    Boolean liked = (Boolean) result.get("liked");
                    Double likeCount = ((Number) result.get("likeCount")).doubleValue();
                    
                    isLiked = liked != null && liked;
                    currentPost.setLikeCount(likeCount.intValue());
                    
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
        if (postId == null) {
            return;
        }
        
        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getLikeStatus(postId);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> result = task.getValue();
                if (result != null) {
                    Boolean liked = (Boolean) result.get("liked");
                    Double likeCount = ((Number) result.get("likeCount")).doubleValue();
                    
                    isLiked = liked != null && liked;
                    if (currentPost != null) {
                        currentPost.setLikeCount(likeCount.intValue());
                    }
                    
                    updateLikeButtonText();
                }
            });
        });
        
        new Thread(task).start();
    }

    private void openEditDialog() {
        if (currentPost == null) {
            return;
        }
        
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                com.teach.javafx.MainApplication.class.getResource("post-edit-dialog.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 600, 500);
            
            PostEditController controller = fxmlLoader.getController();
            controller.setPost(currentPost);
            controller.setCallback(updatedPost -> {
                currentPost = updatedPost;
                loadPostDetail();
            });
            
            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.setTitle("编辑帖子");
            dialogStage.setScene(scene);
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("打开编辑对话框失败");
        }
    }

    private void updateLikeButtonText() {
        if (currentPost != null) {
            int count = currentPost.getLikeCount() != null ? currentPost.getLikeCount() : 0;
            if (isLiked) {
                likeButton.setText("已赞 (" + count + ")");
                likeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            } else {
                likeButton.setText("点赞 (" + count + ")");
                likeButton.setStyle("");
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
}
