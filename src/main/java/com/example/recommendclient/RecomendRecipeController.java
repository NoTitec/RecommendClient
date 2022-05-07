package com.example.recommendclient;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
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

    Socket socket;//이컨트롤러가쓸 socket
    InputStream is;//socket inputstream저장
    BufferedInputStream bis;
    OutputStream os;//socket outputstream저장
    BufferedOutputStream bos;
    Protocol proto=new Protocol();//프로토콜 객체 가져옴 ex)
    byte [] buf;//읽은 패킷 저장할 바이트배열변수
    int pos=0;//buf 인덱싱 변수
    byte [] sendData;//보낼패킷 데이터 저장할 바이트배열변수
    int sendPos =0;//sendData 인덱싱 변수
    int rcvDataCount;// 수신데이터 개수저장할 변수

    String []recommendname= new String[4];//추천요리 이름저장배열
    String []Imageurl=new String[4];//추천요리 이미지 url저장배열

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /*initialize메소드는main에서 fxml파일 로드할때 컨트롤객체 생성하며 실행
        이벤트핸들러 구축
        이벤트핸들러는 fxml에서 onAction속성으로 정의한경우 initialize에서 생성필요 x
        방법1
        resetbutton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
             handleresetbtnAction(event);
          }
        });
        방법2
        resetbutton.setOnAction(event -> handleresetbtnAction(event));
        방법3
        fxml에서 속성으로 onAction정의*/

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

        //접속후 서버에게 요리목록 패킷요청
        proto=new Protocol(0,0);

        try {
            bos.write(proto.getPacket());
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //요리목록패킷 받아서 사진과 이미지 변경 , 받는패킷의 목록순서 날씨음식1,2 계절음식 1,2
        try {
            System.out.println(bis.read(buf));//수신버퍼 읽기시도후 실제 읽은 바이트수 출력하고 buf배열에 읽은것 저장
        } catch (IOException e) {
            e.printStackTrace();
        }
        int type=buf[0]; //타입
        System.out.println(type);
        int code=buf[1]; //코드
        System.out.println(code);
        pos=2;
        for(int i=0;i<4;i++){//전송받은 요리 4개 추출해 저장
            //1개요리 이름길이 추출
            int onerecipenamelength=proto.byteArrayToInt(Arrays.copyOfRange(buf,pos,pos+4));
            pos+=4;
            //1개요리 이름길이만큼 읽어서 추출후 string 변환하여 요리이름배열에저장
            byte[] name = Arrays.copyOfRange(buf, pos, pos+onerecipenamelength);//추출한길이만큼읽어 코드추출
            recommendname[i] = new String(name);//추출 이름 String 변환해 저장
            pos+=onerecipenamelength;
            //1개요리 이미지 url 길이 추출
            int onerecipeurllength=proto.byteArrayToInt(Arrays.copyOfRange(buf,pos,pos+4));
            pos+=4;
            //1개요리 이미지 url길이만큼 읽어서 추출후 string 변환하여 요리이미지배열에 저장
            byte[] url = Arrays.copyOfRange(buf, pos, pos+onerecipeurllength);//추출한길이만큼읽어 코드추출
            Imageurl[i]= new String(url);//추출 word를  string으로 변환하여 저장
            pos+=onerecipenamelength;
        }

        //받은 요리의 이름과 이미지 url이용해 ui 정보 변경
        wetherrecommend1.setText(recommendname[0]);
        wetherImage1.setImage(new Image(Imageurl[0]));
        wetherrecommend2.setText(recommendname[1]);
        wetherImage2.setImage(new Image(Imageurl[1]));
        seasonrecommend1.setText(recommendname[2]);
        seasonImage1.setImage(new Image(Imageurl[2]));
        seasonrecommend2.setText(recommendname[3]);
        seasonImage2.setImage(new Image(Imageurl[3]));

        //수신버퍼 읽을것 없을때까지 읽어서 버퍼비우기(1번째 통신이후 수신버퍼 읽을때 쓰레기값 방지위해)
        while(true){
            try {
                if (bis.read()==-1) break;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        pos=2;//pos변수 다시 2로 초기화
    }
    //----------------- 이밑은 이밴트 핸들러 메소드
    public void handleresetbtnAction(ActionEvent event){
        Thread thread=new Thread(){//버튼 눌렀을때 통신과 ui변경담당 스레드
            @Override
            public void run() {
                System.out.println("새로고침버튼클릭");
                //서버에게 요리목록 패킷요청
                proto=new Protocol(0,0);

                try {
                    bos.write(proto.getPacket());
                    bos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //요리목록패킷 받아서 사진과 이미지 변경 , 받는패킷의 목록순서 날씨음식1,2 계절음식 1,2
                try {
                    System.out.println(bis.read(buf));//수신버퍼 읽기시도후 실제 읽은 바이트수 출력하고 buf배열에 읽은것 저장
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int type=buf[0]; //타입
                int code=buf[1]; //코드
                pos=2;
                for(int i=0;i<4;i++){//전송받은 요리 4개 추출해 저장
                    //1개요리 이름길이 추출
                    int onerecipenamelength=proto.byteArrayToInt(Arrays.copyOfRange(buf,pos,pos+4));
                    pos+=4;
                    //1개요리 이름길이만큼 읽어서 추출후 string 변환하여 요리이름배열에저장
                    byte[] name = Arrays.copyOfRange(buf, pos, pos+onerecipenamelength);//추출한길이만큼읽어 코드추출
                    recommendname[i] = new String(name);//추출 이름 String 변환해 저장
                    pos+=onerecipenamelength;
                    //1개요리 이미지 url 길이 추출
                    int onerecipeurllength=proto.byteArrayToInt(Arrays.copyOfRange(buf,pos,pos+4));
                    pos+=4;
                    //1개요리 이미지 url길이만큼 읽어서 추출후 string 변환하여 요리이미지배열에 저장
                    byte[] url = Arrays.copyOfRange(buf, pos, pos+onerecipeurllength);//추출한길이만큼읽어 코드추출
                    Imageurl[i]= new String(url);//추출 word를  string으로 변환하여 저장
                    pos+=onerecipenamelength;
                }
                //받은 요리의 이름과 이미지 url이용해 ui 정보 변경
                Platform.runLater(()->wetherrecommend1.setText(recommendname[0]));
                Platform.runLater(()->wetherImage1.setImage(new Image(Imageurl[0])));
                Platform.runLater(()->wetherrecommend2.setText(recommendname[1]));
                Platform.runLater(()->wetherImage2.setImage(new Image(Imageurl[1])));
                Platform.runLater(()->seasonrecommend1.setText(recommendname[2]));
                Platform.runLater(()->seasonImage1.setImage(new Image(Imageurl[2])));
                Platform.runLater(()->seasonrecommend2.setText(recommendname[3]));
                Platform.runLater(()->seasonImage2.setImage(new Image(Imageurl[3])));
                //수신버퍼 읽을것 없을때까지 읽어서 버퍼비우기(1번째 통신이후 수신버퍼 읽을때 쓰레기값 방지위해)
                while(true){
                    try {
                        if (bis.read()==-1) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                pos=2;
                //wetherImage1.setImage(new Image("https://recipe1.ezmember.co.kr/cache/recipe/2022/02/16/8e34b759f6386912756c9a0f9d2255f91.jpg"));
            }

        };
        thread.setDaemon(true);
        thread.start();
    }
}