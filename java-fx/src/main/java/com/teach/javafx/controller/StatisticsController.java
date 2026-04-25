package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.Post;
import com.teach.javafx.models.User;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatisticsController extends ToolController {
    
    @FXML
    private TabPane mainTabPane;
    
    @FXML
    private GridPane overviewGrid;
    
    @FXML
    private LineChart<String, Number> quickPostTrendChart;
    
    @FXML
    private CategoryAxis quickPostXAxis;
    
    @FXML
    private NumberAxis quickPostYAxis;
    
    @FXML
    private LineChart<String, Number> quickUserTrendChart;
    
    @FXML
    private CategoryAxis quickUserXAxis;
    
    @FXML
    private NumberAxis quickUserYAxis;
    
    @FXML
    private ChoiceBox<String> userGrowthDaysChoice;
    
    @FXML
    private Button refreshUserBtn;
    
    @FXML
    private LineChart<String, Number> userGrowthChart;
    
    @FXML
    private CategoryAxis userGrowthXAxis;
    
    @FXML
    private NumberAxis userGrowthYAxis;
    
    @FXML
    private PieChart userTypeChart;
    
    @FXML
    private BarChart<String, Number> userActivityChart;
    
    @FXML
    private ChoiceBox<String> activeUserSortChoice;
    
    @FXML
    private TableView<ActiveUserRow> activeUserTableView;
    
    @FXML
    private TableColumn<ActiveUserRow, Number> activeUserRankCol;
    
    @FXML
    private TableColumn<ActiveUserRow, String> activeUserNicknameCol;
    
    @FXML
    private TableColumn<ActiveUserRow, String> activeUserStudentIdCol;
    
    @FXML
    private TableColumn<ActiveUserRow, String> activeUserTypeCol;
    
    @FXML
    private TableColumn<ActiveUserRow, Number> activeUserPostCol;
    
    @FXML
    private TableColumn<ActiveUserRow, Number> activeUserCommentCol;
    
    @FXML
    private TableColumn<ActiveUserRow, Number> activeUserLikeCol;
    
    @FXML
    private ChoiceBox<String> postTrendDaysChoice;
    
    @FXML
    private Button refreshContentBtn;
    
    @FXML
    private LineChart<String, Number> postTrendChart;
    
    @FXML
    private CategoryAxis postTrendXAxis;
    
    @FXML
    private NumberAxis postTrendYAxis;
    
    @FXML
    private PieChart boardDistributionChart;
    
    @FXML
    private PieChart postStatusChart;
    
    @FXML
    private ChoiceBox<String> hotPostSortChoice;
    
    @FXML
    private TableView<HotPostRow> hotPostTableView;
    
    @FXML
    private TableColumn<HotPostRow, Number> hotPostRankCol;
    
    @FXML
    private TableColumn<HotPostRow, String> hotPostTitleCol;
    
    @FXML
    private TableColumn<HotPostRow, String> hotPostBoardCol;
    
    @FXML
    private TableColumn<HotPostRow, String> hotPostAuthorCol;
    
    @FXML
    private TableColumn<HotPostRow, Number> hotPostViewCol;
    
    @FXML
    private TableColumn<HotPostRow, Number> hotPostLikeCol;
    
    @FXML
    private TableColumn<HotPostRow, Number> hotPostCommentCol;
    
    @FXML
    private TableColumn<HotPostRow, Number> hotPostFavoriteCol;
    
    @FXML
    private ChoiceBox<String> interactionDaysChoice;
    
    @FXML
    private Button refreshInteractionBtn;
    
    @FXML
    private LineChart<String, Number> likeTrendChart;
    
    @FXML
    private LineChart<String, Number> followTrendChart;
    
    @FXML
    private TableView<HotCommentRow> hotCommentTableView;
    
    @FXML
    private TableColumn<HotCommentRow, Number> hotCommentRankCol;
    
    @FXML
    private TableColumn<HotCommentRow, String> hotCommentContentCol;
    
    @FXML
    private TableColumn<HotCommentRow, String> hotCommentAuthorCol;
    
    @FXML
    private TableColumn<HotCommentRow, String> hotCommentPostCol;
    
    @FXML
    private TableColumn<HotCommentRow, Number> hotCommentLikeCol;
    
    @FXML
    private TableColumn<HotCommentRow, String> hotCommentTimeCol;
    
    @FXML
    private GridPane moderationGrid;
    
    @FXML
    private ChoiceBox<String> moderationDaysChoice;
    
    @FXML
    private Button refreshSafetyBtn;
    
    @FXML
    private LineChart<String, Number> moderationTrendChart;
    
    @FXML
    private CategoryAxis moderationXAxis;
    
    @FXML
    private NumberAxis moderationYAxis;
    
    @FXML
    private PieChart violationTypeChart;
    
    @FXML
    private GridPane reportStatsGrid;
    
    private final ObservableList<ActiveUserRow> activeUserList = FXCollections.observableArrayList();
    private final ObservableList<HotPostRow> hotPostList = FXCollections.observableArrayList();
    private final ObservableList<HotCommentRow> hotCommentList = FXCollections.observableArrayList();
    
    private static final String[] COLORS = {
        "#3498db", "#2ecc71", "#e74c3c", "#f39c12", "#9b59b6",
        "#1abc9c", "#34495e", "#e67e22", "#95a5a6", "#16a085"
    };
    
    @FXML
    public void initialize() {
        setupTableColumns();
        setupChoiceBoxes();
        setupEventHandlers();
        loadAllData();
    }
    
    private void setupTableColumns() {
        activeUserRankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        activeUserNicknameCol.setCellValueFactory(new PropertyValueFactory<>("nickname"));
        activeUserStudentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        activeUserTypeCol.setCellValueFactory(new PropertyValueFactory<>("userType"));
        activeUserPostCol.setCellValueFactory(new PropertyValueFactory<>("postCount"));
        activeUserCommentCol.setCellValueFactory(new PropertyValueFactory<>("commentCount"));
        activeUserLikeCol.setCellValueFactory(new PropertyValueFactory<>("likeCount"));
        activeUserTableView.setItems(activeUserList);
        
        hotPostRankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        hotPostTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        hotPostBoardCol.setCellValueFactory(new PropertyValueFactory<>("board"));
        hotPostAuthorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        hotPostViewCol.setCellValueFactory(new PropertyValueFactory<>("viewCount"));
        hotPostLikeCol.setCellValueFactory(new PropertyValueFactory<>("likeCount"));
        hotPostCommentCol.setCellValueFactory(new PropertyValueFactory<>("commentCount"));
        hotPostFavoriteCol.setCellValueFactory(new PropertyValueFactory<>("favoriteCount"));
        hotPostTableView.setItems(hotPostList);
        
        hotCommentRankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        hotCommentContentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
        hotCommentAuthorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        hotCommentPostCol.setCellValueFactory(new PropertyValueFactory<>("postTitle"));
        hotCommentLikeCol.setCellValueFactory(new PropertyValueFactory<>("likeCount"));
        hotCommentTimeCol.setCellValueFactory(new PropertyValueFactory<>("commentTime"));
        hotCommentTableView.setItems(hotCommentList);
    }
    
    private void setupChoiceBoxes() {
        userGrowthDaysChoice.setItems(FXCollections.observableArrayList("7天", "30天", "90天"));
        userGrowthDaysChoice.setValue("7天");
        
        postTrendDaysChoice.setItems(FXCollections.observableArrayList("7天", "30天", "90天"));
        postTrendDaysChoice.setValue("7天");
        
        interactionDaysChoice.setItems(FXCollections.observableArrayList("7天", "30天", "90天"));
        interactionDaysChoice.setValue("7天");
        
        moderationDaysChoice.setItems(FXCollections.observableArrayList("7天", "30天", "90天"));
        moderationDaysChoice.setValue("7天");
        
        activeUserSortChoice.setItems(FXCollections.observableArrayList("综合", "发帖数", "评论数"));
        activeUserSortChoice.setValue("综合");
        
        hotPostSortChoice.setItems(FXCollections.observableArrayList("综合", "点赞数", "评论数", "浏览数", "收藏数"));
        hotPostSortChoice.setValue("综合");
    }
    
    private void setupEventHandlers() {
        refreshUserBtn.setOnAction(e -> loadUserStats());
        refreshContentBtn.setOnAction(e -> loadContentStats());
        refreshInteractionBtn.setOnAction(e -> loadInteractionStats());
        refreshSafetyBtn.setOnAction(e -> loadSafetyStats());
        
        userGrowthDaysChoice.setOnAction(e -> loadUserGrowthChart());
        postTrendDaysChoice.setOnAction(e -> loadPostTrendChart());
        interactionDaysChoice.setOnAction(e -> loadInteractionTrendCharts());
        moderationDaysChoice.setOnAction(e -> loadModerationTrendChart());
        
        activeUserSortChoice.setOnAction(e -> loadActiveUsers());
        hotPostSortChoice.setOnAction(e -> loadHotPosts());
    }
    
    private void loadAllData() {
        loadOverviewData();
        loadQuickTrendCharts();
        loadUserStats();
        loadContentStats();
        loadInteractionStats();
        loadSafetyStats();
    }
    
    private void loadOverviewData() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getOverview();
            }
        };
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            Map<String, Object> data = task.getValue();
            if (data != null) {
                updateOverviewCards(data);
            }
        }));
        
        task.setOnFailed(e -> Platform.runLater(() -> 
            showError("加载概览数据失败")));
        
        new Thread(task).start();
    }
    
    private void updateOverviewCards(Map<String, Object> data) {
        overviewGrid.getChildren().clear();
        
        String[][] cardConfigs = {
            {"用户总数", "userCount", "#3498db"},
            {"今日新增用户", "todayNewUsers", "#2ecc71"},
            {"本月活跃用户", "monthlyActiveUsers", "#9b59b6"},
            {"帖子总数", "postCount", "#e74c3c"},
            {"今日新增帖子", "todayNewPosts", "#f39c12"},
            {"评论总数", "commentCount", "#1abc9c"},
            {"今日新增评论", "todayNewComments", "#34495e"},
            {"待审核内容", "pendingModerationCount", "#e67e22"}
        };
        
        for (int i = 0; i < cardConfigs.length; i++) {
            String title = cardConfigs[i][0];
            String key = cardConfigs[i][1];
            String color = cardConfigs[i][2];
            Object valueObj = data.get(key);
            String value = "0";
            if (valueObj != null) {
                if (valueObj instanceof Number) {
                    value = String.valueOf(((Number) valueObj).intValue());
                } else {
                    String str = valueObj.toString();
                    try {
                        if (str.contains(".")) {
                            value = String.valueOf((int) Double.parseDouble(str));
                        } else {
                            value = String.valueOf(Integer.parseInt(str));
                        }
                    } catch (NumberFormatException e) {
                        value = str;
                    }
                }
            }
            
            VBox card = createStatCard(title, value, color);
            int row = i / 4;
            int col = i % 4;
            overviewGrid.add(card, col, row);
        }
    }
    
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0); -fx-padding: 20; -fx-alignment: center;");
        
        Text titleText = new Text(title);
        titleText.setFont(new Font(14));
        titleText.setFill(Color.web("#7f8c8d"));
        
        Text valueText = new Text(value);
        valueText.setFont(new Font(28));
        valueText.setFill(Color.web(color));
        valueText.setStyle("-fx-font-weight: bold;");
        
        card.getChildren().addAll(titleText, valueText);
        return card;
    }
    
    private void loadQuickTrendCharts() {
        loadQuickPostTrend();
        loadQuickUserTrend();
    }
    
    private void loadQuickPostTrend() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() {
                return HttpRequestUtil.getPostTrend(7);
            }
        };
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<Map<String, Object>> data = task.getValue();
            if (data != null && !data.isEmpty()) {
                updateLineChart(quickPostTrendChart, data, "发帖量", "#3498db");
            }
        }));
        
        new Thread(task).start();
    }
    
    private void loadQuickUserTrend() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() {
                return HttpRequestUtil.getUserGrowth(7);
            }
        };
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<Map<String, Object>> data = task.getValue();
            if (data != null && !data.isEmpty()) {
                updateLineChart(quickUserTrendChart, data, "新增用户", "#2ecc71");
            }
        }));
        
        new Thread(task).start();
    }
    
    private void updateLineChart(LineChart<String, Number> chart, List<Map<String, Object>> data, String seriesName, String color) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        
        List<String> dates = new ArrayList<>();
        for (Map<String, Object> item : data) {
            String date = (String) item.get("date");
            if (date == null) date = (String) item.get("time");
            Object countObj = item.get("count");
            if (date != null && countObj != null) {
                dates.add(date);
                Number count = parseNumberSafely(countObj);
                series.getData().add(new XYChart.Data<>(date, count));
            }
        }
        
        chart.getData().add(series);
        applySeriesColor(series, color);
    }
    
    private void applySeriesColor(XYChart.Series<String, Number> series, String color) {
        // 首先设置系列的线条颜色
        if (series.getNode() != null) {
            series.getNode().setStyle("-fx-stroke: " + color + ";");
        }
        // 再设置数据点（端点）的颜色
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-background-color: " + color + ";");
            }
        }
    }
    
    private void loadUserStats() {
        loadUserGrowthChart();
        loadUserTypeChart();
        loadUserActivityChart();
        loadActiveUsers();
    }
    
    private void loadUserGrowthChart() {
        String selected = userGrowthDaysChoice.getValue();
        int days = parseDays(selected);
        
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() {
                return HttpRequestUtil.getUserGrowth(days);
            }
        };
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<Map<String, Object>> data = task.getValue();
            if (data != null && !data.isEmpty()) {
                updateLineChart(userGrowthChart, data, "新增用户", "#2ecc71");
            }
        }));
        
        task.setOnFailed(e -> Platform.runLater(() -> 
            showError("加载用户增长数据失败")));
        
        new Thread(task).start();
    }
    
    private void loadUserTypeChart() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() {
                return HttpRequestUtil.getUserTypeDistribution();
            }
        };
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<Map<String, Object>> data = task.getValue();
            if (data != null && !data.isEmpty()) {
                updatePieChart(userTypeChart, data, "name", "count");
            }
        }));
        
        new Thread(task).start();
    }
    
    private void loadUserActivityChart() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() {
                return HttpRequestUtil.getUserActivityDistribution();
            }
        };
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<Map<String, Object>> data = task.getValue();
            if (data != null && !data.isEmpty()) {
                updateBarChart(userActivityChart, data, "name", "count", "用户活跃度");
            }
        }));
        
        new Thread(task).start();
    }
    
    private void loadActiveUsers() {
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() {
                return HttpRequestUtil.getActiveUserStatistics();
            }
        };
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<User> users = task.getValue();
            if (users != null) {
                activeUserList.clear();
                for (int i = 0; i < users.size(); i++) {
                    User user = users.get(i);
                    ActiveUserRow row = new ActiveUserRow(
                        i + 1,
                        user.getNickname() != null ? user.getNickname() : "",
                        user.getStudentId() != null ? user.getStudentId() : "",
                        user.getAuthority() != null ? user.getAuthority() : "用户",
                        user.getPostCount() != null ? user.getPostCount() : 0,
                        user.getCommentCount() != null ? user.getCommentCount() : 0,
                        user.getPostCount() != null ? user.getPostCount() : 0
                    );
                    activeUserList.add(row);
                }
            }
        }));
        
        task.setOnFailed(e -> Platform.runLater(() -> 
            showError("加载活跃用户失败")));
        
        new Thread(task).start();
    }
    
    private void loadContentStats() {
        loadPostTrendChart();
        loadBoardDistribution();
        loadPostStatusDistribution();
        loadHotPosts();
    }
    
    private void loadPostTrendChart() {
        String selected = postTrendDaysChoice.getValue();
        int days = parseDays(selected);
        
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() {
                return HttpRequestUtil.getPostTrend(days);
            }
        };
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<Map<String, Object>> data = task.getValue();
            if (data != null && !data.isEmpty()) {
                updateLineChart(postTrendChart, data, "发帖量", "#e74c3c");
            }
        }));
        
        task.setOnFailed(e -> Platform.runLater(() -> 
            showError("加载发帖趋势失败")));
        
        new Thread(task).start();
    }
    
    private void loadBoardDistribution() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() {
                return HttpRequestUtil.getBoardDistribution();
            }
        };
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<Map<String, Object>> data = task.getValue();
            if (data != null && !data.isEmpty()) {
                updatePieChart(boardDistributionChart, data, "name", "count");
            }
        }));
        
        new Thread(task).start();
    }
    
    private void loadPostStatusDistribution() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() {
                return HttpRequestUtil.getPostStatusDistribution();
            }
        };
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<Map<String, Object>> data = task.getValue();
            if (data != null && !data.isEmpty()) {
                updatePieChart(postStatusChart, data, "name", "count");
            }
        }));
        
        new Thread(task).start();
    }
    
    private void loadHotPosts() {
        Task<List<Post>> task = new Task<>() {
            @Override
            protected List<Post> call() {
                return HttpRequestUtil.getHotPostStatistics();
            }
        };
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<Post> posts = task.getValue();
            if (posts != null) {
                hotPostList.clear();
                for (int i = 0; i < posts.size(); i++) {
                    Post post = posts.get(i);
                    HotPostRow row = new HotPostRow(
                        i + 1,
                        post.getTitle() != null ? post.getTitle() : "",
                        post.getBoardName() != null ? post.getBoardName() : "",
                        post.getAuthorNickname() != null ? post.getAuthorNickname() : "",
                        post.getViewCount() != null ? post.getViewCount() : 0,
                        post.getLikeCount() != null ? post.getLikeCount() : 0,
                        post.getCommentCount() != null ? post.getCommentCount() : 0,
                        post.getFavoriteCount() != null ? post.getFavoriteCount() : 0
                    );
                    hotPostList.add(row);
                }
            }
        }));
        
        task.setOnFailed(e -> Platform.runLater(() -> 
            showError("加载热门帖子失败")));
        
        new Thread(task).start();
    }
    
    private void loadInteractionStats() {
        loadInteractionTrendCharts();
        loadHotComments();
    }
    
    private void loadInteractionTrendCharts() {
        String selected = interactionDaysChoice.getValue();
        int days = parseDays(selected);
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<Map<String, Object>> likeData = HttpRequestUtil.getLikeTrend(days);
                List<Map<String, Object>> followData = HttpRequestUtil.getFollowTrend(days);
                
                Platform.runLater(() -> {
                    if (likeData != null) updateLineChart(likeTrendChart, likeData, "点赞数", "#e74c3c");
                    if (followData != null) updateLineChart(followTrendChart, followData, "关注数", "#9b59b6");
                });
                return null;
            }
        };
        
        task.setOnFailed(e -> Platform.runLater(() -> 
            showError("加载互动趋势失败")));
        
        new Thread(task).start();
    }
    
    private void loadHotComments() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() {
                return HttpRequestUtil.getHotComments();
            }
        };
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<Map<String, Object>> data = task.getValue();
            if (data != null) {
                hotCommentList.clear();
                for (int i = 0; i < data.size(); i++) {
                    Map<String, Object> item = data.get(i);
                    HotCommentRow row = new HotCommentRow(
                        i + 1,
                        item.get("content") != null ? item.get("content").toString() : "",
                        item.get("author") != null ? item.get("author").toString() : "",
                        item.get("postTitle") != null ? item.get("postTitle").toString() : "",
                        parseIntegerSafely(item.get("likeCount")),
                        item.get("commentTime") != null ? item.get("commentTime").toString() : ""
                    );
                    hotCommentList.add(row);
                }
            }
        }));
        
        new Thread(task).start();
    }
    
    private void loadSafetyStats() {
        loadModerationOverview();
        loadModerationTrendChart();
        loadViolationTypeChart();
        loadReportStats();
    }
    
    private void loadModerationOverview() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getModerationOverview();
            }
        };
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            Map<String, Object> data = task.getValue();
            if (data != null) {
                updateModerationCards(data);
            }
        }));
        
        new Thread(task).start();
    }
    
    private void updateModerationCards(Map<String, Object> data) {
        moderationGrid.getChildren().clear();
        
        String[][] cardConfigs = {
            {"总审核数", "totalModeration", "#3498db"},
            {"待审核数", "pendingCount", "#f39c12"},
            {"审核通过率", "passRate", "#2ecc71"},
            {"审核拒绝率", "rejectRate", "#e74c3c"}
        };
        
        for (int i = 0; i < cardConfigs.length; i++) {
            String title = cardConfigs[i][0];
            String key = cardConfigs[i][1];
            String color = cardConfigs[i][2];
            Object valueObj = data.get(key);
            String value = valueObj != null ? String.valueOf(valueObj) : "0";
            if (key.contains("Rate") && !value.contains("%")) {
                value = value + "%";
            }
            
            VBox card = createStatCard(title, value, color);
            moderationGrid.add(card, i, 0);
        }
    }
    
    private void loadModerationTrendChart() {
        String selected = moderationDaysChoice.getValue();
        int days = parseDays(selected);
        
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() {
                return HttpRequestUtil.getModerationTrend(days);
            }
        };
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<Map<String, Object>> data = task.getValue();
            if (data != null && !data.isEmpty()) {
                updateMultiSeriesLineChart(moderationTrendChart, data);
            }
        }));
        
        task.setOnFailed(e -> Platform.runLater(() -> 
            showError("加载审核趋势失败")));
        
        new Thread(task).start();
    }
    
    private void updateMultiSeriesLineChart(LineChart<String, Number> chart, List<Map<String, Object>> data) {
        chart.getData().clear();
        
        XYChart.Series<String, Number> passedSeries = new XYChart.Series<>();
        passedSeries.setName("通过");
        
        XYChart.Series<String, Number> rejectedSeries = new XYChart.Series<>();
        rejectedSeries.setName("拒绝");
        
        XYChart.Series<String, Number> pendingSeries = new XYChart.Series<>();
        pendingSeries.setName("待审核");
        
        for (Map<String, Object> item : data) {
            String date = (String) item.get("date");
            if (date == null) date = (String) item.get("time");
            if (date == null) continue;
            
            Object passedObj = item.get("passed");
            Object rejectedObj = item.get("rejected");
            Object pendingObj = item.get("pending");
            
            if (passedObj != null) {
                passedSeries.getData().add(new XYChart.Data<>(date, parseNumberSafely(passedObj)));
            }
            if (rejectedObj != null) {
                rejectedSeries.getData().add(new XYChart.Data<>(date, parseNumberSafely(rejectedObj)));
            }
            if (pendingObj != null) {
                pendingSeries.getData().add(new XYChart.Data<>(date, parseNumberSafely(pendingObj)));
            }
        }
        
        chart.getData().addAll(passedSeries, rejectedSeries, pendingSeries);
        applySeriesColor(passedSeries, "#2ecc71");
        applySeriesColor(rejectedSeries, "#e74c3c");
        applySeriesColor(pendingSeries, "#f39c12");
    }
    
    private void loadViolationTypeChart() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() {
                return HttpRequestUtil.getViolationTypeDistribution();
            }
        };
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<Map<String, Object>> data = task.getValue();
            if (data != null && !data.isEmpty()) {
                updatePieChart(violationTypeChart, data, "name", "count");
            }
        }));
        
        new Thread(task).start();
    }
    
    private void loadReportStats() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getReportStats();
            }
        };
        
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            Map<String, Object> data = task.getValue();
            if (data != null) {
                updateReportStatsCards(data);
            }
        }));
        
        new Thread(task).start();
    }
    
    private void updateReportStatsCards(Map<String, Object> data) {
        reportStatsGrid.getChildren().clear();
        
        String[][] cardConfigs = {
            {"举报总数", "totalReports", "#3498db"},
            {"待处理举报", "pendingReports", "#f39c12"},
            {"举报处理率", "handleRate", "#2ecc71"},
            {"有效举报", "validReports", "#9b59b6"}
        };
        
        for (int i = 0; i < cardConfigs.length; i++) {
            String title = cardConfigs[i][0];
            String key = cardConfigs[i][1];
            String color = cardConfigs[i][2];
            Object valueObj = data.get(key);
            String value;
            
            if (key.contains("Rate")) {
                double rate = 0.0;
                if (valueObj instanceof Number) {
                    rate = ((Number) valueObj).doubleValue();
                } else if (valueObj != null) {
                    try {
                        rate = Double.parseDouble(valueObj.toString());
                    } catch (NumberFormatException e) {
                    }
                }
                value = String.format("%.2f%%", rate);
            } else {
                value = valueObj != null ? String.valueOf(((Number) valueObj).intValue()) : "0";
            }
            
            VBox card = createStatCard(title, value, color);
            int row = i / 2;
            int col = i % 2;
            reportStatsGrid.add(card, col, row);
        }
    }
    
    private void updatePieChart(PieChart chart, List<Map<String, Object>> data, String nameKey, String countKey) {
        chart.getData().clear();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        
        for (int i = 0; i < data.size() && i < 10; i++) {
            Map<String, Object> item = data.get(i);
            Object nameObj = item.get(nameKey);
            Object countObj = item.get(countKey);
            if (nameObj != null && countObj != null) {
                String name = nameObj.toString();
                int count = parseIntegerSafely(countObj);
                PieChart.Data pieItem = new PieChart.Data(name, count);
                pieData.add(pieItem);
            }
        }
        
        chart.setData(pieData);
        
        for (int i = 0; i < pieData.size(); i++) {
            PieChart.Data d = pieData.get(i);
            d.getNode().setStyle("-fx-pie-color: " + COLORS[i % COLORS.length] + ";");
        }
    }
    
    private void updateBarChart(BarChart<String, Number> chart, List<Map<String, Object>> data, String nameKey, String countKey, String seriesName) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        
        for (Map<String, Object> item : data) {
            Object nameObj = item.get(nameKey);
            Object countObj = item.get(countKey);
            if (nameObj != null && countObj != null) {
                String name = nameObj.toString();
                Number count = parseNumberSafely(countObj);
                series.getData().add(new XYChart.Data<>(name, count));
            }
        }
        
        chart.getData().add(series);
    }
    
    private int parseIntegerSafely(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            String str = obj.toString();
            if (str.contains(".")) {
                return (int) Double.parseDouble(str);
            }
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private Number parseNumberSafely(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) {
            return (Number) obj;
        }
        try {
            String str = obj.toString();
            if (str.contains(".")) {
                return Double.parseDouble(str);
            }
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private int parseDays(String selected) {
        return switch (selected) {
            case "30天" -> 30;
            case "90天" -> 90;
            default -> 7;
        };
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static class ActiveUserRow {
        private final int rank;
        private final String nickname;
        private final String studentId;
        private final String userType;
        private final int postCount;
        private final int commentCount;
        private final int likeCount;
        
        public ActiveUserRow(int rank, String nickname, String studentId, String userType, 
                              int postCount, int commentCount, int likeCount) {
            this.rank = rank;
            this.nickname = nickname;
            this.studentId = studentId;
            this.userType = userType;
            this.postCount = postCount;
            this.commentCount = commentCount;
            this.likeCount = likeCount;
        }
        
        public int getRank() { return rank; }
        public String getNickname() { return nickname; }
        public String getStudentId() { return studentId; }
        public String getUserType() { return userType; }
        public int getPostCount() { return postCount; }
        public int getCommentCount() { return commentCount; }
        public int getLikeCount() { return likeCount; }
    }
    
    public static class HotPostRow {
        private final int rank;
        private final String title;
        private final String board;
        private final String author;
        private final int viewCount;
        private final int likeCount;
        private final int commentCount;
        private final int favoriteCount;
        
        public HotPostRow(int rank, String title, String board, String author,
                          int viewCount, int likeCount, int commentCount, int favoriteCount) {
            this.rank = rank;
            this.title = title;
            this.board = board;
            this.author = author;
            this.viewCount = viewCount;
            this.likeCount = likeCount;
            this.commentCount = commentCount;
            this.favoriteCount = favoriteCount;
        }
        
        public int getRank() { return rank; }
        public String getTitle() { return title; }
        public String getBoard() { return board; }
        public String getAuthor() { return author; }
        public int getViewCount() { return viewCount; }
        public int getLikeCount() { return likeCount; }
        public int getCommentCount() { return commentCount; }
        public int getFavoriteCount() { return favoriteCount; }
    }
    
    public static class HotCommentRow {
        private final int rank;
        private final String content;
        private final String author;
        private final String postTitle;
        private final int likeCount;
        private final String commentTime;
        
        public HotCommentRow(int rank, String content, String author, String postTitle,
                             int likeCount, String commentTime) {
            this.rank = rank;
            this.content = content;
            this.author = author;
            this.postTitle = postTitle;
            this.likeCount = likeCount;
            this.commentTime = commentTime;
        }
        
        public int getRank() { return rank; }
        public String getContent() { return content; }
        public String getAuthor() { return author; }
        public String getPostTitle() { return postTitle; }
        public int getLikeCount() { return likeCount; }
        public String getCommentTime() { return commentTime; }
    }
}
