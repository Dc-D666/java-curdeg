package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.PageResult;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MyFollowersController extends ToolController {
    @FXML
    private VBox followerCardList;
    @FXML
    private Label followerTotalValueLabel;
    @FXML
    private Label followerWeekValueLabel;
    @FXML
    private Label followerTopUserValueLabel;
    @FXML
    private Label followerActiveRateValueLabel;
    @FXML
    private Label followerPageInfoLabel;
    @FXML
    private Button followerPrevButton;
    @FXML
    private Button followerNextButton;
    @FXML
    private Button followerRefreshButton;

    private static final String DEFAULT_AVATAR = "https://img.phb123.com/uploads/allimg/220607/810-22060G55A40-L.jpeg";
    private static final DateTimeFormatter FOLLOW_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int followerCurrentPageNum = 1;
    private final int currentPageSize = 10;

    @FXML
    public void initialize() {
        followerPrevButton.setOnAction(event -> onFollowerPrevPage());
        followerNextButton.setOnAction(event -> onFollowerNextPage());
        followerRefreshButton.setOnAction(event -> {
            followerCurrentPageNum = 1;
            loadFollowers(followerRefreshButton);
        });
        loadFollowers();
    }

    public void loadFollowers() {
        loadFollowers(null);
    }

    private void loadFollowers(Button refreshBtn) {
        if (refreshBtn != null) {
            refreshBtn.setDisable(true);
            refreshBtn.setText("刷新中");
        }

        Task<PageResult<Map<String, Object>>> task = new Task<PageResult<Map<String, Object>>>() {
            @Override
            protected PageResult<Map<String, Object>> call() {
                return HttpRequestUtil.getMyFollowerPage(followerCurrentPageNum, currentPageSize);
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            if (refreshBtn != null) {
                refreshBtn.setDisable(false);
                refreshBtn.setText("刷新");
            }
            renderFollowerPage(task.getValue());
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            if (refreshBtn != null) {
                refreshBtn.setDisable(false);
                refreshBtn.setText("刷新");
            }
            showError("加载粉丝列表失败");
        }));

        new Thread(task).start();
    }

    private void renderFollowerPage(PageResult<Map<String, Object>> pageResult) {
        followerCardList.getChildren().clear();

        List<Map<String, Object>> users = pageResult != null ? pageResult.getList() : null;
        long total = pageResult != null && pageResult.getTotal() != null ? pageResult.getTotal() : 0;
        int totalPages = Math.max((int) Math.ceil((double) total / currentPageSize), 1);

        updateSummary(users, total);
        followerPageInfoLabel.setText("共 " + total + " 条，第 " + followerCurrentPageNum + " / " + totalPages + " 页");
        followerPrevButton.setDisable(followerCurrentPageNum <= 1);
        followerNextButton.setDisable(followerCurrentPageNum >= totalPages);

        if (users == null || users.isEmpty()) {
            followerCardList.getChildren().add(createEmptyState());
            return;
        }

        for (Map<String, Object> user : users) {
            followerCardList.getChildren().add(createUserCard(user));
        }
    }

    private void updateSummary(List<Map<String, Object>> users, long total) {
        int weekCount = 0;
        int activeCount = 0;
        String topUser = "暂无";

        if (users != null && !users.isEmpty()) {
            LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
            for (Map<String, Object> user : users) {
                LocalDateTime followTime = parseFollowTime(user.get("followTime"));
                if (followTime != null && !followTime.isBefore(weekStart)) {
                    weekCount++;
                }
                if (toInt(user.get("postCount")) > 0 || toInt(user.get("commentCount")) > 0) {
                    activeCount++;
                }
            }

            topUser = users.stream()
                    .max(Comparator.comparingInt(user -> toInt(user.get("postCount")) + toInt(user.get("commentCount"))))
                    .map(user -> stringValue(user.get("nickname"), "暂无"))
                    .orElse("暂无");
        }

        int activeRate = users == null || users.isEmpty() ? 0 : Math.round(activeCount * 100f / users.size());
        followerTotalValueLabel.setText(total + " 人");
        followerWeekValueLabel.setText(weekCount + " 人");
        followerTopUserValueLabel.setText(topUser);
        followerActiveRateValueLabel.setText(activeRate + "%");
    }

    private VBox createUserCard(Map<String, Object> user) {
        VBox card = new VBox(8);
        card.getStyleClass().add("following-user-card");

        HBox mainRow = new HBox(14);
        mainRow.setAlignment(Pos.CENTER_LEFT);

        ImageView avatar = createAvatar(user);
        VBox identityBox = new VBox(6);
        identityBox.setAlignment(Pos.CENTER_LEFT);

        Label nickname = new Label(stringValue(user.get("nickname"), "用户主页"));
        nickname.getStyleClass().add("following-user-name");

        Label signature = new Label("个性签名：" + signatureText(user.get("signature")));
        signature.getStyleClass().add("following-user-signature");
        signature.setWrapText(true);

        identityBox.getChildren().addAll(nickname, signature);
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Boolean isFollowed = (Boolean) user.get("isFollowed");
        Button followButton = new Button(isFollowed != null && isFollowed ? "取消关注" : "关注");
        followButton.getStyleClass().add("following-action-button");
        if (isFollowed != null && isFollowed) {
            followButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
        }
        followButton.setOnAction(event -> toggleFollow(user));

        mainRow.getChildren().addAll(avatar, identityBox, spacer, followButton);

        Label meta = new Label(
                "发帖数：" + toInt(user.get("postCount")) +
                " | 粉丝数：" + toInt(user.get("followerCount")) +
                " | 关注时间：" + stringValue(user.get("followTime"), "-")
        );
        meta.getStyleClass().add("following-user-meta");

        card.getChildren().addAll(mainRow, meta);
        card.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                openUserHome(toLong(user.get("userId")), stringValue(user.get("nickname"), "用户主页"));
            }
        });

        return card;
    }

    private ImageView createAvatar(Map<String, Object> user) {
        String avatarUrl = stringValue(user.get("avatarUrl"), DEFAULT_AVATAR);
        if (!avatarUrl.startsWith("http")) {
            avatarUrl = HttpRequestUtil.serverUrl + avatarUrl;
        }
        ImageView imageView = new ImageView(new Image(avatarUrl, true));
        imageView.setFitHeight(54);
        imageView.setFitWidth(54);
        imageView.setPreserveRatio(false);
        imageView.getStyleClass().add("following-avatar");
        return imageView;
    }

    private VBox createEmptyState() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("table-empty-state");

        Label title = new Label("还没有任何人关注你");
        title.getStyleClass().add("table-empty-title");

        Label hint = new Label("多发帖、多互动，让更多人发现你。");
        hint.getStyleClass().add("table-empty-hint");
        hint.setWrapText(true);

        box.getChildren().addAll(title, hint);
        return box;
    }

    private void openUserHome(Long userId, String tabName) {
        if (userId == null || userId == 0 || AppStore.getMainFrameController() == null) {
            return;
        }
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                    MainApplication.class.getResource("user-home.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
            UserHomeController controller = fxmlLoader.getController();
            controller.setUserId(userId.intValue());

            AppStore.getMainFrameController().changeContentWithScene("user-" + userId, tabName, scene, controller);
        } catch (Exception e) {
            e.printStackTrace();
            showError("打开用户主页失败");
        }
    }

    public void onFollowerPrevPage() {
        if (followerCurrentPageNum > 1) {
            followerCurrentPageNum--;
            loadFollowers();
        }
    }

    public void onFollowerNextPage() {
        followerCurrentPageNum++;
        loadFollowers();
    }

    private void toggleFollow(Map<String, Object> user) {
        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.toggleFollow(toLong(user.get("userId")));
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(this::loadFollowers));
        task.setOnFailed(event -> Platform.runLater(() -> showError("操作失败")));
        new Thread(task).start();
    }

    private LocalDateTime parseFollowTime(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.toString(), FOLLOW_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String signatureText(Object value) {
        String signature = stringValue(value, "");
        return signature.isBlank() ? "（空）" : signature;
    }

    private String stringValue(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String text = value.toString();
        return text.isBlank() ? fallback : text;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private int toInt(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private long toLong(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
