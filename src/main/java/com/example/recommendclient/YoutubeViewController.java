package com.example.recommendclient;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class YoutubeViewController implements Initializable {
    public String youtubeLink;
    @FXML
    private WebView youtubeembedview;

    public WebEngine webEngine;

    public void initYouData(String name){
        youtubeLink=name;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Platform.runLater(()->{//init 가 initialize보다 먼저실행되므로 runlater로  init받고 웹뷰동작하게
            System.out.println(youtubeLink);
            String geturl=youtubeLink;
            webEngine=youtubeembedview.getEngine();
            webEngine.load(geturl);
        });



    }
}
