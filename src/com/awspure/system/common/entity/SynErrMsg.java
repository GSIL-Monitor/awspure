package com.awspure.system.common.entity;

/**
 * 同步数据错误信息持久化类
 * @author Administrator
 */
public class SynErrMsg {

	private String timestamp;//时间戳  主键
	
	private String errmsg;//错误信息
	
	private String data;//需同步，修改，插入 的数据
	
	private String function;//报错的方法名
	
	private String date;//日期

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
