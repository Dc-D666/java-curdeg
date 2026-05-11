package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.HttpRequestUtil;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UserStatisticsController extends ToolController {
    private static final double WHEEL_STEP_PX = 120.0;

    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private Label statusLabel;
    @FXML
    private Button refreshButton;
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
    private Label heatScoreLabel;
    @FXML
    private LineChart<String, Number> contentTrendChart;
    @FXML
    private CategoryAxis contentTrendXAxis;
    @FXML
    private NumberAxis contentTrendYAxis;
    @FXML
    private LineChart<String, Number> interactionTrendChart;
    @FXML
    private CategoryAxis interactionTrendXAxis;
    @FXML
    private NumberAxis interactionTrendYAxis;
    @FXML
    private PieChart interactionPieChart;
    @FXML
    private BarChart<String, Number> postStatusChart;
    @FXML
    private CategoryAxis postStatusXAxis;
    @FXML
    private NumberAxis postStatusYAxis;
    @FXML
    private TableView<TopPostRow> topPostTableView;
    @FXML
    private TableColumn<TopPostRow, Number> rankColumn;
    @FXML
    private TableColumn<TopPostRow, String> titleColumn;
    @FXML
    private TableColumn<TopPostRow, Number> viewColumn;
    @FXML
    private TableColumn<TopPostRow, Number> likeColumn;
    @FXML
    private TableColumn<TopPostRow, Number> commentColumn;
    @FXML
    private TableColumn<TopPostRow, Number> favoriteColumn;
    @FXML
    private TableColumn<TopPostRow, Number> heatColumn;
    @FXML
    private TableColumn<TopPostRow, String> createTimeColumn;

    private final ObservableList<TopPostRow> topPostRows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupPageScroll();
        setupCharts();
        setupTable();
        loadUserStatistics();
    }

    @FXML
    private void onRefresh() {
        loadUserStatistics();
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
        contentTrendChart.setCreateSymbols(false);
        interactionTrendChart.setCreateSymbols(false);
        contentTrendYAxis.setForceZeroInRange(true);
        interactionTrendYAxis.setForceZeroInRange(true);
        postStatusYAxis.setForceZeroInRange(true);
    }

    private void setupTable() {
        rankColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().rank()));
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().title()));
        viewColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().viewCount()));
        likeColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().likeCount()));
        commentColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().commentCount()));
        favoriteColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().favoriteCount()));
        heatColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().heat()));
        createTimeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().createTime()));
        topPostTableView.setItems(topPostRows);
    }

    private void loadUserStatistics() {
        setLoading(true, "正在加载...");
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                Map<String, Object> detail = HttpRequestUtil.getUserStatisticsDetail();
                if (detail != null) {
                    return detail;
                }
                Map<String, Object> overview = HttpRequestUtil.getUserStatistics();
                if (overview == null) {
                    return null;
                }
                return Map.of("overview", overview);
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            setLoading(false, "已更新");
            Map<String, Object> statistics = task.getValue();
            if (statistics == null || statistics.isEmpty()) {
                updateWithEmptyData();
                statusLabel.setText("暂无数据");
                return;
            }
            updateUI(statistics);
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            setLoading(false, "加载失败");
            updateWithEmptyData();
        }));

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateUI(Map<String, Object> statistics) {
        Map<String, Object> overview = asMap(statistics.get("overview"));
        if (overview.isEmpty()) {
            overview = statistics;
        }

        updateOverview(overview);
        updateTrends(asMap(statistics.get("trends")));
        updateDistribution(asMap(statistics.get("distribution")), overview);
        updateTopPosts(asList(statistics.get("topPosts")));
    }

    private void updateWithEmptyData() {
        updateOverview(Collections.emptyMap());
        contentTrendChart.getData().clear();
        interactionTrendChart.getData().clear();
        interactionPieChart.getData().clear();
        postStatusChart.getData().clear();
        topPostRows.clear();
    }

    private void updateOverview(Map<String, Object> overview) {
        int postCount = getInt(overview, "postCount");
        int commentCount = getInt(overview, "commentCount");
        int likeCount = getInt(overview, "totalLikeCount");
        int favoriteCount = getInt(overview, "totalFavoriteCount");
        int viewCount = getInt(overview, "totalViewCount");
        int followingCount = getInt(overview, "followingCount");
        int followerCount = getInt(overview, "followerCount");
        int heatScore = viewCount + likeCount * 5 + commentCount * 3 + favoriteCount * 4 + followerCount * 6;

        postCountLabel.setText(formatNumber(postCount));
        commentCountLabel.setText(formatNumber(commentCount));
        likeCountLabel.setText(formatNumber(likeCount));
        favoriteCountLabel.setText(formatNumber(favoriteCount));
        viewCountLabel.setText(formatNumber(viewCount));
        followingCountLabel.setText(formatNumber(followingCount));
        followerCountLabel.setText(formatNumber(followerCount));
        heatScoreLabel.setText(formatNumber(heatScore));
    }

    private void updateTrends(Map<String, Object> trends) {
        contentTrendChart.getData().clear();
        interactionTrendChart.getData().clear();

        addLineSeries(contentTrendChart, "发帖", asList(trends.get("postTrend")));
        addLineSeries(contentTrendChart, "评论", asList(trends.get("commentTrend")));

        addLineSeries(interactionTrendChart, "获赞", asList(trends.get("receivedLikeTrend")));
        addLineSeries(interactionTrendChart, "被收藏", asList(trends.get("favoriteTrend")));
        addLineSeries(interactionTrendChart, "新增粉丝", asList(trends.get("followTrend")));
    }

    private void addLineSeries(LineChart<String, Number> chart, String name, List<Map<String, Object>> data) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(name);
        for (Map<String, Object> item : data) {
            String date = getString(item, "date");
            if (date.length() > 5) {
                date = date.substring(5);
            }
            series.getData().add(new XYChart.Data<>(date, getInt(item, "count")));
        }
        if (series.getData().isEmpty()) {
            series.getData().add(new XYChart.Data<>("暂无", 0));
        }
        chart.getData().add(series);
    }

    private void updateDistribution(Map<String, Object> distribution, Map<String, Object> overview) {
        updateInteractionPie(asList(distribution.get("interaction")), overview);
        updatePostStatusChart(asList(distribution.get("postStatus")));
    }

    private void updateInteractionPie(List<Map<String, Object>> items, Map<String, Object> overview) {
        interactionPieChart.getData().clear();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        List<Map<String, Object>> source = items;
        if (source.isEmpty()) {
            source = new ArrayList<>();
            source.add(Map.of("name", "浏览", "count", getInt(overview, "totalViewCount")));
            source.add(Map.of("name", "获赞", "count", getInt(overview, "totalLikeCount")));
            source.add(Map.of("name", "被收藏", "count", getInt(overview, "totalFavoriteCount")));
            source.add(Map.of("name", "评论", "count", getInt(overview, "commentCount")));
            source.add(Map.of("name", "粉丝", "count", getInt(overview, "followerCount")));
        }

        for (Map<String, Object> item : source) {
            int count = getInt(item, "count");
            if (count > 0) {
                pieData.add(new PieChart.Data(getString(item, "name"), count));
            }
        }
        if (pieData.isEmpty()) {
            pieData.add(new PieChart.Data("暂无数据", 1));
        }
        interactionPieChart.setData(pieData);
    }

    private void updatePostStatusChart(List<Map<String, Object>> items) {
        postStatusChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("帖子状态");
        for (Map<String, Object> item : items) {
            series.getData().add(new XYChart.Data<>(getString(item, "name"), getInt(item, "count")));
        }
        if (series.getData().isEmpty()) {
            series.getData().add(new XYChart.Data<>("暂无", 0));
        }
        postStatusChart.getData().add(series);
    }

    private void updateTopPosts(List<Map<String, Object>> posts) {
        topPostRows.clear();
        int rank = 1;
        for (Map<String, Object> post : posts) {
            topPostRows.add(new TopPostRow(
                    rank++,
                    getString(post, "title"),
                    getInt(post, "viewCount"),
                    getInt(post, "likeCount"),
                    getInt(post, "commentCount"),
                    getInt(post, "favoriteCount"),
                    getInt(post, "heat"),
                    getString(post, "createTime")
            ));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asList(Object value) {
        if (value instanceof List<?> list) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    result.add((Map<String, Object>) map);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    private int getInt(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return (int) Double.parseDouble(value.toString());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : value.toString();
    }

    private String formatNumber(int value) {
        return String.format("%,d", value);
    }

    private void setLoading(boolean loading, String message) {
        refreshButton.setDisable(loading);
        statusLabel.setText(message);
    }

    private double clamp(double value) {
        if (value < 0) {
            return 0;
        }
        if (value > 1) {
            return 1;
        }
        return value;
    }

    public record TopPostRow(
            int rank,
            String title,
            int viewCount,
            int likeCount,
            int commentCount,
            int favoriteCount,
            int heat,
            String createTime
    ) {
    }
}
