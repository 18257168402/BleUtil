
package ble.lss.com.bleutil.common;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	public static String getUsernameFromPlatformName(String name){
		/*if(name.contains("#")){
			String[] names=name.split("\\#");
			if(names.length==3){
				name=names[2];
			}
			if(names.length==2){
				name=names[1];
			}
		}*/
		int index=0;
		if(name.contains("#")){
			for(int i=0;i<name.length();i++){
				if(name.charAt(i)=='#'){
					index = i;
				}
			}
			name=name.substring(index+1,name.length());
		}
		return name;
	}
	public static boolean checkFileEnd(String filename,String[] filter){
		if(filter==null||filter.length==0){
			return true;
		}
		for (String end:filter){
			if(filename.endsWith(end)){
				return true;
			}
		}
		return false;
	}
	public static boolean isNotEmpty(String obj) {
		if (obj == null) {
			return false;
		}

		if (obj.trim().length() == 0) {
			return false;
		}

		if (obj.equalsIgnoreCase("null")) {
			return false;
		}
		return true;
	}
	public static byte[] getBytesFromAssert(Context context,String file, int maxLen){
		try {
			InputStream is= context.getAssets().open(file);
			byte[] bytes=new byte[maxLen];
			int d=is.read(bytes);
			return bytes;
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}

	}

	public static String email2LowerCase(String email )
	{
		/*String[] all = email.split("@");
		if(all.length == 1 || all.length != 2 ){
			return null ;
		}*/
//		char[] data = email.toCharArray();
//		int dist = 'a' - 'A';
//		for (int i = 0 ; i < email.length(); i++)
//		{
//			if (data[i] >= 'A' && data[i] <= 'Z')
//			{
//				data[i] += dist;
//			}
//		}

		return email.toLowerCase();

	}


	public static boolean checkPsw(String s){
		if(isNotEmpty(s)){
			return s.matches("\\w{6,18}");
		}else{
			return false ;
		}
	}
	public static boolean checkSecurityPsw(String s){
		if(isNotEmpty(s)){
			return s.matches(".{6,32}");
		}else{
			return false ;
		}
	}

	
	public static boolean checkPswRe(String psw,String pswre){
		
		if(isNotEmpty(psw) && isNotEmpty(pswre) ){
		    return	psw.equals(pswre);
		}else{
			return false ;
		}

	}

	public static boolean isEmail(String s){
		if(TextUtils.isEmpty(s)){
			return false;
		}else{
			return s.matches("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
		}

	}

	public static boolean isMobileNO(String mobiles) {
/*
移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
联通：130、131、132、152、155、156、185、186
电信：133、153、180、189、（1349卫通）/^0?1[3|4|5|7|8][0-9]\d{8}$/
总结起来就是第一位必定为1，第二位必定为3或5或8或7（电信运营商），其他位置的可以为0-9
*/
		String telRegex = "[1][34578]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
		if (TextUtils.isEmpty(mobiles))
			return false;
		else
			return mobiles.matches(telRegex);
	}


	/*

	 *130/131/132/155/156/185/186/145/176
	 *133/153/180/181/189/177
	 *134/135/136/137/138/139/150/151/152/157/158/159/182/183/184/187/188/147/178
	 * 
	 * 13 0-9
	 * 14 5 7
	 * 15 0-3 5-8
	 * 17 6-8
	 * 18 0-9
	 */
	public static boolean checkPhoneNumber(String s){
		if(isNotEmpty(s)){
			//LogUtils.e("login", "checkPhoneNumber  " + s);
			//boolean bret=s.matches("^((13[0-9])|(14[5,7])|(15[^4,\\D])|(17[6-8])|(18[0-9]))\\d{8}$");
			for (char c:s.toCharArray()){
				if(c<'0'||c>'9'){
					return false;
				}
			}
			//LogUtils.e("login", "checkPhoneNumber res ");
			return true;
		}else{
			return false ;
		}
	}
	
	public static boolean checkVerificationCode(String s){
		if(isNotEmpty(s)){
			return s.matches("\\d{6}");
		}else{
			return false ;
		}
		
		
	}

	public static List<String> splitWithChar(String src,char c){
		List<String> strlist=new ArrayList<String>();
		String str=new String();
		for(int i=0;i<src.length();i++){
			if(src.charAt(i)==c){
				strlist.add(str);
				str=new String();
			}else{
				str=str+String.valueOf(src.charAt(i));
			}
		}
		strlist.add(str);
		return strlist;
	}
	public static String StringFilter(String src,String regEx){
		Pattern pattern=Pattern.compile(regEx);
		Matcher matcher= pattern.matcher(src);
		String[] strArr=pattern.split(src);
		for (int i=0;i<strArr.length;i++){
			LogUtils.e("filter","strArr "+strArr[i]);
		}
		LogUtils.e("filter","src "+matcher.toString());
		LogUtils.e("filter","result1 "+matcher.toString());
		LogUtils.e("filter","result2 "+matcher.replaceAll("")+"  count "+matcher.groupCount());
//		for (int i=0;i<matcher.groupCount();i++){
//			LogUtils.e("filter","result2while "+matcher.g);
//		}
		return matcher.replaceAll("");
	}
	public static String specielFilter(String src){
		String regEx="[~`!@#$%\\^&*\\(\\)_+=-\\?<>,.;:'\"\n\t\\|\\\\/\\[\\]\\{\\}]";
		return filter(src,regEx);
	}
	public static String filter(String src,String regEx){
		Pattern pattern=Pattern.compile(regEx);
		Matcher matcher =pattern.matcher(src);
		return matcher.replaceAll("");
	}


	/*********************************** 身份证验证开始 ****************************************/
	/**
	 * 身份证号码验证 1、号码的结构 公民身份号码是特征组合码，由十七位数字本体码和一位校验码组成。排列顺序从左至右依次为：六位数字地址码，
	 * 八位数字出生日期码，三位数字顺序码和一位数字校验码。 2、地址码(前六位数）
	 * 表示编码对象常住户口所在县(市、旗、区)的行政区划代码，按GB/T2260的规定执行。 3、出生日期码（第七位至十四位）
	 * 表示编码对象出生的年、月、日，按GB/T7408的规定执行，年、月、日代码之间不用分隔符。 4、顺序码（第十五位至十七位）
	 * 表示在同一地址码所标识的区域范围内，对同年、同月、同日出生的人编定的顺序号， 顺序码的奇数分配给男性，偶数分配给女性。 5、校验码（第十八位数）
	 * （1）十七位数字本体码加权求和公式 S = Sum(Ai * Wi), i = 0, ... , 16 ，先对前17位数字的权求和
	 * Ai:表示第i位置上的身份证号码数字值 Wi:表示第i位置上的加权因子 Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2
	 * （2）计算模 Y = mod(S, 11) （3）通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0 X 9 8 7 6 5 4 3 2
	 */

	/**
	 * 功能：身份证的有效验证
	 *
	 * @param IDStr 身份证号
	 * @return 有效：返回"" 无效：返回String信息
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	private static String IDCardValidate(String IDStr) throws ParseException {
		String errorInfo = "";// 记录错误信息
		String[] ValCodeArr = {"1", "0", "x", "9", "8", "7", "6", "5", "4",
				"3", "2"};
		String[] Wi = {"7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7",
				"9", "10", "5", "8", "4", "2"};
		String Ai = "";
		// ================ 号码的长度 15位或18位 ================
		if (IDStr.length() != 15 && IDStr.length() != 18) {
			errorInfo = "身份证号码长度应该为15位或18位。";
			return errorInfo;
		}
		// =======================(end)========================

		// ================ 数字 除最后以为都为数字 ================
		if (IDStr.length() == 18) {
			Ai = IDStr.substring(0, 17);
		} else if (IDStr.length() == 15) {
			Ai = IDStr.substring(0, 6) + "19" + IDStr.substring(6, 15);
		}
		if (isNumeric(Ai) == false) {
			errorInfo = "身份证15位号码都应为数字 ; 18位号码除最后一位外，都应为数字。";
			return errorInfo;
		}
		// =======================(end)========================

		// ================ 出生年月是否有效 ================
		String strYear = Ai.substring(6, 10);// 年份
		String strMonth = Ai.substring(10, 12);// 月份
		String strDay = Ai.substring(12, 14);// 月份
		if (isDate(strYear + "-" + strMonth + "-" + strDay) == false) {
			errorInfo = "身份证生日无效。";
			return errorInfo;
		}
		GregorianCalendar gc = new GregorianCalendar();
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
		try {
			if ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) > 150
					|| (gc.getTime().getTime() - s.parse(
					strYear + "-" + strMonth + "-" + strDay).getTime()) < 0) {
				errorInfo = "身份证生日不在有效范围。";
				return errorInfo;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		if (Integer.parseInt(strMonth) > 12 || Integer.parseInt(strMonth) == 0) {
			errorInfo = "身份证月份无效";
			return errorInfo;
		}
		if (Integer.parseInt(strDay) > 31 || Integer.parseInt(strDay) == 0) {
			errorInfo = "身份证日期无效";
			return errorInfo;
		}
		// =====================(end)=====================

		// ================ 地区码时候有效 ================
		Hashtable h = GetAreaCode();
		if (h.get(Ai.substring(0, 2)) == null) {
			errorInfo = "身份证地区编码错误。";
			return errorInfo;
		}
		// ==============================================

		// ================ 判断最后一位的值 ================
		int TotalmulAiWi = 0;
		for (int i = 0; i < 17; i++) {
			TotalmulAiWi = TotalmulAiWi
					+ Integer.parseInt(String.valueOf(Ai.charAt(i)))
					* Integer.parseInt(Wi[i]);
		}
		int modValue = TotalmulAiWi % 11;
		String strVerifyCode = ValCodeArr[modValue];
		Ai = Ai + strVerifyCode;

		if (IDStr.length() == 18) {
			if (Ai.equals(IDStr) == false) {
				errorInfo = "身份证无效，不是合法的身份证号码";
				return errorInfo;
			}
		} else {
			return "";
		}
		// =====================(end)=====================
		return "";
	}

	/**
	 * 功能：设置地区编码
	 *
	 * @return Hashtable 对象
	 */
	@SuppressWarnings("unchecked")
	private static Hashtable GetAreaCode() {
		Hashtable hashtable = new Hashtable();
		hashtable.put("11", "北京");
		hashtable.put("12", "天津");
		hashtable.put("13", "河北");
		hashtable.put("14", "山西");
		hashtable.put("15", "内蒙古");
		hashtable.put("21", "辽宁");
		hashtable.put("22", "吉林");
		hashtable.put("23", "黑龙江");
		hashtable.put("31", "上海");
		hashtable.put("32", "江苏");
		hashtable.put("33", "浙江");
		hashtable.put("34", "安徽");
		hashtable.put("35", "福建");
		hashtable.put("36", "江西");
		hashtable.put("37", "山东");
		hashtable.put("41", "河南");
		hashtable.put("42", "湖北");
		hashtable.put("43", "湖南");
		hashtable.put("44", "广东");
		hashtable.put("45", "广西");
		hashtable.put("46", "海南");
		hashtable.put("50", "重庆");
		hashtable.put("51", "四川");
		hashtable.put("52", "贵州");
		hashtable.put("53", "云南");
		hashtable.put("54", "西藏");
		hashtable.put("61", "陕西");
		hashtable.put("62", "甘肃");
		hashtable.put("63", "青海");
		hashtable.put("64", "宁夏");
		hashtable.put("65", "新疆");
		hashtable.put("71", "台湾");
		hashtable.put("81", "香港");
		hashtable.put("82", "澳门");
		hashtable.put("91", "国外");
		return hashtable;
	}

	/**
	 * 功能：判断字符串是否为数字
	 *
	 * @param str
	 * @return
	 */
	private static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (isNum.matches()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 功能：判断字符串是否为日期格式
	 *
	 * @param
	 * @return
	 */
	public static boolean isDate(String strDate) {
		Pattern pattern = Pattern
				.compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1-2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$");
		Matcher m = pattern.matcher(strDate);
		if (m.matches()) {
			return true;
		} else {
			return false;
		}
	}
	/*********************************** 身份证验证结束 ****************************************/

	public static boolean isCardID(String str){
		boolean ret = false;
		try{
			ret = TextUtils.isEmpty(IDCardValidate(str));
		}catch (Exception e){
			ret = false;
		}
		return ret;
	}

	public static boolean isChinese(String str){
		String regEx=".*([\\u4e00-\\u9fa5]+).*";//连续的任意字符后有一个或者多个汉字然后又是连续的任意字符...
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		boolean bres=m.matches();
		if(bres){
			LogUtils.e("regex","str:"+str+" matches "+bres);
		}
		return bres;
	}
	public static String[] sortByDesc(String[] name){
		String lang = Locale.getDefault().getLanguage();
		Comparator comp = null;
		if(lang.equals("zh")){
			comp = Collator.getInstance(Locale.ENGLISH);
		}
		else if(lang.equals("ja")){
			comp = Collator.getInstance(Locale.JAPAN);
		}
		else if(lang.equals("en")){
			comp = Collator.getInstance(Locale.ENGLISH);
		}
		else {
			comp = Collator.getInstance(Locale.ENGLISH);
		}
		Arrays.sort(name, comp);
		return name;
	}
	/**
	 * 密码强度
	 *
	 *  Z = 字母 S = 数字
	 *  @return 0 low 1 middlw 2 high
	 */
	public static int checkPswStrengh(String psw){
		String regexZ = "\\d*";
		String regexS = "[a-zA-Z]+";
		String regexZS = "\\w*";
		if(psw.length()<6){
			return 0;
		}
		if(psw.matches(regexZ)&&psw.length()<=12&&psw.length()>=6){
			return 0;
		}
		else if(psw.matches(regexZ)&&psw.length()>12){
			return 1;
		}
		else if(psw.matches(regexS)&&psw.length()<=12&&psw.length()>=6){
			return 0;
		}
		else if(psw.matches(regexS)&&psw.length()>12){
			return 1;
		}
		else if(psw.matches(regexZS)&&psw.length()>=12){
			return 2;
		}
		if(psw.matches(regexZS)&&psw.length()<=12&&psw.length()>=6){
			return 1;
		}
		return 0;
	}
	public static String formatJson(String json){
		if(!isNotEmpty(json)){
			return "";
		}
		String data;
		try {
			JSONObject obj=new JSONObject(json);
			String language= Locale.getDefault().getLanguage();
			data=obj.optString(language);
			if(!isNotEmpty(data)){
				data=obj.optString("en");
			}
			if(!isNotEmpty(data)){
				data=json;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return json;
		}
		return data;
	}
}
