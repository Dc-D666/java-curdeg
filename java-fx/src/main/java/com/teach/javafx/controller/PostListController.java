package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.Board;
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
import javafx.scene.paint.Color;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class PostListController extends ToolController {
    @FXML
    private ComboBox<Board> boardComboBox;
    @FXML
    private TextField keywordTextField;
    @FXML
    private Button searchButton;
    @FXML
    private Button publishButton;
    @FXML
    private Button refreshButton;
    @FXML
    private TableView<Post> postTableView;
    @FXML
    private TableColumn<Post, String> titleColumn;
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
    private TableColumn<Post, String> moderationStatusColumn;
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
    private Long currentBoardId = null;
    private String currentKeyword = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private User currentUser;
    private Set<Integer> followingUserIds = new HashSet<>();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellFactory(param -> new TableCell<Post, String>() {
            private final ImageView imageView = new ImageView();
            private final Label label = new Label();
            private final HBox hbox = new HBox(5, imageView, label);
            private Consumer<Boolean> listener = null;
            private Long currentUserId = null;
            
            {
                imageView.setFitWidth(24);
                imageView.setFitHeight(24);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setCache(true);
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
                            String fullAvatarUrl = avatarUrl.startsWith("/") ? 
                                HttpRequestUtil.serverUrl + avatarUrl : avatarUrl;
                            Image image = new Image(fullAvatarUrl, true);
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
        
        moderationStatusColumn.setCellFactory(param -> new TableCell<Post, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setText(null);
                    setTextFill(Color.BLACK);
                } else {
                    Post post = getTableView().getItems().get(getIndex());
                    String moderationStatus = post.getModerationStatus();
                    String displayText = post.getModerationStatusText();
                    
                    setText(displayText);
                    
                    if ("pending".equals(moderationStatus) || "manual".equals(moderationStatus)) {
                        setTextFill(Color.ORANGE);
                    } else if ("pass".equals(moderationStatus)) {
                        setTextFill(Color.GREEN);
                    } else if ("reject".equals(moderationStatus)) {
                        setTextFill(Color.RED);
                    } else {
                        setText(null);
                    }
                }
            }
        });

        boardComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentBoardId = newValue.getId();
                System.out.println("Board selected: " + newValue.getName() + ", id=" + currentBoardId);
            } else {
                currentBoardId = null;
                System.out.println("Board deselected");
            }
            currentPageNum = 1;
            loadPostList();
        });

        postTableView.setRowFactory(tv -> {
            TableRow<Post> row = new TableRow<>();
            row.setPrefHeight(36);
            row.setMinHeight(36);
            row.setMaxHeight(36);
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    Post post = row.getItem();
                    openPostDetail(post.getId());
                }
            });
            return row;
        });

        searchButton.setOnAction(event -> {
            String input = keywordTextField.getText();
            if (tryOpenPostFromLink(input)) {
                return;
            }
            currentKeyword = input;
            System.out.println("Search clicked, keyword=" + currentKeyword);
            currentPageNum = 1;
            loadPostList();
        });
        
        keywordTextField.setOnAction(event -> {
            String input = keywordTextField.getText();
            if (tryOpenPostFromLink(input)) {
                return;
            }
            currentKeyword = input;
            System.out.println("Enter pressed, keyword=" + currentKeyword);
            currentPageNum = 1;
            loadPostList();
        });

        publishButton.setOnAction(event -> openPublishPost());

        refreshButton.setOnAction(event -> {
            // 重置并重新加载
            currentPageNum = 1;
            loadPostList(refreshButton);
        });

        prevButton.setOnAction(event -> {
            if (currentPageNum > 1) {
                currentPageNum--;
                loadPostList();
            }
        });

        nextButton.setOnAction(event -> {
            currentPageNum++;
            loadPostList();
        });

        pageSizeComboBox.setItems(FXCollections.observableArrayList(10, 20, 50));
        pageSizeComboBox.setValue(currentPageSize);
        pageSizeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentPageSize = newValue;
                currentPageNum = 1;
                loadPostList();
            }
        });

        publishButton.setVisible(false);
        
        loadBoardList();
        loadCurrentUser();
        loadPostList();
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
                updatePublishButtonVisibility();
                loadFollowingList();
            });
        });
        
        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                updatePublishButtonVisibility();
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
    
    private void updatePublishButtonVisibility() {
        boolean isLoggedIn = currentUser != null;
        boolean isBanned = isLoggedIn && Boolean.TRUE.equals(currentUser.getIsBanned());
        publishButton.setVisible(isLoggedIn && !isBanned);
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
                    Board allBoard = new Board();
                    allBoard.setId(null);
                    allBoard.setName("全部");
                    boardComboBox.getItems().add(allBoard);
                    boardComboBox.getItems().addAll(boards);
                    boardComboBox.getSelectionModel().selectFirst();
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                showError("加载板块列表失败");
            });
        });

        new Thread(task).start();
    }

    public void loadPostList() {
        loadPostList(null);
    }

    private void loadPostList(Button refreshBtn) {
        if (refreshBtn != null) {
            refreshBtn.setDisable(true);
            refreshBtn.setText("刷新中");
        }
        
        Task<PageResult<Post>> task = new Task<PageResult<Post>>() {
            @Override
            protected PageResult<Post> call() {
                return HttpRequestUtil.getPostList(currentBoardId, currentKeyword, currentPageNum, currentPageSize);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                if (refreshBtn != null) {
                    refreshBtn.setDisable(false);
                    refreshBtn.setText("刷新");
                }
                
                PageResult<Post> pageResult = task.getValue();
                System.out.println("loadPostList succeeded: pageResult=" + pageResult);
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
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                if (refreshBtn != null) {
                    refreshBtn.setDisable(false);
                    refreshBtn.setText("刷新");
                }
                showError("加载帖子列表失败");
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

    private void openPublishPost() {
        if (AppStore.getMainFrameController() != null) {
            AppStore.getMainFrameController().changeContent("post-publish", "发布帖子");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean tryOpenPostFromLink(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        String trimmedInput = input.trim();

        // 从文本中提取 bbs://post/{id} 链接（可以包含其他文本）
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("bbs://post/(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(trimmedInput);

        if (matcher.find()) {
            try {
                Long postId = Long.parseLong(matcher.group(1));
                openPostDetail(postId);
                return true;
            } catch (NumberFormatException e) {
                // 解析失败，继续正常搜索
                return false;
            }
        }

        return false;
    }
}
