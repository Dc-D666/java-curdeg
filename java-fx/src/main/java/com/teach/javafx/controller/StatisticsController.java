package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.Post;
import com.teach.javafx.models.User;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Map;

public class StatisticsController extends ToolController {
    @FXML
    private LineChart<Number, Number> dailyPostChart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    @FXML
    private TableView<Post> hotPostTableView;
    @FXML
    private TableColumn<Post, String> hotPostTitleColumn;
    @FXML
    private TableColumn<Post, String> hotPostAuthorColumn;
    @FXML
    private TableColumn<Post, Integer> hotPostLikeCountColumn;
    @FXML
    private TableColumn<Post, Integer> hotPostCommentCountColumn;

    @FXML
    private TableView<User> activeUserTableView;
    @FXML
    private TableColumn<User, String> activeUserNicknameColumn;
    @FXML
    private TableColumn<User, String> activeUserStudentIdColumn;
    @FXML
    private TableColumn<User, Integer> activeUserPostCountColumn;
    @FXML
    private TableColumn<User, Integer> activeUserCommentCountColumn;

    @FXML
    public void initialize() {
        hotPostTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        hotPostAuthorColumn.setCellValueFactory(new PropertyValueFactory<>("authorNickname"));
        hotPostLikeCountColumn.setCellValueFactory(new PropertyValueFactory<>("likeCount"));
        hotPostCommentCountColumn.setCellValueFactory(new PropertyValueFactory<>("commentCount"));

        activeUserNicknameColumn.setCellValueFactory(new PropertyValueFactory<>("nickname"));
        activeUserStudentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        activeUserPostCountColumn.setCellValueFactory(new PropertyValueFactory<>("postCount"));
        activeUserCommentCountColumn.setCellValueFactory(new PropertyValueFactory<>("commentCount"));

        loadDailyPostStatistics();
        loadHotPostStatistics();
        loadActiveUserStatistics();
    }

    private void loadDailyPostStatistics() {
        Task<List<Map<String, Object>>> task = new Task<List<Map<String, Object>>>() {
            @Override
            protected List<Map<String, Object>> call() {
                return HttpRequestUtil.getDailyPostStatistics();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                List<Map<String, Object>> data = task.getValue();
                if (data != null && !data.isEmpty()) {
                    XYChart.Series<Number, Number> series = new XYChart.Series<>();
                    series.setName("发帖量");

                    for (int i = 0; i < data.size(); i++) {
                        Map<String, Object> item = data.get(i);
                        Number count = ((Number) item.get("count"));
                        series.getData().add(new XYChart.Data<>(i, count));
                    }

                    dailyPostChart.getData().clear();
                    dailyPostChart.getData().add(series);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("加载每日发帖量统计失败"));
        });

        new Thread(task).start();
    }

    private void loadHotPostStatistics() {
        Task<List<Post>> task = new Task<List<Post>>() {
            @Override
            protected List<Post> call() {
                return HttpRequestUtil.getHotPostStatistics();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                List<Post> posts = task.getValue();
                if (posts != null) {
                    hotPostTableView.getItems().clear();
                    hotPostTableView.getItems().addAll(posts);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("加载热门帖子统计失败"));
        });

        new Thread(task).start();
    }

    private void loadActiveUserStatistics() {
        Task<List<User>> task = new Task<List<User>>() {
            @Override
            protected List<User> call() {
                return HttpRequestUtil.getActiveUserStatistics();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                List<User> users = task.getValue();
                if (users != null) {
                    activeUserTableView.getItems().clear();
                    activeUserTableView.getItems().addAll(users);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("加载活跃用户统计失败"));
        });

        new Thread(task).start();
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
