package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.util.FollowStateManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Map;

public class UserHomeController extends ToolController {
    private static final String DEFAULT_AVATAR_URL = "https://img.phb123.com/uploads/allimg/220607/810-22060G55A40-L.jpeg";

    @javafx.fxml.FXML
    private ScrollPane mainScrollPane;
    @javafx.fxml.FXML
    private Button backButton;
    @javafx.fxml.FXML
    private Label breadcrumbLabel;
    
    @javafx.fxml.FXML
    private ImageView avatarImageView;
    @javafx.fxml.FXML
    private Label nicknameLabel;
    @javafx.fxml.FXML
    private Label signatureLabel;
    @javafx.fxml.FXML
    private Label postCountLabel;
    @javafx.fxml.FXML
    private Label followerCountLabel;
    @javafx.fxml.FXML
    private Label followingCountLabel;
    
    @javafx.fxml.FXML
    private VBox actionButtonsVBox;
    @javafx.fxml.FXML
    private Button followButton;
    @javafx.fxml.FXML
    private Button messageButton;
    @javafx.fxml.FXML
    private Button reportButton;
    
    @javafx.fxml.FXML
    private Button refreshButton;
    @javafx.fxml.FXML
    private ProgressIndicator refreshProgressIndicator;
    
    @javafx.fxml.FXML
    private VBox postListVBox;
    @javafx.fxml.FXML
    private Button prevPageButton;
    @javafx.fxml.FXML
    private Label pageInfoLabel;
    @javafx.fxml.FXML
    private Button nextPageButton;
    
    private Integer userId;
    private Integer currentUserId;
    private boolean isCurrentUser;
    private boolean isFollowing;
    private boolean currentUserBanned;
    private Map<String, Object> currentProfileData;
    
    private int currentPage = 1;
    private int totalPages = 1;
    
    @javafx.fxml.FXML
    public void initialize() {
        setupPageScroll();
        backButton.setOnAction(event -> handleBack());
        refreshButton.setOnAction(event -> loadUserPosts());
        followButton.setOnAction(event -> toggleFollow());
        messageButton.setOnAction(event -> openPrivateChat());
        reportButton.setOnAction(event -> openProfileReportDialog());
        reportButton.setVisible(false);
        reportButton.setManaged(false);
        prevPageButton.setOnAction(event -> {
            if (currentPage > 1) {
                currentPage--;
                loadUserPosts();
            }
        });
        nextPageButton.setOnAction(event -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadUserPosts();
            }
        });
    }

    private void setupPageScroll() {
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double contentHeight = mainScrollPane.getContent().getBoundsInLocal().getHeight();
            double viewportHeight = mainScrollPane.getViewportBounds().getHeight();
            double scrollableHeight = contentHeight - viewportHeight;
            if (scrollableHeight <= 0) {
                return;
            }

            double delta = event.getDeltaY() / scrollableHeight;
            mainScrollPane.setVvalue(clamp(mainScrollPane.getVvalue() - delta));
            event.consume();
        });
    }

    private double clamp(double value) {
        if (value < 0) {
            return 0;
        }
        if (value > 1) {
            return 1;
        }
        return value;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
        loadCurrentUser();
    }
    
    private void loadCurrentUser() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getCurrentUserData();
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> userData = task.getValue();
                if (userData != null && userData.containsKey("personId")) {
                    Object idObj = userData.get("personId");
                    if (idObj instanceof Number) {
                        currentUserId = ((Number) idObj).intValue();
                    }
                    Object bannedObj = userData.get("isBanned");
                    currentUserBanned = bannedObj instanceof Boolean && (Boolean) bannedObj;
                    isCurrentUser = currentUserId != null && currentUserId.equals(userId);
                    updateButtonVisibility();
                }
                loadUserProfile();
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(this::loadUserProfile);
        });
        
        new Thread(task).start();
    }
    
    private void updateButtonVisibility() {
        if (isCurrentUser) {
            actionButtonsVBox.setVisible(false);
            actionButtonsVBox.setManaged(false);
        } else {
            actionButtonsVBox.setVisible(true);
            actionButtonsVBox.setManaged(true);
            boolean canReport = currentUserId != null && !currentUserBanned && !isCurrentUser;
            reportButton.setVisible(canReport);
            reportButton.setManaged(canReport);
        }
    }
    
    private void loadUserProfile() {
        refreshProgressIndicator.setVisible(true);
        refreshButton.setDisable(true);
        
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getUserProfile(userId);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                refreshProgressIndicator.setVisible(false);
                refreshButton.setDisable(false);
                
                Map<String, Object> userData = task.getValue();
                if (userData != null) {
                    currentProfileData = userData;
                    displayUserProfile(userData);
                } else {
                    showErrorAlert("加载用户信息失败");
                }
                
                loadUserPosts();
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                refreshProgressIndicator.setVisible(false);
                refreshButton.setDisable(false);
                showErrorAlert("加载用户信息失败");
            });
        });
        
        new Thread(task).start();
    }
    
    private void displayUserProfile(Map<String, Object> userData) {
        String nickname = (String) userData.getOrDefault("nickname", "未知用户");
        nicknameLabel.setText(nickname);
        breadcrumbLabel.setText(nickname + "的主页");
        
        String signature = (String) userData.get("signature");
        if (signature != null && !signature.isBlank()) {
            signatureLabel.setText(signature);
        } else {
            signatureLabel.setText("这个人很懒，什么都没写~");
        }
        
        Object postCountObj = userData.get("postCount");
        String postCountText = "0";
        if (postCountObj instanceof Number) {
            postCountText = String.valueOf(((Number) postCountObj).intValue());
        } else if (postCountObj != null) {
            postCountText = String.valueOf(postCountObj);
        }
        postCountLabel.setText(postCountText);
        
        Object followerCountObj = userData.get("followerCount");
        String followerCountText = "0";
        if (followerCountObj instanceof Number) {
            followerCountText = String.valueOf(((Number) followerCountObj).intValue());
        } else if (followerCountObj != null) {
            followerCountText = String.valueOf(followerCountObj);
        }
        followerCountLabel.setText(followerCountText);
        
        Object followingCountObj = userData.get("followingCount");
        String followingCountText = "0";
        if (followingCountObj instanceof Number) {
            followingCountText = String.valueOf(((Number) followingCountObj).intValue());
        } else if (followingCountObj != null) {
            followingCountText = String.valueOf(followingCountObj);
        }
        followingCountLabel.setText(followingCountText);
        
        String avatarUrl = (String) userData.get("avatarUrl");
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            try {
                String fullAvatarUrl = avatarUrl.startsWith("/") ? HttpRequestUtil.serverUrl + avatarUrl : avatarUrl;
                Image image = new Image(fullAvatarUrl, true);
                avatarImageView.setImage(image);
            } catch (Exception e) {
                avatarImageView.setImage(new Image(DEFAULT_AVATAR_URL, true));
            }
        } else {
            avatarImageView.setImage(new Image(DEFAULT_AVATAR_URL, true));
        }
        
        if (!isCurrentUser) {
            loadFollowState();
        }
    }
    
    private void loadFollowState() {
        if (currentUserId == null) {
            isFollowing = false;
            updateFollowButton();
            return;
        }
        
        Boolean cachedState = FollowStateManager.getInstance().getFollowState(userId.longValue());
        if (cachedState != null) {
            isFollowing = cachedState;
            updateFollowButton();
            return;
        }
        
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.checkFollowStatus(userId);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> result = task.getValue();
                if (result != null && result.containsKey("followed")) {
                    isFollowing = (Boolean) result.get("followed");
                    FollowStateManager.getInstance().setFollowState(userId.longValue(), isFollowing);
                }
                updateFollowButton();
            });
        });
        
        new Thread(task).start();
    }
    
    private void updateFollowButton() {
        if (isFollowing) {
            followButton.setText("已关注");
            followButton.setStyle("-fx-background-color: #52c41a; -fx-text-fill: white;");
            messageButton.setVisible(true);
            messageButton.setStyle("-fx-background-color: #52c41a; -fx-text-fill: white;");
        } else {
            followButton.setText("关注");
            followButton.setStyle("");
            messageButton.setVisible(false);
            messageButton.setManaged(false);
        }
    }
    
    private void toggleFollow() {
        if (currentUserId == null) {
            showErrorAlert("请先登录");
            return;
        }
        
        followButton.setDisable(true);
        
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.toggleFollow(userId);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                followButton.setDisable(false);
                Map<String, Object> result = task.getValue();
                if (result != null && result.containsKey("followed")) {
                    isFollowing = (Boolean) result.get("followed");
                    FollowStateManager.getInstance().setFollowState(userId.longValue(), isFollowing);
                    updateFollowButton();
                    loadUserProfile();
                }
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                followButton.setDisable(false);
                showErrorAlert("操作失败");
            });
        });
        
        new Thread(task).start();
    }

    private void openProfileReportDialog() {
        if (userId == null || isCurrentUser || currentUserBanned) {
            return;
        }

        String nickname = currentProfileData != null && currentProfileData.get("nickname") != null
            ? String.valueOf(currentProfileData.get("nickname")) : "该用户";
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("举报个人主页资料卡");
        dialog.setHeaderText("举报用户主页资料卡：" + nickname);
        dialog.setContentText("请输入举报原因：");

        dialog.showAndWait().ifPresent(reason -> {
            String trimmedReason = reason != null ? reason.trim() : "";
            if (trimmedReason.isEmpty()) {
                showErrorAlert("举报原因不能为空");
                return;
            }

            Task<com.teach.javafx.models.Report> task = new Task<>() {
                @Override
                protected com.teach.javafx.models.Report call() {
                    return HttpRequestUtil.submitReport(3, userId.longValue(), trimmedReason);
                }
            };

            task.setOnSucceeded(event -> Platform.runLater(() -> {
                if (task.getValue() != null) {
                    showInfoAlert("举报成功！管理员会尽快处理。");
                } else {
                    showErrorAlert("举报失败，请稍后重试");
                }
            }));

            task.setOnFailed(event -> Platform.runLater(() -> showErrorAlert("举报失败，请稍后重试")));

            new Thread(task).start();
        });
    }
    
    private void loadUserPosts() {
        refreshProgressIndicator.setVisible(true);
        postListVBox.getChildren().clear();
        
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getUserPosts(userId, currentPage, 10);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                refreshProgressIndicator.setVisible(false);
                
                Map<String, Object> result = task.getValue();
                if (result != null) {
                    displayUserPosts(result);
                } else {
                    showErrorAlert("加载帖子列表失败");
                }
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                refreshProgressIndicator.setVisible(false);
                showErrorAlert("加载帖子列表失败");
            });
        });
        
        new Thread(task).start();
    }
    
    private void displayUserPosts(Map<String, Object> result) {
        Object contentObj = result.get("content");
        if (contentObj instanceof Iterable) {
            Iterable<?> posts = (Iterable<?>) contentObj;
            boolean hasPosts = false;
            
            for (Object obj : posts) {
                hasPosts = true;
                if (obj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> postData = (Map<String, Object>) obj;
                    addPostToView(postData);
                }
            }
            
            if (!hasPosts) {
                Label emptyLabel = new Label("该用户还没有发布帖子~");
                emptyLabel.setStyle("-fx-text-fill: #999; -fx-padding: 40 0;");
                postListVBox.getChildren().add(emptyLabel);
            }
        } else {
            Label emptyLabel = new Label("该用户还没有发布帖子~");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-padding: 40 0;");
            postListVBox.getChildren().add(emptyLabel);
        }
        
        Object totalPagesObj = result.get("totalPages");
        if (totalPagesObj instanceof Number) {
            totalPages = ((Number) totalPagesObj).intValue();
        }
        
        Object numberObj = result.get("number");
        if (numberObj instanceof Number) {
            currentPage = ((Number) numberObj).intValue() + 1;
        }
        
        pageInfoLabel.setText("第 " + currentPage + " 页 / 共 " + totalPages + " 页");
        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);
    }
    
    private void addPostToView(Map<String, Object> postData) {
        VBox postBox = new VBox(8);
        postBox.getStyleClass().add("profile-card");
        postBox.setStyle("-fx-padding: 16; -fx-cursor: hand;");
        
        // 帖子标题
        String title = (String) postData.get("title");
        Label titleLabel = new Label(title != null ? title : "");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #333; -fx-wrap-text: true;");
        titleLabel.setWrapText(true);
        
        // 元数据行
        HBox metaHBox = new HBox(15);
        
        // 创建时间
        String createTimeStr = (String) postData.get("createTime");
        Label timeLabel = new Label(createTimeStr != null ? createTimeStr : "");
        timeLabel.getStyleClass().add("post-meta-time");
        
        // 板块名称
        String boardName = (String) postData.get("boardName");
        Label boardLabel = new Label(boardName != null ? boardName : "");
        boardLabel.getStyleClass().add("post-board-chip");
        
        // 点赞数
        Object likeCountObj = postData.get("likeCount");
        int likeCount = likeCountObj instanceof Number ? ((Number) likeCountObj).intValue() : 0;
        Label likeCountLabel = createMetricChip("赞 " + likeCount, "post-meta-chip-like");
        
        // 评论数
        Object commentCountObj = postData.get("commentCount");
        int commentCount = commentCountObj instanceof Number ? ((Number) commentCountObj).intValue() : 0;
        Label commentCountLabel = createMetricChip("评 " + commentCount, "post-meta-chip-comment");
        
        // 浏览数
        Object viewCountObj = postData.get("viewCount");
        int viewCount = viewCountObj instanceof Number ? ((Number) viewCountObj).intValue() : 0;
        Label viewCountLabel = createMetricChip("阅 " + viewCount, "post-meta-chip-view");
        
        metaHBox.getChildren().addAll(timeLabel, boardLabel, likeCountLabel, commentCountLabel, viewCountLabel);
        
        // 内容
        String content = (String) postData.get("content");
        if (content != null && content.length() > 150) {
            content = content.substring(0, 150) + "...";
        }
        Label contentLabel = new Label(content != null ? content : "");
        contentLabel.setStyle("-fx-text-fill: #666; -fx-wrap-text: true;");
        contentLabel.setWrapText(true);
        
        postBox.getChildren().addAll(titleLabel, metaHBox, contentLabel);
        
        // 获取帖子 ID 用于跳转
        Object idObj = postData.get("id");
        if (idObj instanceof Number) {
            Long postId = ((Number) idObj).longValue();
            postBox.setOnMouseClicked(event -> openPostDetail(postId));
        }
        
        postListVBox.getChildren().add(postBox);
    }

    private Label createMetricChip(String text, String accentClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll("post-meta-chip", accentClass);
        return label;
    }
    
    private void openPostDetail(Long postId) {
        if (AppStore.getMainFrameController() != null) {
            try {
                javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(com.teach.javafx.MainApplication.class.getResource("post-detail.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 1024, 768);
                PostDetailController controller = fxmlLoader.getController();
                controller.setPostId(postId);
                
                String tabName = "post-detail-" + postId;
                AppStore.getMainFrameController().changeContentWithScene(tabName, "帖子详情", scene, controller);
            } catch (Exception e) {
                e.printStackTrace();
                showErrorAlert("打开帖子详情失败");
            }
        }
    }
    
    private void handleBack() {
        if (AppStore.getMainFrameController() != null) {
            AppStore.getMainFrameController().closeCurrentTab();
        }
    }
    
    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void openPrivateChat() {
        if (currentUserId == null) {
            showErrorAlert("请先登录");
            return;
        }
        
        if (!isFollowing) {
            showErrorAlert("请先关注该用户才能发起私信");
            return;
        }
        
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getOrCreateConversation(userId);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> data = task.getValue();
                if (data != null) {
                    Long conversationId = null;
                    Object convIdObj = data.get("conversationId");
                    if (convIdObj instanceof Number) {
                        conversationId = ((Number) convIdObj).longValue();
                    }
                    
                    if (conversationId != null && AppStore.getMainFrameController() != null) {
                        try {
                            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                                com.teach.javafx.MainApplication.class.getResource("chat-view.fxml"));
                            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 800, 600);
                            ChatViewController controller = fxmlLoader.getController();
                            controller.setConversation(conversationId, currentProfileData);
                            
                            String tabName = "chat-" + conversationId;
                            String nickname = currentProfileData != null && currentProfileData.get("nickname") != null
                                ? String.valueOf(currentProfileData.get("nickname"))
                                : "用户";
                            AppStore.getMainFrameController().changeContentWithScene(
                                tabName, "与" + nickname + "聊天", scene, controller);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showErrorAlert("打开私信页面失败");
                        }
                    }
                } else {
                    showErrorAlert("操作失败");
                }
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> showErrorAlert("操作失败: " + event.getSource().getException().getMessage()));
        });
        
        new Thread(task).start();
    }
}
