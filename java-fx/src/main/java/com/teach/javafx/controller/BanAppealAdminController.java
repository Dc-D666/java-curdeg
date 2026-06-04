package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.MainFrameController;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BanAppealAdminController extends ToolController {
    
    @FXML
    private ChoiceBox<String> statusChoiceBox;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private ScrollPane appealListScrollPane;
    
    private VBox appealListVBox;
    
    private Integer currentStatus = null;
    
    @FXML
    public void initialize() {
        appealListVBox = new VBox();
        appealListVBox.setSpacing(10);
        appealListVBox.setPadding(new Insets(10));
        appealListScrollPane.setContent(appealListVBox);
        
        statusChoiceBox.getItems().addAll("全部", "待处理", "已通过", "已驳回");
        statusChoiceBox.getSelectionModel().select(0);
        
        statusChoiceBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int index = newVal != null ? newVal.intValue() : 0;
            switch (index) {
                case 0:
                    currentStatus = null;
                    break;
                case 1:
                    currentStatus = 0;
                    break;
                case 2:
                    currentStatus = 1;
                    break;
                case 3:
                    currentStatus = 2;
                    break;
            }
            loadAppeals();
        });
        
        refreshButton.setOnAction(event -> loadAppeals());
        
        loadAppeals();
    }
    
    private void openAppealDetail(Long appealId) {
        System.out.println("[BanAppealAdminController] 准备打开详情页, ID: " + appealId);
        if (appealId == null) {
            System.out.println("[BanAppealAdminController] appealId 为 null");
            return;
        }

        MainFrameController mainFrame = AppStore.getMainFrameController();
        if (mainFrame == null) {
            System.out.println("[BanAppealAdminController] mainFrame 为 null");
            return;
        }

        // 直接调用 MainFrameController 的 openAppealDetail 方法
        mainFrame.openAppealDetail(appealId);
        System.out.println("[BanAppealAdminController] 已调用 mainFrame.openAppealDetail");
    }
    
    private void loadAppeals() {
        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getAllBanAppeals(currentStatus);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> result = task.getValue();
                appealListVBox.getChildren().clear();
                
                if (result != null && result.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) result.get("data");
                    if (data != null && data.containsKey("list")) {
                        List<Map<String, Object>> appealList = (List<Map<String, Object>>) data.get("list");
                        if (appealList != null && !appealList.isEmpty()) {
                            for (Map<String, Object> appeal : appealList) {
                                appealListVBox.getChildren().add(createAppealItem(appeal));
                            }
                        } else {
                            Label emptyLabel = new Label("暂无申诉记录");
                            emptyLabel.setStyle("-fx-text-fill: #999;");
                            appealListVBox.getChildren().add(emptyLabel);
                        }
                    } else {
                        Label emptyLabel = new Label("暂无申诉记录");
                        emptyLabel.setStyle("-fx-text-fill: #999;");
                        appealListVBox.getChildren().add(emptyLabel);
                    }
                } else {
                    Label errorLabel = new Label("加载申诉记录失败");
                    errorLabel.setStyle("-fx-text-fill: #f00;");
                    appealListVBox.getChildren().add(errorLabel);
                }
            });
        });
        
        task.setOnFailed(event -> {
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
        itemBox.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-cursor: hand;");
        itemBox.setSpacing(8);
        
        Long appealId = appeal.get("id") != null ? ((Number) appeal.get("id")).longValue() : null;
        final Long finalAppealId = appealId;
        
        System.out.println("[BanAppealAdminController] 创建申诉项, ID: " + appealId);
        
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
        
        // 用户信息
        Map<String, Object> userInfo = null;
        if (appeal.get("userInfo") != null) {
            userInfo = (Map<String, Object>) appeal.get("userInfo");
        }
        
        if (userInfo != null) {
            String nickname = userInfo.get("nickname") != null ? userInfo.get("nickname").toString() : "未知";
            String studentId = userInfo.get("studentId") != null ? userInfo.get("studentId").toString() : "";
            String role = userInfo.get("role") != null ? userInfo.get("role").toString() : "";
            
            StringBuilder userLabelText = new StringBuilder("用户：" + nickname + " (" + studentId + ")");
            if (!role.isEmpty()) {
                if ("ROLE_SUPER".equals(role)) {
                    userLabelText.append(" [超级管理员]");
                } else if ("ROLE_ADMIN".equals(role)) {
                    userLabelText.append(" [管理员]");
                }
            }
            
            Label userLabel = new Label(userLabelText.toString());
            userLabel.setStyle("-fx-text-fill: #555;");
            itemBox.getChildren().add(userLabel);
        }
        
        Label reasonLabel = new Label("申诉理由：" + (appeal.get("reason") != null ? appeal.get("reason").toString() : ""));
        reasonLabel.setWrapText(true);
        
        itemBox.getChildren().add(topBox);
        itemBox.getChildren().add(reasonLabel);
        
        // 双击打开详情
        itemBox.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                System.out.println("[BanAppealAdminController] 双击打开申诉详情, ID: " + finalAppealId);
                openAppealDetail(finalAppealId);
            }
        });
        
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
        
        // 处理按钮
        if (status == 0) {
            HBox buttonBox = new HBox();
            buttonBox.setSpacing(10);
            buttonBox.setStyle("-fx-padding: 10 0 0 0;");
            
            Button passButton = new Button("通过申诉");
            passButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 5;");
            passButton.setOnAction(event -> {
                System.out.println("[BanAppealAdminController] 通过按钮被点击");
                showHandleDialog(appeal, 1);
            });
            
            Button rejectButton = new Button("驳回申诉");
            rejectButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5;");
            rejectButton.setOnAction(event -> {
                System.out.println("[BanAppealAdminController] 驳回按钮被点击");
                showHandleDialog(appeal, 2);
            });
            
            Button detailButton = new Button("查看详情");
            detailButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;");
            detailButton.setOnAction(event -> {
                System.out.println("[BanAppealAdminController] 查看详情按钮被点击, ID: " + finalAppealId);
                openAppealDetail(finalAppealId);
            });
            
            buttonBox.getChildren().addAll(passButton, rejectButton, detailButton);
            itemBox.getChildren().add(buttonBox);
        }
        
        return itemBox;
    }
    
    private void showHandleDialog(Map<String, Object> appeal, Integer decision) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(decision == 1 ? "通过申诉" : "驳回申诉");
        dialog.setHeaderText(decision == 1 ? "请填写处理结果：" : "请填写驳回原因：");
        
        ButtonType okButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);
        
        TextArea resultArea = new TextArea();
        resultArea.setPrefWidth(400);
        resultArea.setPrefHeight(100);
        resultArea.setWrapText(true);
        resultArea.setPromptText(decision == 1 ? "填写通过原因" : "填写驳回原因");
        
        dialog.getDialogPane().setContent(resultArea);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return resultArea.getText();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isEmpty()) {
            handleAppeal(appeal, decision, result.get());
        } else if (result.isPresent()) {
            showDialog("请填写处理结果");
        }
    }
    
    private void handleAppeal(Map<String, Object> appeal, final Integer decision, final String handleResult) {
        Long appealId = null;
        if (appeal.get("id") instanceof Number) {
            appealId = ((Number) appeal.get("id")).longValue();
        }
        
        if (appealId == null) {
            showDialog("申诉ID无效");
            return;
        }
        
        final Long finalAppealId = appealId;
        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.handleBanAppeal(finalAppealId, decision, handleResult);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> result = task.getValue();
                if (result != null && result.containsKey("code") && ((Number) result.get("code")).intValue() == 0) {
                    showDialog("处理成功");
                    loadAppeals();
                } else {
                    showDialog(result != null && result.containsKey("msg") ? 
                        (String) result.get("msg") : "处理失败");
                }
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                showDialog("处理失败，请检查网络连接");
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
    
    @Override
    public void doRefresh() {
        loadAppeals();
    }
}
