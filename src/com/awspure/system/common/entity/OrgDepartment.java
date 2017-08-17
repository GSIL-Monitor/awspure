package com.awspure.system.common.entity;

public class OrgDepartment {

	private String departmentName;

	private String departmentNo;
	
	private String parentDepartmentNo;

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getDepartmentNo() {
		return departmentNo;
	}

	public void setDepartmentNo(String departmentNo) {
		this.departmentNo = departmentNo;
	}

	public String getParentDepartmentNo() {
		return parentDepartmentNo;
	}

	public void setParentDepartmentNo(String parentDepartmentNo) {
		this.parentDepartmentNo = parentDepartmentNo;
	}
	
	public String toString(){
		return "_departmentName["+departmentName+"]departmentName_ _departmentNo["
				+departmentNo+"]departmentNo_ _parentDepartmentNo["+parentDepartmentNo+"]parentDepartmentNo_";
	}

}
