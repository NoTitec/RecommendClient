package com.example.recommendclient;

import com.example.recommendclient.dataclass.Ingredient;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;

import static com.example.recommendclient.Protocol.*;

public class SelectRecipeInfoController implements Initializable {
    private String selectedRecipeName;
    @FXML
    private Button Previous;
    @FXML
    private Button next;
    @FXML
    private Label onestep;//조리과정 1개 보여주는 라벨
    @FXML
    private TableView ingredientTableView;
    @FXML
    private TableColumn ingredientNameColumn;
    @FXML
    private TableColumn ingredientLinkColumn;
    @FXML
    private Label ingredientLink;
    @FXML
    private ListView commentListview;
    @FXML
    private TextField Mycomment;
    @FXML
    private Button leaveComment;
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
    byte[] sendData=new byte[500];//보낼패킷 데이터 저장할 바이트배열변수
    int sendPos = 0;//sendData 인덱싱 변수
    int rcvDataCount;// 수신데이터 개수저장할 변수

    //조리순서 담을 배열
    String[] steps=null;
    //조리순서 인덱신 추적 변수
    int stepssize;
    //댓글 목록 담을 ObservableList
    ObservableList<String> comments= FXCollections.observableArrayList();
    //재료+재료링크 담을 리스트
    ObservableList<Ingredient> ingredientAndLink=FXCollections.observableArrayList();

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

        ingredientAndLink.clear();

        Thread thread = new Thread(){
            @Override
            public void run(){
                //서버에게 요리이름 전달하여 해당요리의 조리순서,재료목록,댓글 요청
                proto = new Protocol(TYPE_REQUEST, CODE_DETAIL_FOOD_INFO);
                selectedRecipeName=ProgramInfo.transferFoodName;
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
                    //int availablereadbyte=bis.available();//읽기가능한 데이터만큼 바이트배열 생성
                    int availablereadbyte=10000;
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
                //---------------받은정보들로 컨트롤 set

                //조리 순서 개수 추출
                int stepCount = Protocol.byteArrayToInt(Arrays.copyOfRange(readBuf, readPos, readPos + 4));
                readPos+=4;
                System.out.println("stepCount:"+stepCount);
                if(stepCount!=0);{
                    steps = new String[stepCount];
                }
                stepssize=0;
                //댓글 개수 추출
                int commentCount = Protocol.byteArrayToInt(Arrays.copyOfRange(readBuf, readPos, readPos + 4));
                readPos+=4;
                comments.clear();
                //재료 개수 추출
                int ingredientCount = Protocol.byteArrayToInt(Arrays.copyOfRange(readBuf, readPos, readPos + 4));
                readPos+=4;
                //조리순서 추출
                for(int i=0;i<stepCount;i++){
                    int oneStepLength=Protocol.byteArrayToInt(Arrays.copyOfRange(readBuf,readPos,readPos+4));
                    readPos+=4;
                    byte[] oneStepByte=Arrays.copyOfRange(readBuf,readPos,readPos+oneStepLength);
                    readPos+=oneStepLength;
                    try{
                        steps[i]=new String(oneStepByte,"UTF-8");
                    }catch (UnsupportedEncodingException e){
                        e.printStackTrace();
                    }
                }
                //댓글 목록 추출
                for(int i=0;i<commentCount;i++){
                    int oneCommentLength=Protocol.byteArrayToInt(Arrays.copyOfRange(readBuf,readPos,readPos+4));
                    readPos+=4;
                    byte[] oneCommentByte = Arrays.copyOfRange(readBuf, readPos, readPos + oneCommentLength);
                    readPos+=oneCommentLength;
                    try {
                        comments.add(new String(oneCommentByte,"UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                //재료+link 추출
                for(int i=0;i<ingredientCount;i++){
                    int oneIngredientLength=Protocol.byteArrayToInt(Arrays.copyOfRange(readBuf,readPos,readPos+4));
                    readPos+=4;
                    byte[] oneIngredientByte=Arrays.copyOfRange(readBuf,readPos,readPos+oneIngredientLength);
                    readPos+=oneIngredientLength;
                    int oneIngredientLinkLength=Protocol.byteArrayToInt(Arrays.copyOfRange(readBuf,readPos,readPos+4));
                    readPos+=4;
                    byte[] oneIngredientLinkByte=Arrays.copyOfRange(readBuf,readPos,readPos+oneIngredientLinkLength);
                    readPos+=oneIngredientLinkLength;
                    String ingredientName="";
                    String ingredientLink="";
                    try {
                        ingredientName=new String(oneIngredientByte, "UTF-8");
                        ingredientLink=new String(oneIngredientLinkByte, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    ingredientAndLink.add(new Ingredient(ingredientName,ingredientLink));
                }
                readPos=0;
                //------------------ui 설정
                //조리순서 ui set
                if(steps!=null) {

                    Platform.runLater(() -> {
                        onestep.setWrapText(true);
                        onestep.setText(steps[0]);
                    });
                }
                //댓글 목록 ui set
                Platform.runLater(()->commentListview.setItems(comments));
                //재료 목록 ui set
                ingredientNameColumn.setCellValueFactory(new PropertyValueFactory<>("ingredientName"));
                ingredientLinkColumn.setCellValueFactory(new PropertyValueFactory<>("ingredientLink"));
                //--재료 클릭시 링크라벨 변경 이벤트 리스너
                ingredientTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Ingredient>() {
                    @Override
                    public void changed(ObservableValue<? extends Ingredient> observable, Ingredient oldValue, Ingredient newValue) {
                        if(newValue!=null){
                            Platform.runLater(()->ingredientLink.setText(newValue.getIngredientLink()));
                        }
                    }
                });
                Platform.runLater(()->ingredientTableView.setItems(ingredientAndLink));
            }
        };
        thread.setDaemon(true);
        thread.start();

    }
    //_________________________이밑은 핸들러 메소드

    //재료링크 클릭 버튼
    public void ingredientLinkClick(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("IngredientBuyView.fxml"));
        Parent root = (Parent) loader.load();
        IngredientBuyController iController=loader.<IngredientBuyController>getController();
        if(ingredientLink.getText()!=null) {
            iController.initIngredientData(ingredientLink.getText());
        }
        else{
            System.out.println("링크재료가 현재 null값");
            return;
        }
        Scene scene=new Scene(root);
        Stage stage=new Stage();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                iController.webEngine.load(null);
            }
        });
        stage.setScene(scene);
        stage.show();
    }
    //뒤로가기 버튼
    public void handleBackbtnClicked(ActionEvent event) throws IOException {
        backButton.getScene().getWindow().hide();
    }

    //조리순서 이전 버튼
    public void previousbtn(ActionEvent event){
        if(stepssize>0) {
            stepssize--;
            onestep.setText(steps[stepssize]);
        }
        else{
            System.out.println("첫번째 step");
        }
    }
    //조리순서 다음 버튼
    public void nextbtn(ActionEvent event){
        if(stepssize+1<steps.length){
            stepssize++;
            onestep.setText(steps[stepssize]);
        }
        else{
            System.out.println("마지막 step");
        }
    }
    //댓글 달기 버튼
    public void leavecommentbtn(ActionEvent event){
        comments.add(Mycomment.getText());
        String myComment=Mycomment.getText();
        //서버에게 내 댓글 전달하여 등록 요청
        proto = new Protocol(TYPE_REQUEST, CODE_COMMENT_LEAVE);
        sendPos=0;
        byte[] commentByte=myComment.getBytes();
        int foodNameByteLength=commentByte.length;
        System.arraycopy(Protocol.intToByteArray(foodNameByteLength),0,sendData,sendPos,4);
        sendPos+=4;
        System.arraycopy(commentByte,0,sendData,sendPos,commentByte.length);
        proto.setByteData(sendData,sendData.length);

        try {
            bos.write(proto.getPacket());
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
