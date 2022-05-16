package com.example.recommendclient;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import static com.example.recommendclient.Protocol.*;


public class LoginController implements Initializable{

    @FXML public AnchorPane loginAnchorPane;
    @FXML public TextField IDField;
    @FXML public PasswordField PWField;
    @FXML public Button signupButton;
    @FXML public Button loginButton;

    Socket socket;//이컨트롤러가쓸 socket
    InputStream is;//socket inputstream저장
    BufferedInputStream bis;
    OutputStream os;//socket outputstream저장
    BufferedOutputStream bos;
    Protocol proto=new Protocol();//프로토콜 객체 가져옴 ex)


    byte [] receiveBuf;//읽은 패킷 저장할 바이트배열변수
    int receivePos=0;//receiveBuf 인덱싱 변수
    byte [] sendBuf;//보낼패킷 데이터 저장할 바이트배열변수
    int sendPos =0;//sendBuf 인덱싱 변수


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        signupButton.setOnAction(event -> handlerSetSignUpbtnAction(event));
        loginButton.setOnAction(event -> handlerSetLoginbtnAction(event));

        if(!ProgramInfo.socketConnect){//소켓이 아직 연결안되어있으면 소켓 연결
            final String server_Ip="127.0.0.1";//루프백 주소 후에 실제 인터넷연결시 이걸 변경

            final int server_port=3000;//서버가 포트3000으로 서버소켓 만듬
            socket = new Socket();

            try{
                socket.connect(new InetSocketAddress(server_Ip,server_port));
                System.out.println("connected to server");
                ProgramInfo.setSocketConnect(true);
                ProgramInfo.setSocket(socket);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        socket=ProgramInfo.socket;//이컨트롤러의 socket변수에 ProgramInfo소켓전도 전달

        try{//소켓 스트림 생성
            is=socket.getInputStream();
            bis=new BufferedInputStream(is);
            os= socket.getOutputStream();
            bos= new BufferedOutputStream(os);

        }catch (IOException e){
            e.printStackTrace();
        }

    }


    public void handlerSetSignUpbtnAction(ActionEvent event){ //회원가입 버튼클릭 (씬전환)

        try{

            Parent second = FXMLLoader.load(getClass().getResource("SignUp.fxml"));
            Scene scene = new Scene(second);
            Stage stage = (Stage)loginAnchorPane.getScene().getWindow();
            stage.setScene(scene);


        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public void handlerSetLoginbtnAction(ActionEvent event){

        if(IDField.getText().isEmpty() || PWField.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("아이디 또는 비밀번호를 입력하지 않았습니다.");
            alert.show();
            IDField.clear();
            PWField.clear();
            IDField.requestFocus();
        }else if(IDField.getText().length() > 30 || PWField.getText().length() > 30){

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("아이디 또는 비밀번호가 30글자를 초과하였습니다.");
            alert.show();
            IDField.clear();
            PWField.clear();
            IDField.requestFocus();


        }else if(IDField.getText().equals("admin") && PWField.getText().equals("admin")){
            //마스터 계정 ID,PW = admin
            //테스트용으로 만듦.... 나중에 삭제할 것

            try{

                Parent second = FXMLLoader.load(getClass().getResource("RecomendRecipe.fxml"));
                Scene scene = new Scene(second);
                Stage stage = (Stage)loginAnchorPane.getScene().getWindow();
                stage.setScene(scene);


            }catch (Exception e){
                e.printStackTrace();
            }

        }
        else{

            proto = new Protocol(TYPE_REQUEST, CODE_LOGIN);
            sendPos = 2;
            sendBuf = proto.getPacket();

            byte[] id_len = proto.intToByteArray(IDField.getText().length()); //아이디 길이 정보 입력
            System.arraycopy(id_len, 0, sendBuf, sendPos, 4);
            sendPos += 4;

            byte[] id = IDField.getText().getBytes(); //아이디 정보 입력
            System.arraycopy(id, 0, sendBuf, sendPos, id.length);
            sendPos += id.length;

            byte[] pw_len = proto.intToByteArray(PWField.getText().length()); //비밀번호 길이 정보 입력
            System.arraycopy(pw_len, 0, sendBuf, sendPos, 4);
            sendPos += 4;

            byte[] pw = PWField.getText().getBytes(); //비밀번호 정보 입력
            System.arraycopy(pw, 0, sendBuf, sendPos, pw.length);
            sendPos += pw.length;

            try{
                bos.write(sendBuf); //로그인 요청 패킷 전송!
                bos.flush();
            }
            catch (IOException e){
                e.printStackTrace();
            }

            try{
                receiveBuf = proto.getPacket(TYPE_RESPONSE_ERROR, CODE_LOGIN);
                // 기본값 = RESPONSE_ERROR 로 설정
                bis.read(receiveBuf);
                //만약 로그인을 성공했다면 TYPE = 1(RESPONSE) 일것이고 실패했다면(없는계정) TYPE = 2(RESPONSE_ERROR)로 설정될것이다.

            }catch (IOException e){
                e.printStackTrace();
            }

            System.out.println("Type = " + (int)receiveBuf[0] + ", CODE = " + (int)receiveBuf[1] +" 수신함.");
            
            if((int)receiveBuf[0] == TYPE_RESPONSE && (int)receiveBuf[1] == CODE_LOGIN){ //로그인을 성공했을 경우

                try{

                    Parent second = FXMLLoader.load(getClass().getResource("RecomendRecipe.fxml"));
                    Scene scene = new Scene(second);
                    Stage stage = (Stage)loginAnchorPane.getScene().getWindow();
                    stage.setScene(scene);


                }catch (Exception e){
                    e.printStackTrace();
                }


            }else if((int)receiveBuf[0] == TYPE_RESPONSE_ERROR && (int)receiveBuf[1] == CODE_LOGIN){ //로그인을 실패했을 경우 (없는계정)

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("존재하지 않는 계정입니다. 다시 입력해주세요");
                alert.show();
                IDField.clear();
                PWField.clear();
                IDField.requestFocus();

            }

        }


    }



}
