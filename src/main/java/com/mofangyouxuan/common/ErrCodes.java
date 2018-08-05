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
	public static int COMMON_PARAM_ERROR = -100003;	//参数格式不正确
	public static int COMMON_PRIVILEGE_ERROR = -100004;	//权限不正确
	
	//用户管理功能码：101
	public static int USER_PARAM_ERROR = -101001;	//参数格式不正确
	public static int USER_NO_EXISTS = -101002;	//系统中没有该用户
	public static int USER_NOT_REVIEW_ADMIN = -101003;	//不是系统审核管理员
	public static int USER_VIP_VERICODE_ERROR = -101004;	//验证码出现错误
	
	
	//推广二维码功能码：102
	public static int QRCODE_ERROR = -102001; //生成二维码失败
	
	//会员管理功能码：103
	public static int VIP_NO_USER = -103001;		//系统中没有该会员或未激活
	public static int VIP_ACCOUNT_CNT_LIMIT = -103002;		//账户数量限制
	public static int VIP_ACCOUNT_NOT_EXISTS = -103003;		//账户不存在
	public static int VIP_ACCOUNT_SAME_NO = -103004;		//存在同账户账户
	
	//合作伙伴管理功能码：104
	public static int PARTNER_NO_EXISTS = -104001;	//合作伙伴不存在
	public static int PARTNER_PARAM_ERROR = -104002;	//参数有错
	public static int PARTNER_STATUS_ERROR = -104003;	//状态有错
	public static int PARTNER_CERT_IMAGE = -104004;	//证书有错
	public static int PARTNER_STAFF_EXISTS = -104005; //该用户已经存在
	
	//商品管理功能码：105
	public static int GOODS_NO_EXISTS = -105001;		//商品不存在
	public static int GOODS_NO_GOODS = -105002;	//没有商品信息
	public static int GOODS_PARAM_ERROR = -105003;	//参数格式不正确
	public static int GOODS_STATUS_ERROR = -105004;	//状态有错
	public static int GOODS_PRIVILEGE_ERROR = -105005;	//无权执行该操作
	public static int GOODS_CNT_LIMIT = -105006;	//数量限制
	
	//图库管理功能码：106
	public static int IMAGE_DIR_NO_EXISTS = -106001;		//目录不存在
	public static int IMAGE_PARAM_ERROR = -106002;	//参数有错
	public static int IMAGE_FOLDER_FILE_LIMIT = -106003; //单个目录文件数量限制
	public static int IMAGE_FOLDER_LEVEL_LIMIT = -106004; //目录层级限制
	public static int IMAGE_ALL_FILE_LIMIT = -106005; //总文件数量限制
	
	//运费模版管理功能码：107
	public static int POSTAGE_PARAM_ERROR = -107001;	//参数有错
	public static int POSTAGE_USING_NOW = -107002;	//模版正在使用中
	public static int POSTAGE_NO_EXISTS = -107003;	//没有该模版
	public static int POSTAGE_PRIVILEGE_ERROR = -107004;	//无权执行该操作
	public static int POSTAGE_NAME_USED = -107005;	//名称已被使用
	public static int POSTAGE_CNT_LIMIT = -107006;	//数量限制
	
	//收货人管理功能码：108
	public static int RECEIVER_PARAM_ERROR = -108001;	//参数有错
	public static int RECEIVER_NO_EXISTS = -108002;	//没有该记录
	public static int RECEIVER_PRIVILEGE_ERROR = -108003;	//无权执行该操作
	public static int RECEIVER_CNT_LIMIT = -108004;	//数量限制
	
	//订单管理功能码：109
	public static int ORDER_PARAM_ERROR = -109001;	//参数有错
	public static int ORDER_NO_EXISTS = -109002;	//没有该记录
	public static int ORDER_PRIVILEGE_ERROR = -109003;	//无权执行该操作
	public static int ORDER_FOR_PAY_CNT_ALL_LIMIT = -109004;	//待付款订单限制
	public static int ORDER_FOR_PAY_CNT_GOODS_LIMIT = -109005;	//待付款订单限制
	public static int ORDER_SEARCH_PARAM = -109006;	//查询条件有误
	public static int ORDER_BUY_LIMIT = -109007;		//限购
	public static int ORDER_STOCK_OVER = -109008;		//购买超过库存
	public static int ORDER_NO_POSTAGE = -109009;		//不支持配送
	public static int ORDER_ERROR_POSTAGE = -109010;	//该邮费模板不支持该地区配送
	public static int ORDER_STATUS_ERROR = -109011;	//状态不正确
	public static int ORDER_PAY_ERROR = -109012;	//支付失败
	
	//订单管理功能码：110
	public static int COLLECTION_OVER_LIMIT = -110001;	//超出限制
	
	
	//订单管理功能码：111
	public static int COMPLAIN_HAS_EXISTS = -111001;		//已有该订单或商品
	public static int COMPLAIN_NO_EXISTS = -111002;		//没有该记录
	public static int COMPLAIN_STATUS_ERROR = -111011;	//状态不正确	
		
	//提现申请：112
	public static int CASH_APPLY_HAS_REC = -112001;	//已有提现记录
	public static int CASH_APPLY_NO_FULL_BAL = -112002;	//已有提现记录
	
	//登录控制：113
	public static int LOGIN_ERRCNT_LIMIT = -113001;	//登录错误次数限制
	
}


