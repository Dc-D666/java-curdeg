package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.Notification;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyNotificationController extends ToolController {

    private enum FilterMode { ALL, UNREAD, READ }

    @FXML
    private ListView<Object> notificationListView;

    @FXML
    private Label unreadBadge;

    @FXML
    private Label bottomInfoLabel;

    @FXML
    private Button tabAll;

    @FXML
    private Button tabUnread;

    @FXML
    private Button tabRead;

    @FXML
    private Button markAllReadButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button emptyRefreshButton;

    @FXML
    private VBox loadingPane;

    @FXML
    private VBox emptyPane;

    private List<Notification> allNotifications = new ArrayList<>();
    private FilterMode filterMode = FilterMode.ALL;

    private static final Pattern POST_ID_PATTERN = Pattern.compile(
            "(?:帖子ID|帖子 ID|postId|post_id|帖子)[:：]?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONTENT_ID_PATTERN = Pattern.compile(
            "(?:内容ID|内容 ID|ID)[:：]?\\s*(\\d+)");

    private static final DateTimeFormatter FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final int TYPE_SYSTEM = 1;
    private static final int TYPE_REPORT = 2;
    private static final int TYPE_REVIEW = 3;
    private static final int TYPE_COMMENT = 4;
    private static final int TYPE_FOLLOWER = 5;
    private static final int TYPE_FOLLOW_POST = 6;

    static class DateHeader {
        String dateLabel;
        DateHeader(String dateLabel) { this.dateLabel = dateLabel; }
    }

    static class NotificationItem {
        Notification notification;
        NotificationItem(Notification notification) { this.notification = notification; }
    }

    @FXML
    public void initialize() {
        setupTabs();
        setupListView();
        setupButtonActions();
        showLoading();
        loadData();
    }

    private void setupTabs() {
        tabAll.setOnAction(e -> switchTab(FilterMode.ALL));
        tabUnread.setOnAction(e -> switchTab(FilterMode.UNREAD));
        tabRead.setOnAction(e -> switchTab(FilterMode.READ));
    }

    private void switchTab(FilterMode mode) {
        this.filterMode = mode;
        updateTabStyles();
        rebuildDisplayList();
    }

    private void updateTabStyles() {
        tabAll.getStyleClass().removeAll("tab-btn-active");
        tabUnread.getStyleClass().removeAll("tab-btn-active");
        tabRead.getStyleClass().removeAll("tab-btn-active");

        switch (filterMode) {
            case ALL: tabAll.getStyleClass().add("tab-btn-active"); break;
            case UNREAD: tabUnread.getStyleClass().add("tab-btn-active"); break;
            case READ: tabRead.getStyleClass().add("tab-btn-active"); break;
        }

        int total = allNotifications.size();
        long unread = allNotifications.stream().filter(n -> n.getIsRead() == null || n.getIsRead() != 1).count();
        long read = total - unread;

        tabAll.setText("全部 " + total);
        tabUnread.setText("未读 " + unread);
        tabRead.setText("已读 " + read);
    }

    private void setupListView() {
        notificationListView.setCellFactory(lv -> new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setPadding(Insets.EMPTY);
                } else if (item instanceof DateHeader) {
                    setGraphic(createDateHeader((DateHeader) item));
                    setText(null);
                    setPadding(new Insets(0, 0, 0, 0));
                } else if (item instanceof NotificationItem) {
                    setGraphic(createNotificationCard(((NotificationItem) item).notification));
                    setText(null);
                    setPadding(new Insets(4, 20, 4, 20));
                }
            }
        });
    }

    private void setupButtonActions() {
        markAllReadButton.setOnAction(event -> markAllAsRead());
        refreshButton.setOnAction(event -> { showLoading(); loadData(); });
        emptyRefreshButton.setOnAction(event -> { showLoading(); loadData(); });
    }

    private void loadData() {
        Task<List<Notification>> task = new Task<List<Notification>>() {
            @Override
            protected List<Notification> call() {
                return HttpRequestUtil.getMyNotificationList(null, null);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                hideLoading();
                List<Notification> notifications = task.getValue();
                if (notifications != null && !notifications.isEmpty()) {
                    allNotifications = notifications;
                    sortNotifications();
                    updateTabStyles();
                    rebuildDisplayList();
                    loadUnreadCount();
                } else {
                    allNotifications = new ArrayList<>();
                    updateTabStyles();
                    showEmptyState();
                    bottomInfoLabel.setText("共 0 条通知");
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                hideLoading();
                showError("加载通知列表失败");
                bottomInfoLabel.setText("加载失败");
            });
        });

        new Thread(task).start();
    }

    private void loadUnreadCount() {
        Task<Long> task = new Task<Long>() {
            @Override
            protected Long call() {
                return HttpRequestUtil.getUnreadNotificationCount();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Long count = task.getValue();
                int unread = count != null ? count.intValue() : 0;
                unreadBadge.setText(unread + " 条未读");
                unreadBadge.setVisible(unread > 0);
                unreadBadge.setManaged(unread > 0);
                bottomInfoLabel.setText("共 " + allNotifications.size() + " 条通知 · " + unread + " 条未读");
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                bottomInfoLabel.setText("共 " + allNotifications.size() + " 条通知");
            });
        });

        new Thread(task).start();
    }

    private void sortNotifications() {
        allNotifications.sort((a, b) -> {
            String ta = a.getCreateTime() != null ? a.getCreateTime() : "";
            String tb = b.getCreateTime() != null ? b.getCreateTime() : "";
            return tb.compareTo(ta);
        });
    }

    private void rebuildDisplayList() {
        hideEmptyState();

        List<Notification> filtered = new ArrayList<>();
        for (Notification n : allNotifications) {
            boolean isUnread = n.getIsRead() == null || n.getIsRead() != 1;
            switch (filterMode) {
                case ALL: filtered.add(n); break;
                case UNREAD: if (isUnread) filtered.add(n); break;
                case READ: if (!isUnread) filtered.add(n); break;
            }
        }

        List<Object> displayList = new ArrayList<>();
        String currentDateLabel = null;

        for (Notification n : filtered) {
            String dateLabel = getDateLabel(n.getCreateTime());
            if (!dateLabel.equals(currentDateLabel)) {
                currentDateLabel = dateLabel;
                displayList.add(new DateHeader(dateLabel));
            }
            displayList.add(new NotificationItem(n));
        }

        notificationListView.getItems().clear();
        notificationListView.getItems().addAll(displayList);

        if (filtered.isEmpty()) {
            showEmptyState();
        }
    }

    private void showLoading() {
        loadingPane.setVisible(true); loadingPane.setManaged(true);
        notificationListView.setVisible(false); notificationListView.setManaged(false);
        emptyPane.setVisible(false); emptyPane.setManaged(false);
    }

    private void hideLoading() {
        loadingPane.setVisible(false); loadingPane.setManaged(false);
        notificationListView.setVisible(true); notificationListView.setManaged(true);
    }

    private void showEmptyState() {
        emptyPane.setVisible(true); emptyPane.setManaged(true);
        notificationListView.setVisible(false); notificationListView.setManaged(false);
        loadingPane.setVisible(false); loadingPane.setManaged(false);
    }

    private void hideEmptyState() {
        emptyPane.setVisible(false); emptyPane.setManaged(false);
        notificationListView.setVisible(true); notificationListView.setManaged(true);
    }

    // ---- Card Rendering ----

    private Node createDateHeader(DateHeader header) {
        HBox container = new HBox(8);
        container.getStyleClass().add("date-header-hbox");

        Region bar = new Region();
        bar.getStyleClass().add("date-header-bar");

        Label label = new Label(header.dateLabel);
        label.getStyleClass().add("date-header-label");

        container.getChildren().addAll(bar, label);
        return container;
    }

    private Node createNotificationCard(Notification notification) {
        Integer type = notification.getType();
        boolean isRead = notification.getIsRead() != null && notification.getIsRead() == 1;
        Long postId = extractPostId(notification);

        // Card container
        HBox card = new HBox(14);
        card.setAlignment(Pos.TOP_LEFT);
        card.getStyleClass().add("notification-card");
        if (isRead) {
            card.getStyleClass().add("notification-card-read");
        }

        // Left: unread dot
        VBox dotColumn = new VBox();
        dotColumn.setAlignment(Pos.TOP_CENTER);
        dotColumn.setMinWidth(8);
        dotColumn.setPrefWidth(8);
        dotColumn.setPadding(new Insets(6, 0, 0, 0));

        if (!isRead) {
            Region dot = new Region();
            dot.getStyleClass().add("unread-dot-box");
            dotColumn.getChildren().add(dot);
        }

        // Center: type icon
        StackPane iconPane = createTypeIcon(type);

        // Right: text content
        VBox textContent = new VBox(8);
        textContent.setMinWidth(0);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        // Title row
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label typeLabel = new Label(getTypeText(type));
        typeLabel.getStyleClass().add("card-type-label");
        if (isRead) {
            typeLabel.getStyleClass().add("card-type-label-read");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeLabel = new Label(getRelativeTime(notification.getCreateTime()));
        timeLabel.getStyleClass().add("card-time-label");

        titleRow.getChildren().addAll(typeLabel, spacer, timeLabel);

        // Review tag for type=3
        if (type != null && type == TYPE_REVIEW) {
            String reviewResult = extractReviewResult(notification.getContent());
            if (reviewResult != null) {
                Label reviewTag = new Label(reviewResult);
                reviewTag.getStyleClass().add("review-tag");
                if (reviewResult.contains("通过") && !reviewResult.contains("未")) {
                    reviewTag.getStyleClass().add("review-tag-pass");
                } else if (reviewResult.contains("未通过") || reviewResult.contains("拒绝")) {
                    reviewTag.getStyleClass().add("review-tag-fail");
                } else {
                    reviewTag.getStyleClass().add("review-tag-pending");
                }
                titleRow.getChildren().add(2, reviewTag);
            }
        }

        // Content preview
        Label contentLabel = new Label(getContentPreview(notification));
        contentLabel.getStyleClass().add("card-content-label");
        if (isRead) {
            contentLabel.getStyleClass().add("card-content-label-read");
        }
        contentLabel.setWrapText(true);

        textContent.getChildren().addAll(titleRow, contentLabel);

        // Action buttons row
        HBox actionRow = createActionRow(notification, postId);
        textContent.getChildren().add(actionRow);

        boolean currentlyUnread = !isRead;
        actionRow.setVisible(currentlyUnread);
        actionRow.setManaged(currentlyUnread);

        card.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
            boolean curUnread = notification.getIsRead() == null || notification.getIsRead() != 1;
            actionRow.setVisible(isHovered || curUnread);
            actionRow.setManaged(isHovered || curUnread);
        });

        card.getChildren().addAll(dotColumn, iconPane, textContent);

        // Click handler
        card.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                boolean curUnread = notification.getIsRead() == null || notification.getIsRead() != 1;
                if (event.getClickCount() == 1) {
                    if (curUnread) markAsRead(notification);
                } else if (event.getClickCount() == 2 && postId != null) {
                    if (curUnread) markAsRead(notification);
                    openPostDetail(postId);
                }
            }
        });

        // Right-click context menu
        card.setOnContextMenuRequested(event -> {
            ContextMenu contextMenu = new ContextMenu();
            boolean curUnread = notification.getIsRead() == null || notification.getIsRead() != 1;
            if (curUnread) {
                MenuItem markReadItem = new MenuItem("标记为已读");
                markReadItem.setOnAction(e -> markAsRead(notification));
                contextMenu.getItems().add(markReadItem);
            }
            if (postId != null) {
                MenuItem openPostItem = new MenuItem("查看原帖");
                openPostItem.setOnAction(e -> {
                    if (notification.getIsRead() == null || notification.getIsRead() != 1) {
                        markAsRead(notification);
                    }
                    openPostDetail(postId);
                });
                contextMenu.getItems().add(openPostItem);
            }
            if (!contextMenu.getItems().isEmpty()) {
                contextMenu.show(card, event.getScreenX(), event.getScreenY());
            }
        });

        return card;
    }

    private StackPane createTypeIcon(Integer type) {
        StackPane iconPane = new StackPane();
        iconPane.getStyleClass().add("type-icon-box");

        Label iconLabel = new Label();
        iconLabel.getStyleClass().add("type-icon-text");

        String boxStyle;
        String textStyle;
        String text;

        switch (type != null ? type : 0) {
            case TYPE_SYSTEM:
                boxStyle = "type-icon-box-system"; textStyle = "type-icon-text-system"; text = "系"; break;
            case TYPE_REPORT:
                boxStyle = "type-icon-box-report"; textStyle = "type-icon-text-report"; text = "举"; break;
            case TYPE_REVIEW:
                boxStyle = "type-icon-box-review"; textStyle = "type-icon-text-review"; text = "审"; break;
            case TYPE_COMMENT:
                boxStyle = "type-icon-box-comment"; textStyle = "type-icon-text-comment"; text = "评"; break;
            case TYPE_FOLLOWER:
                boxStyle = "type-icon-box-follower"; textStyle = "type-icon-text-follower"; text = "粉"; break;
            case TYPE_FOLLOW_POST:
                boxStyle = "type-icon-box-follow-post"; textStyle = "type-icon-text-follow-post"; text = "关"; break;
            default:
                boxStyle = "type-icon-box-system"; textStyle = "type-icon-text-system"; text = "?"; break;
        }

        iconPane.getStyleClass().add(boxStyle);
        iconLabel.getStyleClass().add(textStyle);
        iconLabel.setText(text);

        iconPane.getChildren().add(iconLabel);
        return iconPane;
    }

    private HBox createActionRow(Notification notification, Long postId) {
        HBox actionRow = new HBox(8);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setPadding(new Insets(2, 0, 0, 0));

        boolean isUnread = notification.getIsRead() == null || notification.getIsRead() != 1;
        if (isUnread) {
            Button markReadBtn = new Button("标记已读");
            markReadBtn.getStyleClass().addAll("card-action-button", "card-action-read");
            markReadBtn.setOnAction(e -> markAsRead(notification));
            actionRow.getChildren().add(markReadBtn);
        }

        if (postId != null) {
            Button viewPostBtn = new Button("查看原帖");
            viewPostBtn.getStyleClass().addAll("card-action-button", "card-action-post");
            viewPostBtn.setOnAction(e -> {
                if (notification.getIsRead() == null || notification.getIsRead() != 1) {
                    markAsRead(notification);
                }
                openPostDetail(postId);
            });
            actionRow.getChildren().add(viewPostBtn);
        }

        return actionRow;
    }

    // ---- Helper Methods ----

    private String getTypeText(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case TYPE_SYSTEM: return "系统通知";
            case TYPE_REPORT: return "举报处理通知";
            case TYPE_REVIEW: return "帖子审核通知";
            case TYPE_COMMENT: return "评论回复通知";
            case TYPE_FOLLOWER: return "新增粉丝通知";
            case TYPE_FOLLOW_POST: return "关注用户发帖通知";
            default: return "未知";
        }
    }

    private String getContentPreview(Notification notification) {
        String content = notification.getContent();
        if (content == null || content.isEmpty()) return "";
        if (content.length() > 80) {
            return content.substring(0, 80) + "...";
        }
        return content;
    }

    private String getRelativeTime(String createTime) {
        if (createTime == null || createTime.isEmpty()) return "";

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime time;

            if (createTime.length() >= 19) {
                time = LocalDateTime.parse(createTime, FULL_DATE_FORMATTER);
            } else if (createTime.length() >= 16) {
                time = LocalDateTime.parse(createTime, SHORT_DATE_FORMATTER);
            } else {
                return createTime;
            }

            Duration duration = Duration.between(time, now);
            long minutes = duration.toMinutes();
            long hours = duration.toHours();
            long days = duration.toDays();

            if (minutes < 1) return "刚刚";
            if (minutes < 60) return minutes + "分钟前";
            if (hours < 24) return hours + "小时前";
            if (days < 7) return days + "天前";

            return createTime.substring(0, Math.min(16, createTime.length()));
        } catch (DateTimeParseException e) {
            return createTime.length() >= 16 ? createTime.substring(0, 16) : createTime;
        }
    }

    private String getDateLabel(String createTime) {
        if (createTime == null || createTime.isEmpty()) return "未知日期";

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime time;

            if (createTime.length() >= 19) {
                time = LocalDateTime.parse(createTime, FULL_DATE_FORMATTER);
            } else if (createTime.length() >= 16) {
                time = LocalDateTime.parse(createTime, SHORT_DATE_FORMATTER);
            } else {
                return createTime;
            }

            String datePart = createTime.substring(0, 10);
            String todayPart = now.toLocalDate().toString();
            String yesterdayPart = now.minusDays(1).toLocalDate().toString();

            if (datePart.equals(todayPart)) {
                return "今天";
            } else if (datePart.equals(yesterdayPart)) {
                return "昨天";
            } else if (Duration.between(time, now).toDays() < 7) {
                String dayOfWeek = time.getDayOfWeek().toString();
                switch (dayOfWeek) {
                    case "MONDAY": return "星期一";
                    case "TUESDAY": return "星期二";
                    case "WEDNESDAY": return "星期三";
                    case "THURSDAY": return "星期四";
                    case "FRIDAY": return "星期五";
                    case "SATURDAY": return "星期六";
                    case "SUNDAY": return "星期日";
                    default: return dayOfWeek;
                }
            } else {
                return time.getYear() + "年" + time.getMonthValue() + "月" + time.getDayOfMonth() + "日";
            }
        } catch (DateTimeParseException e) {
            return createTime.length() >= 10 ? createTime.substring(0, 10) : createTime;
        }
    }

    private String extractReviewResult(String content) {
        if (content == null) return null;
        if (content.contains("审核通过")) return "[通过]";
        if (content.contains("未通过")) return "[未通过]";
        if (content.contains("审核中")) return "[审核中]";
        if (content.contains("拒绝")) return "[未通过]";
        if (content.contains("驳回")) return "[未通过]";
        return null;
    }

    private Long extractPostId(Notification notification) {
        if (notification == null) return null;

        String source = ((notification.getTitle() != null ? notification.getTitle() : "") + " " +
                (notification.getContent() != null ? notification.getContent() : ""));

        Matcher postMatcher = POST_ID_PATTERN.matcher(source);
        if (postMatcher.find()) {
            return parseLong(postMatcher.group(1));
        }

        Integer type = notification.getType();
        if (type != null && (type == TYPE_REVIEW || type == TYPE_COMMENT || type == TYPE_FOLLOW_POST)) {
            Matcher contentMatcher = CONTENT_ID_PATTERN.matcher(source);
            if (contentMatcher.find()) {
                return parseLong(contentMatcher.group(1));
            }
        }
        return null;
    }

    private Long parseLong(String value) {
        try {
            return value != null ? Long.parseLong(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ---- User Actions ----

    private void markAsRead(Notification notification) {
        if (notification == null || notification.getId() == null) return;
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                return HttpRequestUtil.readNotification(notification.getId());
            }
        };

        task.setOnSucceeded(event -> {
            Boolean result = task.getValue();
            if (Boolean.TRUE.equals(result)) {
                notification.setIsRead(1);
                updateTabStyles();
                rebuildDisplayList();
                loadUnreadCount();
                if (AppStore.getMainFrameController() != null) {
                    AppStore.getMainFrameController().loadUnreadNotificationCount();
                }
            }
        });

        new Thread(task).start();
    }

    private void markAllAsRead() {
        if (allNotifications.isEmpty()) {
            showInfo("没有通知可以标记为已读");
            return;
        }
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                return HttpRequestUtil.markAllNotificationsAsRead();
            }
        };

        task.setOnSucceeded(event -> {
            Boolean result = task.getValue();
            if (result != null && result) {
                for (Notification n : allNotifications) {
                    n.setIsRead(1);
                }
                updateTabStyles();
                rebuildDisplayList();
                loadUnreadCount();
                if (AppStore.getMainFrameController() != null) {
                    AppStore.getMainFrameController().loadUnreadNotificationCount();
                }
            } else {
                showError("标记全部已读失败");
            }
        });

        task.setOnFailed(event -> showError("标记全部已读失败"));

        new Thread(task).start();
    }

    private void openPostDetail(Long postId) {
        if (postId == null) {
            showInfo("这条通知没有关联帖子");
            return;
        }
        if (AppStore.getMainFrameController() != null) {
            AppStore.getMainFrameController().openPostDetail(postId);
        }
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

    private void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("成功");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
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
}
