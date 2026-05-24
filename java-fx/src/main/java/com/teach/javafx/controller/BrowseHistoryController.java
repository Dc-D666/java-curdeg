package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class BrowseHistoryController extends ToolController {

    private static final int PAGE_SIZE = 10;

    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private VBox historyListVBox;
    @FXML
    private Label totalLabel;
    @FXML
    private Button refreshButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Label pageLabel;

    private int currentPage = 0;
    private int totalPages = 0;
    private long totalCount = 0;

    @FXML
    public void initialize() {
        mainScrollPane.setFitToWidth(true);
        refreshButton.setOnAction(event -> loadHistory());
        clearButton.setOnAction(event -> clearHistory());
        prevButton.setOnAction(event -> goToPrevPage());
        nextButton.setOnAction(event -> goToNextPage());
        loadHistory();
    }

    private void loadHistory() {
        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getBrowseHistoryList(currentPage, PAGE_SIZE);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> result = task.getValue();
                historyListVBox.getChildren().clear();
                
                if (result != null) {
                    totalCount = result.get("total") instanceof Number ? ((Number) result.get("total")).longValue() : 0;
                    totalPages = result.get("totalPages") instanceof Number ? ((Number) result.get("totalPages")).intValue() : 0;
                    Integer returnedPage = result.get("currentPage") instanceof Number ? ((Number) result.get("currentPage")).intValue() : 0;
                    currentPage = returnedPage;
                    
                    List<Map<String, Object>> historyList = null;
                    Object listObj = result.get("list");
                    if (listObj instanceof List) {
                        historyList = (List<Map<String, Object>>) listObj;
                    }
                    
                    if (historyList != null && !historyList.isEmpty()) {
                        totalLabel.setText("共 " + totalCount + " 条记录");
                        for (Map<String, Object> item : historyList) {
                            addHistoryCard(item);
                        }
                    } else {
                        totalLabel.setText("暂无浏览记录");
                        Label emptyLabel = new Label("暂无浏览记录，去帖子广场看看吧~");
                        emptyLabel.setStyle("-fx-text-fill: #999; -fx-padding: 40 0; -fx-font-size: 14;");
                        emptyLabel.setAlignment(Pos.CENTER);
                        emptyLabel.setMaxWidth(Double.MAX_VALUE);
                        historyListVBox.getChildren().add(emptyLabel);
                    }
                    
                    updatePagination();
                } else {
                    totalLabel.setText("加载失败");
                    showError("加载浏览历史失败");
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("加载浏览历史失败"));
        });

        new Thread(task).start();
    }

    private void updatePagination() {
        pageLabel.setText("第 " + (currentPage + 1) + " / " + Math.max(1, totalPages) + " 页");
        prevButton.setDisable(currentPage <= 0);
        nextButton.setDisable(currentPage >= totalPages - 1);
    }

    private void goToPrevPage() {
        if (currentPage > 0) {
            currentPage--;
            loadHistory();
        }
    }

    private void goToNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadHistory();
        }
    }

    private void addHistoryCard(Map<String, Object> item) {
        VBox card = new VBox(8);
        card.getStyleClass().add("profile-card");
        card.setStyle("-fx-padding: 16; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");

        Long postId = item.get("id") instanceof Number ? ((Number) item.get("id")).longValue() : null;
        String title = item.get("title") != null ? item.get("title").toString() : "无标题";
        String content = item.get("content") != null ? item.get("content").toString() : "";
        String authorNickname = item.get("authorNickname") != null ? item.get("authorNickname").toString() : "";
        String boardName = item.get("boardName") != null ? item.get("boardName").toString() : "";
        String browseTime = item.get("browseTime") != null ? item.get("browseTime").toString() : "";
        Integer likeCount = item.get("likeCount") instanceof Number ? ((Number) item.get("likeCount")).intValue() : 0;
        Integer commentCount = item.get("commentCount") instanceof Number ? ((Number) item.get("commentCount")).intValue() : 0;
        Integer viewCount = item.get("viewCount") instanceof Number ? ((Number) item.get("viewCount")).intValue() : 0;

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1a1a2e; -fx-wrap-text: true;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        if (!boardName.isEmpty()) {
            Label boardLabel = new Label(boardName);
            boardLabel.setStyle("-fx-text-fill: #6366f1; -fx-font-size: 11; -fx-background-color: #eef2ff; -fx-padding: 2 8; -fx-background-radius: 8;");
            titleRow.getChildren().addAll(titleLabel, boardLabel);
        } else {
            titleRow.getChildren().add(titleLabel);
        }

        String preview = content.length() > 150 ? content.substring(0, 150) + "..." : content;
        Label contentLabel = new Label(preview);
        contentLabel.setStyle("-fx-text-fill: #666; -fx-wrap-text: true; -fx-font-size: 13;");
        contentLabel.setWrapText(true);

        HBox bottomRow = new HBox(15);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        Label authorLabel = new Label(authorNickname);
        authorLabel.setStyle("-fx-text-fill: #6366f1; -fx-font-size: 12;");

        Label statsLabel = new Label("浏览 " + viewCount + "  点赞 " + likeCount + "  评论 " + commentCount);
        statsLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeLabel = new Label("浏览于 " + browseTime);
        timeLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12;");

        bottomRow.getChildren().addAll(authorLabel, statsLabel, spacer, timeLabel);

        card.getChildren().addAll(titleRow, contentLabel, bottomRow);

        final Long finalPostId = postId;
        card.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                if (finalPostId != null) {
                    openPostDetail(finalPostId);
                }
            }
        });

        card.setOnMouseEntered(event -> {
            card.setStyle("-fx-padding: 16; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-background-color: #f9fafb;");
        });
        card.setOnMouseExited(event -> {
            card.setStyle("-fx-padding: 16; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
        });

        historyListVBox.getChildren().add(card);
    }

    private void openPostDetail(Long postId) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                MainApplication.class.getResource("post-detail.fxml"));
            javafx.scene.Parent root = loader.load();
            PostDetailController controller = loader.getController();
            controller.setPostId(postId);

            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1024, 768);
            String tabName = "post-detail-" + postId;
            AppStore.getMainFrameController().changeContentWithScene(tabName, "帖子详情", scene, controller);
        } catch (Exception e) {
            e.printStackTrace();
            showError("打开帖子详情失败");
        }
    }

    private void clearHistory() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认清空");
        alert.setHeaderText(null);
        alert.setContentText("确定要清空所有浏览历史吗？此操作不可恢复。");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Task<Boolean> task = new Task<Boolean>() {
                    @Override
                    protected Boolean call() {
                        return HttpRequestUtil.clearBrowseHistory();
                    }
                };

                task.setOnSucceeded(event -> {
                    Platform.runLater(() -> {
                        if (task.getValue()) {
                            showInfo("浏览历史已清空");
                            currentPage = 0;
                            loadHistory();
                        } else {
                            showError("清空失败");
                        }
                    });
                });

                task.setOnFailed(event -> {
                    Platform.runLater(() -> showError("清空失败"));
                });

                new Thread(task).start();
            }
        });
    }

    private void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
