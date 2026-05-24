package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LevelPrivilegesController extends ToolController {

    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private ImageView currentLevelIcon;
    @FXML
    private Label currentLevelName;
    @FXML
    private Label currentPointsLabel;
    @FXML
    private Region progressBarBg;
    @FXML
    private Label nextLevelInfo;
    @FXML
    private VBox currentPrivilegesBox;
    @FXML
    private VBox levelListBox;

    @FXML
    public void initialize() {
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        loadData();
    }

    private void loadData() {
        loadingIndicator.setVisible(true);

        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getMyPrivileges();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                Map<String, Object> data = task.getValue();
                if (data != null) {
                    displayCurrentLevel(data);
                }
                loadLevelConfig();
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                loadLevelConfig();
            });
        });

        new Thread(task).start();
    }

    private void displayCurrentLevel(Map<String, Object> data) {
        Object levelObj = data.get("level");
        int level = levelObj instanceof Number ? ((Number) levelObj).intValue() : 0;

        Object pointsObj = data.get("points");
        int points = pointsObj instanceof Number ? ((Number) pointsObj).intValue() : 0;

        Object levelNameObj = data.get("levelName");
        String levelName = levelNameObj instanceof String ? (String) levelNameObj : "Lv." + level;

        Object iconPathObj = data.get("iconPath");
        String iconPath = iconPathObj instanceof String ? (String) iconPathObj : null;

        Object nextLevelPointsObj = data.get("nextLevelPoints");
        int nextLevelPoints = nextLevelPointsObj instanceof Number ? ((Number) nextLevelPointsObj).intValue() : 0;

        Object pointsToNextObj = data.get("pointsToNext");
        int pointsToNext = pointsToNextObj instanceof Number ? ((Number) pointsToNextObj).intValue() : 0;

        currentLevelName.setText(levelName + " (LV." + level + ")");
        currentPointsLabel.setText("当前拥有 " + points + " 山竹瓣");

        if (iconPath != null && !iconPath.isEmpty()) {
            try {
                String fullUrl = iconPath.startsWith("/") ? HttpRequestUtil.serverUrl + iconPath : iconPath;
                currentLevelIcon.setImage(new Image(fullUrl, true));
            } catch (Exception e) {
            }
        }

        if (nextLevelPoints > 0) {
            nextLevelInfo.setText("距离下一等级还需 " + pointsToNext + " 山竹瓣");
            double progress = (double) points / nextLevelPoints;
            progress = Math.min(progress, 1.0);
            progressBarBg.setStyle(String.format(
                "-fx-background-color: linear-gradient(to right, #52c41a %.1f%%, #f0f0f0 %.1f%%); -fx-background-radius: 4;",
                progress * 100, progress * 100
            ));
        } else {
            nextLevelInfo.setText("已达到最高等级！");
            progressBarBg.setStyle("-fx-background-color: #52c41a; -fx-background-radius: 4;");
        }

        // 显示当前特权
        Object privilegesObj = data.get("privileges");
        if (privilegesObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> privileges = (Map<String, Object>) privilegesObj;
            displayPrivileges(privileges);
        }
    }

    private void displayPrivileges(Map<String, Object> privileges) {
        currentPrivilegesBox.getChildren().clear();

        Label titleLabel = new Label("当前特权：");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        currentPrivilegesBox.getChildren().add(titleLabel);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(0);
        grid.setStyle("-fx-padding: 8 0 0 0;");

        String[][] privilegeDefs = {
            {"发帖权限", "canPost", "canPostDaily"},
            {"AI搜索", "aiSearchLimit", null},
            {"AI总结", "aiSummary", null},
            {"AI配图", "aiImageLimit", null},
            {"文件附件", "attachmentSize", null},
            {"动态头像", "animatedAvatar", null},
            {"删除评论", "canDeleteComment", null},
            {"查看点赞", "canViewLikers", null},
            {"私信限制", "privateMessageLimit", null},
            {"昵称样式", "nicknameStyle", null},
            {"内容推荐", "priorityRecommend", null},
            {"发帖审核", "postAudit", null},
            {"申请管理", "canApplyAdmin", null},
            {"商店折扣", "storeDiscount", null}
        };

        for (int i = 0; i < privilegeDefs.length; i++) {
            String label = privilegeDefs[i][0];
            String key = privilegeDefs[i][1];
            String subKey = privilegeDefs[i][2];
            String value = getPrivilegeText(privileges, key, subKey);

            VBox cell = new VBox(2);
            cell.setAlignment(javafx.geometry.Pos.CENTER);
            cell.setStyle("-fx-padding: 6 4; -fx-background-color: #f6ffed; -fx-background-radius: 6; -fx-border-color: #b7eb8f; -fx-border-radius: 6;");

            Label nameLabel = new Label(label);
            nameLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

            Label valueLabel = new Label(value);
            valueLabel.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 12px;");

            cell.getChildren().addAll(nameLabel, valueLabel);
            grid.add(cell, i, 0);
        }

        currentPrivilegesBox.getChildren().add(grid);
    }

    private String getPrivilegeText(Map<String, Object> privileges, String key, String subKey) {
        Object value = privileges.get(key);
        if (value == null) return "无";

        if (value instanceof Boolean) {
            boolean boolValue = (Boolean) value;
            if ("canPost".equals(key)) {
                if (!boolValue) return "禁止发帖";
                if (subKey != null) {
                    Object dailyObj = privileges.get(subKey);
                    if (dailyObj instanceof Number) {
                        int daily = ((Number) dailyObj).intValue();
                        return daily > 0 ? "每天可发 " + daily + " 帖" : "无限制";
                    }
                }
                return "允许";
            }
            return boolValue ? "已开启" : "未开启";
        } else if (value instanceof Number) {
            int numValue = ((Number) value).intValue();
            if ("aiSearchLimit".equals(key) || "aiImageLimit".equals(key)) {
                return numValue > 0 ? "每天 " + numValue + " 次" : "无限制";
            } else if ("attachmentSize".equals(key)) {
                if (numValue > 0) {
                    long mb = numValue / 1048576;
                    return "最大 " + mb + " MB";
                }
                return "不允许";
            } else if ("privateMessageLimit".equals(key)) {
                return numValue > 0 ? "可发 " + numValue + " 条" : "需互关";
            } else if ("storeDiscount".equals(key)) {
                return numValue + " 折";
            }
            return String.valueOf(numValue);
        } else if (value instanceof String) {
            String strValue = (String) value;
            if ("nicknameStyle".equals(key)) {
                if ("bold_red".equals(strValue)) return "加粗红色";
                if ("bold".equals(strValue)) return "加粗";
                return "普通";
            } else if ("postAudit".equals(key)) {
                if ("none".equals(strValue)) return "免审核";
                if ("ai_post".equals(strValue)) return "AI先审后发";
                return "先发后审";
            }
            return strValue;
        }
        return String.valueOf(value);
    }

    private void loadLevelConfig() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() {
                return HttpRequestUtil.getLevelConfig();
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Object> data = task.getValue();
                if (data != null) {
                    displayLevelList(data);
                }
            });
        });

        new Thread(task).start();
    }

    @SuppressWarnings("unchecked")
    private void displayLevelList(Map<String, Object> data) {
        levelListBox.getChildren().clear();

        List<Map<String, Object>> levels = new ArrayList<>();
        Object levelsObj = data.get("levels");
        if (levelsObj instanceof List) {
            levels = (List<Map<String, Object>>) levelsObj;
        }

        // 按 LV0-3, LV4-6, LV7-9, LV10-12 分4行展示
        int[][] groups = {{0, 1, 2, 3}, {4, 5, 6}, {7, 8, 9}, {10, 11, 12}};
        String[] groupTitles = {"LV0-3 初入山竹园", "LV4-6 成长之路", "LV7-9 渐入佳境", "LV10-12 巅峰荣耀"};

        for (int g = 0; g < groups.length; g++) {
            VBox groupBox = new VBox(8);
            groupBox.setStyle("-fx-padding: 12; -fx-background-color: #fafafa; -fx-background-radius: 8; -fx-border-color: #e8e8e8; -fx-border-radius: 8;");

            Label groupTitle = new Label(groupTitles[g]);
            groupTitle.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #333;");
            groupBox.getChildren().add(groupTitle);

            HBox row = new HBox(12);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            for (int levelId : groups[g]) {
                Map<String, Object> levelData = findLevelData(levels, levelId);
                if (levelData == null) continue;

                HBox cell = createLevelCell(levelData);
                row.getChildren().add(cell);
                HBox.setHgrow(cell, Priority.ALWAYS);
            }

            groupBox.getChildren().add(row);
            levelListBox.getChildren().add(groupBox);
        }
    }

    private Map<String, Object> findLevelData(List<Map<String, Object>> levels, int levelId) {
        for (Map<String, Object> level : levels) {
            Object idObj = level.get("id");
            int id = idObj instanceof Number ? ((Number) idObj).intValue() : -1;
            if (id == levelId) {
                return level;
            }
        }
        return null;
    }

    private HBox createLevelCell(Map<String, Object> levelData) {
        HBox cell = new HBox(12);
        cell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        cell.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-background-radius: 6; -fx-border-color: #d9d9d9; -fx-border-radius: 6;");
        HBox.setHgrow(cell, Priority.ALWAYS);

        Object iconPathObj = levelData.get("iconPath");
        String iconPath = iconPathObj instanceof String ? (String) iconPathObj : null;
        ImageView iconView = new ImageView();
        iconView.setFitHeight(48);
        iconView.setFitWidth(48);
        iconView.setPreserveRatio(true);
        if (iconPath != null && !iconPath.isEmpty()) {
            try {
                String fullUrl = iconPath.startsWith("/") ? HttpRequestUtil.serverUrl + iconPath : iconPath;
                iconView.setImage(new Image(fullUrl, true));
            } catch (Exception e) {
            }
        }

        Object idObj = levelData.get("id");
        int levelId = idObj instanceof Number ? ((Number) idObj).intValue() : 0;

        Object nameObj = levelData.get("levelName");
        String name = nameObj instanceof String ? (String) nameObj : "Lv." + levelId;

        Object minPointsObj = levelData.get("minPoints");
        int minPoints = minPointsObj instanceof Number ? ((Number) minPointsObj).intValue() : 0;

        VBox textBox = new VBox(4);
        textBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label nameLabel = new Label("LV." + levelId + " " + name);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label pointsLabel = new Label(minPoints + " 山竹瓣");
        pointsLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        Object descObj = levelData.get("description");
        String description = descObj instanceof String ? (String) descObj : "";
        if (!description.isEmpty()) {
            Label descLabel = new Label(description);
            descLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px; -fx-wrap-text: true;");
            descLabel.setWrapText(true);
            textBox.getChildren().addAll(nameLabel, pointsLabel, descLabel);
        } else {
            textBox.getChildren().addAll(nameLabel, pointsLabel);
        }

        cell.getChildren().addAll(iconView, textBox);
        return cell;
    }
}
