package com.example.recommendclient;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import static com.example.recommendclient.Protocol.*;

public class SelectRecipeInfoController implements Initializable {
    private String selectedRecipeName;
    @FXML
    private Button Previous;
    @FXML
    private Label onestep;//조리과정 1개 보여주는 라벨
    @FXML
    private Button next;
    @FXML
    private ListView ingredientlistview;
    @FXML
    private Label ingredientLink;
    @FXML
    private ListView commentListview;
    @FXML
    private TextField Mycomment;
    @FXML
    private Button backButton;

    Socket socket;//SelectRecipeInfoController가쓸 소켓변수
    InputStream is;//socket inputstream저장
    BufferedInputStream bis;
    OutputStream os;//socket outputstream저장
    BufferedOutputStream bos;
    BufferedReader reader = null;//문자 수신 스트림
    Protocol proto = new Protocol();//프로토콜 객체 가져옴 ex)
    byte[] readBuf;//읽은 패킷 저장할 바이트배열변수
    int readPos = 0;//buf 인덱싱 변수
    byte[] sendData;//보낼패킷 데이터 저장할 바이트배열변수
    int sendPos = 0;//sendData 인덱싱 변수
    int rcvDataCount;// 수신데이터 개수저장할 변수


    public void initData(String name){
        selectedRecipeName=name;
        System.out.println(selectedRecipeName);
        //link.setText(selectedRecipeName);
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        socket = ProgramInfo.socket;//이컨트롤러의 socket변수에 ProgramInfo소켓전도 전달

        try {//소켓 스트림 생성
            is = socket.getInputStream();
            bis = new BufferedInputStream(is);
            os = socket.getOutputStream();
            bos = new BufferedOutputStream(os);

        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(){
            @Override
            public void run(){
                //서버에게 요리이름 전달하여 해당요리의 조리순서,재료목록,댓글 요청
                proto = new Protocol(TYPE_REQUEST, CODE_DETAIL_FOOD_INFO);
                byte[] foodNameByte=selectedRecipeName.getBytes();
                int foodNameByteLength=foodNameByte.length;
                System.arraycopy(Protocol.intToByteArray(foodNameByteLength),0,sendData,sendPos,4);
                sendPos+=4;
                System.arraycopy(foodNameByte,0,sendData,sendPos,foodNameByte.length);
                proto.setByteData(sendData,sendData.length);

                try {
                    bos.write(proto.getPacket());
                    bos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //서버한테 정보받기
                readBuf=proto.getPacket(TYPE_RESPONSE,CODE_DETAIL_FOOD_INFO);
                try {
                    int availablereadbyte=bis.available();
                    readBuf = new byte[2 + availablereadbyte];
                    bis.read(readBuf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int type = readBuf[0]; //타입
                System.out.println(type);
                int code = readBuf[1]; //코드
                System.out.println(code);
                readPos=2;
                //받은정보들로 컨트롤 set

                //조리순서 추출

                //댓글 목록 추출

                //재료 추출

                //재료 link 추출

                //조리순서 ui set

                //댓글 목록 ui set

                //재료 목록 ui set
            }
        };
        thread.setDaemon(true);
        thread.start();

    }
    //_________________________이밑은 핸들러 메소드

    //뒤로가기 버튼
    public void handleBackbtnClicked(ActionEvent event) throws IOException {
        backButton.getScene().getWindow().hide();
    }

    //조리순서 이전 버튼

    //조리순서 다음 버튼

    //재료목록 1개 선택

    //댓글 달기 버튼


}
