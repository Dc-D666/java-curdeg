package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.Comment;
import com.teach.javafx.models.PageResult;
import com.teach.javafx.models.Post;
import com.teach.javafx.models.Report;
import com.teach.javafx.request.HttpRequestUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;

public class AdminReportController extends ToolController {
    private static final Gson GSON = new Gson();

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
    @FXML
    private VBox reportFlowCard;
    @FXML
    private HBox reportFlowStepsBox;
    @FXML
    private Label reportFlowSummaryLabel;

    private int currentPageNum = 1;
    private int currentPageSize = 10;
    private Integer currentStatus = null;

    @FXML
    public void initialize() {
        if (reportFlowCard != null) {
            reportFlowCard.setVisible(false);
            reportFlowCard.setManaged(false);
        }
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
                    case 3:
                        typeText = "个人主页资料卡";
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
            private final Button clearProfileButton = new Button("清空违规资料");
            private final HBox hbox = new HBox(5, deleteButton, clearProfileButton, rejectButton);
            
            {
                deleteButton.setStyle("-fx-font-size: 11px;");
                rejectButton.setStyle("-fx-font-size: 11px;");
                clearProfileButton.setStyle("-fx-font-size: 11px;");
                
                deleteButton.setOnAction(event -> {
                    Report report = getTableView().getItems().get(getIndex());
                    openHandleDialog(report, 1);
                });

                clearProfileButton.setOnAction(event -> {
                    Report report = getTableView().getItems().get(getIndex());
                    openHandleDialog(report, 3);
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
                    boolean isProfileCard = report.getTargetType() != null && report.getTargetType() == 3;
                    deleteButton.setVisible(isPending && !isProfileCard);
                    deleteButton.setManaged(isPending && !isProfileCard);
                    clearProfileButton.setVisible(isPending && isProfileCard);
                    clearProfileButton.setManaged(isPending && isProfileCard);
                    rejectButton.setVisible(isPending);
                    rejectButton.setManaged(isPending);
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
                    renderReportFlow(report);
                    if (report.getTargetType() != null) {
                        if (report.getTargetType() == 1) {
                            openPostDetailDialog(report);
                        } else if (report.getTargetType() == 2) {
                            openCommentDetailDialog(report);
                        } else if (report.getTargetType() == 3) {
                            openProfileCardDetailDialog(report);
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
                    if (reportFlowCard != null) {
                        reportFlowCard.setVisible(false);
                        reportFlowCard.setManaged(false);
                    }

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
        String defaultText;
        if (handleType == 1) {
            defaultText = "内容确实违规，已删除";
        } else if (handleType == 3) {
            defaultText = "资料卡信息存在违规展示，已清空违规资料";
        } else {
            defaultText = "内容不存在违规，感谢您的监督";
        }
        TextInputDialog dialog = new TextInputDialog(defaultText);
        String title = getHandleTypeText(handleType);
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

    private void openProfileCardDetailDialog(Report report) {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getUserProfile(report.getTargetId().intValue());
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            Map<String, Object> currentProfile = task.getValue();
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("资料卡举报详情");
            dialog.setHeaderText("被举报的个人主页资料卡（用户ID：" + report.getTargetId() + "）");
            dialog.getDialogPane().getButtonTypes().add(new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE));

            GridPane metaGrid = new GridPane();
            metaGrid.setHgap(12);
            metaGrid.setVgap(8);
            addMetaRow(metaGrid, 0, "举报类型", getTargetTypeText(report.getTargetType()));
            addMetaRow(metaGrid, 1, "举报人", safeText(report.getReporterNickname()));
            addMetaRow(metaGrid, 2, "举报时间", safeText(report.getCreateTime()));
            addMetaRow(metaGrid, 3, "处理状态", getStatusText(report.getStatus()));
            addMetaRow(metaGrid, 4, "处理方式", getHandleTypeText(report.getHandleType()));
            addMetaRow(metaGrid, 5, "处理备注", safeText(report.getHandleRemark()));

            TextArea reportReasonArea = createReadOnlyTextArea("举报原因：\n" + safeText(report.getReason()), 4);
            TextArea snapshotArea = createReadOnlyTextArea("举报快照：\n" + formatProfileCardData(parseSnapshot(report.getTargetSnapshot())), 7);
            TextArea currentArea = createReadOnlyTextArea("当前资料卡：\n" + formatProfileCardData(currentProfile), 7);

            VBox content = new VBox(12, metaGrid, reportReasonArea, snapshotArea, currentArea);
            content.setStyle("-fx-padding: 20;");
            dialog.getDialogPane().setContent(content);
            dialog.showAndWait();
        }));

        task.setOnFailed(event -> Platform.runLater(() -> showError("获取资料卡详情失败")));

        new Thread(task).start();
    }

    private void addMetaRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label + "：");
        labelNode.setStyle("-fx-font-weight: bold;");
        Label valueNode = new Label(value);
        valueNode.setWrapText(true);
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private TextArea createReadOnlyTextArea(String text, int rowCount) {
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(rowCount);
        return area;
    }

    private Map<String, Object> parseSnapshot(String targetSnapshot) {
        if (targetSnapshot == null || targetSnapshot.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> snapshot = GSON.fromJson(targetSnapshot, mapType);
            return snapshot != null ? snapshot : new LinkedHashMap<>();
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private String formatProfileCardData(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return "无";
        }
        String nickname = data.get("nickname") != null ? String.valueOf(data.get("nickname")) : "无";
        String avatarUrl = data.get("avatarUrl") != null ? String.valueOf(data.get("avatarUrl")) : "无";
        String signature = data.get("signature") != null && !String.valueOf(data.get("signature")).isBlank()
            ? String.valueOf(data.get("signature")) : "无";
        String capturedAt = data.get("capturedAt") != null ? String.valueOf(data.get("capturedAt")) : null;

        StringBuilder builder = new StringBuilder();
        builder.append("昵称：").append(nickname)
            .append("\n头像：").append(avatarUrl)
            .append("\n个性签名：").append(signature);
        if (capturedAt != null && !capturedAt.isBlank()) {
            builder.append("\n快照时间：").append(capturedAt);
        }
        return builder.toString();
    }

    private String safeText(String value) {
        return value != null && !value.isBlank() ? value : "无";
    }

    private String getTargetTypeText(Integer type) {
        if (type == null) {
            return "未知";
        }
        switch (type) {
            case 1:
                return "帖子";
            case 2:
                return "评论";
            case 3:
                return "个人主页资料卡";
            default:
                return "未知";
        }
    }

    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "待处理";
            case 1:
                return "已处理";
            default:
                return "未知";
        }
    }

    private String getHandleTypeText(Integer handleType) {
        if (handleType == null) {
            return "未处理";
        }
        switch (handleType) {
            case 1:
                return "删除内容";
            case 2:
                return "驳回举报";
            case 3:
                return "清空违规资料";
            default:
                return "未知";
        }
    }

    private void renderReportFlow(Report report) {
        if (reportFlowCard == null || reportFlowStepsBox == null || reportFlowSummaryLabel == null || report == null) {
            return;
        }
        reportFlowCard.setVisible(true);
        reportFlowCard.setManaged(true);
        reportFlowStepsBox.getChildren().clear();

        Report.ReportFlowView flowView = report.getReportFlowView();
        reportFlowSummaryLabel.setText(flowView.getSummary());
        List<Report.ReportFlowStep> steps = flowView.getSteps();

        for (int i = 0; i < steps.size(); i++) {
            Report.ReportFlowStep step = steps.get(i);
            reportFlowStepsBox.getChildren().add(createFlowStepNode(step, i + 1));
            if (i < steps.size() - 1) {
                reportFlowStepsBox.getChildren().add(createFlowConnector(flowView.isConnectorReached(i)));
            }
        }
    }

    private VBox createFlowStepNode(Report.ReportFlowStep step, int stepNumber) {
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

    private String resolveFlowIndexStyleClass(Report.ReportFlowVisualState state) {
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

    private String resolveFlowStateStyleClass(Report.ReportFlowVisualState state) {
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
