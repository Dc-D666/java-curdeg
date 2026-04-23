package com.teach.javafx.controller.base;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.request.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import java.util.prefs.Preferences;
import java.io.IOException;

/**
 * LoginController 登录交互控制类 对应 base/login-view.fxml
 *  @FXML  属性 对应fxml文件中的 fx:id 属性 如TextField usernameField 对应 fx:id="usernameField"
 *  @FXML 方法 对应于fxml文件中的 on***Click的属性  如onLoginButtonClick() 对应onAction="#onLoginButtonClick"
 */
public class LoginController {
    private static final String PREFS_NODE = "com.teach.javafx.login";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "rememberPassword";
    
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox rememberPasswordCheckBox;
    @FXML
    private VBox vbox;
    /**
     * 页面加载对象创建完成初始话方法，页面中控件属性的设置，初始数据显示等初始操作都在这里完成，其他代码都事件处理方法里
     */
    @FXML
    public void initialize() {
        loadSavedCredentials();
        
//        vbox.setId("min");  // id选择器 #
//        vbox.getStyleClass().add("min");  类选择器 .
        vbox.setStyle("-fx-background-image: url('shanda1.jpg'); -fx-background-repeat: no-repeat; -fx-background-size: cover;");  //inline选择器
//        loginButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });
        
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onLoginButtonClick();
            }
        });
    }

    /**
     *  点击登录按钮 执行onLoginButtonClick 方法 从面板上获取用户名和密码，请求后台登录服务，登录成功加载主框架，切换舞台到主框架，登录不成功，提示错误信息
     */
    @FXML
    protected void onLoginButtonClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if(username == null || username.isEmpty()) {
            MessageDialog.showDialog("请输入用户名");
            return;
        }
        if(password == null || password.isEmpty()) {
            MessageDialog.showDialog("请输入密码");
            return;
        }
        LoginRequest loginRequest = new LoginRequest(username,password);
        String msg = HttpRequestUtil.login(loginRequest);
        if(msg != null) {
            MessageDialog.showDialog( msg);
            return;
        }
        saveCredentials();
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
    protected void onRegisterButtonClick() {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("base/register-view.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), -1, -1);
            MainApplication.resetStage("学生交流社区 - 注册", scene, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void loadSavedCredentials() {
        Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
        boolean remember = prefs.getBoolean(KEY_REMEMBER, false);
        rememberPasswordCheckBox.setSelected(remember);
        
        if (remember) {
            String username = prefs.get(KEY_USERNAME, "");
            String password = prefs.get(KEY_PASSWORD, "");
            usernameField.setText(username);
            passwordField.setText(password);
        }
    }
    
    private void saveCredentials() {
        Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
        boolean remember = rememberPasswordCheckBox.isSelected();
        
        if (remember) {
            prefs.put(KEY_USERNAME, usernameField.getText());
            prefs.put(KEY_PASSWORD, passwordField.getText());
            prefs.putBoolean(KEY_REMEMBER, true);
        } else {
            prefs.put(KEY_PASSWORD, "");
            prefs.putBoolean(KEY_REMEMBER, false);
        }
    }
}