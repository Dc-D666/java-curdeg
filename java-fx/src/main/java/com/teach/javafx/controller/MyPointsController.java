package com.teach.javafx.controller;

import com.teach.javafx.AppStore;
import com.teach.javafx.controller.PersonalCenterController;
import com.teach.javafx.controller.base.MainFrameController;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

public class MyPointsController extends ToolController {

    @FXML
    private ImageView levelIcon;

    @FXML
    private Label levelName;

    @FXML
    private Label pointsLabel;

    @FXML
    private Region progressBar;

    @FXML
    private Label progressLabel;

    @FXML
    private Button checkHistoryBtn;

    @FXML
    private Button checkRankBtn;

    @FXML
    private Button checkPrivilegesBtn;

    @FXML
    private VBox earnPointsBox;

    @FXML
    private VBox spendPointsBox;

    @FXML
    public void initialize() {
        loadUserData();
        loadPointRules();

        checkHistoryBtn.setOnAction(e -> navigateToPage("point-history", "积分记录"));
        checkRankBtn.setOnAction(e -> navigateToPage("point-rank", "积分排行"));
        checkPrivilegesBtn.setOnAction(e -> navigateToPage("level-privileges", "等级特权"));
    }

    private void loadUserData() {
        new Thread(() -> {
            try {
                Map<String, Object> data = HttpRequestUtil.getMyPoints();
                if (data != null) {
                    Platform.runLater(() -> {
                        int points = data.get("points") != null ? ((Number) data.get("points")).intValue() : 0;
                        int level = data.get("level") != null ? ((Number) data.get("level")).intValue() : 0;
                        String levelNameStr = data.get("levelName") != null ? data.get("levelName").toString() : "Lv." + level;
                        String iconPath = data.get("iconPath") != null ? data.get("iconPath").toString() : "";
                        int nextLevelPoints = data.get("nextLevelPoints") != null ? ((Number) data.get("nextLevelPoints")).intValue() : 0;
                        int currentLevelPoints = data.get("currentLevelPoints") != null ? ((Number) data.get("currentLevelPoints")).intValue() : 0;

                        levelName.setText(levelNameStr);
                        pointsLabel.setText(points + " 山竹瓣");

                        if (iconPath != null && !iconPath.isEmpty()) {
                            try {
                                String fullUrl = HttpRequestUtil.serverUrl + iconPath;
                                Image image = new Image(fullUrl, true);
                                levelIcon.setImage(image);
                            } catch (Exception e) {
                                System.err.println("Failed to load level icon: " + iconPath);
                            }
                        }

                        if (nextLevelPoints > 0) {
                            int progress = (points - currentLevelPoints) * 100 / (nextLevelPoints - currentLevelPoints);
                            progressBar.setStyle(String.format("-fx-background-color: linear-gradient(to right, #6366f1 %d%%, #f0f0f0 %d%%); -fx-background-radius: 5;", progress, progress));
                            progressLabel.setText(String.format("距离下一等级还需 %d 山竹瓣", nextLevelPoints - points));
                        } else {
                            progressBar.setStyle("-fx-background-color: #6366f1; -fx-background-radius: 5;");
                            progressLabel.setText("已达到最高等级");
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void loadPointRules() {
        new Thread(() -> {
            try {
                List<Map<String, Object>> rules = HttpRequestUtil.getPointRules();
                Platform.runLater(() -> {
                    earnPointsBox.getChildren().clear();
                    spendPointsBox.getChildren().clear();

                    if (rules != null) {
                        for (Map<String, Object> rule : rules) {
                            int pointsChange = rule.get("pointsChange") != null ? ((Number) rule.get("pointsChange")).intValue() : 0;
                            String ruleName = rule.get("ruleName") != null ? rule.get("ruleName").toString() : "";

                            if (pointsChange > 0) {
                                HBox ruleBox = createRuleBox(pointsChange, ruleName);
                                earnPointsBox.getChildren().add(ruleBox);
                            } else if (pointsChange < 0) {
                                HBox ruleBox = createRuleBox(pointsChange, ruleName);
                                spendPointsBox.getChildren().add(ruleBox);
                            }
                        }

                        if (earnPointsBox.getChildren().isEmpty()) {
                            earnPointsBox.getChildren().add(new Label("暂无获取积分规则"));
                        }
                        if (spendPointsBox.getChildren().isEmpty()) {
                            spendPointsBox.getChildren().add(new Label("暂无积分消耗规则"));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private HBox createRuleBox(int pointsChange, String ruleName) {
        HBox box = new HBox();
        box.setSpacing(12);
        box.setStyle("-fx-padding: 8; -fx-background-color: #f8fafc; -fx-background-radius: 8;");

        Label pointsLabel = new Label();
        pointsLabel.setStyle(pointsChange > 0 ? "-fx-text-fill: #10b981;" : "-fx-text-fill: #ef4444;");
        pointsLabel.setText((pointsChange > 0 ? "+" : "") + pointsChange);

        Label nameLabel = new Label(ruleName);
        nameLabel.setStyle("-fx-font-size: 13px;");

        box.getChildren().addAll(pointsLabel, nameLabel);
        return box;
    }

    private void navigateToPage(String pageName, String title) {
        try {
            PersonalCenterController centerController = AppStore.getPersonalCenterController();
            if (centerController != null) {
                centerController.navigateByPage(pageName, title);
            } else {
                MainFrameController mainFrameController = AppStore.getMainFrameController();
                if (mainFrameController != null) {
                    mainFrameController.changeContent(pageName, title);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}