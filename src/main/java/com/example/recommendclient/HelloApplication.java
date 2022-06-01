package com.example.recommendclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Login.fxml")); //시작화면 변경
        Scene scene = new Scene(fxmlLoader.load());//, 320, 240 fxmlLoader 에서 크기지정가능
        primaryStage.setTitle("Hello!");
        /*primaryStage.setOnCloseRequest(event -> {
            try {
                ProgramInfo.closeSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });*/
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        //primaryStage.setWidth(400);//윈도우 크기
        //primaryStage.setHeight(300);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}