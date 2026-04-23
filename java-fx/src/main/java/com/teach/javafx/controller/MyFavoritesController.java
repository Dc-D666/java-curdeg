package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.PageResult;
import com.teach.javafx.models.Post;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

import java.text.SimpleDateFormat;

public class MyFavoritesController extends ToolController {
    @FXML
    private TableView<Post> postTableView;
    @FXML
    private TableColumn<Post, String> titleColumn;
    @FXML
    private TableColumn<Post, String> contentColumn;
    @FXML
    private TableColumn<Post, String> authorColumn;
    @FXML
    private TableColumn<Post, String> createTimeColumn;
    @FXML
    private TableColumn<Post, Integer> likeCountColumn;
    @FXML
    private TableColumn<Post, Integer> commentCountColumn;
    @FXML
    private Label totalLabel;
    @FXML
    private Label pageInfoLabel;
    @FXML
    private Button refreshButton;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;

    private int currentPageNum = 1;
    private int currentPageSize = 10;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        contentColumn.setCellValueFactory(cellData -> {
            String content = cellData.getValue().getContent();
            if (content != null && content.length() > 50) {
                return new javafx.beans.property.SimpleStringProperty(content.substring(0, 50) + "...");
            }
            return new javafx.beans.property.SimpleStringProperty(content != null ? content : "");
        });
        
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("authorNickname"));
        
        createTimeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreateTime() != null) {
                return new javafx.beans.property.SimpleStringProperty(dateFormat.format(cellData.getValue().getCreateTime()));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        likeCountColumn.setCellValueFactory(new PropertyValueFactory<>("likeCount"));
        commentCountColumn.setCellValueFactory(new PropertyValueFactory<>("commentCount"));

        postTableView.setRowFactory(tv -> {
            TableRow<Post> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1 && !row.isEmpty()) {
                    Post post = row.getItem();
                    openPostDetail(post.getId());
                }
            });
            return row;
        });

        prevButton.setOnAction(event -> onPrevPage());
        nextButton.setOnAction(event -> onNextPage());

        refreshButton.setOnAction(event -> {
            currentPageNum = 1;
            loadFavorites(refreshButton);
        });

        loadFavorites();
    }

    public void loadFavorites() {
        loadFavorites(null);
    }

    private void loadFavorites(Button refreshBtn) {
        if (refreshBtn != null) {
            refreshBtn.setDisable(true);
            refreshBtn.setText("刷新中");
        }
        
        Task<PageResult<Post>> task = new Task<PageResult<Post>>() {
            @Override
            protected PageResult<Post> call() {
                return HttpRequestUtil.getMyFavorites(currentPageNum, currentPageSize);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                if (refreshBtn != null) {
                    refreshBtn.setDisable(false);
                    refreshBtn.setText("刷新");
                }
                
                PageResult<Post> pageResult = task.getValue();
                if (pageResult != null && pageResult.getList() != null) {
                    postTableView.getItems().clear();
                    postTableView.getItems().addAll(pageResult.getList());
                    
                    long total = pageResult.getTotal() != null ? pageResult.getTotal() : 0;
                    totalLabel.setText("共 " + total + " 条");
                    pageInfoLabel.setText("第 " + currentPageNum + " 页");
                    
                    prevButton.setDisable(currentPageNum <= 1);
                    int totalPages = (int) Math.ceil((double) total / currentPageSize);
                    nextButton.setDisable(currentPageNum >= totalPages);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                if (refreshBtn != null) {
                    refreshBtn.setDisable(false);
                    refreshBtn.setText("刷新");
                }
                showError("加载收藏列表失败");
            });
        });

        new Thread(task).start();
    }

    public void onPrevPage() {
        if (currentPageNum > 1) {
            currentPageNum--;
            loadFavorites();
        }
    }

    public void onNextPage() {
        currentPageNum++;
        loadFavorites();
    }

    private void openPostDetail(Long postId) {
        if (AppStore.getMainFrameController() != null) {
            try {
                javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(MainApplication.class.getResource("post-detail.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 1024, 768);
                PostDetailController controller = fxmlLoader.getController();
                controller.setPostId(postId);
                
                String tabName = "post-detail-" + postId;
                AppStore.getMainFrameController().changeContentWithScene(tabName, "帖子详情", scene, controller);
            } catch (Exception e) {
                e.printStackTrace();
                showError("打开帖子详情失败");
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
