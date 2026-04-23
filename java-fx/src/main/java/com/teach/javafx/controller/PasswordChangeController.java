package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

public class PasswordChangeController extends ToolController {
    @FXML
    private PasswordField oldPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button changeButton;

    @FXML
    public void initialize() {
        changeButton.setOnAction(event -> onChangePassword());
    }

    private void onChangePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            showError("请输入原密码");
            return;
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            showError("请输入新密码");
            return;
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            showError("请输入确认密码");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("两次输入的新密码不一致");
            return;
        }

        changeButton.setDisable(true);

        Task<String> task = new Task<String>() {
            @Override
            protected String call() {
                return HttpRequestUtil.changePassword(oldPassword, newPassword);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                changeButton.setDisable(false);
                String error = task.getValue();
                if (error == null) {
                    showSuccess("密码修改成功");
                    clearFields();
                } else {
                    showError(error);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                changeButton.setDisable(false);
                showError("密码修改失败，请稍后重试");
            });
        });

        new Thread(task).start();
    }

    private void clearFields() {
        oldPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
