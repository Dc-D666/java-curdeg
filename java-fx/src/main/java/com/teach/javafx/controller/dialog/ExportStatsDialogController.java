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
import java.util.*;

public class ExportStatsDialogController {

    @FXML private RadioButton csvFormatRadio;
    @FXML private RadioButton excelFormatRadio;
    @FXML private RadioButton jsonFormatRadio;
    @FXML private ToggleGroup formatGroup;

    public boolean handleExport() {
        String format = "CSV";
        if (excelFormatRadio.isSelected()) format = "EXCEL";
        else if (jsonFormatRadio.isSelected()) format = "JSON";

        DataRequest request = new DataRequest();
        request.add("format", format);

        String extension;
        String fileName;
        if ("EXCEL".equals(format)) {
            extension = "*.xlsx";
            fileName = "statistics_export.xlsx";
        } else if ("JSON".equals(format)) {
            extension = "*.json";
            fileName = "statistics_export.json";
        } else {
            extension = "*.csv";
            fileName = "statistics_export.csv";
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(format + " Files", extension));
        fileChooser.setInitialFileName(fileName);

        Stage stage = (Stage) csvFormatRadio.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return false;

        Task<byte[]> exportTask = new Task<byte[]>() {
            @Override
            protected byte[] call() {
                DataResponse response = HttpRequestUtil.request("/api/admin/export/statistics", request);
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
