package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.controller.base.ToolController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Map;

public class AppealDetailController extends ToolController {
    
    private Long appealId;
    private Map<String, Object> appealData;
    
    @FXML
    private Label appealIdLabel;
    
    @FXML
    private Label createTimeLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private Label studentIdLabel;
    
    @FXML
    private Label bannedStatusLabel;
    
    @FXML
    private TextArea reasonArea;
    
    @FXML
    private VBox resultSection;
    
    @FXML
    private Label handleTimeLabel;
    
    @FXML
    private Label handleResultLabel;
    
    @FXML
    private HBox actionButtonsBox;
    
    @FXML
    private Button approveButton;
    
    @FXML
    private Button rejectButton;
    
    @FXML
    public void initialize() {
        System.out.println("[AppealDetailController] initialize() 被调用");
    }
    
    public void setAppealId(Long appealId) {
        this.appealId = appealId;
        System.out.println("[AppealDetailController] setAppealId: " + appealId);
        loadAppealDetail();
    }
    
    private void loadAppealDetail() {
        System.out.println("[AppealDetailController] 开始加载申诉详情, ID: " + appealId);
        
        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getBanAppealDetail(appealId);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> result = task.getValue();
                System.out.println("[AppealDetailController] 加载结果: " + result);
                
                if (result != null && ((Number) result.get("code")).intValue() == 0 && result.containsKey("data")) {
                    appealData = (Map<String, Object>) result.get("data");
                    displayAppealDetail();
                } else {
                    showDialog("加载申诉详情失败");
                }
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                showDialog("加载申诉详情失败，请检查网络连接");
            });
        });
        
        new Thread(task).start();
    }
    
    private void displayAppealDetail() {
        if (appealData == null) {
            return;
        }
        
        System.out.println("[AppealDetailController] displayAppealDetail 开始处理数据: " + appealData);
        
        // 获取appeal对象
        Map<String, Object> appeal = null;
        Map<String, Object> user = null;
        
        if (appealData.containsKey("appeal")) {
            appeal = (Map<String, Object>) appealData.get("appeal");
        }
        if (appealData.containsKey("user")) {
            user = (Map<String, Object>) appealData.get("user");
        }
        
        System.out.println("[AppealDetailController] appeal: " + appeal);
        System.out.println("[AppealDetailController] user: " + user);
        
        if (appeal == null) {
            System.out.println("[AppealDetailController] appeal数据为空");
            return;
        }
        
        // 申诉基本信息
        appealIdLabel.setText(String.valueOf(appeal.get("id")));
        createTimeLabel.setText(appeal.get("createTime") != null ? appeal.get("createTime").toString() : "--");
        
        // 状态
        Integer status = appeal.get("status") != null ? ((Number) appeal.get("status")).intValue() : 0;
        String statusText = "";
        switch (status) {
            case 0: statusText = "待处理"; break;
            case 1: statusText = "已通过"; break;
            case 2: statusText = "已驳回"; break;
        }
        statusLabel.setText(statusText);
        
        // 用户信息
        if (user != null) {
            userNameLabel.setText(user.get("nickname") != null ? user.get("nickname").toString() : "--");
            studentIdLabel.setText(user.get("studentId") != null ? user.get("studentId").toString() : "--");
            
            Boolean isBanned = user.get("isBanned") != null ? (Boolean) user.get("isBanned") : false;
            bannedStatusLabel.setText(isBanned ? "已被禁言" : "正常");
        }
        
        // 申诉理由
        reasonArea.setText(appeal.get("reason") != null ? appeal.get("reason").toString() : "");
        
        // 处理结果（如果有）
        if (status != 0) {
            resultSection.setVisible(true);
            handleTimeLabel.setText(appeal.get("handleTime") != null ? appeal.get("handleTime").toString() : "--");
            handleResultLabel.setText(appeal.get("handleResult") != null ? appeal.get("handleResult").toString() : "--");
            actionButtonsBox.setVisible(false);
        } else {
            resultSection.setVisible(false);
            actionButtonsBox.setVisible(true);
        }
    }
    
    @FXML
    private void onApproveClick() {
        showHandleDialog(1);
    }
    
    @FXML
    private void onRejectClick() {
        showHandleDialog(2);
    }
    
    private void showHandleDialog(Integer decision) {
        String title = decision == 1 ? "通过申诉" : "驳回申诉";
        String content = decision == 1 ? "请填写通过原因：" : "请填写驳回原因：";
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(content);
        
        TextArea textArea = new TextArea();
        textArea.setPromptText("请输入处理结果");
        textArea.setPrefHeight(100);
        alert.getDialogPane().setContent(textArea);
        
        ButtonType okButton = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, cancelButton);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == okButton) {
                String handleResult = textArea.getText();
                if (handleResult == null || handleResult.trim().isEmpty()) {
                    showDialog("请填写处理结果");
                    return;
                }
                handleAppeal(decision, handleResult);
            }
        });
    }
    
    private void handleAppeal(Integer decision, String handleResult) {
        System.out.println("[AppealDetailController] 开始处理申诉, ID: " + appealId + ", 决策: " + decision);
        
        Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.handleBanAppeal(appealId, decision, handleResult);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> result = task.getValue();
                System.out.println("[AppealDetailController] 处理结果: " + result);
                
                if (result != null && ((Number) result.get("code")).intValue() == 0) {
                    showDialog("处理成功！");
                    // 返回列表页面
                    actionButtonsBox.setVisible(false);
                    loadAppealDetail(); // 刷新当前页面
                } else {
                    String msg = result != null && result.containsKey("msg") ? result.get("msg").toString() : "处理失败";
                    showDialog(msg);
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
    
    @FXML
    private void onBackClick() {
        // 关闭当前标签页
        com.teach.javafx.controller.base.MainFrameController mainFrame = AppStore.getMainFrameController();
        if (mainFrame != null) {
            mainFrame.closeCurrentTab();
        }
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
        if (appealId != null) {
            loadAppealDetail();
        }
    }
}
