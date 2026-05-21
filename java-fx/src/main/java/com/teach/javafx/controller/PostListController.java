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
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostListController extends ToolController {

    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private ComboBox<Board> boardComboBox;
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
    private ProgressIndicator refreshProgressIndicator;
    @FXML
    private Label pageInfoLabel;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;

    private int currentPageNum = 1;
    private int currentPageSize = 20;
    private Long currentBoardId = null;
    private String currentKeyword = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private User currentUser;
    private Set<Integer> followingUserIds = new HashSet<>();

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

        boardComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentBoardId = newValue.getId();
            } else {
                currentBoardId = null;
            }
            currentPageNum = 1;
            loadPostList();
        });

        publishButton.setVisible(false);

        loadBoardList();
        loadCurrentUser();
        loadPostList();
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

    private void updatePublishButtonVisibility() {
        boolean isLoggedIn = currentUser != null;
        boolean isBanned = isLoggedIn && Boolean.TRUE.equals(currentUser.getIsBanned());
        boolean canPostByLevel = PrivilegeCache.getInstance().canPost();
        publishButton.setVisible(isLoggedIn && !isBanned && canPostByLevel);
        if (isLoggedIn && !isBanned && !canPostByLevel) {
            publishButton.setText("等级不足");
            publishButton.setStyle("-fx-background-color: #d9d9d9; -fx-text-fill: #999;");
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
                    boardComboBox.getSelectionModel().selectFirst();
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
                return HttpRequestUtil.getPostList(currentBoardId, currentKeyword, currentPageNum, currentPageSize);
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
                    int totalPages = (int) Math.ceil((double) total / currentPageSize);
                    pageInfoLabel.setText("共 " + total + " 条，第 " + currentPageNum + " / " + totalPages + " 页");

                    prevButton.setDisable(currentPageNum <= 1);
                    nextButton.setDisable(currentPageNum >= totalPages);
                } else {
                    Label emptyLabel = new Label("暂无帖子~");
                    emptyLabel.setStyle("-fx-text-fill: #999; -fx-padding: 40 0; -fx-font-size: 14;");
                    emptyLabel.setMaxWidth(Double.MAX_VALUE);
                    emptyLabel.setAlignment(javafx.geometry.Pos.CENTER);
                    postListVBox.getChildren().add(emptyLabel);

                    pageInfoLabel.setText("共 0 条");
                    prevButton.setDisable(true);
                    nextButton.setDisable(true);
                }

                mainScrollPane.setVvalue(0);
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
        card.setStyle("-fx-padding: 16; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");

        // 标题行
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String title = post.getTitle() != null ? post.getTitle() : "";
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1a1a2e; -fx-wrap-text: true;");
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
        String avatarUrl = post.getAuthorAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            try {
                String fullAvatarUrl = avatarUrl.startsWith("/") ? HttpRequestUtil.serverUrl + avatarUrl : avatarUrl;
                Image image = new Image(fullAvatarUrl, true);
                avatarView.setImage(image);
            } catch (Exception e) {
                // ignore
            }
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
}
