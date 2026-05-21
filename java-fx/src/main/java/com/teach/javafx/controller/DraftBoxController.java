package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DraftBoxController extends ToolController {

    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private VBox draftListVBox;
    @FXML
    private Label totalLabel;
    @FXML
    private Button refreshButton;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        mainScrollPane.setFitToWidth(true);
        refreshButton.setOnAction(event -> loadDrafts());
        loadDrafts();
    }

    private void loadDrafts() {
        Task<List<Map<String, Object>>> task = new Task<List<Map<String, Object>>>() {
            @Override
            protected List<Map<String, Object>> call() {
                return HttpRequestUtil.getDraftList();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                List<Map<String, Object>> drafts = task.getValue();
                draftListVBox.getChildren().clear();
                if (drafts != null && !drafts.isEmpty()) {
                    totalLabel.setText("共 " + drafts.size() + " 篇草稿");
                    for (Map<String, Object> draft : drafts) {
                        addDraftCard(draft);
                    }
                } else {
                    totalLabel.setText("暂无草稿");
                    Label emptyLabel = new Label("暂无草稿，去发布帖子页面保存草稿吧~");
                    emptyLabel.setStyle("-fx-text-fill: #999; -fx-padding: 40 0; -fx-font-size: 14;");
                    emptyLabel.setAlignment(Pos.CENTER);
                    emptyLabel.setMaxWidth(Double.MAX_VALUE);
                    draftListVBox.getChildren().add(emptyLabel);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("加载草稿列表失败"));
        });

        new Thread(task).start();
    }

    private void addDraftCard(Map<String, Object> draft) {
        VBox card = new VBox(8);
        card.getStyleClass().add("profile-card");
        card.setStyle("-fx-padding: 16; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");

        Object idObj = draft.get("id");
        Long draftId = idObj instanceof Number ? ((Number) idObj).longValue() : 0L;

        String title = draft.get("title") != null ? draft.get("title").toString() : "";
        String content = draft.get("content") != null ? draft.get("content").toString() : "";
        String boardName = draft.get("boardName") != null ? draft.get("boardName").toString() : "";
        String updateTime = draft.get("updateTime") != null ? draft.get("updateTime").toString() : "";

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title.isEmpty() ? "无标题" : title);
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1a1a2e; -fx-wrap-text: true;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 板块标签
        if (!boardName.isEmpty()) {
            Label boardLabel = new Label(boardName);
            boardLabel.setStyle("-fx-text-fill: #6366f1; -fx-font-size: 11; -fx-background-color: #eef2ff; -fx-padding: 2 8; -fx-background-radius: 8;");
            titleRow.getChildren().addAll(titleLabel, boardLabel);
        } else {
            titleRow.getChildren().add(titleLabel);
        }

        // 内容预览
        String preview = content.length() > 100 ? content.substring(0, 100) + "..." : content;
        Label contentLabel = new Label(preview);
        contentLabel.setStyle("-fx-text-fill: #666; -fx-wrap-text: true; -fx-font-size: 13;");
        contentLabel.setWrapText(true);

        // 底部栏：时间 + 操作按钮
        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label(updateTime);
        timeLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12;");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Button editButton = new Button("编辑");
        editButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-padding: 4 14; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 12;");

        Button deleteButton = new Button("删除");
        deleteButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 4 14; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 12;");

        editButton.setOnAction(e -> editDraft(draftId));
        deleteButton.setOnAction(e -> deleteDraft(draftId));

        bottomRow.getChildren().addAll(timeLabel, spacer2, editButton, deleteButton);

        card.getChildren().addAll(titleRow, contentLabel, bottomRow);

        card.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                editDraft(draftId);
            }
        });

        draftListVBox.getChildren().add(card);
    }

    private void editDraft(Long draftId) {
        if (AppStore.getMainFrameController() != null) {
            try {
                javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                    MainApplication.class.getResource("post-publish.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 1024, 768);
                PostPublishController controller = fxmlLoader.getController();
                controller.loadDraft(draftId);

                String tabName = "edit-draft-" + draftId;
                AppStore.getMainFrameController().changeContentWithScene(tabName, "编辑草稿", scene, controller);
            } catch (Exception e) {
                e.printStackTrace();
                showError("打开编辑页面失败");
            }
        }
    }

    private void deleteDraft(Long draftId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除这篇草稿吗？");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
                    @Override
                    protected Map<String, Object> call() {
                        return HttpRequestUtil.deleteDraft(draftId);
                    }
                };
                task.setOnSucceeded(event -> {
                    Platform.runLater(() -> {
                        loadDrafts();
                        showInfo("草稿已删除");
                    });
                });
                new Thread(task).start();
            }
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

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
