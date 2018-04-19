package com.mofangyouxuan.common;

/**
 * 系统的错误常量，都为负值
 * 使用6位负数表示，前三位为功能码，后三位为具体的错误码
 * @author jeekhan
 *
 */
public class ErrCodes {
	
	//用户管理功能码：100
	public static int USER_EXCEPTION = -100000;	//出现系统异常
	public static int USER_PARAM_ERROR = -100001;	//参数格式不正确
	public static int USER_DB_ERROR = -100002;	//数据库处理失败
	public static int USER_NO_USER = -100003;		//系统中没有该用户
	
	//推广二维码功能码：101
	public static int QRCODE_EXCEPTION = -101000;	//出现系统异常
	public static int QRCODE_ERROR = -100001; //生成二维码失败
	

}
