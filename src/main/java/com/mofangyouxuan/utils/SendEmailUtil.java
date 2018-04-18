package com.mofangyouxuan.utils;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility; 

public class SendEmailUtil {
	private static String username = "zhihuang_2009@163.com";
	private static String password = "zhaolongbo1248";
	private static String host = "smtp.163.com";
	private static String port = "25";
	
	public static void main(String[] args){
		List<String> addresses = new ArrayList<String>();
		addresses.add("1079946866@qq.com");
		
		List<String> ccAddr = new ArrayList<String>();
		ccAddr.add("zhihuang_2008@sina.com");
		
		List<String> bccAddr = new ArrayList<String>();
		bccAddr.add("aoksen@hotmail.com");
		
		List<File> attachFiles = new ArrayList<File>();
		File file = new File("D:/123/测试结果");
		File file2 = new File("C:/Users/Jee Khan/Pictures/测试小图片.zip");
		attachFiles.add(file);
		attachFiles.add(file2);
		sendMail("一封测试邮件","测试是否发送成功99999999999",addresses,ccAddr,bccAddr,attachFiles);
	} 
	/**
	 * 
	 * @param subject	邮件主题	
	 * @param content	邮件内容
	 * @param toAddr	收件人列表
	 * @param ccAddr	抄送列表
	 * @param bccAddr	密送列表 
	 * @param attachFiles	附件列表（文件不能是目录且有可读权限）
	 * @return
	 */
	public static String sendMail(String subject,String content,List<String> toAddr,List<String> ccAddr,List<String> bccAddr,List<File> attachFiles){
		if(subject == null || content == null || subject.trim().length()<1 || content.trim().length()<1 || toAddr == null || toAddr.isEmpty()){
			return "邮件要素（主题、内容、收件人）不完整！";
		}
		//这个类主要是设置邮件
		MailSenderInfo mailInfo = new MailSenderInfo(); 
		mailInfo.setMailServerHost(host); 
		mailInfo.setMailServerPort(port); 
		mailInfo.setValidate(true); 
		mailInfo.setUserName(username); 
		mailInfo.setPassword(password);//您的邮箱密码 
		mailInfo.setFromAddress(username); 
		mailInfo.setToAddresses(toAddr); 
		mailInfo.setCcAddresses(ccAddr);
		mailInfo.setBccAddresses(bccAddr);
		mailInfo.setSubject(subject); 
		mailInfo.setContent(content); 
		mailInfo.setAttachFiles(attachFiles);
		//这个类主要来发送邮件
		return sendHtmlMail(mailInfo);//发送html格式
	}
	
	/** 
	  * 以HTML格式发送邮件 
	  * @param mailInfo 待发送的邮件信息 
	  * @return 00-成功
	  */ 
	private static String sendHtmlMail(MailSenderInfo mailInfo){ 
		// 判断是否需要身份认证 
		MyAuthenticator authenticator = null;
		Properties pro = mailInfo.getProperties();
		//如果需要身份认证，则创建一个密码验证器  
		if (mailInfo.isValidate()) { 
			authenticator = new MyAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());
		} 
		// 根据邮件会话属性和密码验证器构造一个发送邮件的session 
		Session sendMailSession = Session.getDefaultInstance(pro,authenticator); 
		try { 
			// 根据session创建一个邮件消息 
			Message mailMessage = new MimeMessage(sendMailSession); 
			// 创建邮件发送者地址 
			Address from = new InternetAddress(mailInfo.getFromAddress()); 
			// 设置邮件消息的发送者 
			mailMessage.setFrom(from); 
			// 创建邮件的接收者地址，并设置到邮件消息中 
			InternetAddress[] addresses = new InternetAddress[mailInfo.getToAddresses().size()];  
			int i=0;
			for (String addr : mailInfo.getToAddresses()) {  
				addresses[i++] = new InternetAddress(addr);  
			} 
			// Message.RecipientType.TO属性表示接收者的类型为TO 
			mailMessage.setRecipients(Message.RecipientType.TO,addresses); 
			//创建邮件抄送地址
			if(mailInfo.getCcAddresses()!=null && !mailInfo.getCcAddresses().isEmpty()){
				i=0;
				InternetAddress[] ccAddr = new InternetAddress[mailInfo.getCcAddresses().size()];
				for (String addr : mailInfo.getCcAddresses()) {  
					ccAddr[i++] = new InternetAddress(addr);  
				}
				mailMessage.setRecipients(Message.RecipientType.CC,ccAddr);
			}
			//创建密送地址
			if(mailInfo.getBccAddresses()!=null && !mailInfo.getBccAddresses().isEmpty()){
				i=0;
				InternetAddress[] bccAddr = new InternetAddress[mailInfo.getBccAddresses().size()];
				for (String addr : mailInfo.getBccAddresses()) {  
					bccAddr[i++] = new InternetAddress(addr);  
				}
				mailMessage.setRecipients(Message.RecipientType.BCC,bccAddr);
			}
			// 设置邮件消息的主题 
			mailMessage.setSubject(mailInfo.getSubject()); 
			// 设置邮件消息发送的时间 
			mailMessage.setSentDate(new Date()); 
			// MiniMultipart类是一个容器类，包含MimeBodyPart类型的对象 
			Multipart multiPart = new MimeMultipart(); 
			// 创建一个包含HTML内容的MimeBodyPart 
			BodyPart html = new MimeBodyPart(); 
			// 设置HTML内容 
			html.setContent(mailInfo.getContent(), "text/html; charset=utf-8"); 
			multiPart.addBodyPart(html); 
			// 将MiniMultipart对象设置为邮件内容 
			mailMessage.setContent(multiPart); 
			// 添加附件的内容
			if (mailInfo.getAttachFiles()!= null && !mailInfo.getAttachFiles().isEmpty()) {
				for(File attachment : mailInfo.getAttachFiles()){
					BodyPart attachmentBodyPart = new MimeBodyPart();
					DataSource source = new FileDataSource(attachment);
					attachmentBodyPart.setDataHandler(new DataHandler(source));
					// 这里很重要，通过下面的Base64编码的转换可以保证你的中文附件标题名在发送时不会变成乱码
					//sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
					//messageBodyPart.setFileName("=?GBK?B?" + enc.encode(attachment.getName().getBytes()) + "?=");
					//MimeUtility.encodeWord可以避免文件名乱码
					attachmentBodyPart.setFileName(MimeUtility.encodeWord(attachment.getName()));
					multiPart.addBodyPart(attachmentBodyPart);
				}
			}
			/*// 设置邮件消息的主要内容 :使用文本
			String mailContent = mailInfo.getContent(); 
			mailMessage.setText(mailContent);*/
			// 发送邮件 
			Transport.send(mailMessage); 
			return "00"; 
		} catch (MessagingException | UnsupportedEncodingException ex) { 
			return ex.getMessage();
		} 
	} 
} 

   /**
      密码验证器
   */
  class MyAuthenticator extends Authenticator{
 	String userName=null;
 	String password=null;
 	 
 	public MyAuthenticator(){
 	}
 	public MyAuthenticator(String username, String password) { 
 		this.userName = username; 
 		this.password = password; 
 	} 
 	@Override
 	protected PasswordAuthentication getPasswordAuthentication(){
 		return new PasswordAuthentication(userName, password);
 	}
 }
  
/** 
* 发送邮件需要使用的基本信息 
*/ 
class MailSenderInfo { 
	
	// 发送邮件的服务器的IP和端口 
	private String mailServerHost; 
	private String mailServerPort = "25"; 
	
	// 邮件发送者的地址 
	private String fromAddress; 
	
	// 邮件接收者的地址 
	private List<String> toAddresses; 
	// 邮件抄送的地址 
	private List<String> ccAddresses; 
	// 邮件密送的地址 
	private List<String> bccAddresses; 
	
	// 登陆邮件发送服务器的用户名和密码 
	private String userName; 
	private String password; 
	
	// 是否需要身份验证 
	private boolean validate = false; 
	
	// 邮件主题 
	private String subject; 
	
	// 邮件的文本内容 
	private String content; 
	
	// 邮件附件
	private List<File> attachFiles; 	
	/** 
	  * 获得邮件会话属性 
	  */ 
	public Properties getProperties(){ 
	  Properties p = new Properties(); 
	  p.put("mail.smtp.host", this.mailServerHost); 
	  p.put("mail.smtp.port", this.mailServerPort); 
	  p.put("mail.smtp.auth", validate ? "true" : "false"); 
	  return p; 
	} 
	public String getMailServerHost() { 
	  return mailServerHost; 
	} 
	public void setMailServerHost(String mailServerHost) { 
	  this.mailServerHost = mailServerHost; 
	}
	public String getMailServerPort() { 
	  return mailServerPort; 
	}
	public void setMailServerPort(String mailServerPort) { 
	  this.mailServerPort = mailServerPort; 
	}
	public boolean isValidate() { 
	  return validate; 
	}
	public void setValidate(boolean validate) { 
	  this.validate = validate; 
	}

	public List<File> getAttachFiles() {
		return attachFiles;
	}
	public void setAttachFiles(List<File> attachFiles) {
		this.attachFiles = attachFiles;
	}
	
	public String getFromAddress() { 
	  return fromAddress; 
	} 
	public void setFromAddress(String fromAddress) { 
	  this.fromAddress = fromAddress; 
	}
	public String getPassword() { 
	  return password; 
	}
	public void setPassword(String password) { 
	  this.password = password; 
	}
	public List<String> getToAddresses() { 
	  return toAddresses; 
	} 
	public void setToAddresses(List<String> toAddresses) { 
	  this.toAddresses = toAddresses; 
	} 
	
	public List<String> getCcAddresses() {
		return ccAddresses;
	}
	public void setCcAddresses(List<String> ccAddresses) {
		this.ccAddresses = ccAddresses;
	}
	public List<String> getBccAddresses() {
		return bccAddresses;
	}
	public void setBccAddresses(List<String> bccAddresses) {
		this.bccAddresses = bccAddresses;
	}
	public String getUserName() { 
	  return userName; 
	}
	public void setUserName(String userName) { 
	  this.userName = userName; 
	}
	public String getSubject() { 
	  return subject; 
	}
	public void setSubject(String subject) { 
	  this.subject = subject; 
	}
	public String getContent() { 
	  return content; 
	}
	public void setContent(String textContent) { 
	  this.content = textContent; 
	} 
} 