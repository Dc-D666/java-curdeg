package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.PageResult;
import com.teach.javafx.models.Post;
import com.teach.javafx.models.User;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.util.FollowStateManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class PostSearchController extends ToolController {
    @FXML
    private ToggleGroup searchModeGroup;
    @FXML
    private ToggleButton titleSearchBtn;
    @FXML
    private ToggleButton fulltextSearchBtn;
    @FXML
    private ToggleButton aiSearchBtn;
    @FXML
    private TextField keywordTextField;
    @FXML
    private Button searchButton;
    @FXML
    private Button refreshButton;
    @FXML
    private TableView<Post> postTableView;
    @FXML
    private TableColumn<Post, String> titleColumn;
    @FXML
    private TableColumn<Post, String> snippetColumn;
    @FXML
    private TableColumn<Post, String> authorColumn;
    @FXML
    private TableColumn<Post, String> createTimeColumn;
    @FXML
    private TableColumn<Post, Integer> likeCountColumn;
    @FXML
    private TableColumn<Post, Integer> commentCountColumn;
    @FXML
    private TableColumn<Post, String> statusColumn;
    @FXML
    private Label pageInfoLabel;
    @FXML
    private ComboBox<Integer> pageSizeComboBox;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;

    private int currentPageNum = 1;
    private int currentPageSize = 20;
    private String currentKeyword = null;
    private String currentSearchType = "title";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private User currentUser;
    private Set<Integer> followingUserIds = new HashSet<>();

    @FXML
    public void initialize() {
        // 设置表格行高
        postTableView.setFixedCellSize(50.0);
        
        // 标题列 - 使用 TextFlow 展示高亮
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setCellFactory(param -> new TableCell<Post, String>() {
            private final TextFlow textFlow = new TextFlow();
            
            {
                textFlow.setPrefWidth(200);
                textFlow.setMaxHeight(40);
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                textFlow.getChildren().clear();
                
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    Post post = getTableRow().getItem();
                    String displayText = item != null ? item : post.getTitle();
                    
                    // 优先使用高亮标题
                    if (post.getHighlightTitle() != null && !post.getHighlightTitle().isEmpty()) {
                        // 解析高亮标题
                        buildHighlightTextFlow(textFlow, post.getHighlightTitle());
                    } else if (displayText != null) {
                        // 普通文本
                        Text text = new Text(displayText);
                        text.setStyle("-fx-font-size: 12px;");
                        textFlow.getChildren().add(text);
                    }
                    setGraphic(textFlow);
                }
            }
        });
        
        authorColumn.setCellFactory(param -> new TableCell<Post, String>() {
            private final ImageView imageView = new ImageView();
            private final Label label = new Label();
            private final HBox hbox = new HBox(5, imageView, label);
            private Consumer<Boolean> listener = null;
            private Long currentUserId = null;

            {
                imageView.setFitWidth(32);
                imageView.setFitHeight(32);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    if (listener != null && currentUserId != null) {
                        FollowStateManager.getInstance().unregisterListener(currentUserId, listener);
                        listener = null;
                        currentUserId = null;
                    }
                    setGraphic(null);
                } else {
                    Post post = getTableView().getItems().get(getIndex());
                    String avatarUrl = post.getAuthorAvatarUrl();
                    String nickname = post.getAuthorNickname();

                    if (avatarUrl != null && !avatarUrl.isBlank()) {
                        try {
                            Image image = new Image(avatarUrl, true);
                            imageView.setImage(image);
                        } catch (Exception e) {
                            imageView.setImage(null);
                        }
                    } else {
                        imageView.setImage(null);
                    }

                    StringBuilder displayText = new StringBuilder();
                    if (nickname != null) {
                        displayText.append(nickname);
                    }

                    Long userId = post.getUserId();
                    if (userId != null) {
                        if (listener != null && !userId.equals(currentUserId)) {
                            FollowStateManager.getInstance().unregisterListener(currentUserId, listener);
                            listener = null;
                        }

                        Boolean isFollowed = FollowStateManager.getInstance().getFollowState(userId);
                        if (isFollowed != null && isFollowed) {
                            displayText.append(" (已关注)");
                        } else if (followingUserIds.contains(userId.intValue())) {
                            displayText.append(" (已关注)");
                        }

                        if (listener == null || !userId.equals(currentUserId)) {
                            currentUserId = userId;
                            final Long finalUserId = userId;
                            listener = (followed) -> {
                                Platform.runLater(() -> {
                                    Post currentPost = getTableView().getItems().get(getIndex());
                                    if (currentPost != null && finalUserId.equals(currentPost.getUserId())) {
                                        StringBuilder newDisplayText = new StringBuilder();
                                        String newNickname = currentPost.getAuthorNickname();
                                        if (newNickname != null) {
                                            newDisplayText.append(newNickname);
                                        }
                                        if (followed) {
                                            newDisplayText.append(" (已关注)");
                                        }
                                        label.setText(newDisplayText.toString());
                                    }
                                });
                            };
                            FollowStateManager.getInstance().registerListener(userId, listener);
                        }
                    }

                    label.setText(displayText.toString());
                    setGraphic(hbox);
                }
            }
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
        statusColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatusText());
        });
        
        // 配置片段列 - 使用 TextFlow 展示高亮
        snippetColumn.setCellValueFactory(new PropertyValueFactory<>("highlightSnippet"));
        snippetColumn.setCellFactory(param -> new TableCell<Post, String>() {
            private final TextFlow textFlow = new TextFlow();
            
            {
                textFlow.setPrefWidth(300);
                textFlow.setMaxHeight(40);
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                textFlow.getChildren().clear();
                
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    // 解析高亮片段
                    buildHighlightTextFlow(textFlow, item);
                    setGraphic(textFlow);
                }
            }
        });

        postTableView.setRowFactory(tv -> {
            TableRow<Post> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    Post post = row.getItem();
                    openPostDetail(post.getId());
                }
            });
            return row;
        });

        searchButton.setOnAction(event -> doSearch());

        keywordTextField.setOnAction(event -> doSearch());

        refreshButton.setOnAction(event -> {
            // 刷新当前搜索结果
            currentPageNum = 1;
            if (currentKeyword != null && !currentKeyword.isBlank()) {
                searchPosts(refreshButton);
            }
        });

        prevButton.setOnAction(event -> {
            if (currentPageNum > 1) {
                currentPageNum--;
                doSearch();
            }
        });

        nextButton.setOnAction(event -> {
            currentPageNum++;
            doSearch();
        });

        pageSizeComboBox.setItems(FXCollections.observableArrayList(10, 20, 50));
        pageSizeComboBox.setValue(currentPageSize);
        pageSizeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentPageSize = newValue;
                currentPageNum = 1;
                doSearch();
            }
        });

        searchModeGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle == titleSearchBtn) {
                currentSearchType = "title";
                keywordTextField.setPromptText("输入帖子标题关键词...");
            } else if (newToggle == fulltextSearchBtn) {
                currentSearchType = "fulltext";
                keywordTextField.setPromptText("输入帖子内容关键词...");
            } else if (newToggle == aiSearchBtn) {
                currentSearchType = "ai";
                keywordTextField.setPromptText("用自然语言描述你想找的内容...");
            }
        });

        loadCurrentUser();
        loadFollowingList();
    }

    // 解析 HTML 高亮标签，构建 TextFlow
    private void buildHighlightTextFlow(TextFlow textFlow, String htmlContent) {
        if (htmlContent == null) {
            return;
        }
        
        // 简单解析 <span style="color: red">xxx</span> 标签
        String remaining = htmlContent;
        while (!remaining.isEmpty()) {
            int spanStart = remaining.indexOf("<span");
            if (spanStart == -1) {
                // 没有标签了，直接添加
                if (!remaining.isEmpty()) {
                    addNormalText(textFlow, remaining);
                }
                break;
            }
            
            // 添加标签前的普通文本
            if (spanStart > 0) {
                addNormalText(textFlow, remaining.substring(0, spanStart));
            }
            
            // 找到 span 结束
            int spanEnd = remaining.indexOf("</span>", spanStart);
            if (spanEnd == -1) {
                // 格式不对，添加全部
                addNormalText(textFlow, remaining.substring(spanStart));
                break;
            }
            
            // 找到标签内容
            int contentStart = remaining.indexOf('>', spanStart) + 1;
            String highlightText = remaining.substring(contentStart, spanEnd);
            
            // 添加高亮文本
            addHighlightText(textFlow, highlightText);
            
            // 继续处理剩余部分
            remaining = remaining.substring(spanEnd + 7); // 7 = "</span>".length()
        }
    }
    
    private void addNormalText(TextFlow textFlow, String text) {
        if (text != null && !text.isEmpty()) {
            Text t = new Text(text);
            // 片段文本使用灰色小字号
            if (snippetColumn != null) {
                t.setStyle("-fx-font-size: 11px; -fx-fill: #555;");
            }
            textFlow.getChildren().add(t);
        }
    }
    
    private void addHighlightText(TextFlow textFlow, String text) {
        if (text != null && !text.isEmpty()) {
            Text t = new Text(text);
            t.setStyle("-fx-font-size: 12px; -fx-fill: red; -fx-font-weight: bold;");
            textFlow.getChildren().add(t);
        }
    }

    private void loadCurrentUser() {
        Task<User> task = new Task<User>() {
            @Override
            protected User call() {
                return HttpRequestUtil.getCurrentUser();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                currentUser = task.getValue();
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                System.out.println("loadCurrentUser failed");
            });
        });

        new Thread(task).start();
    }

    private void loadFollowingList() {
        Task<List<User>> task = new Task<List<User>>() {
            @Override
            protected List<User> call() {
                return HttpRequestUtil.getMyFollowingList();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                List<User> followingList = task.getValue();
                followingUserIds.clear();
                if (followingList != null) {
                    for (User user : followingList) {
                        if (user.getPersonId() != null) {
                            followingUserIds.add(user.getPersonId().intValue());
                            FollowStateManager.getInstance().setFollowState(user.getPersonId().longValue(), true);
                        }
                    }
                }
                postTableView.refresh();
            });
        });

        new Thread(task).start();
    }

    private void doSearch() {
        currentKeyword = keywordTextField.getText();
        System.out.println("Search: type=" + currentSearchType + ", keyword=" + currentKeyword);
        searchPosts();
    }

    public void searchPosts() {
        searchPosts(null);
    }

    private void searchPosts(Button refreshBtn) {
        if (currentKeyword == null || currentKeyword.isBlank()) {
            showAlert("提示", "请输入搜索关键词");
            return;
        }
        
        if (refreshBtn != null) {
            refreshBtn.setDisable(true);
            refreshBtn.setText("刷新中");
        }

        Task<PageResult<Post>> task = new Task<PageResult<Post>>() {
            @Override
            protected PageResult<Post> call() {
                return HttpRequestUtil.searchPosts(currentKeyword, currentSearchType, currentPageNum, currentPageSize);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                if (refreshBtn != null) {
                    refreshBtn.setDisable(false);
                    refreshBtn.setText("刷新");
                }
                
                PageResult<Post> pageResult = task.getValue();
                System.out.println("searchPosts succeeded: pageResult=" + pageResult);
                if (pageResult != null) {
                    System.out.println("  list=" + pageResult.getList() + ", size=" + (pageResult.getList() != null ? pageResult.getList().size() : "null"));
                    System.out.println("  total=" + pageResult.getTotal() + ", pageNum=" + pageResult.getPageNum());
                }
                if (pageResult != null && pageResult.getList() != null) {
                    postTableView.getItems().clear();
                    postTableView.getItems().addAll(pageResult.getList());
                    System.out.println("  Table updated with " + pageResult.getList().size() + " items");

                    long total = pageResult.getTotal() != null ? pageResult.getTotal() : 0;
                    pageInfoLabel.setText("共 " + total + " 条，第 " + currentPageNum + " 页");

                    prevButton.setDisable(currentPageNum <= 1);
                    int totalPages = (int) Math.ceil((double) total / currentPageSize);
                    nextButton.setDisable(currentPageNum >= totalPages);
                } else {
                    System.out.println("  No data to display");
                    postTableView.getItems().clear();
                    pageInfoLabel.setText("未找到结果");
                    prevButton.setDisable(true);
                    nextButton.setDisable(true);
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                if (refreshBtn != null) {
                    refreshBtn.setDisable(false);
                    refreshBtn.setText("刷新");
                }
                showError("搜索失败");
            });
        });

        new Thread(task).start();
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
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
