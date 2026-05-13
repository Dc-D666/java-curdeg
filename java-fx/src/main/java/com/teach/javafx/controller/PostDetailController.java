package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.AttachmentInfo;
import com.teach.javafx.models.Comment;
import com.teach.javafx.models.Post;
import com.teach.javafx.models.User;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.util.AttachmentUtil;
import com.teach.javafx.util.FollowStateManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
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
    private VBox moderationFlowCard;
    @FXML
    private HBox moderationFlowStepsBox;
    @FXML
    private Label moderationFlowSummaryLabel;
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
    private VBox postAttachmentsVBox;
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
    private Button addAttachmentButton;
    @FXML
    private ScrollPane commentImagePreviewScrollPane;
    @FXML
    private FlowPane commentImagePreviewPane;
    @FXML
    private ScrollPane commentAttachmentPreviewScrollPane;
    @FXML
    private FlowPane commentAttachmentPreviewPane;
    @FXML
    private HBox replyTargetBox;
    @FXML
    private Label replyTargetLabel;
    @FXML
    private Button cancelReplyButton;
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
    private final List<Path> selectedCommentImages = new ArrayList<>();
    private final List<Path> selectedCommentAttachments = new ArrayList<>();
    private Comment replyingToComment;

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
        addAttachmentButton.setOnAction(event -> handleAddAttachment());
        cancelReplyButton.setOnAction(event -> clearReplyTarget());
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
        addAttachmentButton.setVisible(false);
        moreButton.setVisible(false);
        refreshCommentImagePreview();
        refreshCommentAttachmentPreview();
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
                        moderationStatusLabel.setStyle(currentPost.getModerationStatusStyle());
                    } else {
                        moderationStatusLabel.setText("");
                        moderationStatusLabel.setStyle("");
                    }
                    renderModerationFlow(currentPost);
                    
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
                    displayPostAttachments();
                    
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
        if (parentComment != null) {
            setReplyTarget(parentComment);
            return;
        }
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
        addAttachmentButton.setVisible(false);
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
            addAttachmentButton.setVisible(isLoggedIn && !isBanned);
            
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

    private void selectCommentImages() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择评论图片");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("图片文件", "*.jpg", "*.jpeg", "*.png", "*.gif"),
            new FileChooser.ExtensionFilter("所有文件", "*.*")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(MainApplication.getMainStage());
        if (files == null || files.isEmpty()) {
            return;
        }

        for (File file : files) {
            if (file == null) {
                continue;
            }
            String fileName = file.getName().toLowerCase();
            if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".gif"))) {
                showError("只能选择 jpg、jpeg、png 或 gif 图片");
                continue;
            }
            if (file.length() > 10 * 1024 * 1024) {
                showError("单张图片不能超过 10MB");
                continue;
            }
            Path imagePath = file.toPath();
            if (selectedCommentImages.contains(imagePath)) {
                continue;
            }
            if (selectedCommentImages.size() >= 9) {
                showInfo("评论最多添加 9 张图片");
                break;
            }
            selectedCommentImages.add(imagePath);
        }
        refreshCommentImagePreview();
    }

    private void refreshCommentImagePreview() {
        if (commentImagePreviewPane == null) {
            return;
        }

        commentImagePreviewPane.getChildren().clear();
        boolean hasImages = !selectedCommentImages.isEmpty();
        commentImagePreviewScrollPane.setVisible(hasImages);
        commentImagePreviewScrollPane.setManaged(hasImages);

        for (Path path : selectedCommentImages) {
            VBox tile = new VBox(6);
            tile.getStyleClass().add("preview-tile");
            tile.setPrefWidth(68);
            tile.setMaxWidth(68);

            ImageView imageView = new ImageView();
            imageView.setFitWidth(56);
            imageView.setFitHeight(44);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setImage(new Image(path.toUri().toString(), true));

            Button removeButton = new Button("删除");
            removeButton.getStyleClass().add("text-button");
            removeButton.setOnAction(event -> {
                selectedCommentImages.remove(path);
                refreshCommentImagePreview();
            });

            tile.getChildren().addAll(imageView, removeButton);
            commentImagePreviewPane.getChildren().add(tile);
        }
    }

    private void clearSelectedCommentImages() {
        selectedCommentImages.clear();
        refreshCommentImagePreview();
    }

    private void selectCommentAttachments() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择评论附件");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("所有文件", "*.*"));

        List<File> files = fileChooser.showOpenMultipleDialog(MainApplication.getMainStage());
        if (files == null || files.isEmpty()) {
            return;
        }

        for (File file : files) {
            if (file == null) {
                continue;
            }
            String error = validateAttachmentFile(file);
            if (error != null) {
                showError(error);
                continue;
            }
            Path path = file.toPath();
            if (selectedCommentAttachments.contains(path)) {
                continue;
            }
            if (selectedCommentAttachments.size() >= 5) {
                showInfo("评论最多添加 5 个附件");
                break;
            }
            selectedCommentAttachments.add(path);
        }
        refreshCommentAttachmentPreview();
    }

    private String validateAttachmentFile(File file) {
        if (!file.exists() || !file.isFile()) {
            return "附件不存在或不是普通文件";
        }
        if (file.length() <= 0) {
            return "附件不能为空";
        }
        if (file.length() > 20L * 1024 * 1024) {
            return "单个附件不能超过 20MB：" + file.getName();
        }
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == name.length() - 1) {
            return "附件必须包含文件扩展名：" + name;
        }
        String ext = name.substring(dotIndex + 1).toLowerCase();
        if (List.of("exe", "bat", "cmd", "msi", "dll", "scr", "com", "jar", "class", "sh", "ps1", "vbs", "reg").contains(ext)) {
            return "不支持上传可执行或高风险附件：" + name;
        }
        return null;
    }

    private void refreshCommentAttachmentPreview() {
        if (commentAttachmentPreviewPane == null) {
            return;
        }

        commentAttachmentPreviewPane.getChildren().clear();
        boolean hasAttachments = !selectedCommentAttachments.isEmpty();
        commentAttachmentPreviewScrollPane.setVisible(hasAttachments);
        commentAttachmentPreviewScrollPane.setManaged(hasAttachments);

        for (Path path : selectedCommentAttachments) {
            HBox item = new HBox(6);
            item.getStyleClass().add("attachment-chip");

            File file = path.toFile();
            Label label = new Label(file.getName() + " (" + AttachmentUtil.formatSize(file.length()) + ")");
            Button removeButton = new Button("删除");
            removeButton.getStyleClass().add("text-button");
            removeButton.setOnAction(event -> {
                selectedCommentAttachments.remove(path);
                refreshCommentAttachmentPreview();
            });

            item.getChildren().addAll(label, removeButton);
            commentAttachmentPreviewPane.getChildren().add(item);
        }
    }

    private void clearSelectedCommentAttachments() {
        selectedCommentAttachments.clear();
        refreshCommentAttachmentPreview();
    }

    private void setReplyTarget(Comment comment) {
        replyingToComment = comment;
        String nickname = comment.getAuthorNickname() != null ? comment.getAuthorNickname() : "未知用户";
        replyTargetLabel.setText("正在回复 @" + nickname);
        replyTargetBox.setVisible(true);
        replyTargetBox.setManaged(true);
        commentTextArea.setPromptText("回复 @" + nickname + "，支持添加图片和附件...");
        submitCommentButton.setText("发布回复");
        commentTextArea.requestFocus();
    }

    private void clearReplyTarget() {
        replyingToComment = null;
        replyTargetBox.setVisible(false);
        replyTargetBox.setManaged(false);
        commentTextArea.setPromptText("发表评论，支持添加图片和附件...");
        submitCommentButton.setText("发布评论");
    }

    private void publishCommentWithImages() {
        String content = commentTextArea.getText().trim();
        if (content.isEmpty() && selectedCommentImages.isEmpty() && selectedCommentAttachments.isEmpty()) {
            showError("评论内容、图片或附件不能全为空");
            return;
        }

        List<Path> imageFiles = new ArrayList<>(selectedCommentImages);
        List<Path> attachmentFiles = new ArrayList<>(selectedCommentAttachments);
        Long parentId = replyingToComment != null ? replyingToComment.getId() : null;
        submitCommentButton.setDisable(true);
        addImageButton.setDisable(true);
        addAttachmentButton.setDisable(true);
        submitCommentButton.setText("发布中...");

        Task<Comment> task = new Task<Comment>() {
            @Override
            protected Comment call() {
                List<String> uploadedUrls = HttpRequestUtil.uploadImages(imageFiles);
                if (uploadedUrls.size() != imageFiles.size()) {
                    throw new IllegalStateException("图片上传失败");
                }
                String imageUrls = String.join(",", uploadedUrls);
                List<AttachmentInfo> uploadedAttachments = HttpRequestUtil.uploadAttachments(attachmentFiles);
                if (uploadedAttachments.size() != attachmentFiles.size()) {
                    throw new IllegalStateException("附件上传失败");
                }
                String attachmentInfos = AttachmentUtil.toJson(uploadedAttachments);
                String safeContent = content.isEmpty() ? (attachmentFiles.isEmpty() ? "分享图片" : "分享附件") : content;
                return HttpRequestUtil.publishComment(postId, safeContent, parentId, imageUrls, attachmentInfos);
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            Comment result = task.getValue();
            if (result != null) {
                showInfo(parentId == null ? "评论发表成功！" : "回复成功！");
                commentTextArea.clear();
                clearSelectedCommentImages();
                clearSelectedCommentAttachments();
                clearReplyTarget();
                loadCommentList();
                loadPostDetail();
            } else {
                showError("评论发表失败，请稍后重试");
            }
            submitCommentButton.setDisable(false);
            addImageButton.setDisable(false);
            addAttachmentButton.setDisable(false);
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            showError("评论发表失败，请检查图片/附件大小或稍后重试");
            submitCommentButton.setDisable(false);
            addImageButton.setDisable(false);
            addAttachmentButton.setDisable(false);
            submitCommentButton.setText(parentId == null ? "发布评论" : "发布回复");
        }));

        new Thread(task).start();
    }

    private void renderModerationFlow(Post post) {
        if (moderationFlowCard == null || moderationFlowStepsBox == null || moderationFlowSummaryLabel == null) {
            return;
        }
        moderationFlowStepsBox.getChildren().clear();
        if (post == null) {
            moderationFlowSummaryLabel.setText("");
            return;
        }

        Post.ModerationFlowView flowView = post.getModerationFlowView();
        moderationFlowSummaryLabel.setText(flowView.getSummary());
        List<Post.ModerationFlowStep> steps = flowView.getSteps();

        for (int i = 0; i < steps.size(); i++) {
            Post.ModerationFlowStep step = steps.get(i);
            moderationFlowStepsBox.getChildren().add(createFlowStepNode(step, i + 1, false));
            if (i < steps.size() - 1) {
                moderationFlowStepsBox.getChildren().add(createFlowConnector(flowView.isConnectorReached(i), false));
            }
        }
    }

    private VBox createFlowStepNode(Post.ModerationFlowStep step, int stepNumber, boolean compact) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("moderation-flow-step");
        if (compact) {
            box.getStyleClass().add("moderation-flow-step-compact");
        }

        Label indexLabel = new Label(String.valueOf(stepNumber));
        indexLabel.getStyleClass().add("moderation-flow-index");
        indexLabel.getStyleClass().add(resolveFlowIndexStyleClass(step.getVisualState()));
        if (compact) {
            indexLabel.getStyleClass().add("moderation-flow-index-compact");
        }

        Label titleLabel = new Label(step.getTitle());
        titleLabel.getStyleClass().add("moderation-flow-step-title");
        if (compact) {
            titleLabel.getStyleClass().add("moderation-flow-step-title-compact");
        }

        Label stateLabel = new Label(step.getStateText());
        stateLabel.getStyleClass().add("moderation-flow-step-state");
        stateLabel.getStyleClass().add(resolveFlowStateStyleClass(step.getVisualState()));
        if (compact) {
            stateLabel.getStyleClass().add("moderation-flow-step-state-compact");
        }

        box.getChildren().addAll(indexLabel, titleLabel, stateLabel);
        return box;
    }

    private Region createFlowConnector(boolean reached, boolean compact) {
        Region connector = new Region();
        connector.getStyleClass().add("moderation-flow-connector");
        connector.getStyleClass().add(reached ? "moderation-flow-connector-active" : "moderation-flow-connector-inactive");
        if (compact) {
            connector.getStyleClass().add("moderation-flow-connector-compact");
        }
        HBox.setHgrow(connector, Priority.ALWAYS);
        return connector;
    }

    private String resolveFlowIndexStyleClass(Post.ModerationFlowVisualState state) {
        switch (state) {
            case COMPLETED:
                return "moderation-flow-index-completed";
            case ACTIVE:
                return "moderation-flow-index-active";
            case WARNING:
                return "moderation-flow-index-warning";
            case DANGER:
                return "moderation-flow-index-danger";
            case INACTIVE:
            default:
                return "moderation-flow-index-inactive";
        }
    }

    private String resolveFlowStateStyleClass(Post.ModerationFlowVisualState state) {
        switch (state) {
            case COMPLETED:
                return "moderation-flow-state-completed";
            case ACTIVE:
                return "moderation-flow-state-active";
            case WARNING:
                return "moderation-flow-state-warning";
            case DANGER:
                return "moderation-flow-state-danger";
            case INACTIVE:
            default:
                return "moderation-flow-state-inactive";
        }
    }

    private void handleAddImage() {
        selectCommentImages();
    }

    private void handleAddAttachment() {
        selectCommentAttachments();
    }

    private void publishComment() {
        publishCommentWithImages();
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

    private void displayPostAttachments() {
        postAttachmentsVBox.getChildren().clear();

        if (currentPost == null) {
            return;
        }

        List<AttachmentInfo> attachments = AttachmentUtil.parse(currentPost.getAttachmentInfos());
        if (attachments.isEmpty()) {
            postAttachmentsVBox.setVisible(false);
            postAttachmentsVBox.setManaged(false);
            return;
        }

        postAttachmentsVBox.setVisible(true);
        postAttachmentsVBox.setManaged(true);

        Label title = new Label("附件");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #172033;");
        postAttachmentsVBox.getChildren().add(title);

        for (AttachmentInfo attachment : attachments) {
            postAttachmentsVBox.getChildren().add(createAttachmentRow(attachment));
        }
    }

    private HBox createAttachmentRow(AttachmentInfo attachment) {
        HBox row = new HBox(10);
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
        return row;
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
