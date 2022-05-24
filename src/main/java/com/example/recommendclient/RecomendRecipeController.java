package com.example.recommendclient;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ResourceBundle;

import static com.example.recommendclient.Protocol.*; //상수 사용을 위한 import


public class RecomendRecipeController implements Initializable {
    //@FXML다음 initialize실행
    @FXML
    public SplitPane splitpane;
    @FXML
    public Button resetbutton;
    @FXML
    public ImageView wetherImage2;
    @FXML
    public Label wetherrecommend2;
    @FXML
    public ImageView seasonImage2;
    @FXML
    public Label seasonrecommend2;
    @FXML
    public ImageView wetherImage1;
    @FXML
    public Label wetherrecommend1;
    @FXML
    public ImageView seasonImage1;
    @FXML
    public Label seasonrecommend1;
    @FXML
    public Button wetherrecommend1youtubepopup;

    Socket socket;//이컨트롤러가쓸 socket
    InputStream is;//socket inputstream저장
    BufferedInputStream bis;
    OutputStream os;//socket outputstream저장
    BufferedOutputStream bos;
    HttpURLConnection urlConnection = null;//웹 연결 클래스
    BufferedReader reader = null;//문자 수신 스트림
    String jsonStr = null;// json결과 String받을때쓸 변수
    JSONObject json=null;//json 오브젝트 담을 변수
    JSONArray jsonArray=null;//json array 오브젝트 담을 변수
    Double lat=null;//위도 담을 변수
    Double lon=null;//경도 담을 변수
    Protocol proto = new Protocol();//프로토콜 객체 가져옴 ex)
    byte[] buf;//읽은 패킷 저장할 바이트배열변수
    int pos = 0;//buf 인덱싱 변수
    byte[] sendData;//보낼패킷 데이터 저장할 바이트배열변수
    int sendPos = 0;//sendData 인덱싱 변수
    int rcvDataCount;// 수신데이터 개수저장할 변수

    String todayWether=null;
    String todayTemperature=null;
    String[] recommendname = new String[4];//추천요리 이름저장배열
    String[] Imageurl = new String[4];//추천요리 이미지 url저장배열
    String[] youtubeLink=new String[4];//추천요리 유튜브 링크 저장배열
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

        wetherImage1.setOnMouseClicked(event -> handlerSetWetherImage1Action(event));
        wetherImage2.setOnMouseClicked(event -> handlerSetWetherImage2Action(event));
        seasonImage1.setOnMouseClicked(event -> handlerSetSeasonImage1Action(event));
        seasonImage2.setOnMouseClicked(event -> handlerSetSeasonImage2Action(event));

        if (!ProgramInfo.socketConnect) {//소켓이 아직 연결안되어있으면 소켓 연결
            final String server_Ip = "127.0.0.1";//루프백 주소 후에 실제 인터넷연결시 이걸 변경

            final int server_port = 3000;//서버가 포트3000으로 서버소켓 만듬
            socket = new Socket();

            try {
                socket.connect(new InetSocketAddress(server_Ip, server_port));
                System.out.println("connected to server");
                ProgramInfo.setSocketConnect(true);
                ProgramInfo.setSocket(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        socket = ProgramInfo.socket;//이컨트롤러의 socket변수에 ProgramInfo소켓전도 전달

        try {//소켓 스트림 생성
            is = socket.getInputStream();
            bis = new BufferedInputStream(is);
            os = socket.getOutputStream();
            bos = new BufferedOutputStream(os);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //접속후 서버에게 요리목록 패킷요청
        proto = new Protocol(TYPE_REQUEST, CODE_RECOMMENDFOOD); //가독성 개선함


        try {//자신 ip주소 가져오고 그 ip로 현재 ip의 위도와 경도를 얻어서 서버에게 전송
            sendData = new byte[proto.LEN_PROTOCOL_BODY];// 3000크기 바이트배열 할당
            //ip 주소 가져오기
            URL checkmyipurl = new URL("http://checkip.amazonaws.com");
            reader = new BufferedReader(new InputStreamReader(checkmyipurl.openStream()));
            String myip = reader.readLine();
            //ip주소로 위도 경도 가져오기
            String ipapiWithMyIp = "http://ip-api.com/json/" + myip ;
            URL url = new URL(ipapiWithMyIp);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            System.out.println(urlConnection.getResponseMessage());

            try (InputStream in = urlConnection.getInputStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                byte[] buf = new byte[1024 * 8];
                int length = 0;
                while ((length = in.read(buf)) != -1) {
                    out.write(buf, 0, length);
                }
                jsonStr = new String(out.toByteArray(), "UTF-8");
            }
            //String 받은 데이터를 json 으로 바꾸고 파싱
            try{
                json= new JSONObject(jsonStr);
                //System.out.println(json.get("lat").getClass().getName());
                lat=json.getDouble("lat");
                lon=json.getDouble("lon");
                String latStr=lat.toString();
                String lonStr=lon.toString();

                //얻은 위도와 경도를 String 변환후 보낼 데이터 패킷에 설정

                byte[] latbyte=latStr.getBytes();
                int latbyteLength=latbyte.length;
                //위도 set
                System.arraycopy(proto.intToByteArray(latbyteLength),0,sendData,sendPos,4);
                sendPos+=4;
                System.arraycopy(latbyte,0,sendData,sendPos,latbyte.length);
                sendPos+=latbyte.length;
                //경도 set
                byte[] lonbyte=lonStr.getBytes();
                int lonbyteLength=lonbyte.length;
                System.arraycopy(proto.intToByteArray(lonbyteLength),0,sendData,sendPos,4);
                sendPos+=4;
                System.arraycopy(lonbyte,0,sendData,sendPos,lonbyte.length);

                //완성된 sendData byte배열 proto.packet에 set

                proto.setByteData(sendData,sendData.length);
            }catch (JSONException e){
                e.printStackTrace();
            }
            bos.write(proto.getPacket());
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //요리목록패킷 받아서 사진과 이미지 변경 , 받는패킷의 목록순서 날씨음식1,2 계절음식 1,2
        try {

            buf = proto.getPacket(TYPE_RESPONSE, CODE_RECOMMENDFOOD); //가독성 개선함

            bis.read(buf);//수신버퍼 읽기시도후 buf배열에 읽은것 저장
        } catch (IOException e) {
            e.printStackTrace();
        }
        int type = buf[0]; //타입
        System.out.println(type);
        int code = buf[1]; //코드
        System.out.println(code);

        pos = 2;
        //오늘 날씨
        int wetherlength=proto.byteArrayToInt(Arrays.copyOfRange(buf, pos, pos + 4));
        pos+=4;
        byte[] wethername = Arrays.copyOfRange(buf, pos, pos + wetherlength);
        try {
            todayWether=new String(wethername,"UTF-8");
            System.out.println(todayWether);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        pos+=wetherlength;
        //온도
        int temperaturelength=proto.byteArrayToInt(Arrays.copyOfRange(buf, pos, pos + 4));
        pos+=4;
        byte[] temperaturevalue = Arrays.copyOfRange(buf, pos, pos + temperaturelength);
        try {
            todayTemperature=new String(temperaturevalue,"UTF-8");
            System.out.println(todayTemperature);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        pos+=temperaturelength;


        for (int i = 0; i < 4; i++) {//전송받은 요리 4개와 요리4개의 유튜브 링크 추출해 저장
            //1개요리 이름길이 추출
            int onerecipenamelength = proto.byteArrayToInt(Arrays.copyOfRange(buf, pos, pos + 4));
            pos += 4;
            //1개요리 이름길이만큼 읽어서 추출후 string 변환하여 요리이름배열에저장
            byte[] name = Arrays.copyOfRange(buf, pos, pos + onerecipenamelength);//추출한길이만큼읽어 코드추출
            try {
                recommendname[i] = new String(name, "UTF-8");//추출 이름 String 변환해 저장
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            pos += onerecipenamelength;
            //1개요리 이미지 url 길이 추출
            int onerecipeurllength = proto.byteArrayToInt(Arrays.copyOfRange(buf, pos, pos + 4));
            pos += 4;
            //1개요리 이미지 url길이만큼 읽어서 추출후 string 변환하여 요리이미지배열에 저장
            byte[] url = Arrays.copyOfRange(buf, pos, pos + onerecipeurllength);//추출한길이만큼읽어 코드추출
            Imageurl[i] = new String(url);//추출 word를  string으로 변환하여 저장
            pos += onerecipeurllength;

            //1개요리 유튜브 링크 추출
            int onerecipeyoutubelength = proto.byteArrayToInt(Arrays.copyOfRange(buf, pos, pos + 4));
            pos += 4;
            //1개요리 유튜브 url길이만큼 읽어서 추출후 string 변환하여 요리유튜브배열에 저장
            byte[] youtubeurl = Arrays.copyOfRange(buf, pos, pos + onerecipeyoutubelength);//추출한길이만큼읽어 코드추출
            youtubeLink[i] = new String(youtubeurl);//추출 word를  string으로 변환하여 저장
            pos += onerecipeyoutubelength;
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
        /*while(true){
            try {
                System.out.println("!!!!");
                if (bis.read()==-1) break;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }*/
        pos = 2;//pos변수 다시 2로 초기화
    }

    //----------------- 이밑은 이밴트 핸들러 메소드
    public void handleresetbtnAction(ActionEvent event) {

        //현재 문제점 : 10번쯤 천천히 클릭하면  1분정도 먹통되는 현상 빈번하게 발생함. (완전히 멈추진 않음)

        Thread thread = new Thread() {//버튼 눌렀을때 통신과 ui변경담당 스레드
            @Override
            public void run() {
                System.out.println("새로고침버튼클릭");
                //서버에게 새로고침요리목록 패킷요청
                //proto= new Protocol(0,0);
                proto = new Protocol(TYPE_REQUEST, CODE_RESET_RECOMMENDFOOD);

                try {
                    bos.write(proto.getPacket());
                    bos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //요리목록패킷 받아서 사진과 이미지 변경 , 받는패킷의 목록순서 날씨음식1,2 계절음식 1,2
                try {
                    buf = proto.getPacket(1, 0);
                    System.out.println(bis.read(buf));//수신버퍼 읽기시도후 실제 읽은 바이트수 출력하고 buf배열에 읽은것 저장
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int type = buf[0]; //타입
                int code = buf[1]; //코드
                pos = 2;
                for (int i = 0; i < 4; i++) {//전송받은 요리 4개 추출해 저장
                    //1개요리 이름길이 추출
                    int onerecipenamelength = proto.byteArrayToInt(Arrays.copyOfRange(buf, pos, pos + 4));
                    pos += 4;
                    //1개요리 이름길이만큼 읽어서 추출후 string 변환하여 요리이름배열에저장
                    byte[] name = Arrays.copyOfRange(buf, pos, pos + onerecipenamelength);//추출한길이만큼읽어 코드추출
                    try {
                        recommendname[i] = new String(name, "UTF-8");//추출 이름 String 변환해 저장
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    System.out.println(recommendname[i]);
                    pos += onerecipenamelength;
                    //1개요리 이미지 url 길이 추출
                    int onerecipeurllength = proto.byteArrayToInt(Arrays.copyOfRange(buf, pos, pos + 4));
                    pos += 4;
                    //1개요리 이미지 url길이만큼 읽어서 추출후 string 변환하여 요리이미지배열에 저장
                    byte[] url = Arrays.copyOfRange(buf, pos, pos + onerecipeurllength);//추출한길이만큼읽어 코드추출
                    Imageurl[i] = new String(url);//추출 word를  string으로 변환하여 저장
                    System.out.println(Imageurl[i]);
                    pos += onerecipeurllength;
                    //1개요리 유튜브 링크 추출
                    int onerecipeyoutubelength = proto.byteArrayToInt(Arrays.copyOfRange(buf, pos, pos + 4));
                    pos += 4;
                    //1개요리 유튜브 url길이만큼 읽어서 추출후 string 변환하여 요리유튜브배열에 저장
                    byte[] youtubeurl = Arrays.copyOfRange(buf, pos, pos + onerecipeyoutubelength);//추출한길이만큼읽어 코드추출
                    youtubeLink[i] = new String(youtubeurl);//추출 word를  string으로 변환하여 저장
                    pos += onerecipeyoutubelength;
                }
                //받은 요리의 이름과 이미지 url이용해 ui 정보 변경
                Platform.runLater(() -> wetherrecommend1.setText(recommendname[0]));
                Platform.runLater(() -> wetherImage1.setImage(new Image(Imageurl[0])));
                Platform.runLater(() -> wetherrecommend2.setText(recommendname[1]));
                Platform.runLater(() -> wetherImage2.setImage(new Image(Imageurl[1])));
                Platform.runLater(() -> seasonrecommend1.setText(recommendname[2]));
                Platform.runLater(() -> seasonImage1.setImage(new Image(Imageurl[2])));
                Platform.runLater(() -> seasonrecommend2.setText(recommendname[3]));
                Platform.runLater(() -> seasonImage2.setImage(new Image(Imageurl[3])));
                //수신버퍼 읽을것 없을때까지 읽어서 버퍼비우기(1번째 통신이후 수신버퍼 읽을때 쓰레기값 방지위해)
                /*while(true){ //쓰레기값 들어올 시 수정해서 사용해보기
                    try {
                        if (bis.read()==-1) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }*/
                pos = 2;

            }

        };
        thread.setDaemon(true);
        thread.start();
    }

    public void handlewether1YoutubebtnAction(ActionEvent event) {//wetherrecomment1 youtube영상 popup창
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("YoutubeView.fxml"));
            Parent root = (Parent) loader.load();
            YoutubeViewController ycontroller = loader.<YoutubeViewController>getController();
            ycontroller.initYouData(youtubeLink[0]);
            Scene scene = new Scene(root);
            Stage ystage = new Stage();
            ystage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    System.out.println("종료");
                    ycontroller.webEngine.load(null);
                }
            });
            ystage.setScene(scene);
            ystage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handlerSetWetherImage1Action(MouseEvent event) {//이미지 선택시 선택이미지 창으로 이동
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("SelectRecipeInfo.fxml"));
            Stage stage = new Stage();

            Parent selectrecipe = loader.load();
            Scene scene = new Scene(selectrecipe);
            SelectRecipeInfoController sController = loader.getController();//선택요리정보 넘겨주기위해 컨트롤러 가져와 초기화
            sController.initData(wetherrecommend1.getText());
            stage.setScene(scene);
            stage.showAndWait();


        } catch (Exception e) {
            e.printStackTrace();
        }
        //wetherImage1.setImage(new Image("https://recipe1.ezmember.co.kr/cache/recipe/2022/02/16/8e34b759f6386912756c9a0f9d2255f91.jpg"));
    }

    public void handlerSetWetherImage2Action(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("SelectRecipeInfo.fxml"));
            Stage stage = new Stage();
            Parent selectrecipe = loader.load();
            Scene scene = new Scene(selectrecipe);
            SelectRecipeInfoController sController = loader.getController();//선택요리정보 넘겨주기위해 컨트롤러 가져와 초기화
            sController.initData(wetherrecommend2.getText());
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handlerSetSeasonImage1Action(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("SelectRecipeInfo.fxml"));
            Stage stage = new Stage();
            Parent selectrecipe = loader.load();
            Scene scene = new Scene(selectrecipe);
            SelectRecipeInfoController sController = loader.getController();//선택요리정보 넘겨주기위해 컨트롤러 가져와 초기화
            sController.initData(seasonrecommend1.getText());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handlerSetSeasonImage2Action(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("SelectRecipeInfo.fxml"));
            Stage stage = new Stage();
            //Stage stage=(Stage)splitpane.getScene().getWindow(); 그냥 빈 new Window 는 wait 와 hide가능한데 특정 fxml 루트컨테이너에 맞춘 stage는 wait메소드호출시 에러
            Parent selectrecipe = loader.load();
            Scene scene = new Scene(selectrecipe);
            SelectRecipeInfoController sController = loader.getController();//선택요리정보 넘겨주기위해 컨트롤러 가져와 초기화
            sController.initData(seasonrecommend2.getText());
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}