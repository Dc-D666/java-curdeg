package com.teach.javafx.controller.base;

import com.teach.javafx.MainApplication;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.util.BackgroundStyle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.regex.Pattern;

public class ForgotPasswordController {
    @FXML
    private TextField studentIdField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField emailCodeField;
    @FXML
    private Button sendCodeButton;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private VBox vbox;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    private int countdownSeconds = 0;
    private Timeline countdownTimeline;

    @FXML
    public void initialize() {
        vbox.getStyleClass().add(BackgroundStyle.BACKGROUND_STYLE_CLASS);
        vbox.setStyle(BackgroundStyle.appBackground());

        studentIdField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                emailField.requestFocus();
            }
        });

        emailField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                emailCodeField.requestFocus();
            }
        });

        emailCodeField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                newPasswordField.requestFocus();
            }
        });

        newPasswordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                confirmPasswordField.requestFocus();
            }
        });

        confirmPasswordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onResetButtonClick();
            }
        });
    }

    @FXML
    protected void onSendCodeButtonClick() {
        String email = emailField.getText();

        if (email == null || email.isEmpty()) {
            MessageDialog.showDialog("请先输入邮箱");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            MessageDialog.showDialog("邮箱格式不正确");
            return;
        }

        String msg = HttpRequestUtil.sendEmailCode(email, "RESET_PASSWORD");
        if (msg != null) {
            MessageDialog.showDialog(msg);
            return;
        }

        startCountdown();
    }

    private void startCountdown() {
        countdownSeconds = 60;
        sendCodeButton.setDisable(true);
        sendCodeButton.setText("60秒后重发");

        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            countdownSeconds--;
            if (countdownSeconds > 0) {
                sendCodeButton.setText(countdownSeconds + "秒后重发");
            } else {
                sendCodeButton.setDisable(false);
                sendCodeButton.setText("发送验证码");
                countdownTimeline.stop();
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    @FXML
    protected void onResetButtonClick() {
        String studentId = studentIdField.getText();
        String email = emailField.getText();
        String emailCode = emailCodeField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (studentId == null || studentId.isEmpty()) {
            MessageDialog.showDialog("学号不能为空");
            return;
        }
        if (studentId.length() != 12) {
            MessageDialog.showDialog("学号必须是12位");
            return;
        }
        if (email == null || email.isEmpty()) {
            MessageDialog.showDialog("邮箱不能为空");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            MessageDialog.showDialog("邮箱格式不正确");
            return;
        }
        if (emailCode == null || emailCode.isEmpty()) {
            MessageDialog.showDialog("请输入邮箱验证码");
            return;
        }
        if (emailCode.length() != 6) {
            MessageDialog.showDialog("验证码必须是6位数字");
            return;
        }
        if (newPassword == null || newPassword.isEmpty()) {
            MessageDialog.showDialog("密码不能为空");
            return;
        }
        if (newPassword.length() < 6) {
            MessageDialog.showDialog("密码至少6位");
            return;
        }
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            MessageDialog.showDialog("请确认密码");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            MessageDialog.showDialog("两次输入的密码不一致");
            return;
        }

        String msg = HttpRequestUtil.resetPassword(studentId, email, emailCode, newPassword);
        if (msg != null) {
            MessageDialog.showDialog(msg);
            return;
        }

        MessageDialog.showDialog("密码重置成功");

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("base/login-view.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), -1, -1);
            MainApplication.loginStage("学生交流社区", scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onBackButtonClick() {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("base/login-view.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), -1, -1);
            MainApplication.loginStage("学生交流社区", scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
