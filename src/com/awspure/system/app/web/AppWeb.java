package com.awspure.system.app.web;

public interface AppWeb {
	
	public String getHello();
	
	public String getTaskList(int size, int page, int type, String gxzx, String params);
	
	public String getTaskDetail(String taskId, String taskTypeId, String stepNo, String type);
	
	public String doExecuteTask(String action, String comment, int processInstanceId, int taskInstanceId);
	
	public String getTaskHistoryList(int processInstanceId);
	
	public String processTask(int processInstanceId, int taskInstanceId, String participants);
	
	public String getUserList(int size, int page, String query);
	
	public String appendTask(int taskId, int processId, String participant, String comment);
	
	public String ccTask(int taskId, int processId, String participant, String comment);
	
	public String assignTask(int taskId, int processId, String comment);
	
	public String getMyInfo();
	
	public String loginNoPassword(String username);
	
	public String login(String username,String password);
	
	public String logout();
	
	public String createProcess(String uuid,String title,String varJson,String target);

	public String setProcessGlobalVariables(String processId, String varJson);
	
	public String entrustTask(int processId, String participants);
	
	public String removeProcess(int processId);
	
	public String getProcessVal(int processId, int taskId, String key);
}
