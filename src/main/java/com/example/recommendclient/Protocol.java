package com.example.recommendclient;

public class Protocol {
    //Type
    public static final int TYPE_REQUEST = 0;
    public static final int TYPE_RESPONSE = 1;

    //Code
    public static final int CODE_RECOMMENDFOOD = 0;

    //Protocol Length
    public static final int LEN_PROTOCOL_TYPE = 1;
    public static final int LEN_PROTOCOL_CODE = 1;
    public static final int LEN_PROTOCOL_BODYLEN = 4;
    public static final int LEN_BODY = 3000;

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

    public byte[] getPacket(int protocolType, int protocolCode){

        if(packet == null){

            switch (protocolCode){

                case CODE_RECOMMENDFOOD:

                    switch (protocolType){

                        case TYPE_REQUEST:
                            packet = new byte[LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE + LEN_PROTOCOL_BODYLEN]; // 지금은 위치 정보 안보낸다고 가정
                            break;

                        case TYPE_RESPONSE:
                            packet = new byte[LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE + LEN_PROTOCOL_BODYLEN + LEN_BODY];
                            break;

                    }





            }
        }

        packet[0] = (byte)protocolType;
        packet[LEN_PROTOCOL_TYPE] = (byte)protocolType;
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

        byte tmp[] = new byte[4];
        System.arraycopy(buf, 2,tmp,0,4);
        this.protocolBodyLen = byteArrayToInt(tmp);

        System.arraycopy(buf,0,packet,0,packet.length);

    }

    public String getData(){ //데이터를 String으로 반환합니다.
        return new String(packet, LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE + LEN_PROTOCOL_BODYLEN, protocolBodyLen).trim();
    }

    public byte[] getByteData(){ //데이터를 byte배열로 반환합니다.
        byte[] tmp = new byte[protocolBodyLen];
        System.arraycopy(packet, LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE + LEN_PROTOCOL_BODYLEN, tmp, 0, protocolBodyLen);
        return tmp;
    }


    public void setData(String data){
        byte[] tmp = intToByteArray(data.length());
        protocolBodyLen = data.length();
        tmp = intToByteArray(protocolBodyLen);

        System.arraycopy(tmp, 0, packet, LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE, LEN_PROTOCOL_BODYLEN);
        System.arraycopy(data.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE + LEN_PROTOCOL_BODYLEN, data.trim().getBytes().length);
    }

    public void setByteData(byte[] data, int size){

        protocolBodyLen = size;
        byte[] tmp = intToByteArray(size);
        System.arraycopy(tmp, 0, packet, LEN_PROTOCOL_TYPE+LEN_PROTOCOL_CODE, LEN_PROTOCOL_BODYLEN);
        System.arraycopy(data, 0, packet, LEN_PROTOCOL_TYPE+LEN_PROTOCOL_CODE+LEN_PROTOCOL_BODYLEN, size);

    }



    public  byte[] intToByteArray(int value) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte)(value >> 24);
        byteArray[1] = (byte)(value >> 16);
        byteArray[2] = (byte)(value >> 8);
        byteArray[3] = (byte)(value);
        return byteArray;
    }

    public  int byteArrayToInt(byte bytes[]) {
        return ((((int)bytes[0] & 0xff) << 24) |
                (((int)bytes[1] & 0xff) << 16) |
                (((int)bytes[2] & 0xff) << 8) |
                (((int)bytes[3] & 0xff)));
    }


}
