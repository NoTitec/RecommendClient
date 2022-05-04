package com.example.recommendclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("RecomendRecipe.fxml"));
        Scene scene = new Scene(fxmlLoader.load());//, 320, 240 fxmlLoader 에서 크기지정가능
        stage.setTitle("Hello!");
        stage.setOnCloseRequest(event -> System.out.println("종료"));
        stage.setScene(scene);
        //stage.setWidth(400);//윈도우 크기
        //stage.setHeight(300);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}