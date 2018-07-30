package com.mofangyouxuan.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class CommonUtil {
	
	/**
	 * 生成30位的订单ID
	 * @param userId
	 * @return
	 */
	public static String genOrderId(Integer userId) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS"); //17位时间
		String currTime = sdf.format(new Date());
		String uId = userId + "";
		int len = 11 - uId.length();
		for(int i=0;i<len;i++) {	//11位用户
			uId = "0" + uId;
		}
		return currTime + uId + NonceStrUtil.getNonceNum(2);
	}
	
	/**
	 * 生成32位的支付流水ID
	 * @param orderId
	 * @return
	 */
	public static String genPayFlowId(String orderId,String oldId) {
		int next = 0;
		if(oldId != null && oldId.length() == 32) {
			next = Integer.parseInt(oldId.substring(30));
		}
		next += 1;
		if(next<99) {
			return orderId + "0" +next;
		}
		return orderId + next;
	}
	
	/**
	 * 生成20位的ImageID
	 * @return
	 */
	public static String genImageId() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS"); //17位时间
		String currTime = sdf.format(new Date());
		return currTime + NonceStrUtil.getNonceNum(3);
	}
	
	
	/**
	 * 获取指定IP的二进制字符串序列
	 * @param address
	 * @return
	 */
	public static String getIpBinarySeq(String address){
		String[] networkAddressArray = address.split("\\."); 
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < networkAddressArray.length; i++) {
			String str = networkAddressArray[i];
			if(Integer.toBinaryString(Integer.parseInt(str)).length()<8){
				int num = 8-Integer.toBinaryString(Integer.parseInt(str)).length();
				String ipStr1 = "";
				for (int j = 0; j < num; j++) {
					ipStr1 += "0";
				}
				sb.append(ipStr1+(String)Integer.toBinaryString(Integer.parseInt(str)));
			}else{
				sb.append((String)Integer.toBinaryString(Integer.parseInt(str)));
			}
		}
		return sb.toString();
	}
	
	/**
	 * 验证Ip是否在白名单中
	 * @param remoteIp	待验证IP
	 * @param whiteIpSegment	IP段：101.226.103.0/25
	 * @return
	 */
	public static boolean isValidIp(String remoteIp,String whiteIpSegment) {
		String[] whiteIpAndSeg = whiteIpSegment.split("/");
		String whiteIp = whiteIpAndSeg[0];
		Integer maskCnt = new Integer(whiteIpAndSeg[1]);
		String ripseq = getIpBinarySeq(remoteIp);
		String wipseq = getIpBinarySeq(whiteIp);
		if(ripseq.substring(0, maskCnt).equals(wipseq.substring(0, maskCnt))) {
			return true;
		}
		return false;
	}	
	
	/**
	 * 获取用户IP地址
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
	
	/**
	 * 统计该文件夹下的文件数量,排除隐藏文件
	 * @param folder
	 * @return
	 */
	public static int countFileCnt(File file) {
		int cnt = 0;
		if(!file.exists()) {//文件不存在
			return 0;
		}
		if(!file.isDirectory()) {//普通文件
			if(file.getName().startsWith(".")) {
				return 0;
			}
			return 1;
		}
		File[] files = file.listFiles();
		if(files == null || files.length<1) {//空目录
			return 1;
		}else {
			cnt += 1; //非空目录
		}
		//迭代目录下的文件
		
		for(File f:files) {
			int c = countFileCnt(f);
			//System.out.println(f.getName() + ":" + c);
			cnt += c;
		}
		return cnt;
	}
	
	/**
	 * 从指定文件夹下查找指定文件
	 * @param folder
	 * @param filename
	 * @return
	 */
	public static File findFile(File file,String filename) {
		File target = null;
		if(file == null) {
			return null;
		}
		if(!file.exists()) {
			return null;
		}
		if(!file.isDirectory()) {//非目录
			if(file.getName().equals(filename)) {
				return file;
			}
		}
		File[] files = file.listFiles();
		if(files == null || files.length<1) {
			return null;
		}
		for(File f:files) {
			target = findFile(f,filename);
			if(target != null) {
				return target;
			}
		}
		return target;
	}

	/**
     * 得到网页中图片的地址
     */
    public static List<String> getImgStr(String htmlStr) {
        List<String> pics = new ArrayList<>();
        String img = "";
        Pattern p_image;
        Matcher m_image;
        //     String regEx_img = "<img.*src=(.*?)[^>]*?>"; //图片链接地址
        String regEx_img = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
        p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
        m_image = p_image.matcher(htmlStr);
        while (m_image.find()) {
            // 得到<img />数据
            img = m_image.group();
            // 匹配<img>中的src数据
            Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);
            while (m.find()) {
                pics.add(m.group(1));
            }
        }
        return pics;
    }
    
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		//System.out.println(genOrderId(100002));
		//System.out.println(SignUtils.encodeSHA256Hex("123456"));
		
		String testStr = "<p>美味鲜甜，不可错过</p>  <p><img alt=\"\" src=\"/shop/gimage/10002/1532177258770.jpg\" style=\"width:80%\" /><img alt=\"\" src=\"/shop/gimage/10002/20180729044308586805\" style=\"width:80%\" /></p>  <p>多汁诱人<img alt=\"\" src=\"/shop/gimage/10002/1532177258766.jpg\" style=\"width:80%\" /></p>  <p><img alt=\"\" src=\"/shop/gimage/10002/20180729044308586804\" style=\"width:80%\" /></p>";
		List<String> list = getImgStr(testStr);
		for(String str:list) {
			System.out.println(str);
		}
		
	}

}
