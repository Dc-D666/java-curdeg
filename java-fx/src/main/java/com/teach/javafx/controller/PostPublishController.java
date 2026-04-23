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
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class PostPublishController extends ToolController {
    @FXML
    private TextField titleTextField;
    @FXML
    private ComboBox<Board> boardComboBox;
    @FXML
    private TextArea contentTextArea;
    @FXML
    private TextField imageUrlsTextField;
    @FXML
    private Button selectImageButton;
    @FXML
    private ImageView imagePreview;
    @FXML
    private Button publishButton;
    @FXML
    private Button cancelButton;
    
    private com.teach.javafx.controller.base.MainFrameController mainFrameController;

    @FXML
    public void initialize() {
        loadBoardList();
        
        publishButton.setOnAction(event -> publishPost());
        cancelButton.setOnAction(event -> closeTab());
        selectImageButton.setOnAction(event -> selectAndUploadImage());
    }
    
    public void setMainFrameController(com.teach.javafx.controller.base.MainFrameController mainFrameController) {
        this.mainFrameController = mainFrameController;
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

    private void publishPost() {
        String title = titleTextField.getText().trim();
        Board selectedBoard = boardComboBox.getValue();
        String content = contentTextArea.getText().trim();
        String imageUrls = imageUrlsTextField.getText().trim();

        if (title.isEmpty()) {
            showError("标题不能为空");
            return;
        }
        if (title.length() > 100) {
            showError("标题不能超过100个字符");
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

        Post post = new Post();
        post.setTitle(title);
        post.setBoardId(selectedBoard.getId());
        post.setContent(content);
        if (!imageUrls.isEmpty()) {
            post.setImages(imageUrls);
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
        
        try {
            Image image = new Image(selectedFile.toURI().toString());
            imagePreview.setImage(image);
        } catch (Exception e) {
            showError("图片加载失败：" + e.getMessage());
            return;
        }
        
        selectImageButton.setDisable(true);
        selectImageButton.setText("上传中...");
        
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
                    String existingUrls = imageUrlsTextField.getText().trim();
                    if (existingUrls.isBlank()) {
                        imageUrlsTextField.setText(imageUrl);
                    } else {
                        imageUrlsTextField.setText(existingUrls + "," + imageUrl);
                    }
                    showInfo("图片上传成功！");
                } else {
                    showError("图片上传失败，请稍后重试");
                }
                selectImageButton.setDisable(false);
                selectImageButton.setText("选择图片");
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                showError("图片上传失败：" + task.getException().getMessage());
                selectImageButton.setDisable(false);
                selectImageButton.setText("选择图片");
            });
        });
        
        new Thread(task).start();
    }
}
