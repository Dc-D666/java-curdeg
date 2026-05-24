package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.PageResult;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MyFollowersController extends ToolController {
    @FXML
    private TableView<Map<String, Object>> followerTableView;
    @FXML
    private TableColumn<Map<String, Object>, ImageView> followerAvatarColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> followerNicknameColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> followerSignatureColumn;
    @FXML
    private TableColumn<Map<String, Object>, Integer> followerPostCountColumn;
    @FXML
    private TableColumn<Map<String, Object>, Integer> followerFollowerCountColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> followerTimeColumn;
    @FXML
    private TableColumn<Map<String, Object>, Button> followerActionColumn;
    @FXML
    private Label followerTotalLabel;
    @FXML
    private Label followerPageInfoLabel;
    @FXML
    private Button followerPrevButton;
    @FXML
    private Button followerNextButton;
    @FXML
    private Button followerRefreshButton;

    private int followerCurrentPageNum = 1;
    private int currentPageSize = 10;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        setupFollowerTable();
        
        loadFollowers();
        
        followerPrevButton.setOnAction(event -> onFollowerPrevPage());
        followerNextButton.setOnAction(event -> onFollowerNextPage());
        
        followerRefreshButton.setOnAction(event -> {
            followerCurrentPageNum = 1;
            loadFollowers(followerRefreshButton);
        });
    }

    private void setupFollowerTable() {
        followerAvatarColumn.setCellFactory(col -> new TableCell<Map<String, Object>, ImageView>() {
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
        followerAvatarColumn.setCellValueFactory(cellData -> {
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

        followerNicknameColumn.setCellValueFactory(cellData -> {
            Map<String, Object> user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty((String) user.get("nickname"));
        });

        followerSignatureColumn.setCellValueFactory(cellData -> {
            Map<String, Object> user = cellData.getValue();
            String signature = (String) user.get("signature");
            if (signature != null && signature.length() > 20) {
                signature = signature.substring(0, 20) + "...";
            }
            return new javafx.beans.property.SimpleStringProperty(signature != null ? signature : "");
        });

        followerPostCountColumn.setCellValueFactory(cellData -> {
            Map<String, Object> user = cellData.getValue();
            Object postCount = user.get("postCount");
            return new javafx.beans.property.SimpleIntegerProperty(toInt(postCount)).asObject();
        });

        followerFollowerCountColumn.setCellValueFactory(cellData -> {
            Map<String, Object> user = cellData.getValue();
            Object followerCount = user.get("followerCount");
            return new javafx.beans.property.SimpleIntegerProperty(toInt(followerCount)).asObject();
        });

        followerTimeColumn.setCellValueFactory(cellData -> {
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

        followerActionColumn.setCellFactory(col -> new TableCell<Map<String, Object>, Button>() {
            private final Button button = new Button("关注");
            {
                button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
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
                    Map<String, Object> user = getTableView().getItems().get(getIndex());
                    Boolean isFollowed = (Boolean) user.get("isFollowed");
                    if (isFollowed != null && isFollowed) {
                        button.setText("取消关注");
                        button.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
                    } else {
                        button.setText("关注");
                        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    }
                    setGraphic(button);
                }
            }
        });
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

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                if (refreshBtn != null) {
                    refreshBtn.setDisable(false);
                    refreshBtn.setText("刷新");
                }
                
                PageResult<Map<String, Object>> pageResult = task.getValue();
                if (pageResult != null && pageResult.getList() != null) {
                    followerTableView.getItems().clear();
                    followerTableView.getItems().addAll(pageResult.getList());
                    
                    long total = pageResult.getTotal() != null ? pageResult.getTotal() : 0;
                    followerTotalLabel.setText("共 " + total + " 条");
                    followerPageInfoLabel.setText("第 " + followerCurrentPageNum + " 页");
                    
                    followerPrevButton.setDisable(followerCurrentPageNum <= 1);
                    int totalPages = (int) Math.ceil((double) total / currentPageSize);
                    followerNextButton.setDisable(followerCurrentPageNum >= totalPages);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                if (refreshBtn != null) {
                    refreshBtn.setDisable(false);
                    refreshBtn.setText("刷新");
                }
                showError("加载粉丝列表失败");
            });
        });

        new Thread(task).start();
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
                Object userIdObj = user.get("userId");
                long userId = toLong(userIdObj);
                return HttpRequestUtil.toggleFollow(userId);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                loadFollowers();
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
