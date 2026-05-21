package com.teach.javafx.controller;

import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PointRankController extends ToolController {

    @FXML
    private Button totalRankBtn;
    @FXML
    private Button weeklyRankBtn;
    @FXML
    private ListView<RankItem> rankList;

    private boolean isWeekly = false;
    private Integer currentUserId;

    @FXML
    public void initialize() {
        totalRankBtn.setOnAction(event -> {
            isWeekly = false;
            updateButtonStyles();
            loadRankData();
        });
        weeklyRankBtn.setOnAction(event -> {
            isWeekly = true;
            updateButtonStyles();
            loadRankData();
        });

        rankList.setCellFactory(lv -> new RankCell());

        loadCurrentUserId();
        updateButtonStyles();
        loadRankData();
    }

    private void updateButtonStyles() {
        if (isWeekly) {
            weeklyRankBtn.setStyle("-fx-background-color: #1890ff; -fx-text-fill: white;");
            totalRankBtn.setStyle("");
        } else {
            totalRankBtn.setStyle("-fx-background-color: #1890ff; -fx-text-fill: white;");
            weeklyRankBtn.setStyle("");
        }
    }

    private void loadCurrentUserId() {
        Map<String, Object> userData = HttpRequestUtil.getCurrentUserData();
        if (userData != null && userData.containsKey("personId")) {
            Object idObj = userData.get("personId");
            if (idObj instanceof Number) {
                currentUserId = ((Number) idObj).intValue();
            }
        }
    }

    private void loadRankData() {
        if (isWeekly) {
            Task<List<Map<String, Object>>> task = new Task<>() {
                @Override
                protected List<Map<String, Object>> call() {
                    return HttpRequestUtil.getWeeklyPointRank();
                }
            };

            task.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    List<Map<String, Object>> result = task.getValue();
                    if (result != null) {
                        displayRankFromList(result);
                    }
                });
            });

            new Thread(task).start();
        } else {
            Task<Map<String, Object>> task = new Task<>() {
                @Override
                protected Map<String, Object> call() {
                    return HttpRequestUtil.getPointRank(1, 100);
                }
            };

            task.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    Map<String, Object> result = task.getValue();
                    if (result != null) {
                        displayRank(result);
                    }
                });
            });

            new Thread(task).start();
        }
    }

    @SuppressWarnings("unchecked")
    private void displayRank(Map<String, Object> result) {
        List<Map<String, Object>> content = new ArrayList<>();
        Object contentObj = result.get("content");
        if (contentObj instanceof List) {
            content = (List<Map<String, Object>>) contentObj;
        } else if (result.get("list") instanceof List) {
            content = (List<Map<String, Object>>) result.get("list");
        }

        displayRankFromList(content);
    }

    private void displayRankFromList(List<Map<String, Object>> content) {
        List<RankItem> items = new ArrayList<>();
        int rank = 1;
        for (Map<String, Object> record : content) {
            RankItem item = new RankItem();
            item.setRank(rank++);
            Object userIdObj = record.get("userId");
            item.setUserId(userIdObj instanceof Number ? ((Number) userIdObj).intValue() : null);
            item.setNickname(getString(record, "nickname"));
            Object pointsObj = record.get("points");
            item.setPoints(pointsObj instanceof Number ? ((Number) pointsObj).intValue() : 0);
            Object levelObj = record.get("level");
            item.setLevel(levelObj instanceof Number ? ((Number) levelObj).intValue() : 0);
            item.setLevelName(getString(record, "levelName"));
            item.setIconPath(getString(record, "iconPath"));
            item.setCurrentUser(currentUserId != null && currentUserId.equals(item.getUserId()));
            items.add(item);
        }

        rankList.getItems().setAll(items);
    }

    private String getString(Map<String, Object> map, String key) {
        Object obj = map.get(key);
        return obj != null ? obj.toString() : "";
    }

    public static class RankItem {
        private int rank;
        private Integer userId;
        private String nickname;
        private int points;
        private int level;
        private String levelName;
        private String iconPath;
        private boolean currentUser;

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public String getLevelName() {
            return levelName;
        }

        public void setLevelName(String levelName) {
            this.levelName = levelName;
        }

        public String getIconPath() {
            return iconPath;
        }

        public void setIconPath(String iconPath) {
            this.iconPath = iconPath;
        }

        public boolean isCurrentUser() {
            return currentUser;
        }

        public void setCurrentUser(boolean currentUser) {
            this.currentUser = currentUser;
        }
    }

    private class RankCell extends ListCell<RankItem> {
        @Override
        protected void updateItem(RankItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            HBox row = new HBox(12);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 10 16;" + (item.isCurrentUser() ? " -fx-background-color: #e6f7ff;" : ""));

            Label rankLabel = new Label(String.valueOf(item.getRank()));
            rankLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-min-width: 30;");
            if (item.getRank() == 1) {
                rankLabel.setStyle(rankLabel.getStyle() + " -fx-text-fill: #f5222d;");
            } else if (item.getRank() == 2) {
                rankLabel.setStyle(rankLabel.getStyle() + " -fx-text-fill: #faad14;");
            } else if (item.getRank() == 3) {
                rankLabel.setStyle(rankLabel.getStyle() + " -fx-text-fill: #1890ff;");
            }

            ImageView iconView = new ImageView();
            iconView.setFitHeight(36);
            iconView.setFitWidth(36);
            iconView.setPreserveRatio(true);
            if (item.getIconPath() != null && !item.getIconPath().isEmpty()) {
                try {
                    String fullUrl = item.getIconPath().startsWith("/") ? HttpRequestUtil.serverUrl + item.getIconPath() : item.getIconPath();
                    iconView.setImage(new Image(fullUrl, true));
                } catch (Exception e) {
                }
            }

            Label nicknameLabel = new Label(item.getNickname() != null ? item.getNickname() : "未知用户");
            nicknameLabel.setStyle("-fx-font-size: 14;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            String levelText = item.getLevelName() != null && !item.getLevelName().isEmpty() ? item.getLevelName() : "Lv." + item.getLevel();
            Label levelLabel = new Label(levelText);
            levelLabel.setStyle("-fx-text-fill: #666;");

            Label pointsLabel = new Label(item.getPoints() + " 山竹瓣");
            pointsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1890ff;");

            row.getChildren().addAll(rankLabel, iconView, nicknameLabel, spacer, levelLabel, pointsLabel);
            setGraphic(row);
        }
    }
}
