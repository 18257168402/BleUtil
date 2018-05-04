package ble.lss.com.bleutil.common;

import android.util.Log;

import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2016/4/23.
 */
public class LogUtils {
    private static boolean isInited=false;
    private static boolean isDebugBuild=false;
    public static void setDebug(boolean isDebugBuild){
        LogUtils.isDebugBuild=isDebugBuild;
    }
    public  static  void v(String tag,String msg){
        if(isDebugBuild)
        Log.v(tag, msg);
    }
    public  static void w(String tag,String msg){
        if(isDebugBuild)
        Log.w(tag, msg);
    }
    public  static void e(String tag,String msg){
        if(isDebugBuild)
        Log.e(tag, getCallPosInfo()+msg);
    }
    public  static  void i(String tag,String msg){
        if(isDebugBuild)
        Log.i(tag, msg);
    }
    public  static void d(String tag,String msg){
        if(isDebugBuild)
        Log.d(tag, msg);
    }
    public static String getCallPosInfo(){
        String info="";
        StackTraceElement[] traces=Thread.currentThread().getStackTrace();
        if(traces!=null){
            if(traces.length<=5){
                info="";
            }else{
                StackTraceElement traceItem = traces[4];
                info=getTraceInfo(traceItem);
            }
        }
        return info;
    }
    private static String getTraceInfo(StackTraceElement traceItem){
        if(traceItem == null){
            return "";
        }else{
            List<String> classNames= StringUtil.splitWithChar(traceItem.getClassName(),'.');
            String simpleClassName="";
            if(classNames!=null&&classNames.size()!=0){
                simpleClassName = classNames.get(classNames.size()-1);
            }
            return String.format(Locale.CHINA,"[%s %s::%s %d],",
                    traceItem.getFileName(),
                    simpleClassName,
                    traceItem.getMethodName(),
                    traceItem.getLineNumber());
        }
    }
}
