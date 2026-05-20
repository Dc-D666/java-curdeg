package com.teach.javafx.controller;

import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.AppSettings;
import com.teach.javafx.util.NotificationManager;
import com.teach.javafx.util.SettingsManager;
import com.teach.javafx.util.StyleManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;

public class SystemSettingsController extends ToolController {

    @FXML
    private ComboBox<String> themeComboBox;
    @FXML
    private ComboBox<String> fontSizeComboBox;
    @FXML
    private CheckBox notificationSoundCheckBox;
    @FXML
    private CheckBox postNotificationCheckBox;
    @FXML
    private CheckBox commentNotificationCheckBox;
    @FXML
    private CheckBox likeNotificationCheckBox;
    @FXML
    private CheckBox followNotificationCheckBox;
    @FXML
    private CheckBox messageNotificationCheckBox;
    @FXML
    private ComboBox<String> defaultBoardComboBox;
    @FXML
    private ComboBox<String> postSortComboBox;
    @FXML
    private CheckBox autoSaveDraftCheckBox;
    @FXML
    private Button clearCacheButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button resetButton;
    @FXML
    private Label themePreview;
    @FXML
    private Label fontSizePreview;
    @FXML
    private Label boardPreview;
    @FXML
    private Label sortPreview;

    @FXML
    public void initialize() {
        initializeOptions();
        loadSettings();
        setupListeners();
    }

    private void initializeOptions() {
        ObservableList<String> themeOptions = FXCollections.observableArrayList(
            "默认主题（浅色）",
            "深色主题"
        );
        themeComboBox.setItems(themeOptions);

        ObservableList<String> fontSizeOptions = FXCollections.observableArrayList(
            "小",
            "中（默认）",
            "大"
        );
        fontSizeComboBox.setItems(fontSizeOptions);

        ObservableList<String> boardOptions = FXCollections.observableArrayList(
            "学习交流",
            "校园生活",
            "公告通知",
            "资源分享",
            "闲聊水区"
        );
        defaultBoardComboBox.setItems(boardOptions);

        ObservableList<String> sortOptions = FXCollections.observableArrayList(
            "最新发布",
            "最多回复",
            "最多点赞",
            "最多浏览"
        );
        postSortComboBox.setItems(sortOptions);
    }

    private void setupListeners() {
        saveButton.setOnAction(this::onSave);
        clearCacheButton.setOnAction(this::onClearCache);
        resetButton.setOnAction(this::onReset);

        themeComboBox.setOnAction(e -> updateThemePreview());
        fontSizeComboBox.setOnAction(e -> updateFontSizePreview());
        defaultBoardComboBox.setOnAction(e -> updateBoardPreview());
        postSortComboBox.setOnAction(e -> updateSortPreview());

        themeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateThemePreview());
        fontSizeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateFontSizePreview());
        defaultBoardComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateBoardPreview());
        postSortComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateSortPreview());
    }

    private void loadSettings() {
        AppSettings settings = SettingsManager.getCurrentSettings();
        
        themeComboBox.getSelectionModel().select(settings.getTheme());
        fontSizeComboBox.getSelectionModel().select(settings.getFontSize());
        notificationSoundCheckBox.setSelected(settings.isNotificationSound());
        postNotificationCheckBox.setSelected(settings.isPostNotification());
        commentNotificationCheckBox.setSelected(settings.isCommentNotification());
        likeNotificationCheckBox.setSelected(settings.isLikeNotification());
        followNotificationCheckBox.setSelected(settings.isFollowNotification());
        messageNotificationCheckBox.setSelected(settings.isMessageNotification());
        defaultBoardComboBox.getSelectionModel().select(settings.getDefaultBoard());
        postSortComboBox.getSelectionModel().select(settings.getPostSort());
        autoSaveDraftCheckBox.setSelected(settings.isAutoSaveDraft());

        updatePreviews();
    }

    private void updatePreviews() {
        updateThemePreview();
        updateFontSizePreview();
        updateBoardPreview();
        updateSortPreview();
    }

    private void updateThemePreview() {
        String selected = themeComboBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if ("深色主题".equals(selected)) {
                themePreview.setText("✓ 深色主题已选择，将应用深色配色方案");
                themePreview.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            } else {
                themePreview.setText("✓ 默认主题已选择，将应用浅色配色方案");
                themePreview.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
            }
        }
    }

    private void updateFontSizePreview() {
        String selected = fontSizeComboBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String size;
            switch (selected) {
                case "小":
                    size = "12px";
                    break;
                case "大":
                    size = "16px";
                    break;
                default:
                    size = "14px";
            }
            fontSizePreview.setText(String.format("✓ 字体大小将调整为 %s", size));
            fontSizePreview.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
        }
    }

    private void updateBoardPreview() {
        String selected = defaultBoardComboBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boardPreview.setText(String.format("✓ 进入帖子广场时将默认显示【%s】板块", selected));
            boardPreview.setStyle("-fx-text-fill: #9C27B0; -fx-font-weight: bold;");
        }
    }

    private void updateSortPreview() {
        String selected = postSortComboBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            sortPreview.setText(String.format("✓ 帖子将按【%s】排序", selected));
            sortPreview.setStyle("-fx-text-fill: #00BCD4; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void onSave(ActionEvent event) {
        try {
            AppSettings settings = new AppSettings();
            settings.setTheme(themeComboBox.getSelectionModel().getSelectedItem());
            settings.setFontSize(fontSizeComboBox.getSelectionModel().getSelectedItem());
            settings.setNotificationSound(notificationSoundCheckBox.isSelected());
            settings.setPostNotification(postNotificationCheckBox.isSelected());
            settings.setCommentNotification(commentNotificationCheckBox.isSelected());
            settings.setLikeNotification(likeNotificationCheckBox.isSelected());
            settings.setFollowNotification(followNotificationCheckBox.isSelected());
            settings.setMessageNotification(messageNotificationCheckBox.isSelected());
            settings.setDefaultBoard(defaultBoardComboBox.getSelectionModel().getSelectedItem());
            settings.setPostSort(postSortComboBox.getSelectionModel().getSelectedItem());
            settings.setAutoSaveDraft(autoSaveDraftCheckBox.isSelected());

            SettingsManager.saveSettings(settings);
            
            applySettings();

            showSuccessAlert("设置保存成功！\n\n" +
                "• 主题已立即应用\n" +
                "• 字体大小将在重启后生效\n" +
                "• 通知设置已更新\n" +
                "• 板块和排序偏好已保存");
                
        } catch (Exception e) {
            showErrorAlert("保存设置时出错：" + e.getMessage());
        }
    }

    private void applySettings() {
        MainApplication.applyCurrentTheme();
        
        if (MainApplication.getMainScene() != null) {
            StyleManager.applyFontSizeToScene(MainApplication.getMainScene());
        }
    }

    @FXML
    private void onClearCache(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("确认清空缓存");
        alert.setHeaderText("确定要清空本地缓存吗？");
        alert.setContentText("这将删除所有本地缓存数据，包括：\n" +
            "• 临时文件\n" +
            "• 本地草稿\n" +
            "• 界面偏好设置\n\n" +
            "注意：您的账号信息不会受到影响。");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    SettingsManager.clearCache();
                    loadSettings();
                    applySettings();
                    showSuccessAlert("缓存清空成功！\n\n" +
                        "所有设置已恢复为默认值。\n" +
                        "请重新配置您的偏好设置。");
                } catch (Exception e) {
                    showErrorAlert("清空缓存时出错：" + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void onReset(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("确认恢复默认");
        alert.setHeaderText("确定要恢复所有设置为默认值吗？");
        alert.setContentText("这将把所有设置恢复为系统默认值。");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SettingsManager.clearCache();
                loadSettings();
                applySettings();
                showSuccessAlert("已恢复所有设置为默认值！");
            }
        });
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("操作成功");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("操作失败");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
