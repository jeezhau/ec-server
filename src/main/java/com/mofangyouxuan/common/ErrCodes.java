package com.mofangyouxuan.common;

/**
 * 系统的错误常量，都为负值
 * 使用6位负数表示，前三位为功能码，后三位为具体的错误码
 * @author jeekhan
 *
 */
public class ErrCodes {
	
	//通用功能码：100
	public static int COMMON_EXCEPTION = -100001; //出现系统异常
	public static int COMMON_DB_ERROR = -100002;	//数据库处理失败
	
	//用户管理功能码：101
	public static int USER_PARAM_ERROR = -101001;	//参数格式不正确
	public static int USER_NO_EXISTS = -101002;	//系统中没有该用户
	public static int USER_NOT_REVIEW_ADMIN = -101003;	//不是系统审核管理员
	
	
	//推广二维码功能码：102
	public static int QRCODE_ERROR = -102001; //生成二维码失败
	
	//会员管理功能码：103
	public static int VIP_NO_USER = -103001;		//系统中没有该会员或未激活
	
	//合作伙伴管理功能码：104
	public static int PARTNER_NO_EXISTS = -104001;		//还没开通合作伙伴
	public static int PARTNER_PARAM_ERROR = -104002;	//参数有错
	public static int PARTNER_STATUS_ERROR = -104003;	//参数有错
	
	
}
