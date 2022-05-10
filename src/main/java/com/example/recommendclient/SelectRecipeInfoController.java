package com.example.recommendclient;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class SelectRecipeInfoController implements Initializable {

    private String selectedRecipeName;
    @FXML
    private Label link;
    @FXML
    private WebView youtubewebview;

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
}
