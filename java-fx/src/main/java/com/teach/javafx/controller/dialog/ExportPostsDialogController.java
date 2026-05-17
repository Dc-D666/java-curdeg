package com.teach.javafx.controller.dialog;

import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExportPostsDialogController {

    @FXML private RadioButton allScopeRadio;
    @FXML private RadioButton boardScopeRadio;
    @FXML private RadioButton dateScopeRadio;
    @FXML private RadioButton userScopeRadio;
    @FXML private ToggleGroup scopeGroup;

    @FXML private ComboBox<Map<String, Object>> boardComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField userIdField;

    @FXML private CheckBox idFieldCheck;
    @FXML private CheckBox titleFieldCheck;
    @FXML private CheckBox contentFieldCheck;
    @FXML private CheckBox authorFieldCheck;
    @FXML private CheckBox boardFieldCheck;
    @FXML private CheckBox likeCountFieldCheck;
    @FXML private CheckBox commentCountFieldCheck;
    @FXML private CheckBox createTimeFieldCheck;
    @FXML private CheckBox updateTimeFieldCheck;
    @FXML private CheckBox statusFieldCheck;

    @FXML private RadioButton csvFormatRadio;
    @FXML private RadioButton excelFormatRadio;
    @FXML private RadioButton jsonFormatRadio;
    @FXML private ToggleGroup formatGroup;

    @FXML private CheckBox includeDeletedCheck;

    @FXML private javafx.scene.layout.VBox boardSection;
    @FXML private javafx.scene.layout.VBox dateSection;
    @FXML private javafx.scene.layout.VBox userSection;

    @FXML
    public void initialize() {
        idFieldCheck.setSelected(true);
        titleFieldCheck.setSelected(true);
        contentFieldCheck.setSelected(true);
        authorFieldCheck.setSelected(true);
        boardFieldCheck.setSelected(true);
        likeCountFieldCheck.setSelected(true);
        commentCountFieldCheck.setSelected(true);
        createTimeFieldCheck.setSelected(true);
        updateTimeFieldCheck.setSelected(true);
        statusFieldCheck.setSelected(true);

        boardSection.setVisible(false);
        boardSection.setManaged(false);
        dateSection.setVisible(false);
        dateSection.setManaged(false);
        userSection.setVisible(false);
        userSection.setManaged(false);

        scopeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boardSection.setVisible(false);
            boardSection.setManaged(false);
            dateSection.setVisible(false);
            dateSection.setManaged(false);
            userSection.setVisible(false);
            userSection.setManaged(false);

            if (newVal == boardScopeRadio) {
                boardSection.setVisible(true);
                boardSection.setManaged(true);
            } else if (newVal == dateScopeRadio) {
                dateSection.setVisible(true);
                dateSection.setManaged(true);
            } else if (newVal == userScopeRadio) {
                userSection.setVisible(true);
                userSection.setManaged(true);
            }
        });

        loadBoards();
    }

    private void loadBoards() {
        Task<List<Map<String, Object>>> task = new Task<List<Map<String, Object>>>() {
            @Override
            protected List<Map<String, Object>> call() {
                DataResponse response = HttpRequestUtil.request("/api/bbs/board/list", new DataRequest());
                if (response.getCode() == 0 && response.getData() != null) {
                    return (List<Map<String, Object>>) response.getData();
                }
                return Collections.emptyList();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                List<Map<String, Object>> boards = task.getValue();
                boardComboBox.getItems().addAll(boards);
                boardComboBox.setCellFactory(lv -> new ListCell<Map<String, Object>>() {
                    @Override
                    protected void updateItem(Map<String, Object> item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : String.valueOf(item.get("name")));
                    }
                });
                boardComboBox.setButtonCell(new ListCell<Map<String, Object>>() {
                    @Override
                    protected void updateItem(Map<String, Object> item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : String.valueOf(item.get("name")));
                    }
                });
            });
        });

        new Thread(task).start();
    }

    public boolean handleExport() {
        String scope = "ALL";
        if (boardScopeRadio.isSelected()) scope = "BOARD";
        else if (dateScopeRadio.isSelected()) scope = "DATE_RANGE";
        else if (userScopeRadio.isSelected()) scope = "USER";

        List<String> fields = new ArrayList<>();
        if (idFieldCheck.isSelected()) fields.add("id");
        if (titleFieldCheck.isSelected()) fields.add("title");
        if (contentFieldCheck.isSelected()) fields.add("content");
        if (authorFieldCheck.isSelected()) fields.add("authorId");
        if (boardFieldCheck.isSelected()) fields.add("boardId");
        if (likeCountFieldCheck.isSelected()) fields.add("likeCount");
        if (commentCountFieldCheck.isSelected()) fields.add("commentCount");
        if (createTimeFieldCheck.isSelected()) fields.add("createTime");
        if (updateTimeFieldCheck.isSelected()) fields.add("updateTime");
        if (statusFieldCheck.isSelected()) fields.add("status");

        if (fields.isEmpty()) {
            showAlert("请至少选择一个导出字段");
            return false;
        }

        String format = "CSV";
        if (excelFormatRadio.isSelected()) format = "EXCEL";
        else if (jsonFormatRadio.isSelected()) format = "JSON";

        DataRequest request = new DataRequest();
        request.add("scope", scope);
        request.add("fields", fields);
        request.add("format", format);
        request.add("includeDeleted", includeDeletedCheck.isSelected());

        if ("BOARD".equals(scope) && boardComboBox.getValue() != null) {
            Map<String, Object> board = boardComboBox.getValue();
            Object idObj = board.get("id");
            if (idObj instanceof Number) {
                request.add("boardId", ((Number) idObj).longValue());
            } else {
                request.add("boardId", Long.parseLong(String.valueOf(idObj)));
            }
        }
        if ("DATE_RANGE".equals(scope)) {
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            if (start == null || end == null) {
                showAlert("请选择时间范围");
                return false;
            }
            request.add("startDate", start.format(DateTimeFormatter.ISO_LOCAL_DATE));
            request.add("endDate", end.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if ("USER".equals(scope)) {
            String userId = userIdField.getText();
            if (userId == null || userId.trim().isEmpty()) {
                showAlert("请输入用户ID");
                return false;
            }
            request.add("userId", Integer.parseInt(userId.trim()));
        }

        FileChooser fileChooser = new FileChooser();
        String extension = "CSV".equals(format) ? "*.csv" : "EXCEL".equals(format) ? "*.xlsx" : "*.json";
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(format + " Files", extension));
        fileChooser.setInitialFileName("posts_export" + ("CSV".equals(format) ? ".csv" : "EXCEL".equals(format) ? ".xlsx" : ".json"));

        Stage stage = (Stage) allScopeRadio.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return false;

        Task<byte[]> exportTask = new Task<byte[]>() {
            @Override
            protected byte[] call() {
                DataResponse response = HttpRequestUtil.request("/api/admin/export/posts", request);
                if (response != null && response.getCode() == 0 && response.getData() != null) {
                    String base64 = response.getData().toString();
                    return Base64.getDecoder().decode(base64);
                }
                return null;
            }
        };

        exportTask.setOnSucceeded(event -> {
            byte[] data = exportTask.getValue();
            if (data != null && data.length > 0) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(data);
                    Platform.runLater(() -> showAlert("导出成功！文件已保存到：" + file.getAbsolutePath()));
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("保存文件失败：" + e.getMessage()));
                }
            } else {
                Platform.runLater(() -> showAlert("导出失败，请稍后重试"));
            }
        });

        exportTask.setOnFailed(event -> {
            Platform.runLater(() -> showAlert("导出失败：" + exportTask.getException().getMessage()));
        });

        new Thread(exportTask).start();
        return true;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
