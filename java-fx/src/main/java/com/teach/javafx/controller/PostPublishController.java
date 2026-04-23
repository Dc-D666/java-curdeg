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
    private Button publishButton;
    @FXML
    private Button cancelButton;
    
    private com.teach.javafx.controller.base.MainFrameController mainFrameController;

    @FXML
    public void initialize() {
        loadBoardList();
        
        publishButton.setOnAction(event -> publishPost());
        cancelButton.setOnAction(event -> closeTab());
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
}
