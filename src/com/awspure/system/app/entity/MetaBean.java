package com.awspure.system.app.entity;

public class MetaBean {

	private String id;

	private String ftitle;

	private String fname;

	private String fieldType;

	private String displayType;

	private String displaySql;

	private int sn;

	private String ylzd;

	private String boName;

	private String boTitle;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFtitle() {
		return ftitle;
	}

	public void setFtitle(String ftitle) {
		this.ftitle = ftitle;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getFieldType() {
		return ifNull(fieldType);
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	public String getDisplayType() {
		return ifNull(displayType);
	}

	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}

	public String getDisplaySql() {
		return ifNull(displaySql);
	}

	public void setDisplaySql(String displaySql) {
		this.displaySql = displaySql;
	}

	private String ifNull(String a) {
		if (a == null) {
			return "";
		}
		return a;
	}

	public int getSn() {
		return sn;
	}

	public void setSn(int sn) {
		this.sn = sn;
	}

	public String getYlzd() {
		return ylzd;
	}

	public void setYlzd(String ylzd) {
		this.ylzd = ylzd;
	}

	public String getBoName() {
		return boName;
	}

	public void setBoName(String boName) {
		this.boName = boName;
	}

	public String getBoTitle() {
		return boTitle;
	}

	public void setBoTitle(String boTitle) {
		this.boTitle = boTitle;
	}
}
