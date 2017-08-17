package com.awspure.system.common.entity;

/**
 * 同步用户所用用户实体
 * @author Administrator
 */
public class OrgUser {

	private String userId;
	
	private String passWord;
	
	private String userName;
	
	private String departmentNo;
	
	private String userNo;
	
	private String status;
	
	private String roleName;
	
	private String isMajor;
	
	private String isManager;
	
	private String positionNo;
	
	private String positionName;
	
	private String level;

	public String getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getDepartmentNo() {
		return departmentNo;
	}

	public void setDepartmentNo(String departmentNo) {
		this.departmentNo = departmentNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getIsMajor() {
		return isMajor;
	}

	public void setIsMajor(String isMajor) {
		this.isMajor = isMajor;
	}

	public String getIsManager() {
		return isManager;
	}

	public void setIsManager(String isManager) {
		this.isManager = isManager;
	}

	public String getPositionNo() {
		return positionNo;
	}

	public void setPositionNo(String positionNo) {
		this.positionNo = positionNo;
	}

	public String getPositionName() {
		return positionName;
	}

	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}
	
	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}
	
	public String toString(){
		return "_userNo["+userNo+"]userNo_ "
				+"_userName["+userName+"]userName_ "
				+"_departmentNo["+departmentNo+"]departmentNo_ "
				+"_roleName["+roleName+"]roleName_ "
				+"_isMajor["+isMajor+"]isMajor_ "
				+"_positionNo["+positionNo+"]positionNo_ "
				+"_positionName["+positionName+"]positionName_ "
				+"_level["+level+"]level_ ";
	}
}
