package com.iplant;

public class Constant {
	//KEY映射
	//账号，用于存放账户信息
	public static final String KEY_ACCOUNT = "account";
	//地址，用于intent传递显示地址
	public static final String KEY_URL 	   = "webUrl";
	
	//地址栏
	public static final String DOMAIN = "http://121.40.188.141:8091/"; 
	//file:///android_asset/test.html
	
	//刷新频率
	//版本检测
	public static final int TICKS_UPDATE_VERSION = 10 * 60 * 1000; //10分钟
	//消息
	public static final int TICKS_UPDATE_GROUP   = 10 * 1000; //10秒
	
}
