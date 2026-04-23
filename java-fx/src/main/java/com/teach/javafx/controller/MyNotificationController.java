package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.Notification;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
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

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

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
                    default:
                        typeText = "未知";
                }
            }
            return new javafx.beans.property.SimpleStringProperty(typeText);
        });
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        contentColumn.setCellValueFactory(new PropertyValueFactory<>("content"));
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
                    if (isRead == null || isRead == 0) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        createTimeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreateTime() != null) {
                return new javafx.beans.property.SimpleStringProperty(dateFormat.format(cellData.getValue().getCreateTime()));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

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

        loadUnreadCount();
        loadNotificationList();
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
                return HttpRequestUtil.getMyNotificationList(null);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                List<Notification> notifications = task.getValue();
                if (notifications != null) {
                    notificationTableView.getItems().clear();
                    notificationTableView.getItems().addAll(notifications);
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
}
