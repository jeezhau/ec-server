package com.mofangyouxuan.common;
/**
 * 分页条件
 * @author Jee Khan
 *
 */
public class PageCond {
	public static int DEFAULT_PAGESIZE = 20;
	private int begin;		//开始记录号
	private int pageSize;	//每页大小
	private int count;		//总记录数
	public PageCond(int begin ,int pageSize){
		if(begin < 0) {
			begin = 0;
		}
		this.begin = begin;
		this.pageSize = pageSize;
	}
	public PageCond(int begin){
		if(begin < 0) {
			begin = 0;
		}
		this.begin = begin;
		this.pageSize = DEFAULT_PAGESIZE;
	}
	public PageCond(){

	}
	public int getBegin() {
		return begin;
	}
	public void setBegin(int begin) {
		this.begin = begin;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}

}
