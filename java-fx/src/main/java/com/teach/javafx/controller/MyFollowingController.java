package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.PageResult;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MyFollowingController extends ToolController {
    @FXML
    private TableView<Map<String, Object>> followingTableView;
    @FXML
    private TableColumn<Map<String, Object>, ImageView> followingAvatarColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> followingNicknameColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> followingSignatureColumn;
    @FXML
    private TableColumn<Map<String, Object>, Integer> followingPostCountColumn;
    @FXML
    private TableColumn<Map<String, Object>, Integer> followingFollowerCountColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> followingTimeColumn;
    @FXML
    private TableColumn<Map<String, Object>, Button> followingActionColumn;
    @FXML
    private Label followingTotalLabel;
    @FXML
    private Label followingPageInfoLabel;
    @FXML
    private Button followingPrevButton;
    @FXML
    private Button followingNextButton;
    @FXML
    private Button followingRefreshButton;

    private int followingCurrentPageNum = 1;
    private int currentPageSize = 10;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        setupFollowingTable();
        
        loadFollowing();
        
        followingPrevButton.setOnAction(event -> onFollowingPrevPage());
        followingNextButton.setOnAction(event -> onFollowingNextPage());
        
        followingRefreshButton.setOnAction(event -> {
            followingCurrentPageNum = 1;
            loadFollowing(followingRefreshButton);
        });
    }

    private void setupFollowingTable() {
        followingAvatarColumn.setCellFactory(col -> new TableCell<Map<String, Object>, ImageView>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(ImageView item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(item);
                }
            }
        });
        followingAvatarColumn.setCellValueFactory(cellData -> {
            Map<String, Object> user = cellData.getValue();
            String avatarUrl = (String) user.get("avatarUrl");
            if (avatarUrl == null || avatarUrl.isBlank()) {
                avatarUrl = "https://img.phb123.com/uploads/allimg/220607/810-22060G55A40-L.jpeg";
            } else if (!avatarUrl.startsWith("http")) {
                avatarUrl = HttpRequestUtil.serverUrl + avatarUrl;
            }
            ImageView imageView = new ImageView(new Image(avatarUrl, true));
            imageView.setFitHeight(50);
            imageView.setFitWidth(50);
            imageView.setPreserveRatio(true);
            return new javafx.beans.property.SimpleObjectProperty<>(imageView);
        });

        followingNicknameColumn.setCellValueFactory(cellData -> {
            Map<String, Object> user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty((String) user.get("nickname"));
        });

        followingSignatureColumn.setCellValueFactory(cellData -> {
            Map<String, Object> user = cellData.getValue();
            String signature = (String) user.get("signature");
            if (signature != null && signature.length() > 20) {
                signature = signature.substring(0, 20) + "...";
            }
            return new javafx.beans.property.SimpleStringProperty(signature != null ? signature : "");
        });

        followingPostCountColumn.setCellValueFactory(cellData -> {
            Map<String, Object> user = cellData.getValue();
            Object postCount = user.get("postCount");
            return new javafx.beans.property.SimpleIntegerProperty(toInt(postCount)).asObject();
        });

        followingFollowerCountColumn.setCellValueFactory(cellData -> {
            Map<String, Object> user = cellData.getValue();
            Object followerCount = user.get("followerCount");
            return new javafx.beans.property.SimpleIntegerProperty(toInt(followerCount)).asObject();
        });

        followingTimeColumn.setCellValueFactory(cellData -> {
            Map<String, Object> user = cellData.getValue();
            Object followTime = user.get("followTime");
            if (followTime != null) {
                String timeStr = followTime.toString();
                if (timeStr.contains("-")) {
                    return new javafx.beans.property.SimpleStringProperty(timeStr);
                } else {
                    return new javafx.beans.property.SimpleStringProperty(dateFormat.format(new Date(toLong(followTime))));
                }
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        followingActionColumn.setCellFactory(col -> new TableCell<Map<String, Object>, Button>() {
            private final Button button = new Button("取消关注");
            {
                button.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
                button.setOnAction(event -> {
                    Map<String, Object> user = getTableView().getItems().get(getIndex());
                    toggleFollow(user);
                });
            }
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(button);
                }
            }
        });

        followingTableView.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    Map<String, Object> user = row.getItem();
                    Long userId = toLong(user.get("userId"));
                    String nickname = (String) user.get("nickname");
                    openUserHome(userId, nickname != null ? nickname : "用户主页");
                }
            });
            return row;
        });
    }

    private void openUserHome(Long userId, String tabName) {
        if (userId == null || AppStore.getMainFrameController() == null) return;
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                MainApplication.class.getResource("user-home.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 1024, 768);
            UserHomeController controller = fxmlLoader.getController();
            controller.setUserId(userId.intValue());

            String id = "user-" + userId;
            AppStore.getMainFrameController().changeContentWithScene(id, tabName, scene, controller);
        } catch (Exception e) {
            e.printStackTrace();
            showError("打开用户主页失败");
        }
    }

    public void loadFollowing() {
        loadFollowing(null);
    }

    private void loadFollowing(Button refreshBtn) {
        if (refreshBtn != null) {
            refreshBtn.setDisable(true);
            refreshBtn.setText("刷新中");
        }
        
        Task<PageResult<Map<String, Object>>> task = new Task<PageResult<Map<String, Object>>>() {
            @Override
            protected PageResult<Map<String, Object>> call() {
                return HttpRequestUtil.getMyFollowingPage(followingCurrentPageNum, currentPageSize);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                if (refreshBtn != null) {
                    refreshBtn.setDisable(false);
                    refreshBtn.setText("刷新");
                }
                
                PageResult<Map<String, Object>> pageResult = task.getValue();
                if (pageResult != null && pageResult.getList() != null) {
                    followingTableView.getItems().clear();
                    followingTableView.getItems().addAll(pageResult.getList());
                    
                    long total = pageResult.getTotal() != null ? pageResult.getTotal() : 0;
                    followingTotalLabel.setText("共 " + total + " 条");
                    followingPageInfoLabel.setText("第 " + followingCurrentPageNum + " 页");
                    
                    followingPrevButton.setDisable(followingCurrentPageNum <= 1);
                    int totalPages = (int) Math.ceil((double) total / currentPageSize);
                    followingNextButton.setDisable(followingCurrentPageNum >= totalPages);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                if (refreshBtn != null) {
                    refreshBtn.setDisable(false);
                    refreshBtn.setText("刷新");
                }
                showError("加载关注列表失败");
            });
        });

        new Thread(task).start();
    }

    public void onFollowingPrevPage() {
        if (followingCurrentPageNum > 1) {
            followingCurrentPageNum--;
            loadFollowing();
        }
    }

    public void onFollowingNextPage() {
        followingCurrentPageNum++;
        loadFollowing();
    }

    private void toggleFollow(Map<String, Object> user) {
        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                Object userIdObj = user.get("userId");
                long userId = toLong(userIdObj);
                return HttpRequestUtil.toggleFollow(userId);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                loadFollowing();
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("操作失败"));
        });

        new Thread(task).start();
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
