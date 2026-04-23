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

import java.text.SimpleDateFormat;
import java.util.List;

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
    private ComboBox<String> typeComboBox;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
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
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1 && !row.isEmpty()) {
                    Notification notification = row.getItem();
                    if (notification.getIsRead() == null || notification.getIsRead() == 0) {
                        markAsRead(notification);
                    }
                }
            });
            return row;
        });
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
        Task<List<Notification>> task = new Task<List<Notification>>() {
            @Override
            protected List<Notification> call() {
                return HttpRequestUtil.getMyNotificationList(null, currentType);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
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
            Platform.runLater(() -> showError("加载通知列表失败"));
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
