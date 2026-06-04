package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.Board;
import com.teach.javafx.models.PageResult;
import com.teach.javafx.models.Post;
import com.teach.javafx.models.User;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.util.FollowStateManager;
import com.teach.javafx.util.NicknameStyleUtil;
import com.teach.javafx.util.PrivilegeCache;
import com.teach.javafx.util.SettingsManager;
import com.teach.javafx.models.AppSettings;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PostListController extends ToolController {

    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private ComboBox<Board> boardComboBox;
    @FXML
    private ComboBox<String> sortComboBox;
    @FXML
    private TextField keywordTextField;
    @FXML
    private Button searchButton;
    @FXML
    private Button publishButton;
    @FXML
    private Button refreshButton;
    @FXML
    private VBox postListVBox;
    @FXML
    private VBox recommendVBox;
    @FXML
    private VBox recommendContentVBox;
    @FXML
    private GridPane postContentGrid;
    @FXML
    private ColumnConstraints postListColumn;
    @FXML
    private ColumnConstraints recommendColumn;
    @FXML
    private ProgressIndicator refreshProgressIndicator;
    @FXML
    private Label pageInfoLabel;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private TextField jumpPageTextField;
    @FXML
    private Button jumpButton;

    private int currentPageNum = 1;
    private int currentPageSize = 20;
    private int totalPages = 1;
    private Long currentBoardId = null;
    private String currentKeyword = null;
    private String currentSort = "latest";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private User currentUser;
    private Set<Integer> followingUserIds = new HashSet<>();
    private Map<String, Object> recommendationsCache = null;
    private static final double POST_LIST_WITH_RECOMMEND_WIDTH = 70.0;
    private static final double RECOMMEND_WIDTH = 30.0;
    private static final double FULL_POST_LIST_WIDTH = 100.0;
    private static final double CONTENT_GRID_GAP = 18.0;

    @FXML
    public void initialize() {
        searchButton.setOnAction(event -> {
            String input = keywordTextField.getText();
            if (tryOpenPostFromLink(input)) {
                return;
            }
            currentKeyword = input;
            currentPageNum = 1;
            loadPostList();
        });

        keywordTextField.setOnAction(event -> {
            String input = keywordTextField.getText();
            if (tryOpenPostFromLink(input)) {
                return;
            }
            currentKeyword = input;
            currentPageNum = 1;
            loadPostList();
        });

        publishButton.setOnAction(event -> openPublishPost());

        refreshButton.setOnAction(event -> {
            // 立即平滑滚动到顶部
            smoothScrollToTop();
            currentPageNum = 1;
            loadPostList(refreshButton);
        });

        prevButton.setOnAction(event -> {
            if (currentPageNum > 1) {
                currentPageNum--;
                loadPostList();
            }
        });

        nextButton.setOnAction(event -> {
            currentPageNum++;
            loadPostList();
        });

        jumpButton.setOnAction(event -> onJumpPageClick());

        boardComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentBoardId = newValue.getId();
            } else {
                currentBoardId = null;
            }
            currentPageNum = 1;
            loadPostList();
        });

        sortComboBox.getItems().addAll(
                "最新发布",
                "最新回复",
                "最多浏览",
                "最多点赞",
                "精华帖优先"
        );
        // 应用设置中的默认排序方式
        AppSettings settings = SettingsManager.getCurrentSettings();
        String defaultSort = settings.getPostSort();
        int sortIndex = sortComboBox.getItems().indexOf(defaultSort);
        if (sortIndex >= 0) {
            sortComboBox.getSelectionModel().select(sortIndex);
            currentSort = mapSortValue(defaultSort);
        } else {
            sortComboBox.getSelectionModel().selectFirst();
        }
        sortComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentSort = mapSortValue(newValue);
            } else {
                currentSort = "latest";
            }
            currentPageNum = 1;
            loadPostList();
        });

        publishButton.setVisible(false);

        loadBoardList();
        loadCurrentUser();
        loadRecommendations();
        loadPostList();
    }

    private String mapSortValue(String displayValue) {
        switch (displayValue) {
            case "最新发布":
                return "latest";
            case "最新回复":
                return "latest_reply";
            case "最多浏览":
                return "most_view";
            case "最多点赞":
                return "most_like";
            case "精华帖优先":
                return "featured_first";
            default:
                return "latest";
        }
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
                updatePublishButtonVisibility();
                loadFollowingList();
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                updatePublishButtonVisibility();
            });
        });

        new Thread(task).start();
    }

    private void loadFollowingList() {
        Task<List<User>> task = new Task<List<User>>() {
            @Override
            protected List<User> call() {
                return HttpRequestUtil.getMyFollowingList();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                List<User> followingList = task.getValue();
                followingUserIds.clear();
                if (followingList != null) {
                    for (User user : followingList) {
                        if (user.getPersonId() != null) {
                            followingUserIds.add(user.getPersonId().intValue());
                            FollowStateManager.getInstance().setFollowState(user.getPersonId().longValue(), true);
                        }
                    }
                }
            });
        });

        new Thread(task).start();
    }

    private void loadRecommendations() {
        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getRecommendations();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                recommendationsCache = task.getValue();
                addRecommendationSection();
            });
        });

        new Thread(task).start();
    }

    private void updatePublishButtonVisibility() {
        boolean isLoggedIn = currentUser != null;
        boolean isBanned = isLoggedIn && Boolean.TRUE.equals(currentUser.getIsBanned());
        boolean canPostByLevel = PrivilegeCache.getInstance().canPost();
        publishButton.setVisible(isLoggedIn);
        publishButton.setDisable(isBanned || !canPostByLevel);
        if (isBanned) {
            publishButton.setText("已被禁言");
            publishButton.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-border-color: #fecaca; -fx-cursor: not-allowed;");
        } else if (isLoggedIn && !isBanned && !canPostByLevel) {
            publishButton.setText("等级不足");
            publishButton.setStyle("-fx-background-color: #d9d9d9; -fx-text-fill: #999; -fx-cursor: not-allowed;");
        } else {
            publishButton.setText("发帖");
            publishButton.setStyle("");
        }
    }

    private void loadBoardList() {
        Task<List<Board>> task = new Task<List<Board>>() {
            @Override
            protected List<Board> call() {
                return HttpRequestUtil.getBoardList();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                List<Board> boards = task.getValue();
                if (boards != null) {
                    boardComboBox.getItems().clear();
                    Board allBoard = new Board();
                    allBoard.setId(null);
                    allBoard.setName("全部");
                    boardComboBox.getItems().add(allBoard);
                    boardComboBox.getItems().addAll(boards);
                    
                    // 应用设置中的默认板块
                    AppSettings settings = SettingsManager.getCurrentSettings();
                    String defaultBoardName = settings.getDefaultBoard();
                    boolean found = false;
                    for (Board board : boardComboBox.getItems()) {
                        if (defaultBoardName.equals(board.getName())) {
                            boardComboBox.getSelectionModel().select(board);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        boardComboBox.getSelectionModel().selectFirst();
                    }
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                showError("加载板块列表失败");
            });
        });

        new Thread(task).start();
    }

    public void loadPostList() {
        loadPostList(null);
    }

    private void loadPostList(Button refreshBtn) {
        if (refreshBtn != null) {
            refreshBtn.setDisable(true);
            refreshBtn.setText("刷新中");
        }
        refreshProgressIndicator.setVisible(true);

        Task<PageResult<Post>> task = new Task<PageResult<Post>>() {
            @Override
            protected PageResult<Post> call() {
                return HttpRequestUtil.getPostList(currentBoardId, currentKeyword, currentPageNum, currentPageSize, currentSort);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                if (refreshBtn != null) {
                    refreshBtn.setDisable(false);
                    refreshBtn.setText("刷新");
                }
                refreshProgressIndicator.setVisible(false);

                PageResult<Post> pageResult = task.getValue();
                postListVBox.getChildren().clear();

                if (pageResult != null && pageResult.getList() != null && !pageResult.getList().isEmpty()) {
                    for (Post post : pageResult.getList()) {
                        addPostCard(post);
                    }

                    long total = pageResult.getTotal() != null ? pageResult.getTotal() : 0;
                    totalPages = (int) Math.ceil((double) total / currentPageSize);
                    int displayTotalPages = Math.max(totalPages, 1);
                    pageInfoLabel.setText("共 " + total + " 条，第 " + currentPageNum + " / " + displayTotalPages + " 页");

                    prevButton.setDisable(currentPageNum <= 1);
                    nextButton.setDisable(currentPageNum >= displayTotalPages);
                } else {
                    Label emptyLabel = new Label("暂无帖子~");
                    emptyLabel.setStyle("-fx-text-fill: #999; -fx-padding: 40 0; -fx-font-size: 14;");
                    emptyLabel.setMaxWidth(Double.MAX_VALUE);
                    emptyLabel.setAlignment(javafx.geometry.Pos.CENTER);
                    postListVBox.getChildren().add(emptyLabel);

                    pageInfoLabel.setText("共 0 条，第 1 / 1 页");
                    totalPages = 1;
                    prevButton.setDisable(true);
                    nextButton.setDisable(true);
                }
                
                // 只有非刷新按钮调用（刷新按钮已在点击时滚动）才需要滚动到顶部
                if (refreshBtn == null) {
                    smoothScrollToTop();
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                if (refreshBtn != null) {
                    refreshBtn.setDisable(false);
                    refreshBtn.setText("刷新");
                }
                refreshProgressIndicator.setVisible(false);
                showError("加载帖子列表失败");
            });
        });

        new Thread(task).start();
    }

    private void addPostCard(Post post) {
        VBox card = new VBox(6);
        card.getStyleClass().add("profile-card");

        // 根据帖子类型设置不同的样式
        boolean isTop = post.getIsTop() != null && post.getIsTop();
        boolean isFeatured = post.getIsFeatured() != null && post.getIsFeatured();

        String baseStyle = "-fx-padding: 16; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;";
        if (isTop && isFeatured) {
            // 既是置顶又是精华
            card.setStyle(baseStyle + "-fx-background-color: linear-gradient(to right, #fef3c7, #fce7f3); -fx-border-color: #f59e0b, #ec4899; -fx-border-width: 2;");
        } else if (isTop) {
            // 置顶帖
            card.setStyle(baseStyle + "-fx-background-color: #fef3c7; -fx-border-color: #f59e0b; -fx-border-width: 2;");
        } else if (isFeatured) {
            // 精华帖
            card.setStyle(baseStyle + "-fx-background-color: #fce7f3; -fx-border-color: #ec4899; -fx-border-width: 2;");
        } else {
            // 普通帖
            card.setStyle(baseStyle + "-fx-border-color: #e5e7eb;");
        }

        // 标题行
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // 帖子类型标签
        if (isTop || isFeatured) {
            HBox tagBox = new HBox(6);
            tagBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            if (isTop) {
                Label topLabel = new Label("置顶");
                topLabel.setStyle("-fx-text-fill: #d97706; -fx-font-size: 11; -fx-font-weight: bold; -fx-background-color: #f59e0b; -fx-text-fill: white; -fx-padding: 2 10; -fx-background-radius: 12;");
                tagBox.getChildren().add(topLabel);
            }
            if (isFeatured) {
                Label featuredLabel = new Label("精华");
                featuredLabel.setStyle("-fx-text-fill: #db2777; -fx-font-size: 11; -fx-font-weight: bold; -fx-background-color: #ec4899; -fx-text-fill: white; -fx-padding: 2 10; -fx-background-radius: 12;");
                tagBox.getChildren().add(featuredLabel);
            }
            titleRow.getChildren().add(tagBox);
        }

        String title = post.getTitle() != null ? post.getTitle() : "";
        Label titleLabel = new Label(title);
        String titleColor = (isTop || isFeatured) ? "#0f172a" : "#1a1a2e";
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: " + titleColor + "; -fx-wrap-text: true;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // 审核状态标签
        String moderationStatus = post.getModerationStatus();
        if (moderationStatus != null && !"pass".equals(moderationStatus)) {
            Label statusLabel = new Label(post.getModerationStatusText());
            if ("pending".equals(moderationStatus) || "manual".equals(moderationStatus)) {
                statusLabel.setStyle("-fx-text-fill: #d97706; -fx-font-size: 12; -fx-background-color: #fef3c7; -fx-padding: 2 8; -fx-background-radius: 10;");
            } else if ("reject".equals(moderationStatus)) {
                statusLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12; -fx-background-color: #fee2e2; -fx-padding: 2 8; -fx-background-radius: 10;");
            }
            titleRow.getChildren().addAll(titleLabel, statusLabel);
        } else {
            titleRow.getChildren().add(titleLabel);
        }

        // 作者与元数据行
        HBox metaRow = new HBox(12);
        metaRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // 作者头像
        ImageView avatarView = new ImageView();
        avatarView.setFitWidth(24);
        avatarView.setFitHeight(24);
        avatarView.setPreserveRatio(true);
        avatarView.setSmooth(true);
        // 正常加载用户自己设置的头像
        String avatarUrl = post.getAuthorAvatarUrl();
        String fullAvatarUrl = avatarUrl.startsWith("/") ? HttpRequestUtil.serverUrl + avatarUrl : avatarUrl;
        try {
            Image image = new Image(fullAvatarUrl, true);
            avatarView.setImage(image);
        } catch (Exception e) {
            // ignore
        }

        // 作者昵称
        String nickname = post.getAuthorNickname() != null ? post.getAuthorNickname() : "未知用户";
        Label nicknameLabel = new Label(nickname);
        nicknameLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #4b5563;");
        NicknameStyleUtil.applyStyle(nicknameLabel, post.getAuthorNicknameStyle());

        Long userId = post.getUserId();
        if (userId != null && followingUserIds.contains(userId.intValue())) {
            Label followedLabel = new Label("已关注");
            followedLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11; -fx-background-color: #d1fae5; -fx-padding: 1 6; -fx-background-radius: 8;");
            metaRow.getChildren().addAll(avatarView, nicknameLabel, followedLabel);
        } else {
            metaRow.getChildren().addAll(avatarView, nicknameLabel);
        }

        // 板块标签
        String boardName = post.getBoardName();
        if (boardName != null && !boardName.isBlank()) {
            Label boardLabel = new Label(boardName);
            boardLabel.setStyle("-fx-text-fill: #6366f1; -fx-font-size: 11; -fx-background-color: #eef2ff; -fx-padding: 1 8; -fx-background-radius: 8;");
            metaRow.getChildren().add(boardLabel);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        metaRow.getChildren().add(spacer);

        // 创建时间
        Date createTime = post.getCreateTime();
        if (createTime != null) {
            Label timeLabel = new Label(dateFormat.format(createTime));
            timeLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12;");
            metaRow.getChildren().add(timeLabel);
        }

        // 统计行
        HBox statsRow = new HBox(18);
        statsRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        int likeCount = post.getLikeCount() != null ? post.getLikeCount() : 0;
        Label likeLabel = new Label("赞 " + likeCount);
        likeLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12;");

        int commentCount = post.getCommentCount() != null ? post.getCommentCount() : 0;
        Label commentLabel = new Label("评 " + commentCount);
        commentLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12;");

        statsRow.getChildren().addAll(likeLabel, commentLabel);

        card.getChildren().addAll(titleRow, metaRow, statsRow);

        // 点击打开帖子详情
        card.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                openPostDetail(post.getId());
            }
        });

        postListVBox.getChildren().add(card);
    }

    private void openPostDetail(Long postId) {
        if (AppStore.getMainFrameController() != null) {
            try {
                javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(MainApplication.class.getResource("post-detail.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 1024, 768);
                PostDetailController controller = fxmlLoader.getController();
                controller.setPostId(postId);

                String tabName = "post-detail-" + postId;
                AppStore.getMainFrameController().changeContentWithScene(tabName, "帖子详情", scene, controller);
            } catch (Exception e) {
                e.printStackTrace();
                showError("打开帖子详情失败");
            }
        }
    }

    private void openPublishPost() {
        if (currentUser != null && Boolean.TRUE.equals(currentUser.getIsBanned())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("账号已被禁言");
            alert.setHeaderText(null);
            alert.setContentText("您的账号已被禁言，无法发布新帖子。\n如需帮助，请联系管理员。");
            alert.showAndWait();
            return;
        }
        if (AppStore.getMainFrameController() != null) {
            AppStore.getMainFrameController().changeContent("post-publish", "发布帖子");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean tryOpenPostFromLink(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        String trimmedInput = input.trim();

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("bbs://post/(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(trimmedInput);

        if (matcher.find()) {
            try {
                Long postId = Long.parseLong(matcher.group(1));
                openPostDetail(postId);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return false;
    }

    private void addRecommendationSection() {
        if (recommendationsCache == null) {
            setRecommendAreaVisible(false);
            return;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> browseRecommendations = (List<Map<String, Object>>) recommendationsCache.get("browseRecommendations");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> followingPosts = (List<Map<String, Object>>) recommendationsCache.get("followingPosts");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> similarPosts = (List<Map<String, Object>>) recommendationsCache.get("similarPosts");

        if ((browseRecommendations == null || browseRecommendations.isEmpty()) &&
            (followingPosts == null || followingPosts.isEmpty()) &&
            (similarPosts == null || similarPosts.isEmpty())) {
            setRecommendAreaVisible(false);
            return;
        }

        recommendContentVBox.getChildren().clear();

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        if (browseRecommendations != null && !browseRecommendations.isEmpty()) {
            Tab browseTab = new Tab("根据浏览历史");
            browseTab.setContent(createRecommendList(browseRecommendations));
            browseTab.setClosable(false);
            tabPane.getTabs().add(browseTab);
        }

        if (followingPosts != null && !followingPosts.isEmpty()) {
            Tab followingTab = new Tab("关注的人");
            followingTab.setContent(createRecommendList(followingPosts));
            followingTab.setClosable(false);
            tabPane.getTabs().add(followingTab);
        }

        if (similarPosts != null && !similarPosts.isEmpty()) {
            Tab similarTab = new Tab("相似帖子");
            similarTab.setContent(createRecommendList(similarPosts));
            similarTab.setClosable(false);
            tabPane.getTabs().add(similarTab);
        }

        tabPane.getSelectionModel().selectFirst();
        recommendContentVBox.getChildren().add(tabPane);
        setRecommendAreaVisible(true);
    }

    private void setRecommendAreaVisible(boolean visible) {
        recommendVBox.setVisible(visible);
        recommendVBox.setManaged(visible);
        if (postContentGrid != null) {
            postContentGrid.setHgap(visible ? CONTENT_GRID_GAP : 0.0);
        }
        if (postListColumn != null && recommendColumn != null) {
            postListColumn.setPercentWidth(visible ? POST_LIST_WITH_RECOMMEND_WIDTH : FULL_POST_LIST_WIDTH);
            recommendColumn.setPercentWidth(visible ? RECOMMEND_WIDTH : 0.0);
        }
    }

    private VBox createRecommendList(List<Map<String, Object>> posts) {
        VBox container = new VBox(10);
        container.setStyle("-fx-padding: 12; -fx-background-color: #fafafa;");

        for (Map<String, Object> postData : posts) {
            VBox card = createRecommendCard(postData);
            container.getChildren().add(card);
        }

        return container;
    }

    private VBox createRecommendCard(Map<String, Object> postData) {
        VBox card = new VBox(8);
        card.setStyle("-fx-padding: 14; -fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e5e7eb; -fx-border-radius: 10; -fx-cursor: hand;");

        Long postId = postData.get("id") instanceof Number ? ((Number) postData.get("id")).longValue() : null;
        String title = postData.get("title") != null ? postData.get("title").toString() : "";
        String authorNickname = postData.get("authorNickname") != null ? postData.get("authorNickname").toString() : "";
        String boardName = postData.get("boardName") != null ? postData.get("boardName").toString() : "";
        Integer likeCount = postData.get("likeCount") instanceof Number ? ((Number) postData.get("likeCount")).intValue() : 0;
        Integer commentCount = postData.get("commentCount") instanceof Number ? ((Number) postData.get("commentCount")).intValue() : 0;

        HBox headerRow = new HBox(10);
        headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        titleLabel.setMaxWidth(500);
        titleLabel.setWrapText(true);
        titleLabel.setEllipsisString("...");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        headerRow.getChildren().add(titleLabel);

        HBox metaRow = new HBox(15);
        metaRow.setStyle("-fx-padding: 3 0 0 0;");

        if (!authorNickname.isEmpty()) {
            Label authorLabel = new Label(authorNickname);
            authorLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #6366f1;");
            metaRow.getChildren().add(authorLabel);
        }

        if (!boardName.isEmpty()) {
            Label boardLabel = new Label(boardName);
            boardLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #9ca3af;");
            metaRow.getChildren().add(boardLabel);
        }

        Label statsLabel = new Label("👍 " + likeCount + "  💬 " + commentCount);
        statsLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #9ca3af;");
        metaRow.getChildren().add(statsLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        metaRow.getChildren().add(spacer);

        card.getChildren().addAll(headerRow, metaRow);

        final Long finalPostId = postId;
        card.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                if (finalPostId != null) {
                    openPostDetail(finalPostId);
                }
            }
        });

        card.setOnMouseEntered(event -> {
            card.setStyle("-fx-padding: 14; -fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-border-color: #c5cae9; -fx-border-radius: 10; -fx-cursor: hand;");
        });

        card.setOnMouseExited(event -> {
            card.setStyle("-fx-padding: 14; -fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e5e7eb; -fx-border-radius: 10; -fx-cursor: hand;");
        });

        return card;
    }

    /**
     * 平滑滚动到ScrollPane顶部
     */
    private void smoothScrollToTop() {
        if (mainScrollPane == null) {
            return;
        }

        // 立即设置滚动位置为0
        mainScrollPane.setVvalue(0);

        // 禁用pannable防止滚动
        mainScrollPane.setPannable(false);

        // 创建平滑动画：将内容向上移动一小段再回到原位，产生视觉反馈
        javafx.scene.Node content = mainScrollPane.getContent();
        if (content != null) {
            // 获取当前垂直滚动位置
            double currentScrollY = mainScrollPane.getVvalue();

            // 创建平滑动画
            Timeline timeline = new Timeline();
            timeline.setCycleCount(1);

            // 动画：短暂向上抖动然后回到原位
            KeyFrame keyFrame = new KeyFrame(
                javafx.util.Duration.millis(150),
                new KeyValue(content.translateYProperty(), 20),
                new KeyValue(mainScrollPane.vvalueProperty(), 0)
            );
            timeline.getKeyFrames().add(keyFrame);

            // 第一阶段动画完成后平滑回到原位
            timeline.setOnFinished(event1 -> {
                Timeline returnTimeline = new Timeline();
                returnTimeline.setCycleCount(1);

                KeyFrame returnFrame = new KeyFrame(
                    javafx.util.Duration.millis(200),
                    new KeyValue(content.translateYProperty(), 0),
                    new KeyValue(mainScrollPane.vvalueProperty(), 0)
                );
                returnTimeline.getKeyFrames().add(returnFrame);

                returnTimeline.setOnFinished(event2 -> {
                    // 动画完成后恢复pannable
                    mainScrollPane.setPannable(true);
                    content.setTranslateY(0);
                });

                returnTimeline.play();
            });

            timeline.play();
        }
    }

    /**
     * 跳转页面处理
     */
    @FXML
    private void onJumpPageClick() {
        if (jumpPageTextField == null || jumpPageTextField.getText() == null) {
            return;
        }
        
        try {
            int targetPage = Integer.parseInt(jumpPageTextField.getText().trim());
            
            if (targetPage < 1) {
                targetPage = 1;
            }
            if (targetPage > totalPages) {
                targetPage = totalPages;
            }
            
            if (targetPage != currentPageNum) {
                currentPageNum = targetPage;
                loadPostList();
            }
            
            jumpPageTextField.setText("");
        } catch (NumberFormatException e) {
            // 输入非数字，忽略
        }
    }
}
