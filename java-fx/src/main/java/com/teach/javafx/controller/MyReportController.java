package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.PageResult;
import com.teach.javafx.models.Report;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class MyReportController extends ToolController {
    @FXML
    private TableView<Report> reportTableView;
    @FXML
    private TableColumn<Report, String> targetTypeColumn;
    @FXML
    private TableColumn<Report, String> reasonColumn;
    @FXML
    private TableColumn<Report, String> statusColumn;
    @FXML
    private TableColumn<Report, String> createTimeColumn;
    @FXML
    private TableColumn<Report, String> handleTimeColumn;
    @FXML
    private TableColumn<Report, String> handleRemarkColumn;
    @FXML
    private Label pageInfoLabel;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;

    private int currentPageNum = 1;
    private int currentPageSize = 10;

    @FXML
    public void initialize() {
        targetTypeColumn.setCellValueFactory(cellData -> {
            Integer type = cellData.getValue().getTargetType();
            String typeText = "";
            if (type != null) {
                switch (type) {
                    case 1:
                        typeText = "帖子";
                        break;
                    case 2:
                        typeText = "评论";
                        break;
                    default:
                        typeText = "未知";
                }
            }
            return new javafx.beans.property.SimpleStringProperty(typeText);
        });
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        statusColumn.setCellValueFactory(cellData -> {
            Integer status = cellData.getValue().getStatus();
            String statusText = "";
            if (status != null) {
                switch (status) {
                    case 0:
                        statusText = "待处理";
                        break;
                    case 1:
                        statusText = "已处理";
                        break;
                    default:
                        statusText = "未知";
                }
            }
            return new javafx.beans.property.SimpleStringProperty(statusText);
        });
        createTimeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreateTime() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreateTime());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        handleTimeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getHandleTime() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getHandleTime());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        handleRemarkColumn.setCellValueFactory(new PropertyValueFactory<>("handleRemark"));

        prevButton.setOnAction(event -> {
            if (currentPageNum > 1) {
                currentPageNum--;
                loadReportList();
            }
        });

        nextButton.setOnAction(event -> {
            currentPageNum++;
            loadReportList();
        });

        loadReportList();
    }

    public void loadReportList() {
        Task<PageResult<Report>> task = new Task<PageResult<Report>>() {
            @Override
            protected PageResult<Report> call() {
                return HttpRequestUtil.getMyReportList(currentPageNum, currentPageSize);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                PageResult<Report> pageResult = task.getValue();
                if (pageResult != null && pageResult.getList() != null) {
                    reportTableView.getItems().clear();
                    reportTableView.getItems().addAll(pageResult.getList());

                    long total = pageResult.getTotal() != null ? pageResult.getTotal() : 0;
                    pageInfoLabel.setText("共 " + total + " 条，第 " + currentPageNum + " 页");

                    prevButton.setDisable(currentPageNum <= 1);
                    int totalPages = (int) Math.ceil((double) total / currentPageSize);
                    nextButton.setDisable(currentPageNum >= totalPages);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("加载举报列表失败"));
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
