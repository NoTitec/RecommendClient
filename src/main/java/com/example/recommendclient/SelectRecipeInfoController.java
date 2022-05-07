package com.example.recommendclient;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class SelectRecipeInfoController implements Initializable {

    private String selectedRecipeName;
    @FXML
    private Label link;
    public void initData(String name){
        selectedRecipeName=name;
        link.setText(selectedRecipeName);
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
