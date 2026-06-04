package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BanAppealController extends ToolController {
    
    @FXML
    private TextArea reasonArea;
    
    @FXML
    private Button submitButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private ScrollPane appealListScrollPane;
    
    private VBox appealListVBox;
    
    @FXML
    public void initialize() {
        System.out.println("[BanAppealController] initialize() 被调用");
        
        appealListVBox = new VBox();
        appealListVBox.setSpacing(10);
        appealListVBox.setPadding(new Insets(10));
        appealListScrollPane.setContent(appealListVBox);
        
        submitButton.setOnAction(event -> {
            System.out.println("[BanAppealController] 提交按钮被点击");
            submitAppeal();
        });
        refreshButton.setOnAction(event -> {
            System.out.println("[BanAppealController] 刷新按钮被点击");
            loadAppeals();
        });
        
        loadAppeals();
    }
    
    private void submitAppeal() {
        System.out.println("[BanAppealController] submitAppeal() 被调用");
        
        String reason = reasonArea.getText().trim();
        System.out.println("[BanAppealController] 申诉理由内容: [" + reason + "]");
        
        if (reason.isEmpty()) {
            showDialog("请填写申诉理由");
            return;
        }
        
        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                System.out.println("[BanAppealController] 开始调用 HttpRequestUtil.submitBanAppeal()");
                Map<String, Object> result = HttpRequestUtil.submitBanAppeal(reason);
                System.out.println("[BanAppealController] submitBanAppeal 返回: " + result);
                return result;
            }
        };
        
        task.setOnSucceeded(event -> {
            System.out.println("[BanAppealController] submitAppeal task 成功完成");
            Platform.runLater(() -> {
                Map<String, Object> result = task.getValue();
                if (result != null && result.containsKey("code") && ((Number) result.get("code")).intValue() == 0) {
                    showDialog("申诉提交成功！");
                    reasonArea.clear();
                    loadAppeals();
                } else {
                    showDialog(result != null && result.containsKey("msg") ? 
                        (String) result.get("msg") : "申诉提交失败");
                }
            });
        });
        
        task.setOnFailed(event -> {
            System.out.println("[BanAppealController] submitAppeal task 失败: " + event.getSource().getException());
            event.getSource().getException().printStackTrace();
            Platform.runLater(() -> {
                showDialog("申诉提交失败，请检查网络连接");
            });
        });
        
        new Thread(task).start();
    }
    
    private void showDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void loadAppeals() {
        System.out.println("[BanAppealController] loadAppeals() 被调用");
        
        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                System.out.println("[BanAppealController] 开始调用 HttpRequestUtil.getMyBanAppeals()");
                Map<String, Object> result = HttpRequestUtil.getMyBanAppeals();
                System.out.println("[BanAppealController] getMyBanAppeals 返回: " + result);
                return result;
            }
        };
        
        task.setOnSucceeded(event -> {
            System.out.println("[BanAppealController] loadAppeals task 成功完成");
            Platform.runLater(() -> {
                Map<String, Object> result = task.getValue();
                System.out.println("[BanAppealController] 开始更新UI，结果: " + result);
                appealListVBox.getChildren().clear();
                
                if (result != null && result.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) result.get("data");
                    if (data != null && data.containsKey("list")) {
                        List<Map<String, Object>> appealList = (List<Map<String, Object>>) data.get("list");
                        if (appealList != null && !appealList.isEmpty()) {
                            System.out.println("[BanAppealController] 找到 " + appealList.size() + " 条申诉记录");
                            for (Map<String, Object> appeal : appealList) {
                                appealListVBox.getChildren().add(createAppealItem(appeal));
                            }
                        } else {
                            System.out.println("[BanAppealController] 申诉列表为空");
                            Label emptyLabel = new Label("暂无申诉记录");
                            emptyLabel.setStyle("-fx-text-fill: #999;");
                            appealListVBox.getChildren().add(emptyLabel);
                        }
                    } else {
                        System.out.println("[BanAppealController] data中没有list字段");
                        Label emptyLabel = new Label("暂无申诉记录");
                        emptyLabel.setStyle("-fx-text-fill: #999;");
                        appealListVBox.getChildren().add(emptyLabel);
                    }
                } else {
                    System.out.println("[BanAppealController] 结果为空或没有data字段");
                    Label errorLabel = new Label("加载申诉记录失败");
                    errorLabel.setStyle("-fx-text-fill: #f00;");
                    appealListVBox.getChildren().add(errorLabel);
                }
            });
        });
        
        task.setOnFailed(event -> {
            System.out.println("[BanAppealController] loadAppeals task 失败: " + event.getSource().getException());
            event.getSource().getException().printStackTrace();
            Platform.runLater(() -> {
                appealListVBox.getChildren().clear();
                Label errorLabel = new Label("加载申诉记录失败，请检查网络连接");
                errorLabel.setStyle("-fx-text-fill: #f00;");
                appealListVBox.getChildren().add(errorLabel);
            });
        });
        
        new Thread(task).start();
    }
    
    private VBox createAppealItem(Map<String, Object> appeal) {
        VBox itemBox = new VBox();
        itemBox.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");
        itemBox.setSpacing(8);
        
        Integer status = appeal.get("status") != null ? ((Number) appeal.get("status")).intValue() : 0;
        String statusText = "";
        String statusColor = "";
        switch (status) {
            case 0:
                statusText = "待处理";
                statusColor = "#f39c12";
                break;
            case 1:
                statusText = "已通过";
                statusColor = "#2ecc71";
                break;
            case 2:
                statusText = "已驳回";
                statusColor = "#e74c3c";
                break;
        }
        
        HBox topBox = new HBox();
        topBox.setSpacing(10);
        
        Label statusLabel = new Label(statusText);
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");
        
        Label timeLabel = new Label(appeal.get("createTime") != null ? appeal.get("createTime").toString() : "");
        timeLabel.setStyle("-fx-text-fill: #999;");
        
        topBox.getChildren().addAll(statusLabel, timeLabel);
        
        Label reasonLabel = new Label("申诉理由：" + (appeal.get("reason") != null ? appeal.get("reason").toString() : ""));
        reasonLabel.setWrapText(true);
        
        itemBox.getChildren().add(topBox);
        itemBox.getChildren().add(reasonLabel);
        
        if (status != 0) {
            Label resultLabel = new Label("处理结果：" + (appeal.get("handleResult") != null ? appeal.get("handleResult").toString() : ""));
            resultLabel.setWrapText(true);
            itemBox.getChildren().add(resultLabel);
            
            if (appeal.get("handleTime") != null) {
                Label handleTimeLabel = new Label("处理时间：" + appeal.get("handleTime").toString());
                handleTimeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12;");
                itemBox.getChildren().add(handleTimeLabel);
            }
        }
        
        return itemBox;
    }
    
    @Override
    public void doRefresh() {
        loadAppeals();
    }
}
