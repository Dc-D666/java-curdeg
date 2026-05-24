package com.teach.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;

public class ImagePreviewController {
    @FXML
    private StackPane imageContainer;
    @FXML
    private ImageView imageView;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button closeButton;
    @FXML
    private Label pageLabel;

    private List<String> imageUrls;
    private int currentIndex;
    private Stage stage;

    @FXML
    public void initialize() {
        prevButton.setOnAction(e -> showPrevImage());
        nextButton.setOnAction(e -> showNextImage());
        closeButton.setOnAction(e -> close());

        imageContainer.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                close();
            }
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setImageUrls(List<String> imageUrls, int startIndex) {
        this.imageUrls = imageUrls;
        this.currentIndex = startIndex;
        updateDisplay();
        updateButtonStates();
    }

    private void updateDisplay() {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        String imageUrl = imageUrls.get(currentIndex);
        Image image = new Image(imageUrl, true);
        imageView.setImage(image);
        pageLabel.setText((currentIndex + 1) + " / " + imageUrls.size());
    }

    private void updateButtonStates() {
        if (imageUrls == null || imageUrls.size() <= 1) {
            prevButton.setDisable(true);
            nextButton.setDisable(true);
        } else {
            prevButton.setDisable(currentIndex <= 0);
            nextButton.setDisable(currentIndex >= imageUrls.size() - 1);
        }
    }

    private void showPrevImage() {
        if (currentIndex > 0) {
            currentIndex--;
            updateDisplay();
            updateButtonStates();
        }
    }

    private void showNextImage() {
        if (imageUrls != null && currentIndex < imageUrls.size() - 1) {
            currentIndex++;
            updateDisplay();
            updateButtonStates();
        }
    }

    private void close() {
        if (stage != null) {
            stage.close();
        }
    }
}
