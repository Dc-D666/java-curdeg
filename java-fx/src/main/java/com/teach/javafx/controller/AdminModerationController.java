package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.Post;
import com.teach.javafx.models.User;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.models.PageResult;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class AdminModerationController extends ToolController {

    @FXML
    private TableView<Post> postTable;

    @FXML
    private TableColumn<Post, Long> idColumn;

    @FXML
    private TableColumn<Post, String> titleColumn;

    @FXML
    private TableColumn<Post, String> authorColumn;

    @FXML
    private TableColumn<Post, String> createTimeColumn;

    @FXML
    private TableColumn<Post, String> moderationStatusColumn;

    @FXML
    private TableColumn<Post, Void> actionColumn;

    @FXML
    private TableColumn<Post, String> moderatorColumn;

    @FXML
    private TableColumn<Post, String> moderationTimeColumn;

    @FXML
    private Button pendingOnlyButton;

    @FXML
    private Button allPostsButton;

    @FXML
    private Label pageInfoLabel;

    @FXML
    private Button prevPageButton;

    @FXML
    private Button nextPageButton;

    @FXML
    private VBox moderationPanel;

    @FXML
    private Label selectedPostTitleLabel;
    @FXML
    private VBox moderationFlowCard;
    @FXML
    private HBox moderationFlowStepsBox;
    @FXML
    private Label moderationFlowSummaryLabel;

    @FXML
    private TextArea postContentArea;

    @FXML
    private ComboBox<String> decisionChoiceBox;

    @FXML
    private ComboBox<String> violationLevelChoiceBox;

    @FXML
    private ComboBox<String> violationTypeChoiceBox;

    @FXML
    private TextArea remarkArea;

    @FXML
    private Button submitModerationButton;

    @FXML
    private Button cancelModerationButton;

    private ObservableList<Post> postList = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int pageSize = 10;
    private boolean showPendingOnly = true;
    private Post selectedPost = null;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(cellData -> {
            Post post = cellData.getValue();
            String authorName = post.getAuthorNickname();
            return new javafx.beans.property.SimpleStringProperty(authorName != null ? authorName : "");
        });
        createTimeColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
        moderationStatusColumn.setCellValueFactory(new PropertyValueFactory<>("moderationStatusText"));
        moderationStatusColumn.setCellFactory(column -> new TableCell<Post, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Post post = getTableRow().getItem();
                    if (post != null) {
                        String status = post.getModerationStatus();
                        if ("pending".equals(status) || "manual".equals(status)) {
                            setStyle("-fx-text-fill: orange;");
                        } else if ("reject".equals(status)) {
                            setStyle("-fx-text-fill: red;");
                        } else if ("pass".equals(status)) {
                            setStyle("-fx-text-fill: green;");
                        }
                    }
                }
            }
        });

        moderatorColumn.setCellValueFactory(cellData -> {
            Post post = cellData.getValue();
            String moderatorName = post.getModeratorNickname();
            return new javafx.beans.property.SimpleStringProperty(moderatorName != null ? moderatorName : "");
        });

        moderationTimeColumn.setCellValueFactory(cellData -> {
            Post post = cellData.getValue();
            String time = post.getModerationTime();
            return new javafx.beans.property.SimpleStringProperty(time != null ? time : "");
        });

        actionColumn.setCellFactory(param -> new TableCell<Post, Void>() {
            private final HBox hBox = new HBox(5);
            private final Button reviewButton = new Button("审核");

            {
                reviewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                reviewButton.setOnAction(event -> {
                    Post post = getTableView().getItems().get(getIndex());
                    showModerationPanel(post);
                });
                hBox.getChildren().add(reviewButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hBox);
                }
            }
        });

        postTable.setItems(postList);

        decisionChoiceBox.getItems().addAll("pass", "reject");
        decisionChoiceBox.setValue("pass");

        violationLevelChoiceBox.getItems().addAll("LOW", "MEDIUM", "HIGH");
        violationLevelChoiceBox.setValue("LOW");

        violationTypeChoiceBox.getItems().addAll("SPAM", "INAPPROPRIATE_CONTENT", "VIOLATION", "OTHER");
        violationTypeChoiceBox.setValue("OTHER");

        moderationPanel.setVisible(false);
        moderationFlowCard.setVisible(false);
        moderationFlowCard.setManaged(false);

        loadPosts();
    }

    @FXML
    private void togglePendingOnly() {
        showPendingOnly = true;
        currentPage = 1;
        pendingOnlyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        allPostsButton.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: black;");
        loadPosts();
    }

    @FXML
    private void toggleAllPosts() {
        showPendingOnly = false;
        currentPage = 1;
        allPostsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        pendingOnlyButton.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: black;");
        loadPosts();
    }

    private void loadPosts() {
        System.out.println("===== AdminModerationController.loadPosts =====");
        System.out.println("当前模式：" + (showPendingOnly ? "只显示待审核" : "显示全部"));
        System.out.println("当前页码：" + currentPage + ", 页大小：" + pageSize);
        
        Task<PageResult<Post>> task = new Task<PageResult<Post>>() {
            @Override
            protected PageResult<Post> call() {
                System.out.println("开始调用API...");
                if (showPendingOnly) {
                    return HttpRequestUtil.getAdminPendingPosts(currentPage, pageSize);
                } else {
                    return HttpRequestUtil.getAdminAllPosts(currentPage, pageSize);
                }
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                PageResult<Post> pageResult = task.getValue();
                System.out.println("API返回结果：" + (pageResult != null ? "有数据" : "null"));
                if (pageResult != null) {
                    List<Post> posts = pageResult.getList();
                    System.out.println("帖子数量：" + (posts != null ? posts.size() : 0));
                    postList.setAll(posts);
                    updatePageInfo(pageResult.getTotal());
                } else {
                    System.err.println("pageResult为null！");
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                System.err.println("===== loadPosts 任务失败 =====");
                event.getSource().getException().printStackTrace();
            });
        });

        System.out.println("启动任务线程...");
        new Thread(task).start();
    }

    private void updatePageInfo(long total) {
        int totalPages = (int) Math.ceil((double) total / pageSize);
        pageInfoLabel.setText("第 " + currentPage + " / " + totalPages + " 页，共 " + total + " 条");
        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);
    }

    @FXML
    private void prevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadPosts();
        }
    }

    @FXML
    private void nextPage() {
        currentPage++;
        loadPosts();
    }

    private void showModerationPanel(Post post) {
        selectedPost = post;
        selectedPostTitleLabel.setText(post.getTitle());
        postContentArea.setText(post.getContent() != null ? post.getContent() : "");
        decisionChoiceBox.setValue("pass");
        violationLevelChoiceBox.setValue("LOW");
        violationTypeChoiceBox.setValue("OTHER");
        remarkArea.setText("");
        renderModerationFlow(post);
        moderationPanel.setVisible(true);
    }

    @FXML
    private void hideModerationPanel() {
        selectedPost = null;
        moderationFlowCard.setVisible(false);
        moderationFlowCard.setManaged(false);
        moderationPanel.setVisible(false);
    }

    @FXML
    private void submitModeration() {
        System.out.println("===== AdminModerationController.submitModeration =====");
        if (selectedPost == null) {
            System.err.println("selectedPost为null，无法审核");
            return;
        }

        String decision = decisionChoiceBox.getValue();
        String violationLevel = "reject".equals(decision) ? violationLevelChoiceBox.getValue() : null;
        String violationType = "reject".equals(decision) ? violationTypeChoiceBox.getValue() : null;
        String remark = remarkArea.getText().isEmpty() ? null : remarkArea.getText();

        System.out.println("帖子ID：" + selectedPost.getId());
        System.out.println("标题：" + selectedPost.getTitle());
        System.out.println("审核决定：" + decision);
        System.out.println("违规级别：" + violationLevel);
        System.out.println("违规类型：" + violationType);
        System.out.println("备注：" + remark);

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                System.out.println("调用审核API...");
                return HttpRequestUtil.moderatePost(selectedPost.getId(), decision, violationLevel, violationType, remark);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Boolean success = task.getValue();
                System.out.println("审核API返回：" + success);
                if (Boolean.TRUE.equals(success)) {
                    System.out.println("审核成功！");
                    hideModerationPanel();
                    loadPosts();
                    showInfo("审核成功！");
                } else {
                    System.err.println("审核失败！");
                    showError("审核失败，请稍后重试");
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                System.err.println("===== submitModeration 任务失败 =====");
                event.getSource().getException().printStackTrace();
                showError("审核失败，请稍后重试");
            });
        });

        System.out.println("启动审核任务...");
        new Thread(task).start();
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

    private void renderModerationFlow(Post post) {
        if (moderationFlowCard == null || moderationFlowStepsBox == null || moderationFlowSummaryLabel == null || post == null) {
            return;
        }
        moderationFlowCard.setVisible(true);
        moderationFlowCard.setManaged(true);
        moderationFlowStepsBox.getChildren().clear();

        Post.ModerationFlowView flowView = post.getModerationFlowView();
        moderationFlowSummaryLabel.setText(flowView.getSummary());
        List<Post.ModerationFlowStep> steps = flowView.getSteps();

        for (int i = 0; i < steps.size(); i++) {
            Post.ModerationFlowStep step = steps.get(i);
            moderationFlowStepsBox.getChildren().add(createFlowStepNode(step, i + 1));
            if (i < steps.size() - 1) {
                moderationFlowStepsBox.getChildren().add(createFlowConnector(flowView.isConnectorReached(i)));
            }
        }
    }

    private VBox createFlowStepNode(Post.ModerationFlowStep step, int stepNumber) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().addAll("moderation-flow-step", "moderation-flow-step-compact");

        Label indexLabel = new Label(String.valueOf(stepNumber));
        indexLabel.getStyleClass().addAll("moderation-flow-index", "moderation-flow-index-compact", resolveFlowIndexStyleClass(step.getVisualState()));

        Label titleLabel = new Label(step.getTitle());
        titleLabel.getStyleClass().addAll("moderation-flow-step-title", "moderation-flow-step-title-compact");

        Label stateLabel = new Label(step.getStateText());
        stateLabel.getStyleClass().addAll("moderation-flow-step-state", "moderation-flow-step-state-compact", resolveFlowStateStyleClass(step.getVisualState()));

        box.getChildren().addAll(indexLabel, titleLabel, stateLabel);
        return box;
    }

    private Region createFlowConnector(boolean reached) {
        Region connector = new Region();
        connector.getStyleClass().addAll("moderation-flow-connector", "moderation-flow-connector-compact");
        connector.getStyleClass().add(reached ? "moderation-flow-connector-active" : "moderation-flow-connector-inactive");
        HBox.setHgrow(connector, Priority.ALWAYS);
        return connector;
    }

    private String resolveFlowIndexStyleClass(Post.ModerationFlowVisualState state) {
        switch (state) {
            case COMPLETED:
                return "moderation-flow-index-completed";
            case ACTIVE:
                return "moderation-flow-index-active";
            case WARNING:
                return "moderation-flow-index-warning";
            case DANGER:
                return "moderation-flow-index-danger";
            case INACTIVE:
            default:
                return "moderation-flow-index-inactive";
        }
    }

    private String resolveFlowStateStyleClass(Post.ModerationFlowVisualState state) {
        switch (state) {
            case COMPLETED:
                return "moderation-flow-state-completed";
            case ACTIVE:
                return "moderation-flow-state-active";
            case WARNING:
                return "moderation-flow-state-warning";
            case DANGER:
                return "moderation-flow-state-danger";
            case INACTIVE:
            default:
                return "moderation-flow-state-inactive";
        }
    }
}
