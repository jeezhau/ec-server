package com.mofangyouxuan.utils;

import java.io.File;
import java.io.FilenameFilter;


public class FileFilter implements FilenameFilter{

	/**
	 * 
	 */
	private String name;
	
	
	public FileFilter(String name) {
		this.name = name;
	}
	
	
	public boolean accept(File file, String path) {
		if (path.indexOf(name) != -1) {
			return true;
		}
		return false;
	}
	
	

}
