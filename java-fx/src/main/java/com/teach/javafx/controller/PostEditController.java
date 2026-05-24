package com.teach.javafx.controller;

import com.teach.javafx.models.Post;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PostEditController {
    @FXML
    private TextField titleTextField;
    @FXML
    private TextArea contentTextArea;
    @FXML
    private TextField imageUrlsTextField;
    @FXML
    private TextArea attachmentInfosTextArea;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    private Long postId;
    private Post originalPost;
    private PostEditCallback callback;

    public interface PostEditCallback {
        void onPostEdited(Post updatedPost);
    }

    @FXML
    public void initialize() {
        cancelButton.setOnAction(event -> closeDialog());
        saveButton.setOnAction(event -> savePost());
    }

    public void setPost(Post post) {
        this.originalPost = post;
        this.postId = post.getId();
        titleTextField.setText(post.getTitle());
        contentTextArea.setText(post.getContent());
        if (post.getImages() != null) {
            imageUrlsTextField.setText(post.getImages());
        }
        if (post.getAttachmentInfos() != null) {
            attachmentInfosTextArea.setText(post.getAttachmentInfos());
        }
    }

    public void setCallback(PostEditCallback callback) {
        this.callback = callback;
    }

    private void savePost() {
        String title = titleTextField.getText().trim();
        String content = contentTextArea.getText().trim();
        String imageUrls = imageUrlsTextField.getText().trim();
        String attachmentInfos = attachmentInfosTextArea.getText().trim();

        if (title.isEmpty()) {
            showError("标题不能为空");
            return;
        }
        if (title.length() > 256) {
            showError("标题不能超过256个字符");
            return;
        }
        if (content.isEmpty()) {
            showError("内容不能为空");
            return;
        }
        if (content.length() > 20000) {
            showError("内容不能超过20000个字符");
            return;
        }

        saveButton.setDisable(true);

        Task<Post> task = new Task<Post>() {
            @Override
            protected Post call() {
                return HttpRequestUtil.updatePost(postId, title, content, imageUrls.isEmpty() ? null : imageUrls, attachmentInfos.isEmpty() ? "" : attachmentInfos);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Post result = task.getValue();
                if (result != null) {
                    showInfo("保存成功！");
                    if (callback != null) {
                        callback.onPostEdited(result);
                    }
                    closeDialog();
                } else {
                    showError("保存失败，请稍后重试");
                    saveButton.setDisable(false);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                showError("保存失败，请稍后重试");
                saveButton.setDisable(false);
            });
        });

        new Thread(task).start();
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showInfo(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
