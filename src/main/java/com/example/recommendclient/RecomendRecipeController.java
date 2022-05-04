package com.example.recommendclient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class RecomendRecipeController implements Initializable {
    //@FXML다음 initialize실행
    @FXML public Button resetbutton;
    @FXML public ImageView wetherImage2;
    @FXML public Label wetherrecommend2;
    @FXML public ImageView seasonImage2;
    @FXML public Label seasonrecommend2;
    @FXML public ImageView wetherImage1;
    @FXML public Label wetherrecommend1;
    @FXML public ImageView seasonImage1;
    @FXML public Label seasonrecommend1;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //initialize메소드는main에서 fxml파일 로드할때 컨트롤객체 생성하며 실행
        //이벤트핸들러 구축
        //이벤트핸들러는 fxml에서 onAction속성으로 정의한경우 initialize에서 생성필요 x
        //방법1
        /*resetbutton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleresetbtnAction(event);
            }
        });*/
        //방법2
        //resetbutton.setOnAction(event -> handleresetbtnAction(event));
        //방법3
        //fxml에서 속성으로 onAction정의
    }
    public void handleresetbtnAction(ActionEvent event){
        System.out.println("새로고침버튼클릭");
    }
}