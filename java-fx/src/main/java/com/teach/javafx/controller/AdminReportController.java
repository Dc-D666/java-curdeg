package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.Comment;
import com.teach.javafx.models.PageResult;
import com.teach.javafx.models.Post;
import com.teach.javafx.models.Report;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;

public class AdminReportController extends ToolController {
    @FXML
    private TableView<Report> reportTableView;
    @FXML
    private TableColumn<Report, String> targetTypeColumn;
    @FXML
    private TableColumn<Report, String> reasonColumn;
    @FXML
    private TableColumn<Report, String> statusColumn;
    @FXML
    private TableColumn<Report, String> reporterColumn;
    @FXML
    private TableColumn<Report, String> createTimeColumn;
    @FXML
    private TableColumn<Report, String> handlerColumn;
    @FXML
    private TableColumn<Report, String> handleTimeColumn;
    @FXML
    private TableColumn<Report, String> handleRemarkColumn;
    @FXML
    private TableColumn<Report, Void> actionColumn;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private Label pageInfoLabel;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button refreshButton;

    private int currentPageNum = 1;
    private int currentPageSize = 10;
    private Integer currentStatus = null;

    @FXML
    public void initialize() {
        targetTypeColumn.setCellValueFactory(cellData -> {
            Integer type = cellData.getValue().getTargetType();
            String typeText = "";
            if (type != null) {
                switch (type) {
                    case 1:
                        typeText = "帖子";
                        break;
                    case 2:
                        typeText = "评论";
                        break;
                    default:
                        typeText = "未知";
                }
            }
            return new javafx.beans.property.SimpleStringProperty(typeText);
        });
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        statusColumn.setCellValueFactory(cellData -> {
            Integer status = cellData.getValue().getStatus();
            String statusText = "";
            if (status != null) {
                switch (status) {
                    case 0:
                        statusText = "待处理";
                        break;
                    case 1:
                        statusText = "已处理";
                        break;
                    default:
                        statusText = "未知";
                }
            }
            return new javafx.beans.property.SimpleStringProperty(statusText);
        });
        reporterColumn.setCellValueFactory(new PropertyValueFactory<>("reporterNickname"));
        createTimeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreateTime() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreateTime());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        handlerColumn.setCellValueFactory(new PropertyValueFactory<>("handlerNickname"));
        handleTimeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getHandleTime() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getHandleTime());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        handleRemarkColumn.setCellValueFactory(new PropertyValueFactory<>("handleRemark"));
        
        actionColumn.setCellFactory(param -> new TableCell<Report, Void>() {
            private final Button deleteButton = new Button("删除内容");
            private final Button rejectButton = new Button("驳回举报");
            private final HBox hbox = new HBox(5, deleteButton, rejectButton);
            
            {
                deleteButton.setStyle("-fx-font-size: 11px;");
                rejectButton.setStyle("-fx-font-size: 11px;");
                
                deleteButton.setOnAction(event -> {
                    Report report = getTableView().getItems().get(getIndex());
                    openHandleDialog(report, 1);
                });
                
                rejectButton.setOnAction(event -> {
                    Report report = getTableView().getItems().get(getIndex());
                    openHandleDialog(report, 2);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    Report report = getTableView().getItems().get(getIndex());
                    boolean isPending = report.getStatus() != null && report.getStatus() == 0;
                    deleteButton.setVisible(isPending);
                    rejectButton.setVisible(isPending);
                    setGraphic(isPending ? hbox : null);
                }
            }
        });
        
        statusComboBox.getItems().addAll("全部", "待处理", "已处理");
        statusComboBox.setValue("全部");
        statusComboBox.setOnAction(event -> {
            String selected = statusComboBox.getValue();
            if ("全部".equals(selected)) {
                currentStatus = null;
            } else if ("待处理".equals(selected)) {
                currentStatus = 0;
            } else if ("已处理".equals(selected)) {
                currentStatus = 1;
            }
            currentPageNum = 1;
            loadReportList();
        });

        reportTableView.setRowFactory(tv -> {
            TableRow<Report> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    Report report = row.getItem();
                    if (report.getTargetType() != null) {
                        if (report.getTargetType() == 1) {
                            openPostDetailDialog(report);
                        } else if (report.getTargetType() == 2) {
                            openCommentDetailDialog(report);
                        }
                    }
                }
            });
            return row;
        });

        prevButton.setOnAction(event -> {
            if (currentPageNum > 1) {
                currentPageNum--;
                loadReportList();
            }
        });

        nextButton.setOnAction(event -> {
            currentPageNum++;
            loadReportList();
        });

        refreshButton.setOnAction(event -> {
            currentPageNum = 1;
            loadReportList(refreshButton);
        });

        loadReportList();
    }

    public void loadReportList() {
        loadReportList(null);
    }

    private void loadReportList(Button refreshBtn) {
        if (refreshBtn != null) {
            refreshBtn.setDisable(true);
            refreshBtn.setText("刷新中");
        }
        
        Task<PageResult<Report>> task = new Task<PageResult<Report>>() {
            @Override
            protected PageResult<Report> call() {
                return HttpRequestUtil.getAdminReportList(currentPageNum, currentPageSize, currentStatus);
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                if (refreshBtn != null) {
                    refreshBtn.setDisable(false);
                    refreshBtn.setText("刷新");
                }
                
                PageResult<Report> pageResult = task.getValue();
                if (pageResult != null && pageResult.getList() != null) {
                    reportTableView.getItems().clear();
                    reportTableView.getItems().addAll(pageResult.getList());

                    long total = pageResult.getTotal() != null ? pageResult.getTotal() : 0;
                    pageInfoLabel.setText("共 " + total + " 条，第 " + currentPageNum + " 页");

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
                showError("加载举报列表失败");
            });
        });

        new Thread(task).start();
    }

    private void openHandleDialog(Report report, int handleType) {
        String defaultText = handleType == 1 ? "内容确实违规，已删除" : "内容不存在违规，感谢您的监督";
        TextInputDialog dialog = new TextInputDialog(defaultText);
        String title = handleType == 1 ? "删除内容" : "驳回举报";
        dialog.setTitle(title);
        dialog.setHeaderText("处理举报（ID：" + report.getId() + "）");
        dialog.setContentText("请输入处理备注：");
        
        dialog.showAndWait().ifPresent(remark -> {
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    return HttpRequestUtil.handleReport(report.getId(), handleType, remark.trim());
                }
            };
            
            task.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    Boolean result = task.getValue();
                    if (Boolean.TRUE.equals(result)) {
                        showInfo("处理成功！");
                        loadReportList();
                    } else {
                        showError("处理失败，请稍后重试");
                    }
                });
            });
            
            task.setOnFailed(event -> {
                Platform.runLater(() -> showError("处理失败，请稍后重试"));
            });
            
            new Thread(task).start();
        });
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

    private void openPostDetailDialog(Report report) {
        Task<Post> task = new Task<Post>() {
            @Override
            protected Post call() {
                return HttpRequestUtil.getPostDetail(report.getTargetId());
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Post post = task.getValue();
                if (post != null) {
                    Dialog<Void> dialog = new Dialog<>();
                    dialog.setTitle("帖子详情");
                    dialog.setHeaderText("被举报的帖子（ID：" + post.getId() + "）");

                    ButtonType closeButton = new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE);
                    dialog.getDialogPane().getButtonTypes().add(closeButton);

                    VBox content = new VBox(10);
                    content.setStyle("-fx-padding: 20;");

                    Label titleLabel = new Label("标题：" + (post.getTitle() != null ? post.getTitle() : ""));
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                    Label authorLabel = new Label("作者：" + (post.getAuthorNickname() != null ? post.getAuthorNickname() : ""));

                    Label boardLabel = new Label("板块：" + (post.getBoardName() != null ? post.getBoardName() : ""));

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String timeText = post.getCreateTime() != null ? dateFormat.format(post.getCreateTime()) : "";
                    Label timeLabel = new Label("发布时间：" + timeText);

                    Label likeCountLabel = new Label("点赞数：" + (post.getLikeCount() != null ? post.getLikeCount() : 0));
                    Label commentCountLabel = new Label("评论数：" + (post.getCommentCount() != null ? post.getCommentCount() : 0));
                    Label viewCountLabel = new Label("浏览数：" + (post.getViewCount() != null ? post.getViewCount() : 0));

                    Label contentLabel = new Label("内容：");
                    contentLabel.setStyle("-fx-font-weight: bold;");

                    TextArea contentArea = new TextArea(post.getContent() != null ? post.getContent() : "");
                    contentArea.setEditable(false);
                    contentArea.setWrapText(true);
                    contentArea.setPrefRowCount(10);

                    content.getChildren().addAll(titleLabel, authorLabel, boardLabel, timeLabel,
                            likeCountLabel, commentCountLabel, viewCountLabel, contentLabel, contentArea);

                    dialog.getDialogPane().setContent(content);
                    dialog.showAndWait();
                } else {
                    showError("获取帖子详情失败");
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("获取帖子详情失败"));
        });

        new Thread(task).start();
    }

    private void openCommentDetailDialog(Report report) {
        Task<Comment> task = new Task<Comment>() {
            @Override
            protected Comment call() {
                return HttpRequestUtil.getCommentDetail(report.getTargetId());
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Comment comment = task.getValue();
                if (comment != null) {
                    Dialog<Void> dialog = new Dialog<>();
                    dialog.setTitle("评论详情");
                    dialog.setHeaderText("被举报的评论（ID：" + comment.getId() + "）");

                    ButtonType closeButton = new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE);
                    dialog.getDialogPane().getButtonTypes().add(closeButton);

                    VBox content = new VBox(10);
                    content.setStyle("-fx-padding: 20;");

                    Label authorLabel = new Label("作者：" + (comment.getAuthorNickname() != null ? comment.getAuthorNickname() : ""));

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String timeText = comment.getCreateTime() != null ? dateFormat.format(comment.getCreateTime()) : "";
                    Label timeLabel = new Label("发布时间：" + timeText);

                    Label contentLabel = new Label("内容：");
                    contentLabel.setStyle("-fx-font-weight: bold;");

                    TextArea contentArea = new TextArea(comment.getContent() != null ? comment.getContent() : "");
                    contentArea.setEditable(false);
                    contentArea.setWrapText(true);
                    contentArea.setPrefRowCount(10);

                    content.getChildren().addAll(authorLabel, timeLabel, contentLabel, contentArea);

                    dialog.getDialogPane().setContent(content);
                    dialog.showAndWait();
                } else {
                    showError("获取评论详情失败");
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> showError("获取评论详情失败"));
        });

        new Thread(task).start();
    }
}
