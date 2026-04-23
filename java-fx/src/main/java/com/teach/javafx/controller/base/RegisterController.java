package com.teach.javafx.controller.base;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.LoginRequest;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.regex.Pattern;

public class RegisterController {
    @FXML
    private TextField studentIdField;
    @FXML
    private TextField nicknameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private VBox vbox;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    @FXML
    public void initialize() {
        vbox.setStyle("-fx-background-image: url('shanda1.jpg'); -fx-background-repeat: no-repeat; -fx-background-size: cover;");
        
        studentIdField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                nicknameField.requestFocus();
            }
        });
        
        nicknameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                emailField.requestFocus();
            }
        });
        
        emailField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });
        
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                confirmPasswordField.requestFocus();
            }
        });
        
        confirmPasswordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onRegisterButtonClick();
            }
        });
    }

    @FXML
    protected void onRegisterButtonClick() {
        String studentId = studentIdField.getText();
        String nickname = nicknameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (studentId == null || studentId.isEmpty()) {
            MessageDialog.showDialog("学号不能为空");
            return;
        }
        if (studentId.length() != 12) {
            MessageDialog.showDialog("学号必须是12位");
            return;
        }
        if (nickname == null || nickname.isEmpty()) {
            MessageDialog.showDialog("昵称不能为空");
            return;
        }
        if (nickname.length() < 1 || nickname.length() > 16) {
            MessageDialog.showDialog("昵称必须是1-16位");
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
        if (password == null || password.isEmpty()) {
            MessageDialog.showDialog("密码不能为空");
            return;
        }
        if (password.length() < 6) {
            MessageDialog.showDialog("密码至少6位");
            return;
        }
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            MessageDialog.showDialog("请确认密码");
            return;
        }
        if (!password.equals(confirmPassword)) {
            MessageDialog.showDialog("两次输入的密码不一致");
            return;
        }

        String msg = HttpRequestUtil.registerUser(studentId, nickname, email, password);
        if (msg != null) {
            MessageDialog.showDialog(msg);
            return;
        }

        LoginRequest loginRequest = new LoginRequest(studentId, password);
        String loginMsg = HttpRequestUtil.login(loginRequest);
        if (loginMsg != null) {
            MessageDialog.showDialog(loginMsg);
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("base/main-frame.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), -1, -1);
            AppStore.setMainFrameController((MainFrameController) fxmlLoader.getController());
            MainApplication.resetStage("学生交流社区", scene);
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
