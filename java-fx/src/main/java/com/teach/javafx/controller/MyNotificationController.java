package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.Notification;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyNotificationController extends ToolController {
    @FXML
    private TableView<Notification> notificationTableView;
    @FXML
    private TableColumn<Notification, String> typeColumn;
    @FXML
    private TableColumn<Notification, String> titleColumn;
    @FXML
    private TableColumn<Notification, String> contentColumn;
    @FXML
    private TableColumn<Notification, String> isReadColumn;
    @FXML
    private TableColumn<Notification, String> createTimeColumn;
    @FXML
    private Label unreadCountLabel;
    @FXML
    private Button markAllReadButton;
    @FXML
    private Button refreshButton;
    @FXML
    private ComboBox<String> typeComboBox;

    private static final Pattern POST_ID_PATTERN = Pattern.compile("(?:帖子ID|帖子 ID|postId|post_id|帖子)[:：]?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONTENT_ID_PATTERN = Pattern.compile("(?:内容ID|内容 ID|ID)[:：]?\\s*(\\d+)");
    private Integer currentType = null;

    @FXML
    public void initialize() {
        typeColumn.setCellValueFactory(cellData -> {
            Integer type = cellData.getValue().getType();
            String typeText = "";
            if (type != null) {
                switch (type) {
                    case 1:
                        typeText = "系统通知";
                        break;
                    case 2:
                        typeText = "举报处理通知";
                        break;
                    case 3:
                        typeText = "帖子审核通知";
                        break;
                    case 4:
                        typeText = "评论回复通知";
                        break;
                    case 5:
                        typeText = "新增粉丝通知";
                        break;
                    case 6:
                        typeText = "关注用户发帖通知";
                        break;
                    default:
                        typeText = "未知";
                }
            }
            return new javafx.beans.property.SimpleStringProperty(typeText);
        });
        typeColumn.setCellFactory(param -> new TableCell<Notification, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (getTableRow().isSelected()) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setCellFactory(param -> new TableCell<Notification, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (getTableRow().isSelected()) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        contentColumn.setCellValueFactory(new PropertyValueFactory<>("content"));
        contentColumn.setCellFactory(param -> new TableCell<Notification, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (getTableRow().isSelected()) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        isReadColumn.setCellValueFactory(cellData -> {
            Integer isRead = cellData.getValue().getIsRead();
            String readText = (isRead != null && isRead == 1) ? "已读" : "未读";
            return new javafx.beans.property.SimpleStringProperty(readText);
        });
        isReadColumn.setCellFactory(param -> new TableCell<Notification, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Notification notification = getTableView().getItems().get(getIndex());
                    Integer isRead = notification.getIsRead();
                    boolean isSelected = getTableRow().isSelected();
                    
                    StringBuilder style = new StringBuilder();
                    if (isRead == null || isRead == 0) {
                        style.append("-fx-font-weight: bold; ");
                    }
                    if (isSelected) {
                        style.append("-fx-text-fill: red;");
                    }
                    setStyle(style.toString());
                }
            }
        });
        
        createTimeColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreateTime() != null ? cellData.getValue().getCreateTime().substring(0, 16) : "");
        });
        createTimeColumn.setCellFactory(param -> new TableCell<Notification, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (getTableRow().isSelected()) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        notificationTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            notificationTableView.refresh();
        });
        
        setupRowFactory();
        loadUnreadCount();
        loadNotificationList();
        
        markAllReadButton.setOnAction(event -> markAllAsRead());

        refreshButton.setOnAction(event -> {
            loadNotificationList(refreshButton);
            loadUnreadCount();
        });
        
        typeComboBox.getItems().addAll("全部类型", "系统通知", "回复我的", "举报处理", "帖子审核", "新增粉丝通知", "关注用户发帖通知");
        typeComboBox.getSelectionModel().selectFirst();
        typeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                switch (newVal) {
                    case "全部类型":
                        currentType = null;
                        break;
                    case "系统通知":
                        currentType = 1;
                        break;
                    case "回复我的":
                        currentType = 4;
                        break;
                    case "举报处理":
                        currentType = 2;
                        break;
                    case "帖子审核":
                        currentType = 3;
                        break;
                    case "新增粉丝通知":
                        currentType = 5;
                        break;
                    case "关注用户发帖通知":
                        currentType = 6;
                        break;
                }
                loadNotificationList();
            }
        });
    }

    private void setupRowFactory() {
        notificationTableView.setRowFactory(tv -> {
            TableRow<Notification> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    openNotificationDetail(row.getItem());
                } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1 && !row.isEmpty()) {
                    Notification notification = row.getItem();
                    if (notification.getIsRead() == null || notification.getIsRead() == 0) {
                        markAsRead(notification);
                    }
                }
            });
            return row;
        });
    }

    private void openNotificationDetail(Notification notification) {
        if (notification == null) {
            return;
        }

        if (notification.getIsRead() == null || notification.getIsRead() == 0) {
            markAsRead(notification);
        }

        Long postId = extractPostId(notification);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("通知详情");
        dialog.setHeaderText(notification.getTitle() != null && !notification.getTitle().isBlank()
                ? notification.getTitle()
                : getTypeText(notification.getType()));

        ButtonType openPostButton = new ButtonType("查看原帖", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeButton = new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE);
        if (postId != null) {
            dialog.getDialogPane().getButtonTypes().addAll(openPostButton, closeButton);
        } else {
            dialog.getDialogPane().getButtonTypes().add(closeButton);
        }

        GridPane metaGrid = new GridPane();
        metaGrid.setHgap(12);
        metaGrid.setVgap(8);
        addMetaRow(metaGrid, 0, "类型", getTypeText(notification.getType()));
        addMetaRow(metaGrid, 1, "状态", notification.getIsRead() != null && notification.getIsRead() == 1 ? "已读" : "未读");
        addMetaRow(metaGrid, 2, "时间", notification.getCreateTime() != null ? notification.getCreateTime() : "");
        if (postId != null) {
            addMetaRow(metaGrid, 3, "关联帖子", String.valueOf(postId));
        }

        TextArea contentArea = new TextArea(notification.getContent() != null ? notification.getContent() : "");
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(8);
        contentArea.setPrefColumnCount(52);

        VBox content = new VBox(12, metaGrid, contentArea);
        content.setPadding(new javafx.geometry.Insets(10));
        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == openPostButton) {
            openPostDetail(postId);
        }
    }

    private void addMetaRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label + "：");
        labelNode.setStyle("-fx-font-weight: bold;");
        Label valueNode = new Label(value != null ? value : "");
        valueNode.setWrapText(true);
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private Long extractPostId(Notification notification) {
        if (notification == null) {
            return null;
        }

        String source = ((notification.getTitle() != null ? notification.getTitle() : "") + " " +
                (notification.getContent() != null ? notification.getContent() : ""));
        Matcher postMatcher = POST_ID_PATTERN.matcher(source);
        if (postMatcher.find()) {
            return parseLong(postMatcher.group(1));
        }

        if (notification.getType() != null && (notification.getType() == 3 || notification.getType() == 4 || notification.getType() == 6)) {
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

    private String getTypeText(Integer type) {
        if (type == null) {
            return "未知";
        }
        switch (type) {
            case 1:
                return "系统通知";
            case 2:
                return "举报处理通知";
            case 3:
                return "帖子审核通知";
            case 4:
                return "评论回复通知";
            case 5:
                return "新增粉丝通知";
            case 6:
                return "关注用户发帖通知";
            default:
                return "未知";
        }
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
                unreadCountLabel.setText("(" + (count != null ? count : 0) + " 条未读)");
            });
        });

        new Thread(task).start();
    }

    public void loadNotificationList() {
        loadNotificationList(null);
    }

    private void loadNotificationList(Button refreshBtn) {
        if (refreshBtn != null) {
            refreshBtn.setDisable(true);
            refreshBtn.setText("刷新中");
        }
        
        Task<List<Notification>> task = new Task<List<Notification>>() {
            @Override
            protected List<Notification> call() {
                return HttpRequestUtil.getMyNotificationList(null, currentType);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                if (refreshBtn != null) {
                    refreshBtn.setDisable(false);
                    refreshBtn.setText("刷新");
                }
                
                List<Notification> notifications = task.getValue();
                if (notifications != null) {
                    notificationTableView.getItems().clear();
                    notificationTableView.getItems().addAll(notifications);
                    
                    if (notifications.isEmpty() && currentType != null) {
                        showInfo("未找到该类型的通知");
                    }
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                if (refreshBtn != null) {
                    refreshBtn.setDisable(false);
                    refreshBtn.setText("刷新");
                }
                showError("加载通知列表失败");
            });
        });

        new Thread(task).start();
    }

    private void markAsRead(Notification notification) {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                return HttpRequestUtil.readNotification(notification.getId());
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Boolean result = task.getValue();
                if (Boolean.TRUE.equals(result)) {
                    notification.setIsRead(1);
                    notificationTableView.refresh();
                    loadUnreadCount();
                    if (AppStore.getMainFrameController() != null) {
                        AppStore.getMainFrameController().loadUnreadNotificationCount();
                    }
                }
            });
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

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void markAllAsRead() {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                return HttpRequestUtil.markAllNotificationsAsRead();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Boolean result = task.getValue();
                if (Boolean.TRUE.equals(result)) {
                    showSuccess("已将所有通知标记为已读");
                    loadNotificationList();
                    loadUnreadCount();
                    if (AppStore.getMainFrameController() != null) {
                        AppStore.getMainFrameController().loadUnreadNotificationCount();
                    }
                } else {
                    showError("标记全部已读失败");
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                showError("标记全部已读失败");
            });
        });

        new Thread(task).start();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
