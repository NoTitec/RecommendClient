package Protocol;

public class Protocol {

    /*
    Type:1바이트 (요청 or 응답 or ..)
    Code:1바이트 (기능 분류 번호)
    BODYLEN:4바이트
    BODY:3000바이트
     */


    //패킷의 종류를 변수로 지정함 (패킷을 읽을때(getPacket) 사용한다.)

    //Type :  현재 0번은 요청이고 1번은 응답이다.
    public static final int TYPE_REQUEST = 0;
    public static final int TYPE_RESPONSE = 1;



    //Code : 현재 0번은 추천음식관련 기능이고, 추후에 기능이 추가되면 1번부터 순차적으로 할당하도록 한다.
    public static final int CODE_RECOMMENDFOOD = 0;



    //헤더의 각 요소의 크기를 변수로 지정함 (패킷을 생성할 때 크기를 지정할 때 숫자대신 변수명을 사용한다)
    public static final int LEN_PROTOCOL_TYPE = 1;
    public static final int LEN_PROTOCOL_CODE = 1;
    public static final int LEN_PROTOCOL_BODYLEN = 4;
    public static final int LEN_BODY = 3000;



    //헤더의 각 요소를 int형 멤버변수로 가지면 관리하기 편해서 선언한다.
    protected int protocolType;
    protected int protocolCode;
    protected int protocolBodyLen; //데이터의 실제 길이 정보


    private byte[] packet;



    public Protocol(int protocolType, int protocolCode){
        this.protocolType = protocolType;
        this.protocolCode = protocolCode;
        getPacket(protocolType, protocolCode); //getPacket의 리턴값은 버린다
    }

    public byte[] getPacket(int protocolType, int protocolCode){ //타입과 코드를 보고 스위치문을 타고 크기에 맞게 적절한 패킷을 생성함

        if(packet == null){

            switch (protocolCode){

                case CODE_RECOMMENDFOOD:

                    switch (protocolType){

                        case TYPE_REQUEST:

                            //음식 추천 요청 패킷 생성 (클라이언트 -> 서버)
                            packet = new byte[LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE + LEN_PROTOCOL_BODYLEN];
                            // 원래 클라이언트의 위치정보를 저장해야해서 LEN_BODY가 추가되야하지만 지금은 그냥 데이터 없는 패킷을 보내는것으로 구현함.
                            break;

                            //음식 추천 응답 패킷 생성 (서버 -> 클라이언트)
                        case TYPE_RESPONSE:
                            packet = new byte[LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE + LEN_PROTOCOL_BODYLEN + LEN_BODY];
                            //데이터 부분에는 음식 정보가 저장되야함.
                            break;

                    }

                //추후 기능이 추가되면 여기서 추가한다.


            }
        }

        packet[0] = (byte)protocolType; //패킷의 타입 지정
        packet[LEN_PROTOCOL_TYPE] = (byte)protocolCode; //패킷의 코드 지정
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

        System.arraycopy(tmp, 0, packet, LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE, LEN_PROTOCOL_BODYLEN); //BODYLEN 부분 입력
        System.arraycopy(data.trim().getBytes(), 0, packet, LEN_PROTOCOL_TYPE + LEN_PROTOCOL_CODE + LEN_PROTOCOL_BODYLEN, data.trim().getBytes().length); //데이터 부분 입력
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
