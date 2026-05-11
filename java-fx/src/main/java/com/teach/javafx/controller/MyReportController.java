package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.models.Comment;
import com.teach.javafx.models.PageResult;
import com.teach.javafx.models.Report;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class MyReportController extends ToolController {
    @FXML
    private TableView<Report> reportTableView;
    @FXML
    private TableColumn<Report, String> targetTypeColumn;
    @FXML
    private TableColumn<Report, String> reasonColumn;
    @FXML
    private TableColumn<Report, String> statusColumn;
    @FXML
    private TableColumn<Report, String> createTimeColumn;
    @FXML
    private TableColumn<Report, String> handleTimeColumn;
    @FXML
    private TableColumn<Report, String> handleRemarkColumn;
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
        createTimeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreateTime() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreateTime());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        handleTimeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getHandleTime() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getHandleTime());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        handleRemarkColumn.setCellValueFactory(new PropertyValueFactory<>("handleRemark"));

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

        setupRowFactory();
        loadReportList();
    }

    private void setupRowFactory() {
        reportTableView.setRowFactory(tv -> {
            TableRow<Report> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    openReportDetail(row.getItem());
                }
            });
            return row;
        });
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
                return HttpRequestUtil.getMyReportList(currentPageNum, currentPageSize);
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openReportDetail(Report report) {
        if (report == null) {
            return;
        }

        Long postId = resolvePostId(report);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("举报详情");
        dialog.setHeaderText(getTargetTypeText(report.getTargetType()) + "举报 #" + report.getId());

        ButtonType openPostButton = new ButtonType("查看原帖", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeButton = new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE);
        if (postId != null) {
            dialog.getDialogPane().getButtonTypes().addAll(openPostButton, closeButton);
        } else {
            dialog.getDialogPane().getButtonTypes().add(closeButton);
        }

        GridPane metaGrid = new GridPane();
        metaGrid.setHgap(12);
        metaGrid.setVgap(8);
        addMetaRow(metaGrid, 0, "举报类型", getTargetTypeText(report.getTargetType()));
        addMetaRow(metaGrid, 1, "目标ID", report.getTargetId() != null ? String.valueOf(report.getTargetId()) : "");
        addMetaRow(metaGrid, 2, "状态", getStatusText(report.getStatus()));
        addMetaRow(metaGrid, 3, "举报时间", report.getCreateTime());
        addMetaRow(metaGrid, 4, "处理时间", report.getHandleTime());
        addMetaRow(metaGrid, 5, "处理方式", getHandleTypeText(report.getHandleType()));
        if (postId != null) {
            addMetaRow(metaGrid, 6, "关联帖子", String.valueOf(postId));
        }

        TextArea reasonArea = new TextArea("举报原因：\n" + safeText(report.getReason()) +
                "\n\n处理备注：\n" + safeText(report.getHandleRemark()));
        reasonArea.setEditable(false);
        reasonArea.setWrapText(true);
        reasonArea.setPrefRowCount(8);
        reasonArea.setPrefColumnCount(52);

        VBox content = new VBox(12, metaGrid, reasonArea);
        content.setPadding(new javafx.geometry.Insets(10));
        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == openPostButton) {
            openPostDetail(postId);
        }
    }

    private Long resolvePostId(Report report) {
        if (report == null || report.getTargetId() == null || report.getTargetType() == null) {
            return null;
        }
        if (report.getTargetType() == 1) {
            return report.getTargetId();
        }
        if (report.getTargetType() == 2) {
            Comment comment = HttpRequestUtil.getCommentDetail(report.getTargetId());
            return comment != null ? comment.getPostId() : null;
        }
        return null;
    }

    private void openPostDetail(Long postId) {
        if (postId == null) {
            showError("没有找到关联帖子");
            return;
        }
        if (AppStore.getMainFrameController() != null) {
            AppStore.getMainFrameController().openPostDetail(postId);
        }
    }

    private void addMetaRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label + "：");
        labelNode.setStyle("-fx-font-weight: bold;");
        Label valueNode = new Label(value != null ? value : "");
        valueNode.setWrapText(true);
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
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
            default:
                return "未知";
        }
    }

    private String safeText(String value) {
        return value != null && !value.isBlank() ? value : "无";
    }
}
