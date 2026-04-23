package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.util.Map;

public class UserStatisticsController extends ToolController {
    @FXML
    private Label postCountLabel;
    @FXML
    private Label commentCountLabel;
    @FXML
    private Label likeCountLabel;
    @FXML
    private Label favoriteCountLabel;
    @FXML
    private Label viewCountLabel;
    @FXML
    private Label followingCountLabel;
    @FXML
    private Label followerCountLabel;

    @FXML
    public void initialize() {
        loadUserStatistics();
    }

    private void loadUserStatistics() {
        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getUserStatistics();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> statistics = task.getValue();
                if (statistics != null) {
                    updateUI(statistics);
                } else {
                    showError("加载统计数据失败");
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("加载统计数据失败"));
        });

        new Thread(task).start();
    }

    private void updateUI(Map<String, Object> statistics) {
        postCountLabel.setText(getNumberValue(statistics, "postCount"));
        commentCountLabel.setText(getNumberValue(statistics, "commentCount"));
        likeCountLabel.setText(getNumberValue(statistics, "likeCount"));
        favoriteCountLabel.setText(getNumberValue(statistics, "favoriteCount"));
        viewCountLabel.setText(getNumberValue(statistics, "viewCount"));
        followingCountLabel.setText(getNumberValue(statistics, "followingCount"));
        followerCountLabel.setText(getNumberValue(statistics, "followerCount"));
    }

    private String getNumberValue(Map<String, Object> statistics, String key) {
        Object value = statistics.get(key);
        if (value instanceof Number) {
            return String.valueOf(((Number) value).intValue());
        }
        return "0";
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
