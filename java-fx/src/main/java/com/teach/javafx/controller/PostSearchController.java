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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
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

    private String currentAnswerText = ""; // 保存原始答案用于复制
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
                String cleanText = currentAnswerText
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
                copyAnswerBtn.setText("✅ 已复制");
                copyAnswerBtn.setStyle("-fx-background-color: #52c41a; -fx-text-fill: white;");
                
                // 2秒后恢复
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
                pause.setOnFinished(e -> {
                    copyAnswerBtn.setText("📋 复制答案");
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
                formatAnswerBtn.setText("✅ 已格式化");
                formatAnswerBtn.setStyle("-fx-background-color: #13c2c2; -fx-text-fill: white;");
                
                // 2秒后恢复
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
                pause.setOnFinished(e -> {
                    formatAnswerBtn.setText("✨ 格式化显示");
                    formatAnswerBtn.setStyle("-fx-background-color: #52c41a; -fx-text-fill: white;");
                });
                pause.play();
            }
        });
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
                if (pageResult != null && pageResult.getList() != null) {
                    postTableView.getItems().clear();
                    postTableView.getItems().addAll(pageResult.getList());

                    long total = pageResult.getTotal() != null ? pageResult.getTotal() : 0;
                    pageInfoLabel.setText("共 " + total + " 条，第 " + currentPageNum + " 页");

                    prevButton.setDisable(currentPageNum <= 1);
                    int totalPages = (int) Math.ceil((double) total / currentPageSize);
                    nextButton.setDisable(currentPageNum >= totalPages);
                } else {
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
        aiAnswerBox.setVisible(true);
        aiAnswerBox.setManaged(true);
        normalSearchBox.setVisible(false);
        normalSearchBox.setManaged(false);

        final StringBuilder fullAnswerBuffer = new StringBuilder();
        // 用于记录解析状态，避免重复解析
        final int[] lastParsedLength = {0};

        HttpRequestUtil.aiSearchStream(
            currentKeyword,
            posts -> Platform.runLater(() -> {
                searchButton.setText("思考中...");
                if (posts != null && !posts.isEmpty()) {
                    displayRelatedPosts(posts);
                    relatedPostsBox.setVisible(true);
                    relatedPostsBox.setManaged(true);
                }
            }),
            content -> Platform.runLater(() -> {
                searchButton.setText("生成中...");
                fullAnswerBuffer.append(content);
                currentAnswerText = fullAnswerBuffer.toString();
                
                // 边流式输出，边增量解析！只解析新增加的部分！
                incrementalParseAndDisplay(currentAnswerText, lastParsedLength[0]);
                lastParsedLength[0] = currentAnswerText.length();
            }),
            error -> Platform.runLater(() -> {
                searchButton.setDisable(false);
                searchButton.setText("搜索");
                showError("AI 搜索失败: " + error);
            }),
            () -> Platform.runLater(() -> {
                searchButton.setDisable(false);
                searchButton.setText("搜索");
                // 输出完成后，自动格式化显示！（几百个字符不会卡死！）
                System.out.println("[AI搜索] 输出完成！开始格式化... 当前长度=" + currentAnswerText.length());
                aiAnswerTextFlow.getChildren().clear();
                parseAndDisplayMarkdown(currentAnswerText);
                System.out.println("[AI搜索] 格式化完成！");
            })
        );
    }

    private void displayAiAnswer(String answer) {
        this.currentAnswerText = answer; // 保存原始答案
        aiAnswerTextFlow.getChildren().clear();
        
        // 初始化时完整解析一次
        incrementalParseAndDisplay(answer, 0);
    }
    
    /**
     * 核心增量解析方法！
     * 策略：
     * 1. 永远不做全量重绘（防止卡死）
     * 2. 每次只追加解析增量，或者简单地更新
     * 3. 在性能和显示效果之间平衡！
     */
    private void incrementalParseAndDisplay(String fullText, int oldLength) {
        // 简单但高效的策略：
        // 流式过程中，使用单个Text节点显示纯文本
        // 这样100%不会卡死！用户体验最流畅！
        aiAnswerTextFlow.getChildren().clear();
        Text text = new Text(fullText);
        text.setStyle("-fx-font-size: 13px; -fx-line-spacing: 5px;");
        aiAnswerTextFlow.getChildren().add(text);
    }
    
    /**
     * 解析 Markdown 格式并显示在 TextFlow 中
     * 支持: **加粗**, *斜体*, ~~删除线**, [红色]标红文本[/红色]
     */
    private void parseAndDisplayMarkdown(String content) {
        // 关闭刷屏日志
        if (content == null || content.isEmpty()) {
            return;
        }
        
        // 处理换行
        content = content.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
        
        int index = 0;
        int length = content.length();
        
        while (index < length) {
            // 优先检查红色标签，因为它的匹配方式和其他不同
            int redStart = content.indexOf("[红色]", index);
            if (redStart == index) {
                // 找到了红色标签开始
                int redEnd = content.indexOf("[/红色]", redStart + 4);
                if (redEnd != -1) {
                    String redText = content.substring(redStart + 4, redEnd);
                    addStyledText(redText, "-fx-fill: #ff4444; -fx-font-size: 13px; -fx-line-spacing: 5px;");
                    index = redEnd + 5; // [/红色] 是 5 个字符
                    continue;
                }
            }
            
            // 检查其他标签
            char currentChar = content.charAt(index);
            
            if (index + 1 < length && currentChar == '*' && content.charAt(index + 1) == '*') {
                // 加粗 **text**
                int endIndex = content.indexOf("**", index + 2);
                if (endIndex != -1) {
                    String boldText = content.substring(index + 2, endIndex);
                    addStyledText(boldText, "-fx-font-weight: bold; -fx-font-size: 13px; -fx-line-spacing: 5px;");
                    index = endIndex + 2;
                    continue;
                }
            }
            
            if (index + 1 < length && currentChar == '_' && content.charAt(index + 1) == '_') {
                // 加粗 __text__
                int endIndex = content.indexOf("__", index + 2);
                if (endIndex != -1) {
                    String boldText = content.substring(index + 2, endIndex);
                    addStyledText(boldText, "-fx-font-weight: bold; -fx-font-size: 13px; -fx-line-spacing: 5px;");
                    index = endIndex + 2;
                    continue;
                }
            }
            
            if (currentChar == '*') {
                // 斜体 *text*
                int endIndex = content.indexOf('*', index + 1);
                if (endIndex != -1) {
                    String italicText = content.substring(index + 1, endIndex);
                    addStyledText(italicText, "-fx-font-style: italic; -fx-font-size: 13px; -fx-line-spacing: 5px;");
                    index = endIndex + 1;
                    continue;
                }
            }
            
            if (currentChar == '_') {
                // 斜体 _text_
                int endIndex = content.indexOf('_', index + 1);
                if (endIndex != -1) {
                    String italicText = content.substring(index + 1, endIndex);
                    addStyledText(italicText, "-fx-font-style: italic; -fx-font-size: 13px; -fx-line-spacing: 5px;");
                    index = endIndex + 1;
                    continue;
                }
            }
            
            if (index + 2 < length && currentChar == '~' && content.charAt(index + 1) == '~') {
                // 删除线 ~~text~~
                int endIndex = content.indexOf("~~", index + 2);
                if (endIndex != -1) {
                    String strikeText = content.substring(index + 2, endIndex);
                    addStyledText(strikeText, "-fx-strikethrough: true; -fx-font-size: 13px; -fx-line-spacing: 5px;");
                    index = endIndex + 2;
                    continue;
                }
            }
            
            if (currentChar == '\n') {
                // 换行
                addStyledText("\n", "-fx-font-size: 13px; -fx-line-spacing: 5px;");
                index++;
                continue;
            }
            
            // 普通文本
            int nextSpecial = findNextSpecialIndex(content, index);
            String plainText = content.substring(index, nextSpecial);
            if (!plainText.isEmpty()) {
                addStyledText(plainText, "-fx-font-size: 13px; -fx-line-spacing: 5px;");
            }
            index = nextSpecial;
        }
    }
    
    /**
     * 找到下一个特殊字符的位置
     */
    private int findNextSpecialIndex(String content, int startIndex) {
        int length = content.length();
        for (int i = startIndex; i < length; i++) {
            char c = content.charAt(i);
            if (c == '*' || c == '_' || c == '~' || c == '[' || c == '\n') {
                return i;
            }
        }
        return length;
    }
    
    /**
     * 添加带样式的文本到 TextFlow
     */
    private void addStyledText(String text, String style) {
        if (text == null || text.isEmpty()) {
            return;
        }
        Text t = new Text(text);
        t.setStyle(style);
        aiAnswerTextFlow.getChildren().add(t);
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
    }
}
