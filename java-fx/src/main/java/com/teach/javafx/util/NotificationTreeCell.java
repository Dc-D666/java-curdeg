package com.teach.javafx.util;

import com.teach.javafx.models.AppSettings;
import com.teach.javafx.request.MyTreeNode;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.HBox;

public class NotificationTreeCell extends TreeCell<MyTreeNode> {

    private HBox hbox;
    private Label textLabel;
    private Label badgeLabel;

    public NotificationTreeCell() {
        hbox = new HBox();
        hbox.setSpacing(4);

        textLabel = new Label();
        updateFontSize();

        badgeLabel = new Label();
        badgeLabel.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-padding: 1 5 1 5; " +
                "-fx-border-radius: 10; -fx-background-radius: 10;");
        badgeLabel.setMinWidth(20);
        badgeLabel.setAlignment(javafx.geometry.Pos.CENTER);
        badgeLabel.setVisible(false);

        hbox.getChildren().addAll(textLabel, badgeLabel);
    }

    private void updateFontSize() {
        AppSettings settings = SettingsManager.getCurrentSettings();
        String fontSize = settings.getFontSize();
        double fontSizeValue;
        
        switch (fontSize) {
            case "小":
                fontSizeValue = 12;
                break;
            case "大":
                fontSizeValue = 16;
                break;
            case "中（默认）":
            default:
                fontSizeValue = 14;
                break;
        }
        
        textLabel.setStyle(String.format("-fx-text-fill: black; -fx-font-size: %.0fpx; -fx-padding: 4 0 4 0;", fontSizeValue));
    }

    @Override
    protected void updateItem(MyTreeNode item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        updateFontSize();
        
        String label = item.getLabel();
        textLabel.setText(label);

        long count = 0;

        if ("我的通知".equals(label)) {
            count = NotificationCounter.getNotificationCount();
        } else if ("我的私信".equals(label)) {
            count = NotificationCounter.getMessageCount();
        }

        if (count > 0) {
            badgeLabel.setText(String.valueOf(count));
            badgeLabel.setVisible(true);
        } else {
            badgeLabel.setVisible(false);
        }

        setGraphic(hbox);
        setText(null);
    }
}
