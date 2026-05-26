package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UserStatisticsController extends ToolController {
    private static final double WHEEL_STEP_PX = 120.0;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int TREND_BUCKET_DAYS = 3;
    private static final List<String> INTERACTION_BAR_COLORS = List.of(
            "#4f8fbf", "#63a78a", "#7f95c9", "#5fa8a0", "#9aa7b8"
    );
    private static final List<String> STATUS_BAR_COLORS = List.of(
            "#4f8fbf", "#7f95c9", "#9aa7b8", "#b77985"
    );

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
    private BarChart<String, Number> interactionStructureChart;
    @FXML
    private CategoryAxis interactionStructureXAxis;
    @FXML
    private NumberAxis interactionStructureYAxis;
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
        interactionStructureYAxis.setForceZeroInRange(true);
        postStatusYAxis.setForceZeroInRange(true);
        contentTrendYAxis.setMinorTickVisible(false);
        interactionTrendYAxis.setMinorTickVisible(false);
        interactionStructureYAxis.setMinorTickVisible(false);
        postStatusYAxis.setMinorTickVisible(false);
        contentTrendYAxis.setTickUnit(1);
        interactionTrendYAxis.setTickUnit(1);
        contentTrendXAxis.setLabel("");
        contentTrendYAxis.setLabel("");
        interactionTrendXAxis.setLabel("");
        interactionTrendYAxis.setLabel("");
        interactionStructureXAxis.setLabel("");
        interactionStructureYAxis.setLabel("");
        postStatusXAxis.setLabel("");
        postStatusYAxis.setLabel("");
        contentTrendXAxis.setTickLabelRotation(0);
        interactionTrendXAxis.setTickLabelRotation(0);
        interactionStructureXAxis.setTickLabelRotation(0);
        postStatusXAxis.setTickLabelRotation(0);
        interactionStructureChart.setCategoryGap(26);
        interactionStructureChart.setBarGap(6);
        postStatusChart.setCategoryGap(34);
        postStatusChart.setBarGap(6);
        styleXYChart(contentTrendChart);
        styleXYChart(interactionTrendChart);
        styleXYChart(interactionStructureChart);
        styleXYChart(postStatusChart);
        
        contentTrendChart.setLegendSide(javafx.geometry.Side.BOTTOM);
        interactionTrendChart.setLegendSide(javafx.geometry.Side.BOTTOM);
        postStatusChart.setLegendSide(javafx.geometry.Side.BOTTOM);
    }

    private void styleXYChart(XYChart<?, ?> chart) {
        chart.setVerticalGridLinesVisible(false);
        chart.setAlternativeColumnFillVisible(false);
        chart.setAlternativeRowFillVisible(false);
        chart.setVerticalZeroLineVisible(false);
        chart.setHorizontalZeroLineVisible(false);
    }
    
    private void updateLegendLayout() {
        Platform.runLater(() -> {
            setupLegendLayout(contentTrendChart);
            setupLegendLayout(interactionTrendChart);
            setupLegendLayout(postStatusChart);
        });
    }
    
    private void setupLegendLayout(javafx.scene.chart.Chart chart) {
        javafx.scene.Node legend = chart.lookup(".chart-legend");
        if (legend != null) {
            if (legend instanceof javafx.scene.layout.FlowPane flowPane) {
                flowPane.setPrefWrapLength(620);
                flowPane.setHgap(14);
                flowPane.setVgap(6);
                flowPane.setMinWidth(Region.USE_COMPUTED_SIZE);
                flowPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
                flowPane.setMaxWidth(Region.USE_COMPUTED_SIZE);
            }
        }
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
        
        topPostTableView.setRowFactory(tv -> {
            TableRow<TopPostRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1 && !row.isEmpty()) {
                    TopPostRow post = row.getItem();
                    openPostDetail(post.postId());
                }
            });
            return row;
        });
    }

    private void openPostDetail(Long postId) {
        if (postId != null && AppStore.getMainFrameController() != null) {
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
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        
        // 分多次延迟应用图例布局，确保在界面渲染稳定后生效
        Platform.runLater(() -> updateLegendLayout());
        javafx.animation.PauseTransition pause1 = new javafx.animation.PauseTransition(javafx.util.Duration.millis(50));
        pause1.setOnFinished(event -> updateLegendLayout());
        pause1.play();
        javafx.animation.PauseTransition pause2 = new javafx.animation.PauseTransition(javafx.util.Duration.millis(200));
        pause2.setOnFinished(event -> updateLegendLayout());
        pause2.play();
    }

    private void updateWithEmptyData() {
        updateOverview(Collections.emptyMap());
        contentTrendChart.getData().clear();
        interactionTrendChart.getData().clear();
        interactionStructureChart.getData().clear();
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

        List<String> last30Days = generateLast30Days();
        List<DateBucket> trendBuckets = buildDateBuckets(last30Days);
        
        addLineSeriesWithBuckets(contentTrendChart, "发帖", asList(trends.get("postTrend")), trendBuckets);
        addLineSeriesWithBuckets(contentTrendChart, "评论", asList(trends.get("commentTrend")), trendBuckets);

        addLineSeriesWithBuckets(interactionTrendChart, "获赞", asList(trends.get("receivedLikeTrend")), trendBuckets);
        addLineSeriesWithBuckets(interactionTrendChart, "被收藏", asList(trends.get("favoriteTrend")), trendBuckets);
        addLineSeriesWithBuckets(interactionTrendChart, "新增粉丝", asList(trends.get("followTrend")), trendBuckets);
    }

    private List<String> generateLast30Days() {
        List<String> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            dates.add(today.minusDays(i).format(DATE_FORMATTER));
        }
        return dates;
    }

    private List<DateBucket> buildDateBuckets(List<String> allDates) {
        List<DateBucket> buckets = new ArrayList<>();
        for (int i = 0; i < allDates.size(); i += TREND_BUCKET_DAYS) {
            List<String> dates = allDates.subList(i, Math.min(i + TREND_BUCKET_DAYS, allDates.size()));
            String start = dates.get(0);
            String end = dates.get(dates.size() - 1);
            String label = bucketLabel(start, end);
            buckets.add(new DateBucket(label, new ArrayList<>(dates)));
        }
        return buckets;
    }

    private void addLineSeriesWithBuckets(LineChart<String, Number> chart, String name, List<Map<String, Object>> data, List<DateBucket> buckets) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(name);
        
        Map<String, Integer> dataMap = new HashMap<>();
        for (Map<String, Object> item : data) {
            String date = getString(item, "date");
            int count = getInt(item, "count");
            dataMap.put(date, count);
        }
        
        for (DateBucket bucket : buckets) {
            int count = 0;
            for (String date : bucket.dates()) {
                count += dataMap.getOrDefault(date, 0);
            }
            series.getData().add(new XYChart.Data<>(bucket.label(), count));
        }
        
        if (series.getData().isEmpty()) {
            series.getData().add(new XYChart.Data<>("暂无", 0));
        }
        chart.getData().add(series);
    }

    private String bucketLabel(String start, String end) {
        if (start == null || end == null || start.length() < 10 || end.length() < 10) {
            return shortDate(start) + "-" + shortDate(end);
        }
        String startMonth = start.substring(5, 7);
        String startDay = start.substring(8, 10);
        String endMonth = end.substring(5, 7);
        String endDay = end.substring(8, 10);
        if (startMonth.equals(endMonth)) {
            return startMonth + "/" + startDay + "-" + endDay;
        }
        return startMonth + "/" + startDay + "-" + endMonth + "/" + endDay;
    }

    private String shortDate(String date) {
        return date != null && date.length() > 5 ? date.substring(5).replace("-", "/") : String.valueOf(date);
    }

    private void updateDistribution(Map<String, Object> distribution, Map<String, Object> overview) {
        updateInteractionStructureChart(asList(distribution.get("interaction")), overview);
        updatePostStatusChart(asList(distribution.get("postStatus")));
    }

    private void updateInteractionStructureChart(List<Map<String, Object>> items, Map<String, Object> overview) {
        List<Map<String, Object>> source = items;
        if (source.isEmpty()) {
            source = new ArrayList<>();
            source.add(Map.of("name", "浏览", "count", getInt(overview, "totalViewCount")));
            source.add(Map.of("name", "获赞", "count", getInt(overview, "totalLikeCount")));
            source.add(Map.of("name", "被收藏", "count", getInt(overview, "totalFavoriteCount")));
            source.add(Map.of("name", "评论", "count", getInt(overview, "commentCount")));
            source.add(Map.of("name", "粉丝", "count", getInt(overview, "followerCount")));
        }

        interactionStructureChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("互动结构");
        List<Integer> values = new ArrayList<>();
        for (Map<String, Object> item : source) {
            int count = getInt(item, "count");
            values.add(count);
            series.getData().add(new XYChart.Data<>(getString(item, "name"), count));
        }
        if (series.getData().isEmpty()) {
            series.getData().add(new XYChart.Data<>("暂无", 0));
            values.add(0);
        }
        interactionStructureChart.getData().add(series);
        configureBarAxis(interactionStructureYAxis, values);
        decorateBarSeries(series, INTERACTION_BAR_COLORS);
    }

    private void updatePostStatusChart(List<Map<String, Object>> items) {
        postStatusChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("帖子状态");
        Map<String, Integer> values = new LinkedHashMap<>();
        values.put("已发布", 0);
        values.put("待审核", 0);
        values.put("草稿", 0);
        values.put("已隐藏", 0);

        for (Map<String, Object> item : items) {
            String normalizedName = normalizePostStatus(getString(item, "name"));
            values.merge(normalizedName, getInt(item, "count"), Integer::sum);
        }
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        if (series.getData().isEmpty()) {
            series.getData().add(new XYChart.Data<>("暂无", 0));
        }
        postStatusChart.getData().add(series);
        configureBarAxis(postStatusYAxis, values.values());
        decorateBarSeries(series, STATUS_BAR_COLORS);
    }

    private void configureBarAxis(NumberAxis axis, Collection<Integer> values) {
        int max = values.stream().mapToInt(Integer::intValue).max().orElse(0);
        double upperBound = niceUpperBound(max);
        axis.setAutoRanging(false);
        axis.setLowerBound(0);
        axis.setUpperBound(upperBound);
        axis.setTickUnit(Math.max(1, upperBound / 5));
    }

    private double niceUpperBound(int max) {
        if (max <= 0) {
            return 5;
        }
        double padded = max * 1.18;
        double magnitude = Math.pow(10, Math.floor(Math.log10(padded)));
        double normalized = padded / magnitude;
        double niceNormalized;
        if (normalized <= 1.5) {
            niceNormalized = 1.5;
        } else if (normalized <= 2) {
            niceNormalized = 2;
        } else if (normalized <= 2.5) {
            niceNormalized = 2.5;
        } else if (normalized <= 5) {
            niceNormalized = 5;
        } else {
            niceNormalized = 10;
        }
        return niceNormalized * magnitude;
    }

    private void decorateBarSeries(XYChart.Series<String, Number> series, List<String> colors) {
        Platform.runLater(() -> {
            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data<String, Number> data = series.getData().get(i);
                if (data.getNode() != null) {
                    String color = colors.get(i % colors.size());
                    data.getNode().setStyle("-fx-bar-fill: " + color + "; -fx-background-color: " + color + ";");
                }
                if (data.getYValue().doubleValue() <= 0) {
                    continue;
                }
                if (data.getNode() instanceof StackPane node) {
                    Label label = new Label(String.valueOf(data.getYValue().intValue()));
                    label.getStyleClass().add("statistics-bar-value");
                    label.setMouseTransparent(true);
                    label.setTranslateY(-18);
                    StackPane.setAlignment(label, Pos.TOP_CENTER);
                    node.getChildren().removeIf(child -> child instanceof Label && child.getStyleClass().contains("statistics-bar-value"));
                    node.getChildren().add(label);
                    label.toFront();
                }
            }
        });
    }

    private String normalizePostStatus(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.contains("待") || name.equalsIgnoreCase("pending")) {
            return "待审核";
        }
        if (name.contains("草稿") || name.equalsIgnoreCase("draft")) {
            return "草稿";
        }
        if (name.contains("隐藏") || name.contains("删除") || name.equalsIgnoreCase("hidden")) {
            return "已隐藏";
        }
        if (name.isEmpty() || name.contains("发布") || name.contains("true") || name.equalsIgnoreCase("published")) {
            return "已发布";
        }
        if (name.contains("false")) {
            return "已隐藏";
        }
        return name;
    }

    private void updateTopPosts(List<Map<String, Object>> posts) {
        topPostRows.clear();
        int rank = 1;
        for (Map<String, Object> post : posts) {
            topPostRows.add(new TopPostRow(
                    rank++,
                    getLong(post, "id"),
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

    private long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(value.toString());
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
            Long postId,
            String title,
            int viewCount,
            int likeCount,
            int commentCount,
            int favoriteCount,
            int heat,
            String createTime
    ) {
    }

    private record DateBucket(String label, List<String> dates) {
    }
}
