package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PointHistoryController extends ToolController {

    @FXML
    private TableView<PointRecordItem> pointTable;
    @FXML
    private TableColumn<PointRecordItem, String> timeColumn;
    @FXML
    private TableColumn<PointRecordItem, String> actionColumn;
    @FXML
    private TableColumn<PointRecordItem, String> changeColumn;
    @FXML
    private TableColumn<PointRecordItem, String> balanceColumn;
    @FXML
    private Pagination pagination;

    private int pageSize = 20;
    private int currentPage = 0;
    private int totalPages = 1;

    @FXML
    public void initialize() {
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        changeColumn.setCellValueFactory(new PropertyValueFactory<>("pointsChange"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balanceAfter"));

        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            currentPage = newIndex.intValue();
            loadData();
        });

        loadData();
    }

    private void loadData() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getPointHistory(currentPage, pageSize);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> result = task.getValue();
                if (result != null) {
                    displayData(result);
                }
            });
        });

        new Thread(task).start();
    }

    @SuppressWarnings("unchecked")
    private void displayData(Map<String, Object> result) {
        List<Map<String, Object>> content = new ArrayList<>();
        Object contentObj = result.get("content");
        if (contentObj instanceof List) {
            content = (List<Map<String, Object>>) contentObj;
        }

        List<PointRecordItem> items = new ArrayList<>();
        for (Map<String, Object> record : content) {
            PointRecordItem item = new PointRecordItem();
            item.setCreateTime(getString(record, "createTime"));
            item.setDescription(getString(record, "description"));
            Object changeObj = record.get("pointsChange");
            int change = changeObj instanceof Number ? ((Number) changeObj).intValue() : 0;
            item.setPointsChange(change > 0 ? "+" + change : String.valueOf(change));
            Object balanceObj = record.get("balanceAfter");
            item.setBalanceAfter(String.valueOf(balanceObj instanceof Number ? ((Number) balanceObj).intValue() : 0));
            items.add(item);
        }

        pointTable.getItems().setAll(items);

        Object totalPagesObj = result.get("totalPages");
        if (totalPagesObj instanceof Number) {
            totalPages = ((Number) totalPagesObj).intValue();
        }
        pagination.setPageCount(Math.max(totalPages, 1));
        pagination.setCurrentPageIndex(currentPage);
    }

    private String getString(Map<String, Object> map, String key) {
        Object obj = map.get(key);
        return obj != null ? obj.toString() : "";
    }

    public static class PointRecordItem {
        private String createTime;
        private String description;
        private String pointsChange;
        private String balanceAfter;

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPointsChange() {
            return pointsChange;
        }

        public void setPointsChange(String pointsChange) {
            this.pointsChange = pointsChange;
        }

        public String getBalanceAfter() {
            return balanceAfter;
        }

        public void setBalanceAfter(String balanceAfter) {
            this.balanceAfter = balanceAfter;
        }
    }
}
