package com.mofangyouxuan.interceptor;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.mofangyouxuan.utils.CommonUtil;

/**
 * 支付通知拦截
 * @author jeekhan
 *
 */
public class PayNotifyInterceptor extends HandlerInterceptorAdapter{
	Logger log = LoggerFactory.getLogger(PayNotifyInterceptor.class);
	private String[] wxWhiteIpArr= {"101.226.103.0/25","140.207.54.0/25","103.7.30.0/25","183.3.234.0/25","58.251.80.0/25"};
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
        String requestUri = request.getRequestURI();  
        String contextPath = request.getContextPath();  
        String url = requestUri.substring(contextPath.length());  
        String remoteIp = CommonUtil.getIpAddr(request);
        PrintWriter out = response.getWriter();
        if(url.startsWith("/wxpay/notify")) {
        		for(String whiteIpSeg:wxWhiteIpArr) {
        			if(CommonUtil.isValidIp(remoteIp, whiteIpSeg)) {
        				return true;
        			}
        		}
        		out.write("<xml><return_code>FAIL</return_code><return_msg>IP验证失败</return_msg></xml>");
        		out.flush();
        		return false;
        }else if(url.startsWith("/alipay")) {
	        	//out.write("failure");
	    		//out.flush();
	    		return true;
        }
        return false;
	}

}
