package com.mofangyouxuan.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.mofangyouxuan.common.ErrCodes;
import com.mofangyouxuan.common.PageCond;
import com.mofangyouxuan.mapper.LoginLogMapper;
import com.mofangyouxuan.model.LoginLog;
import com.mofangyouxuan.service.LoginLogService;

@Service
@Transactional
public class LoginLogServiceImpl implements LoginLogService{

	@Autowired
	private LoginLogMapper loginLogMapper;
	
	@Value("${sys.login-errcnt-limit}")
	private 	int 	loginErrCntLimit;
	
	@Override
	public JSONObject add(LoginLog log) {
		JSONObject jsonRet = new JSONObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currDate = sdf.format(new Date());
		int errn = 0;
		if("0".equals(log.getIsSucc())) {
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("userId", log.getUserId());
			if(log.getPartnerId() != null) {
				params.put("partnerId", log.getPartnerId());
			}
			params.put("beginCrtTime", currDate);
			params.put("isSuss", "0");
			errn = this.loginLogMapper.countAll(params);
			if(errn >= this.loginErrCntLimit) {
				jsonRet.put("errcode", ErrCodes.LOGIN_ERRCNT_LIMIT);
				jsonRet.put("errmsg", "登录错误次数达到上限，如果您忘记了密码，请使用忘记密码找回！");
				return jsonRet;
			}
		}
		log.setCrtTime(new Date());
		int cnt = this.loginLogMapper.insert(log);
		if(cnt <= 0) {
			jsonRet.put("errcode", ErrCodes.COMMON_DB_ERROR);
			jsonRet.put("errmsg", "数据保存错误！");
		}else {
			jsonRet.put("errcode", 0);
			jsonRet.put("errmsg", "ok");
			if("0".equals(log.getIsSucc())) {
				jsonRet.put("errmsg","您今天还有" + (this.loginErrCntLimit-errn-1) + "次尝试登录机会！");
			}
		}
		return jsonRet;
	}

	@Override
	public int countAll(Map<String, Object> params) {
		return this.loginLogMapper.countAll(params);
	}

	@Override
	public List<LoginLog> getAll(Map<String, Object> params, PageCond pageCond) {
		return this.loginLogMapper.selectAll(params, pageCond);
	}
	
}
