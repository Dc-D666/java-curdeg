package com.teach.javafx.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class SystemSummaryPanelController extends ToolController {

    private static final double WHEEL_STEP_PX = 120.0;
    private static final int DEFAULT_TREND_DAYS = 30;
    private int currentUserTrendDays = DEFAULT_TREND_DAYS;
    private int currentPostTrendDays = DEFAULT_TREND_DAYS;

    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private Label lastUpdateLabel;
    @FXML
    private Button refreshButton;

    // 核心指标标签
    @FXML
    private Label totalUserCountLabel;
    @FXML
    private Label todayNewUsersLabel;
    @FXML
    private Label userTrendLabel;
    @FXML
    private Label monthlyActiveUsersLabel;
    @FXML
    private Label todayPostsLabel;
    @FXML
    private Label totalPostsLabel;
    @FXML
    private Label pendingModerationLabel;
    @FXML
    private Hyperlink pendingModerationLink;
    @FXML
    private Label todayCommentsLabel;
    @FXML
    private Label totalCommentsLabel;
    @FXML
    private Label pendingReportsLabel;
    @FXML
    private Hyperlink pendingReportsLink;
    @FXML
    private Label aiPassRateLabel;
    @FXML
    private Label totalModerationsLabel;
    @FXML
    private Circle healthIndicator;
    @FXML
    private Label healthStatusLabel;

    // 趋势图表
    @FXML
    private LineChart<String, Number> userGrowthChart;
    @FXML
    private CategoryAxis userGrowthXAxis;
    @FXML
    private NumberAxis userGrowthYAxis;
    @FXML
    private LineChart<String, Number> postTrendChart;
    @FXML
    private CategoryAxis postTrendXAxis;
    @FXML
    private NumberAxis postTrendYAxis;
    @FXML
    private Button userTrend7Btn;
    @FXML
    private Button userTrend30Btn;
    @FXML
    private Button postTrend7Btn;
    @FXML
    private Button postTrend30Btn;

    // 分布图表
    @FXML
    private PieChart userTypePieChart;
    @FXML
    private PieChart postStatusPieChart;
    @FXML
    private BarChart<String, Number> violationTypeChart;
    @FXML
    private CategoryAxis violationTypeXAxis;
    @FXML
    private NumberAxis violationTypeYAxis;

    // 排行榜
    @FXML
    private TableView<HotPostRow> hotPostsTableView;
    @FXML
    private TableColumn<HotPostRow, Number> hotPostRankColumn;
    @FXML
    private TableColumn<HotPostRow, String> hotPostTitleColumn;
    @FXML
    private TableColumn<HotPostRow, Number> hotPostLikesColumn;
    @FXML
    private TableColumn<HotPostRow, Number> hotPostCommentsColumn;

    @FXML
    private TableView<ActiveUserRow> activeUsersTableView;
    @FXML
    private TableColumn<ActiveUserRow, Number> activeUserRankColumn;
    @FXML
    private TableColumn<ActiveUserRow, String> activeUserNicknameColumn;
    @FXML
    private TableColumn<ActiveUserRow, Number> activeUserPostsColumn;
    @FXML
    private TableColumn<ActiveUserRow, Number> activeUserCommentsColumn;

    // AI 监控
    @FXML
    private LineChart<String, Number> moderationTrendChart;
    @FXML
    private CategoryAxis moderationTrendXAxis;
    @FXML
    private NumberAxis moderationTrendYAxis;
    @FXML
    private Label aiTodayCountLabel;
    @FXML
    private Label suggestion1Label;
    @FXML
    private Label suggestion2Label;
    @FXML
    private Label suggestion3Label;

    private final ObservableList<HotPostRow> hotPostRows = FXCollections.observableArrayList();
    private final ObservableList<ActiveUserRow> activeUserRows = FXCollections.observableArrayList();

    private final Gson gson = new Gson();

    @FXML
    public void initialize() {
        setupPageScroll();
        setupCharts();
        setupTables();
        setupTableClickHandlers();
        loadAllData();
        updateLastUpdateTime();
    }

    private void setupTableClickHandlers() {
        // 热门帖子表格点击事件
        hotPostsTableView.setRowFactory(tv -> {
            javafx.scene.control.TableRow<HotPostRow> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (!row.isEmpty())) {
                    HotPostRow rowData = row.getItem();
                    onHotPostClick(rowData);
                }
            });
            return row;
        });

        // 活跃用户表格点击事件
        activeUsersTableView.setRowFactory(tv -> {
            javafx.scene.control.TableRow<ActiveUserRow> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (!row.isEmpty())) {
                    ActiveUserRow rowData = row.getItem();
                    onActiveUserClick(rowData);
                }
            });
            return row;
        });
    }

    private void onHotPostClick(HotPostRow post) {
        if (post.id() != null) {
            // 使用openPostDetail方法打开帖子详情页面
            AppStore.getMainFrameController().openPostDetail(post.id());
        }
    }

    private void onActiveUserClick(ActiveUserRow user) {
        if (user.personId() != null) {
            // 使用openUserHome方法打开用户主页
            AppStore.getMainFrameController().openUserHome(user.personId());
        }
    }

    private void setupPageScroll() {
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        if (mainScrollPane.getContent() instanceof Region contentRegion) {
            contentRegion.prefWidthProperty().bind(mainScrollPane.viewportBoundsProperty().map(bounds -> bounds.getWidth()));
        }
        mainScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double contentHeight = mainScrollPane.getContent().getLayoutBounds().getHeight();
            double viewportHeight = mainScrollPane.getViewportBounds().getHeight();
            double scrollableHeight = contentHeight - viewportHeight;
            if (scrollableHeight <= 0) {
                event.consume();
                return;
            }
            double direction = event.getDeltaY() < 0 ? 1 : -1;
            double step = Math.min(0.18, WHEEL_STEP_PX / scrollableHeight);
            mainScrollPane.setVvalue(clamp(mainScrollPane.getVvalue() + direction * step));
            event.consume();
        });
    }

    private void setupCharts() {
        userGrowthChart.setCreateSymbols(false);
        postTrendChart.setCreateSymbols(false);
        moderationTrendChart.setCreateSymbols(false);
        userGrowthYAxis.setForceZeroInRange(true);
        postTrendYAxis.setForceZeroInRange(true);
        moderationTrendYAxis.setForceZeroInRange(true);
    }

    private void setupTables() {
        hotPostRankColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().rank));
        hotPostTitleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().title));
        hotPostLikesColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().likes));
        hotPostCommentsColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().comments));
        hotPostsTableView.setItems(hotPostRows);

        activeUserRankColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().rank));
        activeUserNicknameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nickname));
        activeUserPostsColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().posts));
        activeUserCommentsColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().comments));
        activeUsersTableView.setItems(activeUserRows);
    }

    @FXML
    private void onRefresh() {
        loadAllData();
        updateLastUpdateTime();
    }

    private void updateLastUpdateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        Platform.runLater(() -> lastUpdateLabel.setText("最后更新: " + now.format(formatter)));
    }

    private void loadAllData() {
        setLoading(true, "正在加载...");

        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                Map<String, Object> result = new HashMap<>();

                // 并行加载多个数据源
                Map<String, Object> overview = HttpRequestUtil.get("/api/bbs/statistics/overview");
                if (overview != null) {
                    result.put("overview", overview.get("data"));
                }

                List<Map<String, Object>> userGrowth = HttpRequestUtil.getList("/api/bbs/statistics/user-growth?days=" + currentUserTrendDays);
                if (userGrowth != null) {
                    result.put("userGrowth", userGrowth);
                }

                List<Map<String, Object>> postTrend = HttpRequestUtil.getList("/api/bbs/statistics/post-trend?days=" + currentPostTrendDays);
                if (postTrend != null) {
                    result.put("postTrend", postTrend);
                }

                List<Map<String, Object>> userType = HttpRequestUtil.getList("/api/bbs/statistics/user-type");
                if (userType != null) {
                    result.put("userType", userType);
                }

                List<Map<String, Object>> postStatus = HttpRequestUtil.getList("/api/bbs/statistics/post-status");
                if (postStatus != null) {
                    result.put("postStatus", postStatus);
                }

                List<Map<String, Object>> violationTypes = HttpRequestUtil.getList("/api/bbs/statistics/violation-types");
                if (violationTypes != null) {
                    result.put("violationTypes", violationTypes);
                }

                List<Map<String, Object>> hotPosts = HttpRequestUtil.getList("/api/bbs/statistics/hot-posts?sortBy=composite");
                if (hotPosts != null) {
                    result.put("hotPosts", hotPosts);
                }

                List<Map<String, Object>> activeUsers = HttpRequestUtil.getList("/api/bbs/statistics/active-users?sortBy=composite");
                if (activeUsers != null) {
                    result.put("activeUsers", activeUsers);
                }

                Map<String, Object> moderationOverview = HttpRequestUtil.get("/api/bbs/statistics/moderation-overview");
                if (moderationOverview != null) {
                    result.put("moderationOverview", moderationOverview.get("data"));
                }

                List<Map<String, Object>> moderationTrend = HttpRequestUtil.getList("/api/bbs/statistics/moderation-trend?days=7");
                if (moderationTrend != null) {
                    result.put("moderationTrend", moderationTrend);
                }

                Map<String, Object> reportStats = HttpRequestUtil.get("/api/bbs/statistics/report-statistics");
                if (reportStats != null) {
                    result.put("reportStats", reportStats.get("data"));
                }

                return result;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            setLoading(false, "已更新");
            Map<String, Object> data = task.getValue();
            if (data != null) {
                updateOverview(data);
                updateUserGrowthChart(data);
                updatePostTrendChart(data);
                updateDistributionCharts(data);
                updateHotPosts(data);
                updateActiveUsers(data);
                updateModerationPanel(data);
                updateAISuggestions(data);
            }
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            setLoading(false, "加载失败");
            showAlert(Alert.AlertType.ERROR, "错误", "数据加载失败，请检查网络连接");
        }));

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @SuppressWarnings("unchecked")
    private void updateOverview(Map<String, Object> data) {
        Map<String, Object> overview = (Map<String, Object>) data.get("overview");
        if (overview == null) return;

        System.out.println("=== updateOverview ===");
        System.out.println("overview data: " + overview);

        // 1. 总用户数
        animateValue(totalUserCountLabel, toInt(overview.get("userCount")));
        // 2. 今日新增用户
        animateValue(todayNewUsersLabel, toInt(overview.get("todayNewUsers")), "今日+");
        // 3. 月活跃用户
        animateValue(monthlyActiveUsersLabel, toInt(overview.get("monthlyActiveUsers")));
        // 4. 今日发帖
        animateValue(todayPostsLabel, toInt(overview.get("todayNewPosts")));
        // 5. 总帖子数
        animateValue(totalPostsLabel, toInt(overview.get("postCount")), "总计: ");
        // 6. 待审核
        animateValue(pendingModerationLabel, toInt(overview.get("pendingModerationCount")));
        // 7. 今日评论
        animateValue(todayCommentsLabel, toInt(overview.get("todayNewComments")));
        // 8. 总评论数
        animateValue(totalCommentsLabel, toInt(overview.get("commentCount")), "总计: ");
        // 9. 待处理举报（从overview直接获取）
        if (overview.get("pendingReports") != null) {
            animateValue(pendingReportsLabel, toInt(overview.get("pendingReports")));
        }
        // 10. AI审核通过率（从overview直接获取）
        if (overview.get("aiPassRate") != null) {
            double passRate = toDouble(overview.get("aiPassRate"));
            aiPassRateLabel.setText(String.format("%.1f%%", passRate));
        }

        // 同时也检查单独的reportStats和moderationOverview，作为后备方案
        Map<String, Object> reportStats = (Map<String, Object>) data.get("reportStats");
        if (reportStats != null && overview.get("pendingReports") == null) {
            System.out.println("reportStats data: " + reportStats);
            animateValue(pendingReportsLabel, toInt(reportStats.get("pendingReports")));
        }

        // AI 审核统计
        Map<String, Object> moderationOverview = (Map<String, Object>) data.get("moderationOverview");
        if (moderationOverview != null && overview.get("aiPassRate") == null) {
            System.out.println("moderationOverview data: " + moderationOverview);
            double passRate = toDouble(moderationOverview.get("passRate"));
            aiPassRateLabel.setText(String.format("%.1f%%", passRate));
            animateValue(totalModerationsLabel, toInt(moderationOverview.get("totalModeration")), "总审核: ");
        }

        // 根据待审核数量设置健康状态
        int pendingCount = toInt(overview.get("pendingModerationCount"));
        if (pendingCount > 100) {
            healthIndicator.setFill(javafx.scene.paint.Color.web("#f59e0b"));
            healthStatusLabel.setText("负载较高");
        } else if (pendingCount > 50) {
            healthIndicator.setFill(javafx.scene.paint.Color.web("#3b82f6"));
            healthStatusLabel.setText("运行正常");
        } else {
            healthIndicator.setFill(javafx.scene.paint.Color.web("#10b981"));
            healthStatusLabel.setText("运行正常");
        }
    }

    @SuppressWarnings("unchecked")
    private void updateUserGrowthChart(Map<String, Object> data) {
        List<Map<String, Object>> userGrowth = (List<Map<String, Object>>) data.get("userGrowth");
        if (userGrowth == null || userGrowth.isEmpty()) {
            System.out.println("userGrowth data is null or empty");
            return;
        }

        System.out.println("=== updateUserGrowthChart ===");
        System.out.println("userGrowth size: " + userGrowth.size());
        for (Map<String, Object> item : userGrowth) {
            System.out.println("  date: " + item.get("date") + ", count: " + item.get("count"));
        }

        userGrowthChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("新增用户");

        // 确保按日期排序
        List<Map<String, Object>> sortedData = new ArrayList<>(userGrowth);
        sortedData.sort(Comparator.comparing(item -> String.valueOf(item.get("date"))));

        for (Map<String, Object> item : sortedData) {
            String date = String.valueOf(item.get("date"));
            Number count = toNumber(item.get("count"));
            series.getData().add(new XYChart.Data<>(date, count));
        }

        userGrowthChart.getData().add(series);
        
        // 刷新图表
        userGrowthChart.layout();
    }

    @SuppressWarnings("unchecked")
    private void updatePostTrendChart(Map<String, Object> data) {
        List<Map<String, Object>> postTrend = (List<Map<String, Object>>) data.get("postTrend");
        if (postTrend == null || postTrend.isEmpty()) {
            System.out.println("postTrend data is null or empty");
            return;
        }

        System.out.println("=== updatePostTrendChart ===");
        System.out.println("postTrend size: " + postTrend.size());
        for (Map<String, Object> item : postTrend) {
            System.out.println("  date: " + item.get("date") + ", count: " + item.get("count"));
        }

        postTrendChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("发帖数");

        // 确保按日期排序
        List<Map<String, Object>> sortedData = new ArrayList<>(postTrend);
        sortedData.sort(Comparator.comparing(item -> String.valueOf(item.get("date"))));

        for (Map<String, Object> item : sortedData) {
            String date = String.valueOf(item.get("date"));
            Number count = toNumber(item.get("count"));
            series.getData().add(new XYChart.Data<>(date, count));
        }

        postTrendChart.getData().add(series);
        
        // 刷新图表
        postTrendChart.layout();
    }

    @SuppressWarnings("unchecked")
    private void updateDistributionCharts(Map<String, Object> data) {
        // 用户类型分布
        List<Map<String, Object>> userType = (List<Map<String, Object>>) data.get("userType");
        userTypePieChart.getData().clear();
        if (userType != null) {
            for (Map<String, Object> item : userType) {
                String name = String.valueOf(item.get("name"));
                int count = toInt(item.get("count"));
                if (count > 0) {
                    PieChart.Data slice = new PieChart.Data(name, count);
                    userTypePieChart.getData().add(slice);
                }
            }
        }

        // 帖子状态分布
        List<Map<String, Object>> postStatus = (List<Map<String, Object>>) data.get("postStatus");
        postStatusPieChart.getData().clear();
        if (postStatus != null) {
            for (Map<String, Object> item : postStatus) {
                String name = String.valueOf(item.get("name"));
                int count = toInt(item.get("count"));
                if (count > 0) {
                    PieChart.Data slice = new PieChart.Data(name, count);
                    postStatusPieChart.getData().add(slice);
                }
            }
        }

        // 违规类型分布
        List<Map<String, Object>> violationTypes = (List<Map<String, Object>>) data.get("violationTypes");
        violationTypeChart.getData().clear();
        if (violationTypes != null && !violationTypes.isEmpty()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("违规数量");

            for (Map<String, Object> item : violationTypes) {
                String name = String.valueOf(item.get("name"));
                int count = toInt(item.get("count"));
                series.getData().add(new XYChart.Data<>(name, count));
            }

            violationTypeChart.getData().add(series);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateHotPosts(Map<String, Object> data) {
        List<Map<String, Object>> hotPosts = (List<Map<String, Object>>) data.get("hotPosts");
        hotPostRows.clear();
        if (hotPosts != null) {
            int rank = 1;
            for (Map<String, Object> post : hotPosts) {
                String title = String.valueOf(post.get("title"));
                if (title.length() > 20) {
                    title = title.substring(0, 17) + "...";
                }
                Long postId = null;
                if (post.get("id") != null) {
                    postId = ((Number) post.get("id")).longValue();
                }
                hotPostRows.add(new HotPostRow(
                    rank++,
                    postId,
                    title,
                    toInt(post.get("likeCount")),
                    toInt(post.get("commentCount"))
                ));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void updateActiveUsers(Map<String, Object> data) {
        List<Map<String, Object>> activeUsers = (List<Map<String, Object>>) data.get("activeUsers");
        activeUserRows.clear();
        if (activeUsers != null) {
            int rank = 1;
            for (Map<String, Object> user : activeUsers) {
                Integer personId = null;
                if (user.get("personId") != null) {
                    personId = ((Number) user.get("personId")).intValue();
                }
                activeUserRows.add(new ActiveUserRow(
                    rank++,
                    personId,
                    String.valueOf(user.get("nickname")),
                    toInt(user.get("postCount")),
                    toInt(user.get("commentCount"))
                ));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void updateModerationPanel(Map<String, Object> data) {
        List<Map<String, Object>> moderationTrend = (List<Map<String, Object>>) data.get("moderationTrend");
        moderationTrendChart.getData().clear();

        if (moderationTrend != null && !moderationTrend.isEmpty()) {
            XYChart.Series<String, Number> passedSeries = new XYChart.Series<>();
            passedSeries.setName("通过");
            XYChart.Series<String, Number> rejectedSeries = new XYChart.Series<>();
            rejectedSeries.setName("违规");
            XYChart.Series<String, Number> pendingSeries = new XYChart.Series<>();
            pendingSeries.setName("待审");

            for (Map<String, Object> item : moderationTrend) {
                String date = String.valueOf(item.get("date"));
                passedSeries.getData().add(new XYChart.Data<>(date, toNumber(item.get("passed"))));
                rejectedSeries.getData().add(new XYChart.Data<>(date, toNumber(item.get("rejected"))));
                pendingSeries.getData().add(new XYChart.Data<>(date, toNumber(item.get("pending"))));
            }

            moderationTrendChart.getData().add(passedSeries);
            moderationTrendChart.getData().add(rejectedSeries);
            moderationTrendChart.getData().add(pendingSeries);

            // 计算今日审核数
            int todayCount = 0;
            if (!moderationTrend.isEmpty()) {
                Map<String, Object> today = moderationTrend.get(moderationTrend.size() - 1);
                todayCount = toInt(today.get("passed")) + toInt(today.get("rejected")) + toInt(today.get("pending"));
            }
            aiTodayCountLabel.setText("今日审核: " + todayCount);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateAISuggestions(Map<String, Object> data) {
        Map<String, Object> overview = (Map<String, Object>) data.get("overview");
        Map<String, Object> moderationOverview = (Map<String, Object>) data.get("moderationOverview");

        String suggestion1 = "系统运行正常，建议保持当前审核策略。";
        String suggestion2 = "近期帖子质量良好，社区氛围健康。";
        String suggestion3 = "暂无异常提醒。";

        if (overview != null && moderationOverview != null) {
            int pendingModeration = toInt(overview.get("pendingModerationCount"));
            double passRate = toDouble(moderationOverview.get("passRate"));

            if (pendingModeration > 50) {
                suggestion1 = String.format("当前待审核帖子较多（%d条），建议加快审核进度。", pendingModeration);
            }

            if (passRate < 80) {
                suggestion2 = String.format("近期内容审核通过率为 %.1f%%，略低于正常水平，建议关注内容质量。", passRate);
            }

            if (passRate > 95) {
                suggestion3 = "系统运行良好，社区内容质量较高，继续保持！";
            }
        }

        suggestion1Label.setText(suggestion1);
        suggestion2Label.setText(suggestion2);
        suggestion3Label.setText(suggestion3);
    }

    @FXML
    private void onUserTrend7Days() {
        currentUserTrendDays = 7;
        userTrend7Btn.getStyleClass().add("btn-period-active");
        userTrend30Btn.getStyleClass().remove("btn-period-active");
        loadUserGrowthTrend();
    }

    @FXML
    private void onUserTrend30Days() {
        currentUserTrendDays = 30;
        userTrend30Btn.getStyleClass().add("btn-period-active");
        userTrend7Btn.getStyleClass().remove("btn-period-active");
        loadUserGrowthTrend();
    }

    @FXML
    private void onPostTrend7Days() {
        currentPostTrendDays = 7;
        postTrend7Btn.getStyleClass().add("btn-period-active");
        postTrend30Btn.getStyleClass().remove("btn-period-active");
        loadPostTrend();
    }

    @FXML
    private void onPostTrend30Days() {
        currentPostTrendDays = 30;
        postTrend30Btn.getStyleClass().add("btn-period-active");
        postTrend7Btn.getStyleClass().remove("btn-period-active");
        loadPostTrend();
    }

    private void loadUserGrowthTrend() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() {
                return HttpRequestUtil.getList("/api/bbs/statistics/user-growth?days=" + currentUserTrendDays);
            }
        };

        task.setOnSucceeded(event -> {
            Map<String, Object> data = new HashMap<>();
            data.put("userGrowth", task.getValue());
            Platform.runLater(() -> updateUserGrowthChart(data));
        });

        new Thread(task).start();
    }

    private void loadPostTrend() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() {
                return HttpRequestUtil.getList("/api/bbs/statistics/post-trend?days=" + currentPostTrendDays);
            }
        };

        task.setOnSucceeded(event -> {
            Map<String, Object> data = new HashMap<>();
            data.put("postTrend", task.getValue());
            Platform.runLater(() -> updatePostTrendChart(data));
        });

        new Thread(task).start();
    }

    @FXML
    private void onNavigateToModeration() {
        // 跳转到内容审核页面
        AppStore.getMainFrameController().changeContent("AdminModeration", "内容审核");
    }

    @FXML
    private void onNavigateToReports() {
        // 跳转到举报处理页面
        AppStore.getMainFrameController().changeContent("AdminReport", "举报处理");
    }

    @FXML
    private void onViewAllHotPosts() {
        // TODO: 查看全部热门帖子
        showAlert(Alert.AlertType.INFORMATION, "提示", "正在加载全部热门帖子...");
    }

    @FXML
    private void onViewAllActiveUsers() {
        // TODO: 查看全部活跃用户
        showAlert(Alert.AlertType.INFORMATION, "提示", "正在加载全部活跃用户...");
    }

    @FXML
    private void onRefreshAISuggestions() {
        // TODO: 刷新 AI 建议
        loadAISuggestions();
    }

    private void loadAISuggestions() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                Map<String, Object> result = new HashMap<>();
                Map<String, Object> overview = HttpRequestUtil.get("/api/bbs/statistics/overview");
                Map<String, Object> moderationOverview = HttpRequestUtil.get("/api/bbs/statistics/moderation-overview");
                if (overview != null) result.put("overview", overview.get("data"));
                if (moderationOverview != null) result.put("moderationOverview", moderationOverview.get("data"));
                return result;
            }
        };

        task.setOnSucceeded(event -> {
            Map<String, Object> data = task.getValue();
            if (data != null) {
                Platform.runLater(() -> updateAISuggestions(data));
            }
        });

        new Thread(task).start();
    }

    // 快捷操作
    @FXML
    private void onQuickActionPosts() {
        // 跳转到帖子广场界面
        AppStore.getMainFrameController().changeContent("PostList", "帖子广场");
    }

    @FXML
    private void onQuickActionUsers() {
        // 用户管理已删除，但保留方法避免错误
    }

    @FXML
    private void onQuickActionReports() {
        // 跳转到举报处理界面
        AppStore.getMainFrameController().changeContent("AdminReport", "举报处理");
    }

    @FXML
    private void onQuickActionModeration() {
        // 跳转到内容审核界面
        AppStore.getMainFrameController().changeContent("AdminModeration", "内容审核");
    }

    @FXML
    private void onQuickActionBoards() {
        // 板块管理已删除，但保留方法避免错误
    }

    // 辅助方法
    private int toInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double toDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(obj));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private Number toNumber(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return (Number) obj;
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void animateValue(Label label, int targetValue) {
        animateValue(label, targetValue, "");
    }

    private void animateValue(Label label, int targetValue, String prefix) {
        AnimationTimer timer = new AnimationTimer() {
            private int current = 0;
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate > 16_000_000) { // ~60fps
                    if (current < targetValue) {
                        current += Math.ceil((targetValue - current) * 0.15);
                        if (current > targetValue) current = targetValue;
                    } else if (current > targetValue) {
                        current -= Math.ceil((current - targetValue) * 0.15);
                        if (current < targetValue) current = targetValue;
                    }
                    label.setText(prefix + current);
                    lastUpdate = now;

                    if (current == targetValue) {
                        stop();
                    }
                }
            }
        };
        timer.start();
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private void setLoading(boolean loading, String message) {
        Platform.runLater(() -> {
            if (loading) {
                refreshButton.setDisable(true);
                refreshButton.setText(message);
            } else {
                refreshButton.setDisable(false);
                refreshButton.setText("刷新数据");
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    // 内部类：热门帖子行
    public static class HotPostRow {
        private final int rank;
        private final Long id;
        private final String title;
        private final int likes;
        private final int comments;

        public HotPostRow(int rank, Long id, String title, int likes, int comments) {
            this.rank = rank;
            this.id = id;
            this.title = title;
            this.likes = likes;
            this.comments = comments;
        }

        public int rank() { return rank; }
        public Long id() { return id; }
        public String title() { return title; }
        public int likes() { return likes; }
        public int comments() { return comments; }
    }

    // 内部类：活跃用户行
    public static class ActiveUserRow {
        private final int rank;
        private final Integer personId;
        private final String nickname;
        private final int posts;
        private final int comments;

        public ActiveUserRow(int rank, Integer personId, String nickname, int posts, int comments) {
            this.rank = rank;
            this.personId = personId;
            this.nickname = nickname;
            this.posts = posts;
            this.comments = comments;
        }

        public int rank() { return rank; }
        public Integer personId() { return personId; }
        public String nickname() { return nickname; }
        public int posts() { return posts; }
        public int comments() { return comments; }
    }
}
