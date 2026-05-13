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
import javafx.scene.Node;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.text.SimpleDateFormat;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
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
    private Label progressLabel;
    @FXML
    private VBox emptyResultBox;
    @FXML
    private Label emptyTitleLabel;
    @FXML
    private Label emptyDescLabel;
    @FXML
    private Button quickPostBtn;

    @FXML
    private VBox aiAnswerBox;
    @FXML
    private TextFlow aiAnswerTextFlow;
    @FXML
    private Button formatAnswerBtn;
    @FXML
    private VBox relatedPostsBox;
    @FXML
    private TableView<Post> relatedPostsTable;
    @FXML
    private TableColumn<Post, String> relatedTitleColumn;
    @FXML
    private TableColumn<Post, String> relatedAuthorColumn;
    @FXML
    private TableColumn<Post, String> relatedCreateTimeColumn;
    @FXML
    private VBox normalSearchBox;
    @FXML
    private Button copyAnswerBtn;
    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private VBox searchContentBox;

    private String currentAnswerText = ""; // 保存原始答案用于复制
    private int currentPageNum = 1;
    private int currentPageSize = 20;
    private String currentKeyword = null;
    private String currentSearchType = "title";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private User currentUser;
    private Set<Integer> followingUserIds = new HashSet<>();
    
    // AI 推荐的发帖信息
    private String suggestedPostTitle = null;
    private String suggestedPostContent = null;
    private boolean hasRelatedPosts = false; // 是否有相关帖子
    
    // AI 搜索缓冲机制相关变量
    private final StringBuilder streamBuffer = new StringBuilder();
    private final AtomicBoolean bufferDirty = new AtomicBoolean(false);
    private final AtomicLong lastFlushTime = new AtomicLong(0);
    private ScheduledExecutorService flushExecutor;
    private final Object bufferLock = new Object();
    private static final double PAGE_SCROLL_STEP = 120.0;
    private static final long BUFFER_FLUSH_INTERVAL_MS = 100; // 100ms 刷新窗口

    @FXML
    public void initialize() {
        // 启动缓冲刷新定时器
        startBufferFlushTimer();
        setupPageScroll();
        
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

        quickPostBtn.setOnAction(event -> {
            openQuickPostPage();
        });

        searchModeGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if (newToggle == titleSearchBtn) {
                currentSearchType = "title";
                keywordTextField.setPromptText("输入帖子标题关键词...");
                resetToNormalSearch();
            } else if (newToggle == fulltextSearchBtn) {
                currentSearchType = "fulltext";
                keywordTextField.setPromptText("输入帖子内容关键词...");
                resetToNormalSearch();
            } else if (newToggle == aiSearchBtn) {
                currentSearchType = "ai";
                keywordTextField.setPromptText("用自然语言描述你想找的内容...");
                aiAnswerBox.setVisible(false);
                aiAnswerBox.setManaged(false);
                relatedPostsBox.setVisible(false);
                relatedPostsBox.setManaged(false);
                normalSearchBox.setVisible(true);
                normalSearchBox.setManaged(true);
            }
        });

        loadCurrentUser();
        loadFollowingList();

        // 初始化AI搜索相关组件
        relatedPostsTable.setFixedCellSize(40.0);
        relatedTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        relatedAuthorColumn.setCellValueFactory(new PropertyValueFactory<>("authorNickname"));
        relatedCreateTimeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreateTime() != null) {
                return new javafx.beans.property.SimpleStringProperty(dateFormat.format(cellData.getValue().getCreateTime()));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        relatedPostsTable.setRowFactory(tv -> {
            TableRow<Post> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    Post post = row.getItem();
                    openPostDetail(post.getId());
                }
            });
            return row;
        });

        // 绑定复制答案按钮事件
        copyAnswerBtn.setOnAction(event -> {
            if (currentAnswerText != null && !currentAnswerText.isEmpty()) {
                // 清理标签，只保留纯文本
                String cleanText = sanitizeAiDisplayText(currentAnswerText)
                        .replaceAll("\\*\\*", "")
                        .replaceAll("\\*", "")
                        .replaceAll("~~", "")
                        .replaceAll("\\[红色\\]", "")
                        .replaceAll("\\[/红色\\]", "");
                
                // 复制到剪贴板
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(cleanText);
                clipboard.setContent(content);
                
                // 提示用户
                copyAnswerBtn.setText("已复制");
                copyAnswerBtn.setStyle("-fx-background-color: #52c41a; -fx-text-fill: white;");
                
                // 2秒后恢复
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
                pause.setOnFinished(e -> {
                    copyAnswerBtn.setText("复制答案");
                    copyAnswerBtn.setStyle("-fx-background-color: #1890ff; -fx-text-fill: white;");
                });
                pause.play();
                
                System.out.println("[AI搜索前端] 答案已复制: " + cleanText);
            } else {
                System.out.println("[AI搜索前端] 没有可复制的答案");
            }
        });
        
        // 绑定格式化显示按钮事件！
        formatAnswerBtn.setOnAction(event -> {
            if (currentAnswerText != null && !currentAnswerText.isEmpty()) {
                System.out.println("[AI搜索前端] 正在格式化答案...");
                aiAnswerTextFlow.getChildren().clear();
                parseAndDisplayMarkdown(currentAnswerText);
                formatAnswerBtn.setText("已格式化");
                formatAnswerBtn.setStyle("-fx-background-color: #13c2c2; -fx-text-fill: white;");
                
                // 2秒后恢复
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
                pause.setOnFinished(e -> {
                    formatAnswerBtn.setText("格式化显示");
                    formatAnswerBtn.setStyle("-fx-background-color: #52c41a; -fx-text-fill: white;");
                });
                pause.play();
            }
        });
    }

    // 解析 HTML 高亮标签，构建 TextFlow
    private void setupPageScroll() {
        if (mainScrollPane == null) {
            return;
        }

        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        if (searchContentBox != null) {
            searchContentBox.prefWidthProperty().bind(mainScrollPane.viewportBoundsProperty().map(bounds -> bounds.getWidth()));
        } else if (mainScrollPane.getContent() instanceof Region contentRegion) {
            contentRegion.prefWidthProperty().bind(mainScrollPane.viewportBoundsProperty().map(bounds -> bounds.getWidth()));
        }

        mainScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (isFromResultTable(event.getTarget())) {
                return;
            }

            double scrollableHeight = mainScrollPane.getContent().getBoundsInLocal().getHeight()
                    - mainScrollPane.getViewportBounds().getHeight();
            if (scrollableHeight <= 0) {
                return;
            }

            double delta = event.getDeltaY() == 0 ? event.getTextDeltaY() * PAGE_SCROLL_STEP : event.getDeltaY();
            double nextPixel = mainScrollPane.getVvalue() * scrollableHeight - delta;
            mainScrollPane.setVvalue(clamp(nextPixel / scrollableHeight, 0, 1));
            event.consume();
        });
    }

    private boolean isFromResultTable(Object target) {
        if (!(target instanceof Node node)) {
            return false;
        }
        while (node != null) {
            if (node == postTableView || node == relatedPostsTable) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

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

        if ("ai".equals(currentSearchType)) {
            aiSearch();
        } else {
            searchPosts();
        }
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
        
        // 先隐藏空结果框
        emptyResultBox.setVisible(false);
        emptyResultBox.setManaged(false);

        Task<PageResult<Post>> task = new Task<PageResult<Post>>() {
            @Override
            protected PageResult<Post> call() {
                return HttpRequestUtil.searchPosts(currentKeyword, currentSearchType, 1, 20);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                if (refreshBtn != null) {
                    refreshBtn.setDisable(false);
                    refreshBtn.setText("刷新");
                }
                
                PageResult<Post> pageResult = task.getValue();
                if (pageResult != null && pageResult.getList() != null && !pageResult.getList().isEmpty()) {
                    postTableView.getItems().clear();
                    postTableView.getItems().addAll(pageResult.getList());
                } else {
                    postTableView.getItems().clear();
                    showEmptyResultBox();
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

    /**
     * 启动缓冲刷新定时器
     */
    private void startBufferFlushTimer() {
        flushExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AI-Search-Flush-Thread");
            t.setDaemon(true);
            return t;
        });
        
        flushExecutor.scheduleAtFixedRate(this::flushBufferIfNeeded, 
                                           BUFFER_FLUSH_INTERVAL_MS, 
                                           BUFFER_FLUSH_INTERVAL_MS, 
                                           TimeUnit.MILLISECONDS);
        
        System.out.println("[AI搜索前端] 缓冲刷新定时器已启动，刷新间隔=" + BUFFER_FLUSH_INTERVAL_MS + "ms");
    }
    
    /**
     * 刷新缓冲区（如果需要）
     */
    private void flushBufferIfNeeded() {
        if (bufferDirty.get()) {
            flushBuffer();
        }
    }
    
    /**
     * 强制刷新缓冲区到 UI
     */
    private void flushBuffer() {
        String contentToFlush;
        synchronized (bufferLock) {
            if (streamBuffer.length() == 0) {
                bufferDirty.set(false);
                return;
            }
            contentToFlush = streamBuffer.toString();
            bufferDirty.set(false);
            lastFlushTime.set(System.currentTimeMillis());
        }
        
        final String finalContent = contentToFlush;
        Platform.runLater(() -> {
            currentAnswerText = finalContent;
            updateStreamDisplay(finalContent);
        });
    }
    
    /**
     * 更新流式显示（纯文本）
     */
    private void updateStreamDisplay(String text) {
        // 先清理 SUGGEST_POST 标签，确保不会显示
        String cleanedText = sanitizeAiDisplayText(removeSuggestPostTags(text));
        aiAnswerTextFlow.getChildren().clear();
        Text t = new Text(cleanedText);
        t.setStyle("-fx-font-size: 13px; -fx-line-spacing: 5px;");
        aiAnswerTextFlow.getChildren().add(t);
    }

private void aiSearch() {
        if (currentKeyword == null || currentKeyword.isBlank()) {
            showAlert("提示", "请输入搜索关键词");
            return;
        }

        searchButton.setDisable(true);
        searchButton.setText("搜索中...");

        // 清空并准备
        currentAnswerText = "";
        aiAnswerTextFlow.getChildren().clear();
        normalSearchBox.setVisible(false);
        normalSearchBox.setManaged(false);
        relatedPostsBox.setVisible(false);
        relatedPostsBox.setManaged(false);
        aiAnswerBox.setVisible(false);
        aiAnswerBox.setManaged(false);
        emptyResultBox.setVisible(false);
        emptyResultBox.setManaged(false);
        
        // 重置 AI 推荐信息
        suggestedPostTitle = null;
        suggestedPostContent = null;
        
        // 显示进度提示
        showProgressLabel("正在理解用户意图...");
        
        // 清空缓冲
        synchronized (bufferLock) {
            streamBuffer.setLength(0);
            bufferDirty.set(false);
        }
        
        final StringBuilder fullAnswerBuffer = new StringBuilder();
        final AtomicBoolean streamStarted = new AtomicBoolean(false);
        final AtomicBoolean hasRelatedPostsLocal = new AtomicBoolean(false);
        final AtomicLong contentReceivedCount = new AtomicLong(0);

        System.out.println("[AI搜索前端] 开始 AI 搜索，关键词=" + currentKeyword);

        HttpRequestUtil.aiSearchStream(
            currentKeyword,
            posts -> Platform.runLater(() -> {
                showProgressLabel("正在连接 AI 服务...");
                if (posts != null && !posts.isEmpty()) {
                    hasRelatedPostsLocal.set(true);
                    hasRelatedPosts = true;
                    displayRelatedPosts(posts);
                    relatedPostsBox.setVisible(true);
                    relatedPostsBox.setManaged(true);
                } else {
                    hasRelatedPosts = false;
                }
            }),
            content -> {
                long count = contentReceivedCount.incrementAndGet();
                System.out.println("[AI搜索前端] 收到内容片段 #" + count + ", 长度=" + content.length());
                
                fullAnswerBuffer.append(content);
                
                // 添加到缓冲区
                synchronized (bufferLock) {
                    streamBuffer.append(content);
                    bufferDirty.set(true);
                }
                
                // 第一次收到内容时立即更新 UI
                if (streamStarted.compareAndSet(false, true)) {
                    Platform.runLater(() -> {
                        showProgressLabel("AI 正在回复...");
                        aiAnswerBox.setVisible(true);
                        aiAnswerBox.setManaged(true);
                    });
                }
            },
            error -> {
                System.out.println("[AI搜索前端] 收到错误: " + error);
                // 先刷新剩余缓冲
                flushBuffer();
                Platform.runLater(() -> {
                    hideProgressLabel();
                    searchButton.setDisable(false);
                    searchButton.setText("搜索");
                    showError("AI 搜索失败: " + error);
                });
            },
            () -> {
                System.out.println("[AI搜索前端] 流完成，总共收到 " + contentReceivedCount.get() + " 个片段");
                // 强制刷新所有缓冲
                flushBuffer();
                Platform.runLater(() -> {
                    String rawAnswer = fullAnswerBuffer.toString();
                    // 先清理 SUGGEST_POST 标签，再保存和显示
                    currentAnswerText = sanitizeAiDisplayText(removeSuggestPostTags(rawAnswer));
                    
                    if (currentAnswerText.isEmpty() && !hasRelatedPostsLocal.get()) {
                        // 没有结果也没有相关帖子，显示引导界面
                        hideProgressLabel();
                        searchButton.setDisable(false);
                        searchButton.setText("搜索");
                        showEmptyResultBox();
                    } else {
                        // 有内容，显示进度完成并格式化
                        showProgressLabel("回复完成！");
                        
                        if (!currentAnswerText.isEmpty()) {
                            System.out.println("[AI搜索前端] 开始格式化 Markdown，原始长度=" + rawAnswer.length() + "，清理后长度=" + currentAnswerText.length());
                            parseAndDisplayMarkdown(currentAnswerText);
                        }
                        
                        searchButton.setDisable(false);
                        searchButton.setText("搜索");
                        
                        // 2秒后隐藏进度提示
                        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
                        pause.setOnFinished(e -> hideProgressLabel());
                        pause.play();
                    }
                });
            },
            (title, content) -> {
                // 收到 AI 建议的发帖信息
                Platform.runLater(() -> {
                    System.out.println("[AI搜索前端] 收到 AI 发帖建议！标题=" + title);
                    suggestedPostTitle = title;
                    suggestedPostContent = content;
                    // 显示引导界面
                    hideProgressLabel();
                    searchButton.setDisable(false);
                    searchButton.setText("搜索");
                    showEmptyResultBox();
                });
            }
        );
    }

    private void displayAiAnswer(String answer) {
        this.currentAnswerText = answer; // 保存原始答案
        aiAnswerTextFlow.getChildren().clear();
        updateStreamDisplay(answer);
    }
    
    /**
     * 清理资源（在控制器销毁时调用）
     */
    public void cleanup() {
        if (flushExecutor != null && !flushExecutor.isShutdown()) {
            flushExecutor.shutdownNow();
            System.out.println("[AI搜索前端] 缓冲刷新定时器已关闭");
        }
    }
    
    /**
     * 解析 Markdown 格式并显示在 TextFlow 中
     * 简化版本：优先保证所有文字都显示，再应用格式
     */
    private void parseAndDisplayMarkdown(String content) {
        long startTime = System.currentTimeMillis();
        // System.out.println("[Markdown解析] 开始解析，内容长度=" + (content != null ? content.length() : 0));
        
        if (content == null || content.isEmpty()) {
            // System.out.println("[Markdown解析] 内容为空，直接返回");
            return;
        }
        
        try {
            // 先输出原始内容的前100字符用于调试
            // System.out.println("[Markdown解析] 原始内容前100字符：" + 
            //     (content.length() > 100 ? content.substring(0, 100) + "..." : content));
            
            // 1. 先清理 SUGGEST_POST 标签，确保不会显示在前端
            String cleanedContent = sanitizeAiDisplayText(removeSuggestPostTags(content));
            System.out.println("[Markdown解析] 清理后内容长度=" + cleanedContent.length());
            
            // 2. 再在内存中构建所有 Text 节点
            List<Text> textNodes = new ArrayList<>();
            String processedContent = cleanedContent.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
            int length = processedContent.length();
            
            int index = 0;
            int safetyCounter = 0;
            int maxIterations = length * 3; // 更宽松的安全保护
            
            while (index < length && safetyCounter < maxIterations) {
                safetyCounter++;
                
                boolean matched = false;
                
                // System.out.println("[Markdown解析] 当前索引：" + index + "，字符：" + 
                //     (index < length ? "'" + processedContent.charAt(index) + "'" : "END"));
                
                // 检查标题（必须在行首或换行后）
                if (index == 0 || (index > 0 && processedContent.charAt(index - 1) == '\n')) {
                    // 检查 #### Heading 4
                    if (index + 4 <= length && processedContent.startsWith("####", index) && 
                        (index + 4 == length || processedContent.charAt(index + 4) == ' ')) {
                        int endIndex = processedContent.indexOf('\n', index + 5);
                        if (endIndex == -1) endIndex = length;
                        String headingText = processedContent.substring(index + 5, endIndex).trim();
                        // System.out.println("[Markdown解析] 找到H4标题：" + headingText);
                        if (!headingText.isEmpty()) {
                            textNodes.add(createStyledText(headingText + "\n", 
                                "-fx-font-weight: bold; -fx-font-size: 15px; -fx-line-spacing: 8px;"));
                        }
                        index = endIndex;
                        matched = true;
                    }
                    // 检查 ### Heading 3
                    else if (!matched && index + 3 <= length && processedContent.startsWith("###", index) && 
                        (index + 3 == length || processedContent.charAt(index + 3) == ' ')) {
                        int endIndex = processedContent.indexOf('\n', index + 4);
                        if (endIndex == -1) endIndex = length;
                        String headingText = processedContent.substring(index + 4, endIndex).trim();
                        // System.out.println("[Markdown解析] 找到H3标题：" + headingText);
                        if (!headingText.isEmpty()) {
                            textNodes.add(createStyledText(headingText + "\n", 
                                "-fx-font-weight: bold; -fx-font-size: 17px; -fx-line-spacing: 8px;"));
                        }
                        index = endIndex;
                        matched = true;
                    }
                    // 检查 ## Heading 2
                    else if (!matched && index + 2 <= length && processedContent.startsWith("##", index) && 
                        (index + 2 == length || processedContent.charAt(index + 2) == ' ')) {
                        int endIndex = processedContent.indexOf('\n', index + 3);
                        if (endIndex == -1) endIndex = length;
                        String headingText = processedContent.substring(index + 3, endIndex).trim();
                        // System.out.println("[Markdown解析] 找到H2标题：" + headingText);
                        if (!headingText.isEmpty()) {
                            textNodes.add(createStyledText(headingText + "\n", 
                                "-fx-font-weight: bold; -fx-font-size: 19px; -fx-line-spacing: 8px;"));
                        }
                        index = endIndex;
                        matched = true;
                    }
                    // 检查 # Heading 1
                    else if (!matched && index + 1 <= length && processedContent.startsWith("#", index) && 
                        (index + 1 == length || processedContent.charAt(index + 1) == ' ')) {
                        int endIndex = processedContent.indexOf('\n', index + 2);
                        if (endIndex == -1) endIndex = length;
                        String headingText = processedContent.substring(index + 2, endIndex).trim();
                        // System.out.println("[Markdown解析] 找到H1标题：" + headingText);
                        if (!headingText.isEmpty()) {
                            textNodes.add(createStyledText(headingText + "\n", 
                                "-fx-font-weight: bold; -fx-font-size: 21px; -fx-line-spacing: 8px;"));
                        }
                        index = endIndex;
                        matched = true;
                    }
                }
                
                if (matched) {
                    continue;
                }
                
                // 检查红色标签 [红色]text[/红色]
                String redStartTag = "[红色]";
                String redEndTag = "[/红色]";
                if (processedContent.startsWith(redStartTag, index)) {
                    int contentStart = index + redStartTag.length();
                    int redEnd = processedContent.indexOf(redEndTag, contentStart);
                    if (redEnd != -1) {
                        String redText = processedContent.substring(contentStart, redEnd);
                        // System.out.println("[Markdown解析] 找到红色文本：" + redText);
                        textNodes.add(createStyledText(redText, "-fx-fill: #d4380d; -fx-font-weight: bold; -fx-font-size: 13px; -fx-line-spacing: 5px;"));
                        index = redEnd + redEndTag.length();
                        continue;
                    }
                }
                
                char currentChar = processedContent.charAt(index);
                
                // 加粗 **text**
                if (index + 1 < length && currentChar == '*' && processedContent.charAt(index + 1) == '*') {
                    int endIndex = processedContent.indexOf("**", index + 2);
                    if (endIndex != -1) {
                        String boldText = processedContent.substring(index + 2, endIndex);
                        // System.out.println("[Markdown解析] 找到粗体：" + boldText);
                        textNodes.add(createStyledText(boldText, "-fx-font-weight: bold; -fx-font-size: 13px; -fx-line-spacing: 5px;"));
                        index = endIndex + 2;
                        continue;
                    }
                }
                
                // 删除线 ~~text~~
                if (index + 1 < length && currentChar == '~' && processedContent.charAt(index + 1) == '~') {
                    int endIndex = processedContent.indexOf("~~", index + 2);
                    if (endIndex != -1) {
                        String strikeText = processedContent.substring(index + 2, endIndex);
                        // System.out.println("[Markdown解析] 找到删除线：" + strikeText);
                        textNodes.add(createStyledText(strikeText, "-fx-strikethrough: true; -fx-font-size: 13px; -fx-line-spacing: 5px;"));
                        index = endIndex + 2;
                        continue;
                    }
                }
                
                // 斜体 *text*
                if (currentChar == '*') {
                    int endIndex = processedContent.indexOf('*', index + 1);
                    if (endIndex != -1) {
                        String italicText = processedContent.substring(index + 1, endIndex);
                        // System.out.println("[Markdown解析] 找到斜体：" + italicText);
                        textNodes.add(createStyledText(italicText, "-fx-font-style: italic; -fx-font-size: 13px; -fx-line-spacing: 5px;"));
                        index = endIndex + 1;
                        continue;
                    }
                }
                
                // 普通文本处理 - 一次只处理一个字符，确保不跳过标记
                textNodes.add(createStyledText(String.valueOf(currentChar), "-fx-font-size: 13px; -fx-line-spacing: 5px;"));
                index++;
            }
            
            if (safetyCounter >= maxIterations) {
                // System.err.println("[Markdown解析] 警告：达到安全迭代限制，可能有解析错误");
            }
            
            // 验证节点完整性
            StringBuilder nodeContent = new StringBuilder();
            for (Text node : textNodes) {
                nodeContent.append(node.getText());
            }
            // System.out.println("[Markdown解析] 节点构建完成，共 " + textNodes.size() + " 个节点");
            // System.out.println("[Markdown解析] 合并后内容长度：" + nodeContent.length());
            
            // 2. 一次性更新 UI
            aiAnswerTextFlow.getChildren().clear();
            aiAnswerTextFlow.getChildren().addAll(textNodes);
            // System.out.println("[Markdown解析] UI更新完成，总耗时=" + (System.currentTimeMillis() - startTime) + "ms");
            
        } catch (Exception e) {
            // System.err.println("[Markdown解析] 解析出错：" + e.getMessage());
            // e.printStackTrace();
            // 出错时降级显示纯文本
            aiAnswerTextFlow.getChildren().clear();
            Text t = new Text(content);
            t.setStyle("-fx-font-size: 13px; -fx-line-spacing: 5px;");
            aiAnswerTextFlow.getChildren().add(t);
        }
    }
    
    /**
     * 创建带样式的 Text 对象（不在 UI 线程也可以创建）
     */
    private Text createStyledText(String text, String style) {
        if (text == null || text.isEmpty()) {
            return new Text("");
        }
        Text t = new Text(text);
        t.setStyle(style);
        return t;
    }

    private String sanitizeAiDisplayText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder builder = new StringBuilder(text.length());
        text.codePoints().forEach(codePoint -> {
            if (isUnsupportedAiSymbol(codePoint)) {
                return;
            }
            builder.appendCodePoint(codePoint);
        });
        return builder.toString();
    }

    private boolean isUnsupportedAiSymbol(int codePoint) {
        if (codePoint >= 0x1F000 && codePoint <= 0x1FAFF) {
            return true;
        }
        if (codePoint >= 0x2600 && codePoint <= 0x27BF) {
            return true;
        }
        if (codePoint == 0xFE0F || codePoint == 0x200D) {
            return true;
        }
        return "✓✗★☆✔✘".indexOf(codePoint) >= 0;
    }
    
    /**
     * 从内容中移除 SUGGEST_POST 标签及其包裹的内容
     */
    private String removeSuggestPostTags(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        try {
            // 移除 [SUGGEST_POST] ... [/SUGGEST_POST] 标签及其内容
            String pattern = "\\[SUGGEST_POST\\]\\s*标题：.*?\\s*内容：.*?\\s*\\[/SUGGEST_POST\\]";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher m = p.matcher(content);
            String result = m.replaceAll("");
            
            // 如果替换后只剩余空白字符，返回空字符串
            if (result.trim().isEmpty()) {
                return "";
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("[Markdown解析] 移除 SUGGEST_POST 标签失败: " + e.getMessage());
            return content;
        }
    }
    
    /**
     * 找到下一个特殊字符的位置
     */
    private int findNextSpecialIndex(String content, int startIndex) {
        int length = content.length();
        int nextIndex = length;
        
        // 查找各种标记
        int nextStar = content.indexOf('*', startIndex);
        int nextTilde = content.indexOf('~', startIndex);
        int nextBracket = content.indexOf('[', startIndex);
        int nextNewLine = content.indexOf('\n', startIndex);
        
        // 找最小的那个
        if (nextStar != -1 && nextStar < nextIndex) nextIndex = nextStar;
        if (nextTilde != -1 && nextTilde < nextIndex) nextIndex = nextTilde;
        if (nextBracket != -1 && nextBracket < nextIndex) nextIndex = nextBracket;
        if (nextNewLine != -1 && nextNewLine < nextIndex) nextIndex = nextNewLine;
        
        // System.out.println("[Markdown解析] findNextSpecialIndex(" + startIndex + ") → " + nextIndex);
        return nextIndex;
    }
    


    private void displayRelatedPosts(List<Post> posts) {
        relatedPostsTable.getItems().clear();
        relatedPostsTable.getItems().addAll(posts);
    }

    private void resetToNormalSearch() {
        aiAnswerBox.setVisible(false);
        aiAnswerBox.setManaged(false);
        relatedPostsBox.setVisible(false);
        relatedPostsBox.setManaged(false);
        normalSearchBox.setVisible(true);
        normalSearchBox.setManaged(true);
        emptyResultBox.setVisible(false);
        emptyResultBox.setManaged(false);
        hideProgressLabel();
    }
    
    private void showProgressLabel(String text) {
        progressLabel.setText(text);
        progressLabel.setVisible(true);
        progressLabel.setManaged(true);
    }
    
    private void hideProgressLabel() {
        progressLabel.setVisible(false);
        progressLabel.setManaged(false);
    }
    
    private void showEmptyResultBox() {
        // 根据是否有相关帖子显示不同文案
        if (hasRelatedPosts) {
            emptyTitleLabel.setText("对搜索结果不满意？");
            emptyTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1890ff;");
            emptyDescLabel.setText("直接发帖问大家，获得更多人帮助！");
        } else {
            emptyTitleLabel.setText("未找到相关帖子");
            emptyTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #d46b08;");
            emptyDescLabel.setText("要不要发布一个问题，让大家来帮你解答？");
        }
        emptyResultBox.setVisible(true);
        emptyResultBox.setManaged(true);
    }
    
    private void openQuickPostPage() {
        if (AppStore.getMainFrameController() != null) {
            try {
                javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(MainApplication.class.getResource("post-publish.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 1024, 768);
                com.teach.javafx.controller.PostPublishController controller = fxmlLoader.getController();
                
                // 优先使用 AI 推荐的标题和内容
                if (suggestedPostTitle != null && !suggestedPostTitle.isBlank()) {
                    controller.prefillPost(suggestedPostTitle, suggestedPostContent);
                } else if (currentKeyword != null && !currentKeyword.isBlank()) {
                    controller.prefillTitle(currentKeyword);
                }
                
                AppStore.getMainFrameController().changeContentWithScene("quick-post", "发布帖子", scene, controller);
            } catch (Exception e) {
                e.printStackTrace();
                showError("打开发帖页面失败");
            }
        }
    }
}
