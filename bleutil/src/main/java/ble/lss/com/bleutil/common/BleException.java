package ble.lss.com.bleutil.common;

/**
 * Created by admin on 2018/4/10.
 */

public class BleException extends Exception{
    public static int ERR_NO_BLE = 1;
    public static int ERR_SCAN = 2;
    public static int ERR_CONN = 3;
    public static int ERR_DISCOVER = 4;
    public static int ERR_READ = 5;
    public static int ERR_WRITE = 6;
    public static int ERR_DISCONNECT = 7;
    public static int ERR_CANCELD = 8;
    public static int ERR_TIMEOUT = 9;
    public static int ERR_AdvertiseStart = 10;
    public static int ERR_AdvertiseAlreadyStart = 11;
    public int bleErrCode = 0;
    public BleException(int code){
        this(code,"ble error");
    }
    public BleException(int code,String msg){
        super(msg+":"+code);
        this.bleErrCode = code;
    }
}
