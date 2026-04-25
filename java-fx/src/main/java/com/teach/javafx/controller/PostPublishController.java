package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.Board;
import com.teach.javafx.models.Post;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PostPublishController extends ToolController {
    @FXML
    private TextField titleTextField;
    @FXML
    private Label titleCountLabel;
    @FXML
    private ComboBox<Board> boardComboBox;
    @FXML
    private TextArea contentTextArea;
    @FXML
    private Label contentCountLabel;
    @FXML
    private TextField imageUrlsTextField;
    @FXML
    private Button selectImageButton;
    @FXML
    private Button aiImageButton;
    @FXML
    private Label imageUploadStatusLabel;
    @FXML
    private FlowPane imagePreviewPane;
    @FXML
    private Button publishButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button previewButton;
    @FXML
    private Button saveDraftButton;
    
    // AI写作助手相关控件
    @FXML
    private TitledPane aiAssistantPane;
    @FXML
    private Button aiHelpWriteButton;
    @FXML
    private Button aiContinueButton;
    @FXML
    private Button aiPolishButton;
    @FXML
    private TextArea aiInstructionTextArea;
    @FXML
    private Button aiGenerateButton;
    @FXML
    private Label aiStatusLabel;
    
    // AI 进度条 - 使用JavaFX自带ProgressBar
    @FXML
    private VBox aiProgressContainer;
    @FXML
    private ProgressBar aiProgressBar;
    @FXML
    private Label aiProgressLabel;
    
    // AI 进度条相关
    private Thread aiProgressThread;
    private volatile boolean aiRunning = false;
    private long aiStartTime = 0;
    private static final long TOTAL_EXPECTED_TIME = 6000; // 假设总共需要6秒
    
    // 保存原始内容，用于"弃用"功能
    private String originalTitle;
    private String originalContent;
    
    // 存储的图片URL列表
    private List<String> imageUrls = new ArrayList<>();
    
    // 草稿相关
    private static final String DRAFT_FILE_NAME = "post_draft.txt";
    private Path draftFilePath;
    
    private com.teach.javafx.controller.base.MainFrameController mainFrameController;
    
    // 自动保存定时器（简化实现）
    private boolean autoSaveEnabled = true;
    private long lastAutoSaveTime = 0;
    private static final long AUTO_SAVE_INTERVAL = 60000; // 60秒

    @FXML
    public void initialize() {
        loadBoardList();
        initDraftPath();
        
        // 字数统计监听器
        setupWordCountListeners();
        
        // 按钮事件
        publishButton.setOnAction(event -> publishPost());
        cancelButton.setOnAction(event -> closeTab());
        selectImageButton.setOnAction(event -> selectAndUploadImage());
        aiImageButton.setOnAction(event -> openAiImageDialog());
        previewButton.setOnAction(event -> showPreview());
        saveDraftButton.setOnAction(event -> saveDraftManually());
        
        // AI写作助手事件初始化
        setupAiAssistantListeners();
        
        // 检查是否有草稿需要恢复
        checkAndRecoverDraft();
        
        // 设置初始字数统计
        updateWordCount();
    }
    
    private void initDraftPath() {
        String userHome = System.getProperty("user.home");
        draftFilePath = Paths.get(userHome, ".trae_bbs", DRAFT_FILE_NAME);
    }
    
    private void setupWordCountListeners() {
        // 标题字数统计
        titleTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateWordCount();
            triggerAutoSave();
        });
        
        // 内容字数统计
        contentTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            updateWordCount();
            triggerAutoSave();
        });
    }
    
    private void updateWordCount() {
        int titleLen = titleTextField.getText().length();
        int contentLen = contentTextArea.getText().length();
        
        titleCountLabel.setText(titleLen + "/256");
        contentCountLabel.setText(contentLen + "/20000");
        
        // 超过限制时显示警告色
        if (titleLen > 256) {
            titleCountLabel.setStyle("-fx-text-fill: #f5222d; -fx-min-width: 60px; -fx-font-weight: bold;");
        } else {
            titleCountLabel.setStyle("-fx-text-fill: #666; -fx-min-width: 60px;");
        }
        
        if (contentLen > 20000) {
            contentCountLabel.setStyle("-fx-text-fill: #f5222d; -fx-font-weight: bold;");
        } else {
            contentCountLabel.setStyle("-fx-text-fill: #666;");
        }
    }
    
    private void setupAiAssistantListeners() {
        aiInstructionTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            aiGenerateButton.setDisable(newValue == null || newValue.trim().isEmpty());
        });
        
        aiHelpWriteButton.setOnAction(event -> fillPresetInstruction("help_write"));
        aiContinueButton.setOnAction(event -> fillPresetInstruction("continue"));
        aiPolishButton.setOnAction(event -> fillPresetInstruction("polish"));
        
        aiGenerateButton.setOnAction(event -> generateWithAI());
    }
    
    private void triggerAutoSave() {
        if (!autoSaveEnabled) return;
        
        long now = System.currentTimeMillis();
        if (now - lastAutoSaveTime > AUTO_SAVE_INTERVAL) {
            saveDraft(false);
            lastAutoSaveTime = now;
        }
    }
    
    private void checkAndRecoverDraft() {
        if (!Files.exists(draftFilePath)) return;
        
        try {
            String content = Files.readString(draftFilePath);
            if (content == null || content.trim().isEmpty()) return;
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("草稿恢复");
            alert.setHeaderText("发现未发布的草稿");
            alert.setContentText("是否恢复上次未发布的草稿？");
            
            ButtonType recoverButton = new ButtonType("恢复草稿");
            ButtonType deleteButton = new ButtonType("删除草稿", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(recoverButton, deleteButton);
            
            alert.showAndWait().ifPresent(response -> {
                if (response == recoverButton) {
                    recoverDraft(content);
                } else {
                    deleteDraft();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void recoverDraft(String content) {
        try {
            String[] lines = content.split("\n");
            String draftTitle = "";
            String draftContent = "";
            String draftImageUrls = "";
            
            int mode = 0; // 0: title, 1: content, 2: imageUrls
            StringBuilder contentBuilder = new StringBuilder();
            
            for (String line : lines) {
                if (line.equals("--- TITLE ---")) {
                    mode = 0;
                } else if (line.equals("--- CONTENT ---")) {
                    mode = 1;
                } else if (line.equals("--- IMAGE_URLS ---")) {
                    mode = 2;
                } else {
                    switch (mode) {
                        case 0:
                            draftTitle += line + "\n";
                            break;
                        case 1:
                            contentBuilder.append(line).append("\n");
                            break;
                        case 2:
                            draftImageUrls = line;
                            break;
                    }
                }
            }
            
            if (!draftTitle.isEmpty()) {
                titleTextField.setText(draftTitle.trim());
            }
            if (contentBuilder.length() > 0) {
                contentTextArea.setText(contentBuilder.toString().trim());
            }
            if (!draftImageUrls.isEmpty()) {
                String[] urls = draftImageUrls.split(",");
                for (String url : urls) {
                    if (!url.trim().isEmpty()) {
                        addImageUrl(url.trim());
                    }
                }
            }
            
            updateWordCount();
            showInfo("草稿已恢复");
        } catch (Exception e) {
            e.printStackTrace();
            showError("恢复草稿失败：" + e.getMessage());
        }
    }
    
    private void saveDraftManually() {
        saveDraft(true);
    }
    
    private void saveDraft(boolean showNotification) {
        try {
            Path parentDir = draftFilePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("--- TITLE ---\n");
            sb.append(titleTextField.getText()).append("\n");
            sb.append("--- CONTENT ---\n");
            sb.append(contentTextArea.getText()).append("\n");
            sb.append("--- IMAGE_URLS ---\n");
            sb.append(String.join(",", imageUrls)).append("\n");
            
            Files.writeString(draftFilePath, sb.toString());
            
            if (showNotification) {
                showInfo("草稿已保存");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (showNotification) {
                showError("保存草稿失败：" + e.getMessage());
            }
        }
    }
    
    private void deleteDraft() {
        try {
            if (Files.exists(draftFilePath)) {
                Files.delete(draftFilePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadBoardList() {
        Task<List<Board>> task = new Task<List<Board>>() {
            @Override
            protected List<Board> call() {
                return HttpRequestUtil.getBoardList();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                List<Board> boards = task.getValue();
                if (boards != null) {
                    boardComboBox.getItems().clear();
                    boardComboBox.getItems().addAll(boards);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("加载板块列表失败"));
        });

        new Thread(task).start();
    }
    
    private void addImageUrl(String url) {
        imageUrls.add(url);
        updateImageUrlsTextField();
        addImagePreview(url);
    }
    
    private void removeImageUrl(String url) {
        imageUrls.remove(url);
        updateImageUrlsTextField();
        refreshImagePreview();
    }
    
    private void updateImageUrlsTextField() {
        imageUrlsTextField.setText(String.join(",", imageUrls));
    }
    
    private void addImagePreview(String url) {
        VBox imageContainer = new VBox(5);
        imageContainer.setStyle("-fx-alignment: center;");
        
        ImageView imageView = new ImageView();
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        
        try {
            // 处理相对路径，添加serverUrl前缀
            String fullUrl = url;
            if (url.startsWith("/")) {
                fullUrl = HttpRequestUtil.serverUrl + url;
            } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
                fullUrl = HttpRequestUtil.serverUrl + "/" + url;
            }
            
            Image image = new Image(fullUrl, true);
            imageView.setImage(image);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果加载失败，不显示图片
        }
        
        Button deleteButton = new Button("删除");
        deleteButton.setStyle("-fx-background-color: #f5222d; -fx-text-fill: white; -fx-font-size: 11px;");
        deleteButton.setOnAction(e -> removeImageUrl(url));
        
        imageContainer.getChildren().addAll(imageView, deleteButton);
        imagePreviewPane.getChildren().add(imageContainer);
    }
    
    private void refreshImagePreview() {
        imagePreviewPane.getChildren().clear();
        for (String url : imageUrls) {
            addImagePreview(url);
        }
    }

    private void publishPost() {
        String title = titleTextField.getText().trim();
        Board selectedBoard = boardComboBox.getValue();
        String content = contentTextArea.getText().trim();
        String imageUrlsStr = String.join(",", imageUrls);

        // 验证
        if (title.isEmpty()) {
            showError("标题不能为空");
            return;
        }
        if (title.length() > 256) {
            showError("标题不能超过256字");
            return;
        }
        if (selectedBoard == null) {
            showError("请选择板块");
            return;
        }
        if (content.isEmpty()) {
            showError("内容不能为空");
            return;
        }
        if (content.length() > 20000) {
            showError("内容不能超过20000字");
            return;
        }

        Post post = new Post();
        post.setTitle(title);
        post.setBoardId(selectedBoard.getId());
        post.setContent(content);
        if (!imageUrlsStr.isEmpty()) {
            post.setImages(imageUrlsStr);
        }

        Task<Post> task = new Task<Post>() {
            @Override
            protected Post call() {
                return HttpRequestUtil.publishPost(post);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Post result = task.getValue();
                if (result != null) {
                    showInfo("发布成功！");
                    // 发布成功后删除草稿
                    deleteDraft();
                    autoSaveEnabled = false;
                    closeTab();
                    if (mainFrameController != null) {
                        com.teach.javafx.controller.PostListController plc = mainFrameController.getPostListController();
                        if (plc != null) {
                            plc.loadPostList();
                        }
                    }
                } else {
                    showError("发布失败，请稍后重试");
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("发布失败，请稍后重试"));
        });

        new Thread(task).start();
    }

    private void closeTab() {
        if (AppStore.getMainFrameController() != null) {
            AppStore.getMainFrameController().closeCurrentTab();
        }
    }
    
    private void showPreview() {
        String title = titleTextField.getText().trim();
        String content = contentTextArea.getText().trim();
        Board board = boardComboBox.getValue();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("帖子预览");
        alert.setHeaderText("预览您的帖子");
        
        String contentText = "标题: " + (title.isEmpty() ? "(未填写)" : title) + "\n";
        contentText += "板块: " + (board == null ? "(未选择)" : board.getName()) + "\n";
        contentText += "\n内容:\n" + (content.isEmpty() ? "(未填写)" : content);
        
        if (!imageUrls.isEmpty()) {
            contentText += "\n\n图片数量: " + imageUrls.size();
        }
        
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void selectAndUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择图片");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("图片文件", "*.jpg", "*.jpeg", "*.png", "*.gif"),
            new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null) {
            return;
        }
        
        selectImageButton.setDisable(true);
        selectImageButton.setText("上传中...");
        imageUploadStatusLabel.setText("正在上传图片...");
        
        Task<String> task = new Task<String>() {
            @Override
            protected String call() {
                return HttpRequestUtil.uploadImage(selectedFile.getAbsolutePath());
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                String imageUrl = task.getValue();
                if (imageUrl != null && !imageUrl.isBlank()) {
                    addImageUrl(imageUrl);
                    imageUploadStatusLabel.setText("上传成功！");
                } else {
                    showError("图片上传失败，请稍后重试");
                    imageUploadStatusLabel.setText("上传失败");
                }
                selectImageButton.setDisable(false);
                selectImageButton.setText("选择图片");
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                showError("图片上传失败：" + task.getException().getMessage());
                imageUploadStatusLabel.setText("上传失败");
                selectImageButton.setDisable(false);
                selectImageButton.setText("选择图片");
            });
        });
        
        new Thread(task).start();
    }
    
    private void openAiImageDialog() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/teach/javafx/ai-image-dialog.fxml"));
            javafx.scene.layout.VBox dialogRoot = loader.load();
            AiImageDialogController dialogController = loader.getController();
            
            dialogController.setInitialPrompt(titleTextField.getText().trim(), contentTextArea.getText().trim());
            dialogController.setCallback(imageUrl -> {
                addImageUrl(imageUrl);
                showInfo("图片已添加！");
            });
            
            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.setTitle("AI一键配图");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(aiImageButton.getScene().getWindow());
            
            javafx.scene.Scene scene = new javafx.scene.Scene(dialogRoot);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("打开对话框失败：" + e.getMessage());
        }
    }
    
    // AI写作助手相关方法
    
    private void fillPresetInstruction(String operation) {
        String title = titleTextField.getText().trim();
        String content = contentTextArea.getText().trim();
        String instruction = "";
        
        switch (operation) {
            case "help_write":
                instruction = "请帮我写一篇校园论坛帖子";
                if (!title.isEmpty()) {
                    instruction += "，标题是\"" + title + "\"";
                }
                if (!content.isEmpty()) {
                    instruction += "，参考以下内容：" + content;
                }
                instruction += "。";
                break;
            case "continue":
                if (content.isEmpty()) {
                    showError("请先输入一些内容，然后再让AI续写！");
                    return;
                }
                instruction = "请帮我续写以下帖子内容。";
                if (!title.isEmpty()) {
                    instruction += " 标题：" + title;
                }
                instruction += " 现有内容：" + content;
                break;
            case "polish":
                if (content.isEmpty()) {
                    showError("请先输入一些内容，然后再让AI润色！");
                    return;
                }
                instruction = "请帮我润色以下帖子内容，让它更有吸引力，语言更流畅。";
                if (!title.isEmpty()) {
                    instruction += " 标题：" + title;
                }
                instruction += " 内容：" + content;
                break;
        }
        
        aiInstructionTextArea.setText(instruction);
    }
    
    private void generateWithAI() {
        // 保存原始内容
        originalTitle = titleTextField.getText().trim();
        originalContent = contentTextArea.getText().trim();
        String instruction = aiInstructionTextArea.getText().trim();
        
        // 禁用按钮，显示加载状态和进度条
        aiGenerateButton.setDisable(true);
        aiHelpWriteButton.setDisable(true);
        aiContinueButton.setDisable(true);
        aiPolishButton.setDisable(true);
        aiStatusLabel.setText("AI正在生成中，请稍候...");
        
        // 显示并启动进度条动画
        startProgressAnimation();
        
        Task<com.teach.javafx.models.AiWriteResponse> task = new Task<com.teach.javafx.models.AiWriteResponse>() {
            @Override
            protected com.teach.javafx.models.AiWriteResponse call() {
                return HttpRequestUtil.aiWrite(originalTitle, originalContent, instruction, null);
            }
        };
        
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                // 停止进度条动画
                stopProgressAnimation(true);
                
                com.teach.javafx.models.AiWriteResponse response = task.getValue();
                aiGenerateButton.setDisable(false);
                aiHelpWriteButton.setDisable(false);
                aiContinueButton.setDisable(false);
                aiPolishButton.setDisable(false);
                
                if (response != null && response.getSuccess()) {
                    aiStatusLabel.setText("AI生成成功！");
                    showAcceptOrRejectDialog(response);
                } else {
                    String errorMsg = (response != null && response.getMessage() != null) 
                        ? response.getMessage() 
                        : "AI生成失败，请稍后重试";
                    aiStatusLabel.setText(errorMsg);
                    showError(errorMsg);
                }
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                // 停止进度条动画
                stopProgressAnimation(false);
                
                aiGenerateButton.setDisable(false);
                aiHelpWriteButton.setDisable(false);
                aiContinueButton.setDisable(false);
                aiPolishButton.setDisable(false);
                aiStatusLabel.setText("AI生成失败：" + task.getException().getMessage());
                showError("AI生成失败，请稍后重试");
            });
        });
        
        new Thread(task).start();
    }
    
    /**
     * 启动进度条动画（模仿浏览器加载效果）
     */
    private void startProgressAnimation() {
        // 显示进度条
        aiProgressContainer.setVisible(true);
        aiProgressContainer.setManaged(true);
        
        // 重置进度
        aiProgressBar.setProgress(0.0);
        aiProgressLabel.setText("正在连接 AI 服务...");
        
        // 记录开始时间
        aiStartTime = System.currentTimeMillis();
        
        // 先停止之前的动画
        stopProgressThread();
        
        // 启动新的线程
        aiRunning = true;
        aiProgressThread = new Thread(() -> {
            while (aiRunning) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
                
                // 计算已经过去的时间
                long elapsed = System.currentTimeMillis() - aiStartTime;
                
                // 计算进度百分比 (0.0-0.95)
                double progress = (double) elapsed / TOTAL_EXPECTED_TIME;
                
                // 限制最大进度
                if (progress > 0.95) {
                    progress = 0.95;
                }
                
                // 根据时间选择状态文本
                String statusText = "正在处理...";
                if (progress < 0.2) {
                    statusText = "正在连接 AI 服务...";
                } else if (progress < 0.5) {
                    statusText = "AI 正在思考中...";
                } else if (progress < 0.8) {
                    statusText = "正在生成内容...";
                } else {
                    statusText = "即将完成...";
                }
                
                // 更新 UI - 用Platform.runLater
                final double finalProgress = progress;
                final String finalStatusText = statusText;
                Platform.runLater(() -> {
                    aiProgressBar.setProgress(finalProgress);
                    aiProgressLabel.setText(finalStatusText);
                });
            }
        });
        aiProgressThread.setDaemon(true);
        aiProgressThread.start();
    }
    
    private void stopProgressThread() {
        aiRunning = false;
        if (aiProgressThread != null && aiProgressThread.isAlive()) {
            aiProgressThread.interrupt();
        }
        aiProgressThread = null;
    }
    
    /**
     * 停止进度条动画
     * @param success 是否成功
     */
    private void stopProgressAnimation(boolean success) {
        stopProgressThread();
        
        if (success) {
            // 成功：快速走到 100%
            aiProgressBar.setProgress(1.0);
            aiProgressLabel.setText("完成！");
            
            // 延迟隐藏
            Thread hideThread = new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // ignore
                }
                Platform.runLater(() -> {
                    aiProgressContainer.setVisible(false);
                    aiProgressContainer.setManaged(false);
                });
            });
            hideThread.setDaemon(true);
            hideThread.start();
        } else {
            // 失败：立即隐藏
            aiProgressContainer.setVisible(false);
            aiProgressContainer.setManaged(false);
        }
    }
    
    private void showAcceptOrRejectDialog(com.teach.javafx.models.AiWriteResponse response) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("AI写作助手");
        alert.setHeaderText("AI已生成新的内容");
        
        // 显示对比信息
        StringBuilder contentText = new StringBuilder();
        contentText.append("【原始标题】: ").append(originalTitle.isEmpty() ? "(无)" : originalTitle).append("\n");
        contentText.append("【AI标题】: ").append((response.getTitle() == null || response.getTitle().isEmpty()) ? "(无修改)" : response.getTitle()).append("\n\n");
        contentText.append("【原始内容】: \n").append(originalContent.isEmpty() ? "(无)" : originalContent.substring(0, Math.min(150, originalContent.length())));
        if (originalContent.length() > 150) {
            contentText.append("...");
        }
        contentText.append("\n\n");
        contentText.append("【AI生成内容】: \n").append((response.getContent() == null || response.getContent().isEmpty()) ? "(无修改)" : response.getContent().substring(0, Math.min(150, response.getContent().length())));
        if (response.getContent() != null && response.getContent().length() > 150) {
            contentText.append("...");
        }
        contentText.append("\n\n是否采纳AI生成的内容？");
        
        alert.setContentText(contentText.toString());
        
        ButtonType acceptButton = new ButtonType("采纳");
        ButtonType rejectButton = new ButtonType("弃用", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(acceptButton, rejectButton);
        
        alert.showAndWait().ifPresent(button -> {
            if (button == acceptButton) {
                // 采纳：填充AI内容
                if (response.getTitle() != null && !response.getTitle().isEmpty()) {
                    titleTextField.setText(response.getTitle());
                }
                if (response.getContent() != null && !response.getContent().isEmpty()) {
                    contentTextArea.setText(response.getContent());
                }
                aiStatusLabel.setText("已采纳AI生成的内容");
            } else {
                // 弃用：保持原样
                aiStatusLabel.setText("已保持原始内容");
            }
            updateWordCount();
        });
    }
    
    public void setMainFrameController(com.teach.javafx.controller.base.MainFrameController mainFrameController) {
        this.mainFrameController = mainFrameController;
    }
}
