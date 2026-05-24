package com.teach.javafx.controller.dialog;

import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Map;

public class FeedbackDialogController {
    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField contactField;

    @FXML
    public void initialize() {
        typeComboBox.getItems().addAll("Bug 报告", "功能建议", "体验优化", "其他");
        typeComboBox.getSelectionModel().selectFirst();
    }

    public boolean handleSubmit() {
        String type = typeComboBox.getValue();
        String title = titleField.getText();
        String description = descriptionArea.getText();
        String contact = contactField.getText();

        if (title == null || title.trim().isEmpty()) {
            showAlert("请输入标题");
            return false;
        }
        if (description == null || description.trim().isEmpty()) {
            showAlert("请输入详细描述");
            return false;
        }

        String typeCode;
        if ("Bug 报告".equals(type)) typeCode = "BUG";
        else if ("功能建议".equals(type)) typeCode = "FEATURE";
        else if ("体验优化".equals(type)) typeCode = "EXPERIENCE";
        else typeCode = "OTHER";

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                DataRequest request = new DataRequest();
                request.add("type", typeCode);
                request.add("title", title.trim());
                request.add("description", description.trim());
                if (contact != null && !contact.trim().isEmpty()) {
                    request.add("contact", contact.trim());
                }
                DataResponse response = HttpRequestUtil.request("/api/bbs/feedback", request);
                return response.getCode() == 0;
            }
        };

        task.setOnSucceeded(event -> {
            Boolean result = task.getValue();
            Platform.runLater(() -> {
                if (result) {
                    showAlert("反馈提交成功，感谢您的反馈！");
                } else {
                    showAlert("反馈提交失败，请稍后重试");
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showAlert("网络错误，请稍后重试"));
        });

        new Thread(task).start();
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
