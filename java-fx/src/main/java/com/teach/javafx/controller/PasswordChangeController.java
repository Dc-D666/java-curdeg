package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.User;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.io.IOException;

public class PasswordChangeController extends ToolController {
    @FXML
    private PasswordField oldPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField emailCodeField;
    @FXML
    private Button sendCodeButton;
    @FXML
    private Button changeButton;
    @FXML
    private Label emailInfoLabel;
    @FXML
    private Label statusLabel;

    private Timeline countdownTimeline;
    private int countdownSeconds;

    @FXML
    public void initialize() {
        sendCodeButton.setOnAction(event -> onSendCode());
        changeButton.setOnAction(event -> onChangePassword());
        loadBoundEmailInfo();
    }

    private void loadBoundEmailInfo() {
        Task<User> task = new Task<>() {
            @Override
            protected User call() {
                return HttpRequestUtil.getCurrentUser();
            }
        };

        task.setOnSucceeded(event -> {
            User user = task.getValue();
            if (user != null && user.getPersonEmail() != null && !user.getPersonEmail().isBlank()) {
                emailInfoLabel.setText("验证码将发送到绑定邮箱：" + maskEmail(user.getPersonEmail()));
            } else {
                emailInfoLabel.setText("当前账号未读取到绑定邮箱，请先在个人资料中绑定邮箱。");
            }
        });

        runTask(task);
    }

    private void onSendCode() {
        setStatus("正在发送验证码...", false);
        setBusy(true, true);

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return HttpRequestUtil.sendChangePasswordCode();
            }
        };

        task.setOnSucceeded(event -> {
            setBusy(false, true);
            String error = task.getValue();
            if (error == null) {
                setStatus("验证码已发送，请查收绑定邮箱。", false);
                startCountdown();
            } else {
                setStatus(error, true);
                showError(error);
            }
        });

        task.setOnFailed(event -> {
            setBusy(false, true);
            setStatus("验证码发送失败，请稍后重试。", true);
            showError("验证码发送失败，请稍后重试。");
        });

        runTask(task);
    }

    private void onChangePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String emailCode = emailCodeField.getText();

        String validationError = validateInput(oldPassword, newPassword, confirmPassword, emailCode);
        if (validationError != null) {
            setStatus(validationError, true);
            showError(validationError);
            return;
        }

        setStatus("正在修改密码...", false);
        setBusy(true, false);

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return HttpRequestUtil.changePassword(oldPassword, newPassword, emailCode);
            }
        };

        task.setOnSucceeded(event -> {
            setBusy(false, false);
            String error = task.getValue();
            if (error == null) {
                clearFields();
                showSuccess("密码修改成功，请使用新密码重新登录。");
                goToLogin();
            } else {
                setStatus(error, true);
                showError(error);
            }
        });

        task.setOnFailed(event -> {
            setBusy(false, false);
            setStatus("密码修改失败，请稍后重试。", true);
            showError("密码修改失败，请稍后重试。");
        });

        runTask(task);
    }

    private String validateInput(String oldPassword, String newPassword, String confirmPassword, String emailCode) {
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return "请输入原密码";
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return "请输入新密码";
        }
        if (!newPassword.matches("^(?=.*[a-zA-Z])(?=.*\\d).{8,20}$")) {
            return "新密码需为8-20位，并且至少包含字母和数字";
        }
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            return "请再次输入新密码";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "两次输入的新密码不一致";
        }
        if (oldPassword.equals(newPassword)) {
            return "新密码不能与原密码相同";
        }
        if (emailCode == null || emailCode.trim().isEmpty()) {
            return "请输入邮箱验证码";
        }
        return null;
    }

    private void startCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        countdownSeconds = 60;
        sendCodeButton.setDisable(true);
        sendCodeButton.setText(countdownSeconds + "秒后重发");

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            countdownSeconds--;
            if (countdownSeconds <= 0) {
                countdownTimeline.stop();
                sendCodeButton.setDisable(false);
                sendCodeButton.setText("发送验证码");
            } else {
                sendCodeButton.setText(countdownSeconds + "秒后重发");
            }
        }));
        countdownTimeline.setCycleCount(60);
        countdownTimeline.play();
    }

    private void setBusy(boolean busy, boolean onlySendingCode) {
        if (!onlySendingCode) {
            changeButton.setDisable(busy);
        }
        if (busy && onlySendingCode) {
            sendCodeButton.setDisable(true);
        } else if (!busy && onlySendingCode && countdownSeconds <= 0) {
            sendCodeButton.setDisable(false);
        }
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
        statusLabel.setStyle(error ? "-fx-text-fill: #dc2626;" : "-fx-text-fill: #2563eb;");
    }

    private void clearFields() {
        oldPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        emailCodeField.clear();
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }
        String prefix = email.substring(0, Math.min(2, atIndex));
        return prefix + "***" + email.substring(atIndex);
    }

    private void goToLogin() {
        AppStore.setJwt(null);
        AppStore.setMainFrameController(null);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("base/login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 320, 240);
            MainApplication.loginStage("学生交流社区", scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private void runTask(Task<?> task) {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
