package com.teach.javafx.controller;

import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.models.AiImageResponse;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class AiImageDialogController {
    @FXML
    private ComboBox<String> sizeComboBox;
    @FXML
    private TextArea promptTextArea;
    @FXML
    private Button generateButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button regenerateButton;
    @FXML
    private Button confirmButton;
    @FXML
    private VBox progressContainer;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressLabel;
    @FXML
    private VBox imagePreviewContainer;
    @FXML
    private ImageView imagePreviewView;
    @FXML
    private Label placeholderLabel;

    private AiImageCallback callback;
    private Thread progressThread;
    private volatile boolean aiRunning;
    private long aiStartTime;
    private static final long TOTAL_EXPECTED_TIME = 30000;
    private String generatedImageUrl;

    public interface AiImageCallback {
        void onImageConfirmed(String imageUrl);
    }

    private static final List<String> SIZE_OPTIONS = Arrays.asList(
        "1024x1024",
        "1280x1280",
        "1568x1056",
        "1056x1568",
        "1472x1088",
        "1088x1472",
        "1728x960",
        "960x1728"
    );

    @FXML
    public void initialize() {
        sizeComboBox.getItems().addAll(SIZE_OPTIONS);
        sizeComboBox.setValue("1024x1024");
        
        cancelButton.setOnAction(event -> closeDialog());
        generateButton.setOnAction(event -> generateImage());
        regenerateButton.setOnAction(event -> generateImage());
        confirmButton.setOnAction(event -> confirmImage());
        
        setMode(Mode.INITIAL);
    }

    public void setInitialPrompt(String title, String content) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("为校园论坛帖子配图");
        if (title != null && !title.trim().isEmpty()) {
            prompt.append("，帖子标题：").append(title);
        }
        if (content != null && !content.trim().isEmpty()) {
            String shortContent = content.length() > 100 ? content.substring(0, 100) + "..." : content;
            prompt.append("，内容：").append(shortContent);
        }
        prompt.append("。请生成一张美观、符合校园风格的配图。");
        promptTextArea.setText(prompt.toString());
    }

    public void setCallback(AiImageCallback callback) {
        this.callback = callback;
    }

    private void generateImage() {
        String prompt = promptTextArea.getText().trim();
        String size = sizeComboBox.getValue();
        
        if (prompt.isEmpty()) {
            showError("描述不能为空！");
            return;
        }
        
        setMode(Mode.GENERATING);
        startProgressAnimation();
        
        Task<AiImageResponse> task = new Task<AiImageResponse>() {
            @Override
            protected AiImageResponse call() {
                return HttpRequestUtil.aiImageGenerate(prompt, size);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                stopProgressAnimation(true);
                AiImageResponse response = task.getValue();
                
                if (response != null && response.getSuccess() && response.getImageUrl() != null) {
                    generatedImageUrl = response.getImageUrl();
                    displayGeneratedImage(generatedImageUrl);
                    setMode(Mode.PREVIEW);
                } else {
                    String errorMsg = (response != null && response.getMessage() != null) 
                        ? response.getMessage() 
                        : "图片生成失败，请稍后重试";
                    setMode(Mode.INITIAL);
                    showError(errorMsg);
                }
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                stopProgressAnimation(false);
                setMode(Mode.INITIAL);
                showError("图片生成失败：" + task.getException().getMessage());
            });
        });
        
        new Thread(task).start();
    }

    private void displayGeneratedImage(String imageUrl) {
        try {
            String fullUrl;
            if (imageUrl.startsWith("http")) {
                fullUrl = imageUrl;
            } else {
                fullUrl = HttpRequestUtil.serverUrl + imageUrl;
            }
            Image image = new Image(fullUrl, true);
            imagePreviewView.setImage(image);
            placeholderLabel.setVisible(false);
        } catch (Exception e) {
            System.err.println("加载图片失败: " + e.getMessage());
        }
    }

    private void confirmImage() {
        if (generatedImageUrl != null && callback != null) {
            callback.onImageConfirmed(generatedImageUrl);
        }
        closeDialog();
    }

    private void closeDialog() {
        stopProgressThread();
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private enum Mode {
        INITIAL,
        GENERATING,
        PREVIEW
    }

    private void setMode(Mode mode) {
        switch (mode) {
            case INITIAL:
            default:
                progressContainer.setVisible(false);
                progressContainer.setManaged(false);
                placeholderLabel.setVisible(true);
                imagePreviewView.setImage(null);
                regenerateButton.setVisible(false);
                confirmButton.setVisible(false);
                generateButton.setVisible(true);
                generateButton.setDisable(false);
                cancelButton.setDisable(false);
                break;
                
            case GENERATING:
                progressContainer.setVisible(true);
                progressContainer.setManaged(true);
                regenerateButton.setVisible(false);
                confirmButton.setVisible(false);
                generateButton.setVisible(false);
                cancelButton.setDisable(true);
                break;
                
            case PREVIEW:
                progressContainer.setVisible(false);
                progressContainer.setManaged(false);
                regenerateButton.setVisible(true);
                confirmButton.setVisible(true);
                generateButton.setVisible(false);
                cancelButton.setDisable(false);
                break;
        }
    }

    private void startProgressAnimation() {
        progressBar.setProgress(0.0);
        progressLabel.setText("正在连接 AI 服务...");
        aiStartTime = System.currentTimeMillis();
        
        stopProgressThread();
        aiRunning = true;
        progressThread = new Thread(() -> {
            while (aiRunning) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
                long elapsed = System.currentTimeMillis() - aiStartTime;
                double progress = (double) elapsed / TOTAL_EXPECTED_TIME;
                if (progress > 0.95) {
                    progress = 0.95;
                }
                String statusText = "正在处理...";
                if (progress < 0.2) {
                    statusText = "正在连接 AI 服务...";
                } else if (progress < 0.5) {
                    statusText = "AI 正在生成图片...";
                } else if (progress < 0.8) {
                    statusText = "正在保存图片...";
                } else {
                    statusText = "即将完成...";
                }
                final double finalProgress = progress;
                final String finalStatusText = statusText;
                Platform.runLater(() -> {
                    progressBar.setProgress(finalProgress);
                    progressLabel.setText(finalStatusText);
                });
            }
        });
        progressThread.setDaemon(true);
        progressThread.start();
    }

    private void stopProgressThread() {
        aiRunning = false;
        if (progressThread != null && progressThread.isAlive()) {
            progressThread.interrupt();
        }
        progressThread = null;
    }

    private void stopProgressAnimation(boolean success) {
        stopProgressThread();
        if (success) {
            progressBar.setProgress(1.0);
            progressLabel.setText("完成！");
        }
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
