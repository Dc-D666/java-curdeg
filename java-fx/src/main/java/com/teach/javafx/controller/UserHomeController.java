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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Map;

public class UserHomeController extends ToolController {
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
    
    private int currentPage = 1;
    private int totalPages = 1;
    
    @javafx.fxml.FXML
    public void initialize() {
        backButton.setOnAction(event -> handleBack());
        refreshButton.setOnAction(event -> loadUserPosts());
        followButton.setOnAction(event -> toggleFollow());
        messageButton.setOnAction(event -> showInfoAlert("私信功能开发中..."));
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
                avatarImageView.setImage(null);
            }
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
        } else {
            followButton.setText("关注");
            followButton.setStyle("");
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
        postBox.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #e8e8e8; -fx-border-width: 1; -fx-border-radius: 8; -fx-cursor: hand;");
        
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
        timeLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");
        
        // 板块名称
        String boardName = (String) postData.get("boardName");
        Label boardLabel = new Label(boardName != null ? boardName : "");
        boardLabel.setStyle("-fx-background-color: #e6f7ff; -fx-text-fill: #1890ff; -fx-padding: 2 8; -fx-background-radius: 4; -fx-font-size: 12;");
        
        // 点赞数
        Object likeCountObj = postData.get("likeCount");
        int likeCount = likeCountObj instanceof Number ? ((Number) likeCountObj).intValue() : 0;
        Label likeCountLabel = new Label("❤️ " + likeCount);
        likeCountLabel.setStyle("-fx-text-fill: #f5222d; -fx-font-size: 12;");
        
        // 评论数
        Object commentCountObj = postData.get("commentCount");
        int commentCount = commentCountObj instanceof Number ? ((Number) commentCountObj).intValue() : 0;
        Label commentCountLabel = new Label("💬 " + commentCount);
        commentCountLabel.setStyle("-fx-text-fill: #1890ff; -fx-font-size: 12;");
        
        // 浏览数
        Object viewCountObj = postData.get("viewCount");
        int viewCount = viewCountObj instanceof Number ? ((Number) viewCountObj).intValue() : 0;
        Label viewCountLabel = new Label("👁️ " + viewCount);
        viewCountLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");
        
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
}
