package com.example.recommendclient;

public class Protocol {
    //Type
    public static final int TYPE_REQUEST = 0;
    public static final int TYPE_RESPONSE = 1;
    public static final int TYPE_RESPONSE_ERROR = 2; //무언가 실패 했을 때 보냄 (필요할때만 사용하기)

    //Code
    public static final int CODE_RECOMMENDFOOD = 0;
    public static final int CODE_LOGIN = 1;
    public static final int CODE_SIGNUP = 2;
    public static final int CODE_RESET_RECOMMENDFOOD=3;// 최초 code_recommendfood일 때 위도경도보내고 이후 새로고침에는 위도경도를 서버가 저장하므로 이코드로 새로운 요리 요청
    public static final int CODE_DETAIL_FOOD_INFO=4;
    public static final int CODE_COMMENT_LEAVE=5;
    //Protocol Length
    public static final int LEN_PROTOCOL_TYPE = 1;
    public static final int LEN_PROTOCOL_CODE = 1;
    public static final int LEN_PROTOCOL_BODY = 10000;

    // 중요!!! *****헤더에 길이정보를 저장하는 구 방식에서 바디에 길이정보를 같이 저장하는 신 방식으로 변경함.******
    // 헤더에는 TYPE과 CODE만 저장함

    protected int protocolType;
    protected int protocolCode;
    protected int protocolBodyLen; //데이터의 실제 길이 정보

    private byte[] packet;

    public Protocol(){//기본생성자

    }
    public Protocol(int protocolType, int protocolCode){
        this.protocolType = protocolType;
        this.protocolCode = protocolCode;
        getPacket(protocolType, protocolCode);
    }

    public byte[] getPacket(int protocolType, int protocolCode){//type ,code순으로 보내는데 판단을 왜 코드,타입순으로하는지 모르겠음 가독성 저하

            switch (protocolCode){

                case CODE_RECOMMENDFOOD://code가 추천코드인경우

                    switch (protocolType){

                        case TYPE_REQUEST:
                            packet = new byte[LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE + LEN_PROTOCOL_BODY]; // 지금은 위치 정보 안보낸다고 가정
                            break;

                        case TYPE_RESPONSE:
                            packet = new byte[LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE + LEN_PROTOCOL_BODY];
                            break;

                    }


                    break;

                case CODE_COMMENT_LEAVE:
                case CODE_DETAIL_FOOD_INFO:
                    switch (protocolType){
                        case TYPE_RESPONSE:
                        case TYPE_REQUEST:
                            packet=new byte[LEN_PROTOCOL_TYPE+LEN_PROTOCOL_CODE+LEN_PROTOCOL_BODY];
                            break;
                    }
                    break;
                case CODE_RESET_RECOMMENDFOOD:// code 가 새로운 추천인경우

                    switch (protocolType){
                        case TYPE_REQUEST:
                            packet=new byte[LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE];
                            break;
                        case TYPE_RESPONSE:
                            packet=new byte[LEN_PROTOCOL_TYPE+LEN_PROTOCOL_CODE+LEN_PROTOCOL_BODY];
                            break;
                    }
                    break;
                case CODE_LOGIN:

                    switch (protocolType){

                        case TYPE_REQUEST:
                            packet = new byte[LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE + LEN_PROTOCOL_BODY];
                            break;

                        case TYPE_RESPONSE: //응답 (로그인 성공)
                            packet = new byte[LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE];
                            break;

                        case TYPE_RESPONSE_ERROR: //응답 (로그인 실패(없는계정))
                            packet = new byte[LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE];
                            break;
                    }

                    break;
                    
                case CODE_SIGNUP:
                    
                    switch (protocolType){

                        case TYPE_REQUEST:
                            packet = new byte[LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE + LEN_PROTOCOL_BODY];
                            break;

                        case TYPE_RESPONSE: //응답 (회원가입 성공)
                            packet = new byte[LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE];
                            break;

                        case TYPE_RESPONSE_ERROR: //응답 (회원가입 실패 (중복아이디 존재))
                            packet = new byte[LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE];
                            break;
                            
                    }
                    break;

            }


        packet[0] = (byte)protocolType;
        packet[1] = (byte)protocolCode;
        return packet;

    }

    public int getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(int protocolType) {
        this.protocolType = protocolType;
        packet[0] = (byte)protocolType;
    }

    public void setProtocolCode(int protocolCode){
        this.protocolCode = protocolCode;
        packet[LEN_PROTOCOL_TYPE] = (byte) protocolCode;
    }

    public int getProtocolCode(){return protocolCode;}

    public int getProtocolBodyLen(){
        return protocolBodyLen;
    }

    public byte[] getPacket(){
        return packet;
    }

    public void setPacket(int protocolType, int protocolCode, byte[] buf){
        packet = null;
        packet = getPacket(protocolType, protocolCode);
        this.protocolType = protocolType;
        this.protocolCode = protocolCode;

        System.arraycopy(buf,0,packet,0,packet.length);

    }

    public String getData(){ //데이터를 String으로 반환합니다.
        return new String(packet, LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE, protocolBodyLen).trim();
    }

    public byte[] getByteData(){ //데이터를 byte배열로 반환합니다.
        byte[] tmp = new byte[protocolBodyLen];
        System.arraycopy(packet, LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE, tmp, 0, protocolBodyLen);
        return tmp;
    }


    public void setData(String data){
        byte[] tmp = intToByteArray(data.length());
        protocolBodyLen = data.length();
        tmp = intToByteArray(protocolBodyLen);

        System.arraycopy(data.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE, data.trim().getBytes().length);
    }

    public void setByteData(byte[] data, int size){

        // = size; 이 두 줄은 왜있는것?
        //byte[] tmp = intToByteArray(size);
        System.arraycopy(data, 0, packet, LEN_PROTOCOL_TYPE+LEN_PROTOCOL_CODE, size);

    }



    public static byte[] intToByteArray(int value) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte)(value >> 24);
        byteArray[1] = (byte)(value >> 16);
        byteArray[2] = (byte)(value >> 8);
        byteArray[3] = (byte)(value);
        return byteArray;
    }

    public static int byteArrayToInt(byte bytes[]) {
        return ((((int)bytes[0] & 0xff) << 24) |
                (((int)bytes[1] & 0xff) << 16) |
                (((int)bytes[2] & 0xff) << 8) |
                (((int)bytes[3] & 0xff)));
    }


}
