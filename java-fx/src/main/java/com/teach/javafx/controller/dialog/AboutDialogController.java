package com.teach.javafx.controller.dialog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import java.awt.Desktop;
import java.net.URI;

public class AboutDialogController {
    @FXML
    private Hyperlink githubLink;

    @FXML
    public void initialize() {
        githubLink.setOnAction(event -> {
            try {
                Desktop.getDesktop().browse(new URI(githubLink.getText()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
