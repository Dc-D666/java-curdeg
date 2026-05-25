package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.PageResult;
import com.teach.javafx.models.Post;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.util.NicknameStyleUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyPostsController extends ToolController {
    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private VBox postListVBox;
    @FXML
    private Label totalLabel;
    @FXML
    private Label pageInfoLabel;
    @FXML
    private Button refreshButton;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;

    private int currentPageNum = 1;
    private final int currentPageSize = 10;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        totalLabel.setVisible(false);
        totalLabel.setManaged(false);
        prevButton.setOnAction(event -> onPrevPage());
        nextButton.setOnAction(event -> onNextPage());
        refreshButton.setOnAction(event -> {
            currentPageNum = 1;
            loadPosts(refreshButton);
        });
        loadPosts();
    }

    public void loadPosts() {
        loadPosts(null);
    }

    private void loadPosts(Button refreshBtn) {
        if (refreshBtn != null) {
            refreshBtn.setDisable(true);
            refreshBtn.setText("刷新中");
        }

        Task<PageResult<Post>> task = new Task<>() {
            @Override
            protected PageResult<Post> call() {
                return HttpRequestUtil.getMyPosts(currentPageNum, currentPageSize);
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            if (refreshBtn != null) {
                refreshBtn.setDisable(false);
                refreshBtn.setText("刷新");
            }

            PageResult<Post> pageResult = task.getValue();
            postListVBox.getChildren().clear();
            if (pageResult != null && pageResult.getList() != null) {
                for (Post post : pageResult.getList()) {
                    addPostCard(post);
                }

                long total = pageResult.getTotal() != null ? pageResult.getTotal() : 0;
                int totalPages = (int) Math.ceil((double) total / currentPageSize);
                int displayTotalPages = Math.max(totalPages, 1);
                pageInfoLabel.setText("共 " + total + " 条，第 " + currentPageNum + " / " + displayTotalPages + " 页");

                prevButton.setDisable(currentPageNum <= 1);
                nextButton.setDisable(currentPageNum >= displayTotalPages);
            }
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            if (refreshBtn != null) {
                refreshBtn.setDisable(false);
                refreshBtn.setText("刷新");
            }
            showError("加载帖子列表失败");
        }));

        new Thread(task).start();
    }

    private void addPostCard(Post post) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("profile-card", "feed-card");

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(post.getTitle() != null ? post.getTitle() : "");
        titleLabel.getStyleClass().add("feed-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleRow.getChildren().add(titleLabel);

        String moderationStatus = post.getModerationStatus();
        if (moderationStatus != null && !"pass".equals(moderationStatus)) {
            Label statusLabel = new Label(isReportedDownPost(post) ? "举报下架" : post.getModerationStatusText());
            if ("pending".equals(moderationStatus) || "manual".equals(moderationStatus)) {
                statusLabel.getStyleClass().add("warning-chip");
            } else {
                statusLabel.getStyleClass().add("danger-chip");
            }
            titleRow.getChildren().add(statusLabel);
        }

        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        String nickname = post.getAuthorNickname() != null ? post.getAuthorNickname() : "未知用户";
        Label authorLabel = new Label(nickname);
        authorLabel.getStyleClass().add("feed-author");
        NicknameStyleUtil.applyStyle(authorLabel, post.getAuthorNicknameStyle());
        metaRow.getChildren().add(authorLabel);

        if (post.getBoardName() != null && !post.getBoardName().isBlank()) {
            Label boardLabel = new Label(post.getBoardName());
            boardLabel.getStyleClass().add("post-board-chip");
            metaRow.getChildren().add(boardLabel);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        metaRow.getChildren().add(spacer);

        if (post.getCreateTime() != null) {
            Label timeLabel = new Label(dateFormat.format(post.getCreateTime()));
            timeLabel.getStyleClass().add("post-meta-time");
            metaRow.getChildren().add(timeLabel);
        }

        Label excerptLabel = new Label(buildExcerpt(post.getContent()));
        excerptLabel.getStyleClass().add("feed-excerpt");
        excerptLabel.setWrapText(true);

        HBox statsRow = new HBox(8);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.getChildren().addAll(
                createMetaChip("赞 " + safeNumber(post.getLikeCount()), "post-meta-chip", "post-meta-chip-like"),
                createMetaChip("评 " + safeNumber(post.getCommentCount()), "post-meta-chip", "post-meta-chip-comment")
        );

        card.getChildren().addAll(titleRow, metaRow, excerptLabel, statsRow);
        card.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                openPostDetail(post.getId());
            }
        });

        postListVBox.getChildren().add(card);
    }

    private Label createMetaChip(String text, String... styleClasses) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styleClasses);
        return label;
    }

    private String buildExcerpt(String content) {
        if (content == null || content.isBlank()) {
            return "暂无内容摘要";
        }
        String normalized = content.replace("\r", " ").replace("\n", " ").trim();
        if (normalized.length() > 120) {
            return normalized.substring(0, 120) + "...";
        }
        return normalized;
    }

    private int safeNumber(Integer value) {
        return value != null ? value : 0;
    }

    public void onPrevPage() {
        if (currentPageNum > 1) {
            currentPageNum--;
            loadPosts();
        }
    }

    public void onNextPage() {
        currentPageNum++;
        loadPosts();
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isReportedDownPost(Post post) {
        return post != null && post.getStatus() != null && post.getStatus() == 0
                && "reject".equals(post.getModerationStatus());
    }
}
