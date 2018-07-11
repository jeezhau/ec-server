package com.mofangyouxuan.utils;
 
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
 
 
/**
 * ZIP工具包(支持中文)
 */
public class CHZipUtils {
 
	/**使用GBK编码可以避免压缩中文文件名乱码*/
	private static final String CHINESE_CHARSET = "GBK";
	/**文件读取缓冲区大小*/
	private static final int CACHE_SIZE = 1024;
 
	/**
	 * 压缩文件
	 * @param sourceFolder 压缩文件夹
	 * @param zipFilePath 压缩文件输出路径
	 */
	public static void zip(String sourceFolder, String zipFilePath) {
		OutputStream os = null;
		BufferedOutputStream bos = null;
		ZipOutputStream zos = null;
		try {
			os = new FileOutputStream(zipFilePath);
			bos = new BufferedOutputStream(os);
			zos = new ZipOutputStream(bos,Charset.forName("gbk"));
			// 解决中文文件名乱码
			//zos.setEncoding(CHINESE_CHARSET);
			File file = new File(sourceFolder);
			String basePath = null;
			if (file.isDirectory()) {//压缩文件夹
				basePath = file.getPath();
			} else {
				basePath = file.getParent();
			}
			zipFile(file, basePath, zos);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				if (zos != null) {
					zos.closeEntry();
					zos.close();
				}
				if (bos != null) {
					bos.close();
				}
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
 
	
	/**
	 * 递归压缩文件
	 * @param parentFile
	 * @param basePath
	 * @param zos
	 * @throws Exception
	 */
	private static void zipFile(File parentFile, String basePath, ZipOutputStream zos) throws Exception {
		File[] files = new File[0];
		if (parentFile.isDirectory()) {
			files = parentFile.listFiles();
		} else {
			files = new File[1];
			files[0] = parentFile;
		}
		String pathName;
		InputStream is;
		BufferedInputStream bis;
		byte[] cache = new byte[CACHE_SIZE];
		for (File file : files) {
			if (file.isDirectory()) {
				pathName = file.getPath().substring(basePath.length() + 1) + File.separator;
				zos.putNextEntry(new ZipEntry(pathName));
				zipFile(file, basePath, zos);
			} else {
				pathName = file.getPath().substring(basePath.length() + 1);
				is = new FileInputStream(file);
				bis = new BufferedInputStream(is);
				zos.putNextEntry(new ZipEntry(pathName));
				int nRead = 0;
				while ((nRead = bis.read(cache, 0, CACHE_SIZE)) != -1) {
					zos.write(cache, 0, nRead);
				}
				bis.close();
				is.close();
			}
		}
	}
 
	/**
	 * 解压压缩包
	 * @param zipFilePath 压缩文件路径
	 * @param destDir 解压目录
	 */
	public static void unZip(String zipFilePath, String destDir) {
		ZipFile zipFile = null;
		try {
			BufferedInputStream bis = null;
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			zipFile = new ZipFile(zipFilePath);
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			File file, parentFile;
			ZipEntry entry;
			byte[] cache = new byte[CACHE_SIZE];
			while (zipEntries.hasMoreElements()) {
				entry = (ZipEntry) zipEntries.nextElement();
				if (entry.isDirectory()) {
					new File(destDir + entry.getName()).mkdirs();
					continue;
				}
				bis = new BufferedInputStream(zipFile.getInputStream(entry));
				file = new File(destDir + entry.getName());
				parentFile = file.getParentFile();
				if (parentFile != null && (!parentFile.exists())) {
					parentFile.mkdirs();
				}
				fos = new FileOutputStream(file);
				bos = new BufferedOutputStream(fos, CACHE_SIZE);
				int readIndex = 0;
				while ((readIndex = bis.read(cache, 0, CACHE_SIZE)) != -1) {
					fos.write(cache, 0, readIndex);
				}
				bos.flush();
				bos.close();
				fos.close();
				bis.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				zipFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
 
	/**
	 * 解压压缩包
	 * @param zipFilePath 压缩文件路径
	 * @param destDir 解压目录
	 */
	public static void unGzip(String zipFilePath, String destFile) {
		try {
			BufferedInputStream bis = null;
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;

			bis = new BufferedInputStream(new FileInputStream(zipFilePath));
			GZIPInputStream gzip = new GZIPInputStream(bis);
			File dest = new File(destFile);
			File parentFile = dest.getParentFile();
			if (parentFile != null && (!parentFile.exists())) {
				parentFile.mkdirs();
			}
			fos = new FileOutputStream(dest);
			bos = new BufferedOutputStream(fos, CACHE_SIZE);
			int readIndex = 0;
			byte[] cache = new byte[CACHE_SIZE];
			while ((readIndex = gzip.read(cache, 0, CACHE_SIZE)) != -1) {
				fos.write(cache, 0, readIndex);
			}
			bos.flush();
			bos.close();
			fos.close();
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
//		String sourceFolder = "D:/test/1.txt";
//		String sourceFolder = "D:/test/中文名.txt";
		//String sourceFolder = "D:/test/cms";
		String zipFilePath = "/Users/jeekhan/mfyx/paybills/wxpay20180707_1.gzip";
		String destDir = "/Users/jeekhan/mfyx/paybills/wxpay20180707_1";
		//CHZipUtils.zip(sourceFolder, zipFilePath);
		CHZipUtils.unGzip(zipFilePath, destDir);
		System.out.println("********执行成功**********");
	}
 
}
