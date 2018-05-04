package ble.lss.com.bleutil.common;

import android.content.Context;
import android.util.TypedValue;

public class HexTrans {
	public static int dip2px(Context context,float dipValue){
//	     final float scale=context.getResources().getDisplayMetrics().density;
//	     return (int)(dipValue*scale+0.5f);
		float px=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, context.getResources().getDisplayMetrics());
		return (int)px;
	}

	public static int sp2px(Context context,float spValue){
//	     final float scale=context.getResources().getDisplayMetrics().density;
//	     return (int)(dipValue*scale+0.5f);
		float px=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
		return (int)px;
	}

	public static int px2dp(Context context,float pxValue){
//	    final float scale = context.getResources().getDisplayMetrics().density; 
//	    return (int)(pxValue/scale+0.5f);
	    float dp=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX,pxValue, context.getResources().getDisplayMetrics());
	    return (int)dp;
	}
	public static int getUnsignedByte(byte data){
		return data&0x0FF ;
	}
	public static int getUnsignedShort (short data){      //将data字节型数据转换为0~65535 (0xFFFF 即 WORD)。
		return data&0x0FFFF ;
	}
	public static long getUnsignedInt (int data){     //将int数据转换为0~4294967295 (0xFFFFFFFF即DWORD)。
		return data&0x0FFFFFFFF ;
	}
	public static byte[] intToByte(int data){
		byte[] bytes=new byte[4];
		bytes[0] = (byte)( data      & 0xff);
		bytes[1] = (byte)((data>>8)  & 0xff);
		bytes[2] = (byte)((data>>16) & 0xff);
		bytes[3] = (byte)((data>>24) & 0xff);
		return bytes;
	}
	public static int parseFromByte(byte[] bytes){
		int data=0;
		for(int i=bytes.length-1;i >=0 ; i--){
			data = (data<<8 | bytes[i]&0xff);
		}
		return data;
	}
	public static String byteArr2Str(byte[] bytes){
		if(bytes==null){
			return "";
		}
		String[] STRARR={"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
		String str = "";
		for(int i=0;i<bytes.length;i++){
			str+= STRARR[(bytes[i]&0xf0)>> 4];
			str+= STRARR[bytes[i]&0x0f];
		}
		return str;
	}
	public static byte[] hexStr2ByteArr(String hexStr) {
		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toUpperCase().toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int n;
		for (int i = 0; i < bytes.length; i++) {
			n = str.indexOf(hexs[2 * i]) * 16;
			n += str.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (n & 0xff);
		}
		return bytes;
	}
}
