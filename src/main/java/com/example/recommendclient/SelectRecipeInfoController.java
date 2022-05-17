package com.example.recommendclient;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SelectRecipeInfoController implements Initializable {
    private String selectedRecipeName;
    @FXML
    private SplitPane splitpane;
    @FXML
    private Label link;
    @FXML
    private WebView youtubewebview;
    @FXML
    private Button backButton;

    private WebEngine engine;
    public void initData(String name){
        selectedRecipeName=name;
        link.setText(selectedRecipeName);
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        engine=youtubewebview.getEngine();
        engine.load("https://www.youtube.com/embed/N1phCu00GWU");


    }
    //_________________________이밑은 핸들러 메소드
    public void handleBackbtnClicked(ActionEvent event) throws IOException {
        backButton.getScene().getWindow().hide();
    }
}
