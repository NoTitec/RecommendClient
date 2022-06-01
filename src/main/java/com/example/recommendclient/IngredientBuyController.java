package com.example.recommendclient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class IngredientBuyController implements Initializable {
    public String ingredientLink;
    @FXML
    private WebView ingredientwebview;
    public WebEngine webEngine;
    public void initIngredientData(String name){ ingredientLink=name;}
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(()->{//init 가 initialize보다 먼저실행되므로 runlater로  init받고 웹뷰동작하게
            System.out.println(ingredientLink);
            String geturl=ingredientLink;
            webEngine=ingredientwebview.getEngine();
            webEngine.load(geturl);
        });
    }
}
