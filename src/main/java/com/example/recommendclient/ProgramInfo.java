package com.example.recommendclient;

import java.net.Socket;

/**
 * 프로그램의 개인 정보를 저장하는 클래스
 * 현재는 클라이언트의 소켓정보만 저장 추후 필요 정적데이터도 이 클래스에 할당
 */

public class ProgramInfo {
    public static Socket socket; //싱글톤 소켓-> ui 화면전환시 컨트롤러도 바뀌는데 static이아니면 매번 서버에 재연결요청하는문제해결

    public static boolean socketConnect=false;//소켓이 할당되어있는지 확인하는 변수

    public static void setSocket(Socket socket) {
        ProgramInfo.socket = socket;
    }

    public static void setSocketConnect(boolean socketConnect) {
        ProgramInfo.socketConnect = socketConnect;
    }


}
