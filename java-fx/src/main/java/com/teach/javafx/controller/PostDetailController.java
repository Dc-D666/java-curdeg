package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.Comment;
import com.teach.javafx.models.Post;
import com.teach.javafx.models.User;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.util.FollowStateManager;
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
import java.util.function.Consumer;

public class PostDetailController extends ToolController {
    @FXML
    private Button backButton;
    @FXML
    private Label breadcrumbLabel;
    @FXML
    private Label boardLabel;
    @FXML
    private Button refreshButton;
    @FXML
    private ProgressIndicator refreshProgress;
    @FXML
    private Label titleLabel;
    @FXML
    private ImageView authorImageView;
    @FXML
    private Label authorLabel;
    @FXML
    private Label createTimeLabel;
    @FXML
    private Label viewCountLabel;
    @FXML
    private Label likeCountLabel;
    @FXML
    private Label commentCountLabel;
    @FXML
    private Label favoriteCountLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label moderationStatusLabel;
    @FXML
    private VBox violationInfoVBox;
    @FXML
    private Label violationTypeLabel;
    @FXML
    private Label violationFragmentLabel;
    @FXML
    private Label violationSuggestionLabel;
    @FXML
    private Label violationRemarkLabel;
    @FXML
    private Button editViolationButton;
    @FXML
    private Label contentLabel;
    @FXML
    private VBox postImagesVBox;
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
    private Button favoriteButton;
    @FXML
    private Button shareButton;
    @FXML
    private Button reportButton;
    @FXML
    private MenuButton moreButton;
    @FXML
    private MenuItem editMenuItem;
    @FXML
    private MenuItem deleteMenuItem;
    @FXML
    private MenuItem topMenuItem;
    @FXML
    private MenuItem featureMenuItem;
    @FXML
    private MenuItem summaryMenuItem;
    @FXML
    private Button addImageButton;
    @FXML
    private Button followButton;
    @FXML
    private TitledPane aiSummaryPane;
    @FXML
    private ProgressIndicator aiSummaryProgress;
    @FXML
    private Label postSummaryLabel;
    @FXML
    private Label postSummaryContentLabel;
    @FXML
    private Label commentHotspotsTitleLabel;
    @FXML
    private Label commentHotspotsContentLabel;

    private Long postId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private User currentUser;
    private Post currentPost;
    private boolean isLiked = false;
    private boolean isFollowed = false;
    private boolean isFavorited = false;

    @FXML
    public void initialize() {
        backButton.setOnAction(event -> handleBack());
        refreshButton.setOnAction(event -> handleRefresh());
        breadcrumbLabel.setOnMouseClicked(event -> handleBack());
        
        // 头像点击事件
        authorImageView.setStyle("-fx-cursor: hand;");
        authorImageView.setOnMouseClicked(event -> {
            if (currentPost != null && currentPost.getUserId() != null) {
                openUserHome(currentPost.getUserId().intValue(), currentPost.getAuthorNickname());
            }
        });
        
        submitCommentButton.setOnAction(event -> publishComment());
        likeButton.setOnAction(event -> toggleLike());
        favoriteButton.setOnAction(event -> toggleFavorite());
        followButton.setOnAction(event -> toggleFollow());
        reportButton.setOnAction(event -> openReportDialog());
        shareButton.setOnAction(event -> handleShare());
        addImageButton.setOnAction(event -> handleAddImage());
        editViolationButton.setOnAction(event -> openEditDialog());
        
        editMenuItem.setOnAction(event -> openEditDialog());
        deleteMenuItem.setOnAction(event -> deletePost());
        topMenuItem.setOnAction(event -> toggleTop());
        featureMenuItem.setOnAction(event -> toggleFeature());
        summaryMenuItem.setOnAction(event -> handleSummary());
        
        submitCommentButton.setVisible(false);
        likeButton.setVisible(false);
        favoriteButton.setVisible(false);
        shareButton.setVisible(false);
        reportButton.setVisible(false);
        followButton.setVisible(false);
        addImageButton.setVisible(false);
        moreButton.setVisible(false);
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
                    
                    if (currentPost.getBoardName() != null) {
                        boardLabel.setText(currentPost.getBoardName());
                    }
                    
                    String avatarUrl = currentPost.getAuthorAvatarUrl();
                    if (avatarUrl != null && !avatarUrl.isBlank()) {
                        try {
                            String fullAvatarUrl = avatarUrl.startsWith("/") ? 
                                HttpRequestUtil.serverUrl + avatarUrl : avatarUrl;
                            Image image = new Image(fullAvatarUrl, true);
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
                    
                    viewCountLabel.setText("浏览量：" + (currentPost.getViewCount() != null ? currentPost.getViewCount() : 0));
                    likeCountLabel.setText("点赞量：" + (currentPost.getLikeCount() != null ? currentPost.getLikeCount() : 0));
                    commentCountLabel.setText("评论量：" + (currentPost.getCommentCount() != null ? currentPost.getCommentCount() : 0));
                    favoriteCountLabel.setText("收藏量：" + (currentPost.getFavoriteCount() != null ? currentPost.getFavoriteCount() : 0));
                    
                    statusLabel.setText(currentPost.getStatusText());
                    
                    String moderationStatusText = currentPost.getModerationStatusText();
                    if (moderationStatusText != null && !moderationStatusText.isEmpty()) {
                        moderationStatusLabel.setText(moderationStatusText);
                        String moderationStatus = currentPost.getModerationStatus();
                        if ("pending".equals(moderationStatus)) {
                            moderationStatusLabel.setStyle("-fx-padding: 2 8; -fx-background-radius: 4; -fx-font-size: 12px; -fx-background-color: #fa8c16; -fx-text-fill: white;");
                        } else if ("manual".equals(moderationStatus)) {
                            moderationStatusLabel.setStyle("-fx-padding: 2 8; -fx-background-radius: 4; -fx-font-size: 12px; -fx-background-color: #fadb14; -fx-text-fill: #333;");
                        } else if ("pass".equals(moderationStatus)) {
                            moderationStatusLabel.setStyle("-fx-padding: 2 8; -fx-background-radius: 4; -fx-font-size: 12px; -fx-background-color: #52c41a; -fx-text-fill: white;");
                        } else if ("reject".equals(moderationStatus)) {
                            moderationStatusLabel.setStyle("-fx-padding: 2 8; -fx-background-radius: 4; -fx-font-size: 12px; -fx-background-color: #f5222d; -fx-text-fill: white;");
                        }
                    } else {
                        moderationStatusLabel.setText("");
                        moderationStatusLabel.setStyle("");
                    }
                    
                    // 显示违规信息
                    boolean isRejected = "reject".equals(currentPost.getModerationStatus());
                    violationInfoVBox.setVisible(isRejected);
                    violationInfoVBox.setManaged(isRejected);
                    if (isRejected) {
                        // 违规类型
                        StringBuilder typeText = new StringBuilder();
                        if (currentPost.getModerationViolationLevel() != null && !currentPost.getModerationViolationLevel().isEmpty()) {
                            typeText.append("违规等级：").append(currentPost.getModerationViolationLevel());
                        }
                        if (currentPost.getModerationViolationType() != null && !currentPost.getModerationViolationType().isEmpty()) {
                            if (typeText.length() > 0) typeText.append(" | ");
                            typeText.append("违规类型：").append(currentPost.getModerationViolationType());
                        }
                        violationTypeLabel.setText(typeText.length() > 0 ? typeText.toString() : "内容不符合社区规范");
                        violationTypeLabel.setVisible(typeText.length() > 0 || true);
                        
                        // 违规片段
                        if (currentPost.getModerationViolationFragments() != null && !currentPost.getModerationViolationFragments().isEmpty()) {
                            violationFragmentLabel.setText("违规片段：" + currentPost.getModerationViolationFragments());
                            violationFragmentLabel.setVisible(true);
                        } else {
                            violationFragmentLabel.setVisible(false);
                        }
                        
                        // 审核建议
                        if (currentPost.getModerationSuggestion() != null && !currentPost.getModerationSuggestion().isEmpty()) {
                            violationSuggestionLabel.setText("审核建议：" + currentPost.getModerationSuggestion());
                            violationSuggestionLabel.setVisible(true);
                        } else {
                            violationSuggestionLabel.setVisible(false);
                        }
                        
                        // 审核备注
                        if (currentPost.getModerationRemark() != null && !currentPost.getModerationRemark().isEmpty()) {
                            violationRemarkLabel.setText("备注：" + currentPost.getModerationRemark());
                            violationRemarkLabel.setVisible(true);
                        } else {
                            violationRemarkLabel.setVisible(false);
                        }
                        
                        // 编辑按钮
                        boolean isAuthor = currentUser != null && currentUser.getPersonId() != null && currentPost.getUserId() != null && currentUser.getPersonId().longValue() == currentPost.getUserId().longValue();
                        editViolationButton.setVisible(isAuthor);
                    }
                    
                    contentLabel.setText(currentPost.getContent());
                    
                    displayPostImages();
                    
                    updateButtonVisibility();
                    loadLikeStatus();
                    loadFavoriteStatus();
                    loadFollowStatus();
                    
                    // 关键修复：强制把焦点设置到ScrollPane，不让TextArea有机会搞事情
                    Platform.runLater(() -> {
                        Platform.runLater(() -> {
                            Platform.runLater(() -> {
                                mainScrollPane.requestFocus();
                                mainScrollPane.setVvalue(0.0);
                            });
                        });
                    });
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
                        try {
                            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                                com.teach.javafx.MainApplication.class.getResource("comment-item.fxml"));
                            VBox commentNode = fxmlLoader.load();
                            
                            CommentItemController controller = fxmlLoader.getController();
                            controller.setData(comment, currentUser, postId, this::openReplyDialog, this::loadCommentList, false);
                            
                            commentVBox.getChildren().add(commentNode);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Label noCommentLabel = new Label("暂无评论");
                    noCommentLabel.setStyle("-fx-text-fill: gray;");
                    commentVBox.getChildren().add(noCommentLabel);
                }
                
                // 关键修复：强制把焦点设置到ScrollPane，不让TextArea有机会搞事情
                Platform.runLater(() -> {
                    Platform.runLater(() -> {
                        Platform.runLater(() -> {
                            mainScrollPane.requestFocus();
                            mainScrollPane.setVvalue(0.0);
                        });
                    });
                });
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
        
        // 检查是否是违规帖
        boolean isRejected = currentPost != null && "reject".equals(currentPost.getModerationStatus());
        
        // 重置所有菜单和按钮为不可见
        submitCommentButton.setVisible(false);
        likeButton.setVisible(false);
        favoriteButton.setVisible(false);
        shareButton.setVisible(false);
        reportButton.setVisible(false);
        followButton.setVisible(false);
        addImageButton.setVisible(false);
        moreButton.setVisible(false);
        editMenuItem.setVisible(false);
        deleteMenuItem.setVisible(false);
        topMenuItem.setVisible(false);
        featureMenuItem.setVisible(false);
        summaryMenuItem.setVisible(false);
        
        // 如果是违规帖，只显示编辑和删除菜单（给作者和管理员）
        if (isRejected) {
            boolean canEditDelete = isLoggedIn && !isBanned && (isAuthor || isAdmin);
            if (canEditDelete) {
                moreButton.setVisible(true);
                editMenuItem.setVisible(true);
                deleteMenuItem.setVisible(true);
            }
        } else {
            // 正常显示按钮和菜单
            submitCommentButton.setVisible(isLoggedIn && !isBanned);
            likeButton.setVisible(isLoggedIn && !isBanned);
            favoriteButton.setVisible(isLoggedIn && !isBanned);
            shareButton.setVisible(isLoggedIn && !isBanned);
            reportButton.setVisible(isLoggedIn && !isBanned && !isAuthor);
            followButton.setVisible(isLoggedIn && !isBanned && !isAuthor);
            addImageButton.setVisible(isLoggedIn && !isBanned);
            
            boolean hasMoreOptions = false;
            boolean canEditDelete = isLoggedIn && !isBanned && (isAuthor || isAdmin);
            if (canEditDelete) {
                editMenuItem.setVisible(true);
                deleteMenuItem.setVisible(true);
                hasMoreOptions = true;
            }
            if (isAdmin) {
                topMenuItem.setVisible(true);
                featureMenuItem.setVisible(true);
                hasMoreOptions = true;
            }
            if (isLoggedIn && !isBanned) {
                summaryMenuItem.setVisible(true);
                hasMoreOptions = true;
            }
            moreButton.setVisible(hasMoreOptions);
        }
    }
    
    private void handleSummary() {
        summaryMenuItem.setDisable(true);
        aiSummaryProgress.setVisible(true);
        aiSummaryPane.setExpanded(true);
        postSummaryContentLabel.setText("正在总结中，请稍后......");
        commentHotspotsContentLabel.setText("正在总结中，请稍后......");

        Task<com.teach.javafx.request.DataResponse<Map<String, Object>>> task = new Task<com.teach.javafx.request.DataResponse<Map<String, Object>>>() {
            @Override
            protected com.teach.javafx.request.DataResponse<Map<String, Object>> call() {
                return HttpRequestUtil.getPostSummary(postId);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                com.teach.javafx.request.DataResponse<Map<String, Object>> result = task.getValue();
                if (result != null) {
                    if (result.getCode() == 0) {
                        // 成功！
                        Map<String, Object> data = result.getData();
                        if (data != null) {
                            String postSummary = (String) data.get("postSummary");
                            String commentHotspots = (String) data.get("commentHotspots");

                            if (postSummary != null && !postSummary.isEmpty()) {
                                postSummaryContentLabel.setText(postSummary);
                            }

                            if (commentHotspots != null && !commentHotspots.isEmpty()) {
                                commentHotspotsContentLabel.setText(commentHotspots);
                            }
                        }
                    } else {
                        // 失败或info情况！显示msg
                        String message = result.getMsg();
                        if (message != null && !message.isEmpty()) {
                            postSummaryContentLabel.setText(message);
                            commentHotspotsContentLabel.setText(message);
                        } else {
                            showError("获取总结失败，请稍后重试");
                        }
                    }
                } else {
                    showError("获取总结失败，请稍后重试");
                }
                aiSummaryProgress.setVisible(false);
                summaryMenuItem.setDisable(false);
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                showError("获取总结失败，请稍后重试");
                aiSummaryProgress.setVisible(false);
                summaryMenuItem.setDisable(false);
            });
        });

        new Thread(task).start();
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
    
    private void handleShare() {
        if (currentPost == null || postId == null) {
            showError("无法获取帖子信息");
            return;
        }

        // 生成分享链接
        String shareUrl = "bbs://post/" + postId;
        
        // 处理长文本截断
        String title = currentPost.getTitle() != null ? currentPost.getTitle() : "帖子";
        String truncatedTitle = title.length() > 50 ? title.substring(0, 50) + "..." : title;
        
        String content = currentPost.getContent() != null ? currentPost.getContent() : "";
        String truncatedContent = content.length() > 100 ? content.substring(0, 100) + "..." : content;
        
        // 获取用户名
        String userName = currentUser != null && currentUser.getNickname() != null ? currentUser.getNickname() : "您的好友";
        
        // 生成分享文本
        String shareText = userName + "邀您查看论坛帖子【" + truncatedTitle + "】" + 
            truncatedContent + "，复制该文本到帖子广场查看：" + shareUrl;

        // 创建自定义对话框
        javafx.scene.control.Dialog<ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("分享帖子");
        dialog.setHeaderText("分享：" + (currentPost.getTitle() != null ? currentPost.getTitle() : "帖子"));

        // 设置按钮
        ButtonType copyButtonType = new ButtonType("复制链接", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(copyButtonType, cancelButtonType);

        // 创建内容
        javafx.scene.control.Label label = new javafx.scene.control.Label("分享链接：");
        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(shareText);
        textArea.setEditable(false);
        textArea.setPrefRowCount(4);
        textArea.setWrapText(true);

        VBox dialogContent = new VBox(10);
        dialogContent.getChildren().addAll(label, textArea);
        dialogContent.setPadding(new javafx.geometry.Insets(20));
        dialog.getDialogPane().setContent(dialogContent);

        // 处理按钮事件
        dialog.setResultConverter(buttonType -> {
            if (buttonType == copyButtonType) {
                try {
                    javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                    javafx.scene.input.ClipboardContent clipboardContent = new javafx.scene.input.ClipboardContent();
                    clipboardContent.putString(shareText);
                    clipboard.setContent(clipboardContent);
                    showInfo("分享链接已复制到剪贴板！");
                } catch (Exception e) {
                    showError("复制失败，请稍后重试");
                }
            }
            return buttonType;
        });

        dialog.showAndWait();
    }
    
    private void handleAddImage() {
        showInfo("图片添加功能开发中...");
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

    private void toggleFavorite() {
        if (currentPost == null || postId == null) {
            return;
        }
        
        favoriteButton.setDisable(true);
        
        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.toggleFavorite(postId);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> result = task.getValue();
                if (result != null) {
                    Boolean favorited = (Boolean) result.get("favorited");
                    Double favoriteCount = ((Number) result.get("favoriteCount")).doubleValue();
                    
                    isFavorited = favorited != null && favorited;
                    currentPost.setFavoriteCount(favoriteCount.intValue());
                    
                    updateFavoriteButtonText();
                }
                favoriteButton.setDisable(false);
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                showError("操作失败，请稍后重试");
                favoriteButton.setDisable(false);
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

    private void loadFavoriteStatus() {
        if (postId == null) {
            return;
        }
        
        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getFavoriteStatus(postId);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> result = task.getValue();
                if (result != null) {
                    Boolean favorited = (Boolean) result.get("favorited");
                    Double favoriteCount = ((Number) result.get("favoriteCount")).doubleValue();
                    
                    isFavorited = favorited != null && favorited;
                    if (currentPost != null) {
                        currentPost.setFavoriteCount(favoriteCount.intValue());
                    }
                    
                    updateFavoriteButtonText();
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
                likeButton.setText("点赞");
                likeButton.setStyle("");
            }
        }
    }

    private void updateFavoriteButtonText() {
        if (currentPost != null) {
            int count = currentPost.getFavoriteCount() != null ? currentPost.getFavoriteCount() : 0;
            if (isFavorited) {
                favoriteButton.setText("已收藏 (" + count + ")");
                favoriteButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            } else {
                favoriteButton.setText("收藏");
                favoriteButton.setStyle("");
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

    private void toggleFollow() {
        if (currentPost == null || currentPost.getUserId() == null) {
            return;
        }

        followButton.setDisable(true);

        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.toggleFollow(currentPost.getUserId());
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> result = task.getValue();
                if (result != null) {
                    Boolean followed = (Boolean) result.get("followed");
                    isFollowed = followed != null && followed;
                    FollowStateManager.getInstance().setFollowState(currentPost.getUserId(), isFollowed);
                    updateFollowButtonText(isFollowed);
                }
                followButton.setDisable(false);
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                showError("操作失败，请稍后重试");
                followButton.setDisable(false);
            });
        });

        new Thread(task).start();
    }

    private void loadFollowStatus() {
        if (currentPost == null || currentPost.getUserId() == null) {
            return;
        }

        Long userId = currentPost.getUserId();
        Boolean cachedState = FollowStateManager.getInstance().getFollowState(userId);
        
        if (cachedState != null) {
            isFollowed = cachedState;
            updateFollowButtonText(isFollowed);
            FollowStateManager.getInstance().registerListener(userId, (followed) -> {
                Platform.runLater(() -> {
                    isFollowed = followed;
                    updateFollowButtonText(followed);
                });
            });
        } else {
            Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
                @Override
                protected Map<String, Object> call() {
                    return HttpRequestUtil.checkFollowStatus(userId);
                }
            };

            task.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    Map<String, Object> result = task.getValue();
                    if (result != null) {
                        Boolean followed = (Boolean) result.get("followed");
                        isFollowed = followed != null && followed;
                        FollowStateManager.getInstance().setFollowState(userId, isFollowed);
                        updateFollowButtonText(isFollowed);
                        FollowStateManager.getInstance().registerListener(userId, (newFollowed) -> {
                            Platform.runLater(() -> {
                                isFollowed = newFollowed;
                                updateFollowButtonText(newFollowed);
                            });
                        });
                    }
                });
            });

            new Thread(task).start();
        }
    }

    private void updateFollowButtonText(boolean isFollowed) {
        if (isFollowed) {
            followButton.setText("已关注");
            followButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        } else {
            followButton.setText("关注");
            followButton.setStyle("");
        }
    }
    
    private void displayPostImages() {
        postImagesVBox.getChildren().clear();
        
        if (currentPost == null) {
            return;
        }
        
        String imagesStr = currentPost.getImages();
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
                imageView.setFitWidth(600);
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
                
                postImagesVBox.getChildren().add(imageView);
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
    
    private void handleBack() {
        if (AppStore.getMainFrameController() != null) {
            AppStore.getMainFrameController().closeCurrentTab();
        }
    }
    
    private void handleRefresh() {
        // 显示加载状态
        refreshButton.setDisable(true);
        refreshProgress.setVisible(true);

        // 加载帖子详情
        loadPostDetail();

        // 加载评论列表
        loadCommentList();

        // 延迟隐藏加载状态（确保任务完成）
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
        pause.setOnFinished(event -> {
            refreshButton.setDisable(false);
            refreshProgress.setVisible(false);
        });
        pause.play();
    }
    
    private void openUserHome(Integer userId, String nickname) {
        if (AppStore.getMainFrameController() != null) {
            AppStore.getMainFrameController().openUserHome(userId, nickname);
        }
    }
}
