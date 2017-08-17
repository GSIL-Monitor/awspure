package com.awspure.system.app.entity;

public class TaskBean {

	private int id;

	private int bindId;

	private String target;

	private int status;

	private String title;

	private int wfId; // 流程ID

	private int wfsId; // 流程节点ID

	private int readTask; // 0未读 1已读

	private String readTime;

	private String flowName;// 流程民称

	private String appId;// 流程标识

	private String stepName;

	private String stepNo;

	private String wfStyle;

	private String beginTime;

	private int priority;

	private String createDate;

	private String positionName;

	private int wfEnd; // 0进行中 1已结束
	
	private int subId;
	
	private String subTitle;
	
	private String extendId;
	
	private int waitCount; // 是否为加签的加签的数量

	private String uuid;
	
	private boolean isModify;//表单是否可编辑

	private String isGxzx;
	// 转化为Boolean
	public boolean isRead() {
		return readTask == 1;
	}

	// 转化为Boolean
	public boolean isEnd() {
		return wfEnd == 1;
	}

	// 转化为String
	public String getProcessId() {
		return String.valueOf(bindId);
	}

	// 转化为String
	public String getTaskId() {
		return String.valueOf(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getBindId() {
		return bindId;
	}

	public void setBindId(int bindId) {
		this.bindId = bindId;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getWfId() {
		return wfId;
	}

	public void setWfId(int wfId) {
		this.wfId = wfId;
	}

	public int getWfsId() {
		return wfsId;
	}

	public void setWfsId(int wfsId) {
		this.wfsId = wfsId;
	}

	public int getReadTask() {
		return readTask;
	}

	public void setReadTask(int readTask) {
		this.readTask = readTask;
	}

	public String getReadTime() {
		return readTime;
	}

	public void setReadTime(String readTime) {
		this.readTime = readTime;
	}

	public String getFlowName() {
		return flowName;
	}

	public void setFlowName(String flowName) {
		this.flowName = flowName;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getStepName() {
		return stepName;
	}

	public void setStepName(String stepName) {
		this.stepName = stepName;
	}

	public String getStepNo() {
		return stepNo;
	}

	public void setStepNo(String stepNo) {
		this.stepNo = stepNo;
	}

	public String getWfStyle() {
		return wfStyle;
	}

	public void setWfStyle(String wfStyle) {
		this.wfStyle = wfStyle;
	}

	public String getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getPositionName() {
		return positionName;
	}

	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}

	public int getWfEnd() {
		return wfEnd;
	}

	public void setWfEnd(int wfEnd) {
		this.wfEnd = wfEnd;
	}

	public int getSubId() {
		return subId;
	}

	public void setSubId(int subId) {
		this.subId = subId;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public String getExtendId() {
		return extendId;
	}

	public void setExtendId(String extendId) {
		this.extendId = extendId;
	}

	public int getWaitCount() {
		return waitCount;
	}

	public void setWaitCount(int waitCount) {
		this.waitCount = waitCount;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public boolean isModify() {
		return isModify;
	}

	public void setModify(boolean isModify) {
		this.isModify = isModify;
	}

	public String getIsGxzx() {
		return isGxzx;
	}

	public void setIsGxzx(String isGxzx) {
		this.isGxzx = isGxzx;
	}
	
}
