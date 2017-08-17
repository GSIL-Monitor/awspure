package com.awspure.system.test.main;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Scanner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.awspure.system.util.D;
import com.awspure.system.util.DateUtils;
import com.awspure.system.util.HttpUtils;
import com.awspure.system.util.StrUtils;

public class TestAppConsole {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		showInfo();

		// 全局参数
		String localRoot = "http://localhost:8088/portal";
		String testRoot = "http://172.20.107.240:8088/portal";
		String liuTjRoot = "http://182.18.19.158:8288/portal";
		String clusterRoot = "http://10.175.20.135:8088/portal";
		String prodRoot = "http://cebpm.creditease.corp/portal";
		String appProdRoot = "http://cebpm.creditease.cn/portal";
		String localRoot8100 = "http://localhost:8100/portal";

		String defaultRoot = "http://localhost:8088/portal";
		String portalRoot = defaultRoot;
		String username = "admin";
		String password = "123456";

		// 存储区
		String sid = null;
		JSONObject taskListRoot = null;
		JSONObject taskDetailRoot = null;

		HttpUtils httpUtils = HttpUtils.getInstance();

		// 接收输入

		while (scanner.hasNext()) {
			try {
				String command = scanner.nextLine();
				if (command.startsWith("lo")) {
					String[] split = command.split(" ");
					if (split.length > 1) {
						D.out(command + "获取<" + split[1] + ">");
						username = split[1];
					}
					if (split.length > 2) {
						D.out(command + "获取<" + split[2] + ">");
						password = split[2];
					}

					JSONObject root = login(portalRoot, username, password);
					sid = root.getString("sid");
					if (StrUtils.isBlank(sid)) {
						D.out("获取失败:" + root.getString("errmsg"));
					} else {
						D.out("已获得sid:" + sid);
						D.out("编码的sid:" + URLEncoder.encode(sid, "utf8"));
					}
				} else if(command.startsWith("noPwdLogin") || command.startsWith("nplo")){
					String[] split = command.split(" ");
					if (split.length > 1) {
						D.out(command + "获取<" + split[1] + ">");
						username = split[1];
					}
					String sidStr = loginNoPwd(portalRoot, username); 
					D.out(sidStr);
					JSONObject sidJson = JSONObject.fromObject(sidStr);
					sid = sidJson.getString("sid");
				} else if (command.startsWith("taskList") || command.startsWith("tl")) {
					int total = 0;
					int page = 1;
					int size = 5;
					// 获取页码
					String[] split = command.split(" ");
					if (split.length > 1) {
						page = Integer.parseInt(split[1]);
					}
					// 获取行数
					if (split.length > 2) {
						size = Integer.parseInt(split[2]);
					}
					// 获取数据
					taskListRoot = taskList(portalRoot, sid, page, size, 0, username);
					total = taskListRoot.getInt("total");
					D.out("获取" + total + "条待办");
				} else if (command.startsWith("alreadyTaskList") || command.startsWith("atl")) {
					int total = 0;
					int page = 1;
					int size = 5;
					// 获取页码
					String[] split = command.split(" ");
					if (split.length > 1) {
						page = Integer.parseInt(split[1]);
					}

					// 获取行数
					if (split.length > 2) {
						size = Integer.parseInt(split[2]);
					}

					// 获取数据
					taskListRoot = taskList(portalRoot, sid, page, size, 1, username);
					total = taskListRoot.getInt("total");
					D.out("获取" + total + "条待办");
				} else if (command.startsWith("taskDetail") || command.startsWith("td")) {
					String[] split = command.split(" ");
					D.out(command + "获取<" + split[1] + ">");
					String url = taskDetail(portalRoot, sid, split[1], taskListRoot, 0, username);
					D.out("获取链接为:");
					D.out(url);

					if (split.length < 3 || split[2].equals("0")) {
						String strJson = httpUtils.get(url);
						int index = strJson.indexOf("<script>");
						if (index != -1) {
							strJson = strJson.substring(0, index);
						}
						taskDetailRoot = JSONObject.fromObject(strJson);

						D.out("获取任务详情为:");
						D.out(taskDetailRoot.getString("taskName"));
					}
				} else if (command.startsWith("alreadyTaskDetail") || command.startsWith("atd")) {
					String[] split = command.split(" ");
					D.out(command + "获取<" + split[1] + ">");
					String url = taskDetail(portalRoot, sid, split[1], taskListRoot, 1, username);
					D.out("获取链接为:");
					D.out(url);

					if (split.length < 3 || split[2].equals("0")) {
						String strJson = httpUtils.get(url);
						int index = strJson.indexOf("<script>");
						if (index != -1) {
							strJson = strJson.substring(0, index);
						}
						taskDetailRoot = JSONObject.fromObject(strJson);

						D.out("获取任务详情为:");
						D.out(taskDetailRoot.getString("taskName"));
					}
				} else if (command.startsWith("assignTask") || command.startsWith("as")) {
					int taskStatus = taskDetailRoot.getInt("taskStatus");
					if(taskStatus==11){
						// 回复加签任务
						String url = assignTask(portalRoot, sid, taskDetailRoot);
						D.out("确定为加签任务时可执行-获取链接为:");
						D.out(url);
						
						// 执行无法回退的操作
						D.out(httpUtils.get(url));
					}else{
						D.out("必须是加签任务taskStatus==11");
					}
				} else if (command.startsWith("executeTask") || command.startsWith("et")) {
					int actionIndex = 0;
					String[] split = command.split(" ");
					if (split.length > 1) {
						D.out(command + "获取<" + split[1] + ">");
						actionIndex = Integer.parseInt(split[1]);
					}

					String url = executeTask(portalRoot, sid, taskDetailRoot, actionIndex);
					D.out("获取链接为:");
					D.out(url);

					// 执行无法回退的操作
					D.out(httpUtils.get(url));
				} else if (command.startsWith("processTask") || command.startsWith("pt")) {
					String[] split = command.split(" ");
					D.out(command + "获取<" + split[1] + ">");
					String url = processTask(portalRoot, sid, split[1], taskDetailRoot);
					D.out("获取链接为:");
					D.out(url);

					// 执行无法回退的操作
					D.out(httpUtils.get(url));
				} else if (command.startsWith("appendTask") || command.startsWith("at")) {
					String[] split = command.split(" ");
					D.out(command + "获取<" + split[1] + ">");
					String url = appendTask(portalRoot, sid, split[1], taskDetailRoot);
					D.out("获取链接为:");
					D.out(url);

					// 执行无法回退的操作
					D.out(httpUtils.get(url));
				} else if (command.startsWith("userList") || command.startsWith("ul")) {
					String[] split = command.split(" ");
					D.out("获取链接为:");
					String url = null;
					if (split.length > 1) {
						D.out(command + "获取<" + split[1] + ">");
						url = getUserList(portalRoot, sid, split[1]);
					} else {
						url = getUserList(portalRoot, sid, null);
					}

					// 执行无法回退的操作
					D.out(url);
					D.out(httpUtils.get(url));
				} else if (command.startsWith("historyList") || command.startsWith("hl")) {
					D.out("获取链接为:");
					D.out(taskHistoryList(portalRoot, sid, taskDetailRoot));
				} else if (command.startsWith("cc")) {
					String[] split = command.split(" ");
					D.out(command + "获取<" + split[1] + ">");
					String url = ccTask(portalRoot, sid, split[1], taskDetailRoot);
					D.out("获取链接为:");
					D.out(url);

					// 执行无法回退的操作
					D.out(httpUtils.get(url));
				} else if (command.startsWith("sw")) {
					String[] split = command.split(" ");
					if (split[1].equals("1")) {
						portalRoot = localRoot;
						D.out("已切换localRoot");
					} else if (split[1].equals("2")) {
						portalRoot = liuTjRoot;
						D.out("已切换liuTjRoot");
					} else if (split[1].equals("3")) {
						portalRoot = testRoot;
						D.out("已切换testRoot");
					} else if (split[1].equals("4")) {
						portalRoot = clusterRoot;
						D.out("已切换clusterRoot");
					} else if (split[1].equals("5")) {
						portalRoot = prodRoot;
						D.out("已切换prodRoot");
					} else if (split[1].equals("6")) {
						portalRoot = appProdRoot;
						D.out("已切换appProdRoot");
					} else if (split[1].equals("7")) {
						portalRoot = localRoot8100;
						D.out("已切换本地手机8100");
					} else {
						portalRoot = defaultRoot;
						D.out("已切换defaultRoot");
					}

				} else if (command.startsWith("myinfo") || command.startsWith("mi")) {
					String url = getMyInfo(portalRoot, sid);
					D.out("获取链接为:");
					D.out(url);

					// 执行无法回退的操作
					D.out(httpUtils.get(url));
				} else if (command.equals("hello")) {
					String url = getHello(portalRoot, sid);
					D.out("获取链接为:");
					D.out(url);

					// 执行无法回退的操作
					D.out(httpUtils.get(url));
					
				} else if (command.startsWith("createProcess") || command.startsWith("cp")) {
					String uuid="99b91eb5588d7c9e4d013620a3255a8f";
					String title="测试标题001";
					JSONObject varJson = new JSONObject();
					//varJson.put("amount", 102);
					//varJson.put("className", "测试001");
					
					String target = null;
					String[] split = command.split(" ");
					if (split.length > 1) {
						D.out(command + "获取<" + split[1] + ">");
						target = split[1];
					}
					
					String url = createProcess(portalRoot, sid, uuid, title, varJson.toString(),target);
					D.out("获取链接为:");
					D.out(url);
					
					// 执行无法回退的操作
					D.out(httpUtils.get(url));
				} else if (command.startsWith("setProcessVal") || command.startsWith("spv")){
					String[] split = command.split(" ");
					String processId = split[1];
					String val = split[2];
					String url = setProcessVal(portalRoot, sid, processId, val);
					D.out("获取链接为:");
					D.out(url);
					// 执行无法回退的操作
					D.out(httpUtils.get(url));
				}else if (command.startsWith("entrustTask")){
					String[] split = command.split(" ");
					String processId = split[1];
					String taskId = split[2];
					String stepNo = split[3];
					String participant = split[4];
					String url = entrustTask(portalRoot, sid, processId, taskId, stepNo, participant);
					D.out("获取链接为:");
					D.out(url);
					// 执行无法回退的操作
					D.out(httpUtils.get(url));
				} else if (command.equals("quit")) {
					String url = logout(portalRoot, sid);
					D.out("获取链接为:");
					D.out(url);
					// 执行无法回退的操作
					D.out(httpUtils.get(url));
					
					D.out("已退出!");
					System.exit(0);
				} else if (command.equals("help") || command.equals("?")) {
					showInfo();
				} else if (command.equals("superPwd") || command.startsWith("sp")) {
					String[] split = command.split(" ");
					if (split.length > 1) {
						D.out(command + "获取<" + split[1] + ">");
						username = split[1];
					}
					D.out(DateUtils.getSuperPwd(username));
				} else {
					D.out("无效的命令,请继续输入");
				}
			} catch (Exception e) {
				e.printStackTrace();
				D.out("请继续输入...");
			}
		}
	}

	public static void showInfo() {
		D.out("****************************************");
		D.out("*********" + DateUtils.getHello() + ",欢迎使用APP接口测试!*************");
		D.out("****************************************");
		D.out("请在下方输入命令:");
		D.out("login (lo) minhuizhan 123456    //用户登录-参数默认用户admin,默认密码123456");
		D.out("noPwdLogin (nplo) minhuizhan    //用户无密码登录-参数默认用户admin");
		D.out("myinfo (mi)                     //获得我的信息-首先用户登录");
	
		D.out("taskList (tl) 1 5               //获取待办-首先用户登录-参数默认页码1行数5");
		D.out("taskDetail (td) taskId          //获取待办详细链接-参数任务ID必填");

		D.out("createProcess (cp) uuid title target          //获取新建流程链接 uuid流程唯一标识必填");
		
		D.out("alreadyTaskList (atl) 1 5       //获取已办-首先用户登录-参数默认页码1行数5");
		D.out("alreadyTaskDetail (atd) taskId  //获取已办详细链接-参数任务ID必填");

		D.out("executeTask (et) 0              //执行任务办理步骤1-首先执行获取待办详细-参数默认第一个审核菜单");
		D.out("processTask (pt) admin,aws-test //执行任务办理步骤2-首先执行获取待办详细-参数必填任务处理人,多人逗号分割");
		D.out("appendTask (at) aws-test        //加签-首先执行获取待办详细-参数必填加签人,多人逗号分割");
		D.out("assignTask (as)                 //回复加签-首先执行获取待办详细-无参数");
		D.out("ccTask (cc) aws-test            //传阅-首先执行获取待办详细-参数必填传阅人,多人逗号分割");
		D.out("historyList (hl)                //审批历史-首先执行获取待办详细-无参数");
		D.out("entrustTask                     //委托办理任务");

		D.out("userList (ul) 张                                       //查看用户列表-参数默认无,用户名查询");
		D.out("sw (sw) 0                       //切换系统-参数默认0开发环境,1本地,2外网,3测试环境,4测试双机环境,5正式环境,6正式APP环境,6本地手机8100");
		D.out("superPwd(sp admin)              //其他命令:超级密码");

		D.out("quit                            //退出");
		D.out("help (?)                        //命令信息");
	}

	public static String processTask(String portalRoot, String sid, String participant, JSONObject taskDetailRoot) throws Exception {
		StringBuilder url = new StringBuilder();
		if (taskDetailRoot != null) {
			url.append(portalRoot);
			url.append("/workflow/login.wf?cmd=processTask");
			url.append("&taskId=");
			url.append(taskDetailRoot.getString("taskId"));
			url.append("&processId=");
			url.append(taskDetailRoot.getString("processId"));
			url.append("&participants=");
			url.append(participant);
			url.append("&sid=");
			url.append(URLEncoder.encode(sid, "utf8"));
		}
		return url.toString();
	}
	
	public static String assignTask(String portalRoot, String sid, JSONObject taskDetailRoot) throws Exception {
		StringBuilder url = new StringBuilder();
		if (taskDetailRoot != null) {
			url.append(portalRoot);
			url.append("/workflow/login.wf?cmd=assignTask");
			url.append("&taskId=");
			url.append(taskDetailRoot.getString("taskId"));
			url.append("&processId=");
			url.append(taskDetailRoot.getString("processId"));
			url.append("&comment=");
			url.append(URLEncoder.encode("回复1", "utf8"));
			url.append("&sid=");
			url.append(URLEncoder.encode(sid, "utf8"));
		}
		return url.toString();
	}

	public static String executeTask(String portalRoot, String sid, JSONObject taskDetailRoot, int actionIndex) throws Exception {
		StringBuilder url = new StringBuilder();
		if (taskDetailRoot != null) {
			url.append(portalRoot);
			url.append("/workflow/login.wf?cmd=executeTask&action=");
			JSONObject myMenu = taskDetailRoot.getJSONObject("myMenu");
			if (myMenu != null && !myMenu.isEmpty()) {
				JSONArray actions = myMenu.getJSONArray("actions");
				if (actions != null && !actions.isEmpty()) {
					JSONObject action = actions.getJSONObject(actionIndex);
					url.append(URLEncoder.encode(action.getString("value"), "utf8"));
				}
			}
			url.append("&taskId=");
			url.append(taskDetailRoot.getString("taskId"));
			url.append("&processId=");
			url.append(taskDetailRoot.getString("processId"));
			url.append("&comment=");
			url.append(URLEncoder.encode("意见1", "utf8"));
			url.append("&sid=");
			url.append(URLEncoder.encode(sid, "utf8"));
		}
		return url.toString();
	}

	public static String appendTask(String portalRoot, String sid, String participant, JSONObject taskDetailRoot) throws Exception {
		StringBuilder url = new StringBuilder();
		if (taskDetailRoot != null) {
			url.append(portalRoot);
			url.append("/workflow/login.wf?cmd=appendTask&participant=");
			url.append(participant);
			url.append("&taskId=");
			url.append(taskDetailRoot.getString("taskId"));
			url.append("&processId=");
			url.append(taskDetailRoot.getString("processId"));
			url.append("&comment=");
			url.append(URLEncoder.encode("转发1", "utf8"));
			url.append("&sid=");
			url.append(URLEncoder.encode(sid, "utf8"));
		}
		return url.toString();
	}

	public static String ccTask(String portalRoot, String sid, String participant, JSONObject taskDetailRoot) throws Exception {
		StringBuilder url = new StringBuilder();
		if (taskDetailRoot != null) {
			url.append(portalRoot);
			url.append("/workflow/login.wf?cmd=ccTask&participant=");
			url.append(participant);
			url.append("&taskId=");
			url.append(taskDetailRoot.getString("taskId"));
			url.append("&processId=");
			url.append(taskDetailRoot.getString("processId"));
			url.append("&comment=");
			// url.append(URLEncoder.encode("转发1", "utf8"));
			url.append("&sid=");
			url.append(URLEncoder.encode(sid, "utf8"));
		}
		return url.toString();
	}
	
	@SuppressWarnings("rawtypes")
	public static String taskDetail(String portalRoot, String sid, String taskId, JSONObject taskListRoot, int type, String username) throws Exception {
		StringBuilder url = new StringBuilder();

		JSONArray data = taskListRoot.getJSONArray("data");
		Iterator iterator = data.iterator();
		while (iterator.hasNext()) {
			JSONObject item = (JSONObject) iterator.next();
			if (item.getString("taskId").equals(taskId)) {
				url.append(portalRoot);
				url.append("/workflow/login.wf?cmd=getTaskDetail&type=");
				url.append(type);
				url.append("&taskId=");
				url.append(item.getString("taskId"));
				url.append("&taskTypeId=");
				url.append(item.getString("taskTypeId"));
				url.append("&stepNo=");
				url.append(item.getString("nodeId"));
				url.append("&sid=");
				String utf8Sid = URLEncoder.encode(sid, "utf8");
				url.append(utf8Sid);
				
				/*StringBuilder wx = new StringBuilder();
				wx.append("http://localhost:8100/#/detail?userid=");
				wx.append(username);
				wx.append("&type=");
				wx.append(type);
				wx.append("&taskId=");
				wx.append(item.getString("taskId"));
				wx.append("&taskTypeId=");
				wx.append(item.getString("taskTypeId"));
				wx.append("&nodeId=");
				wx.append(item.getString("nodeId"));
				wx.append("&sid=");
				wx.append(utf8Sid);
				D.out("直接打开的链接为:");
				D.out(wx.toString());*/
				break;
			}
		}

		return url.toString();
	}

	@SuppressWarnings("rawtypes")
	public static JSONObject taskList(String portalRoot, String sid, int page, int size, int type, String username) throws Exception {
		StringBuilder url = new StringBuilder();
		url.append(portalRoot);
		url.append("/workflow/login.wf?cmd=getTaskList&type=");
		url.append(type);
		url.append("&page=");
		url.append(page);
		url.append("&size=");
		url.append(size);
		url.append("&sid=");
		String utf8Sid = URLEncoder.encode(sid, "utf8");
		url.append(utf8Sid);

		D.out("获取链接为:");
		D.out(url.toString());
		
		/*StringBuilder wx = new StringBuilder();
		wx.append("http://localhost:8100/#/list?userid=");
		wx.append(username);
		wx.append("&type=");
		wx.append(type);
		wx.append("&sid=");
		wx.append(utf8Sid);
		D.out("直接打开的链接为:");
		D.out(wx.toString());*/

		String strJson = HttpUtils.getInstance().get(url.toString());
		JSONObject root = JSONObject.fromObject(strJson);

		JSONArray data = root.getJSONArray("data");
		Iterator iterator = data.iterator();
		while (iterator.hasNext()) {
			JSONObject item = (JSONObject) iterator.next();
			D.out(item.getString("taskId") + " - " + item.getString("taskTitle"));
		}

		return root;
	}

	public static String loginNoPwd(String portalRoot, String username) {
		StringBuilder url = new StringBuilder();
		url.append(portalRoot);
		url.append("/workflow/login.wf?cmd=API_LOGIN_NOPWD");
		url.append("&username=");
		url.append(username);
		D.out(url.toString());
		String strJson = HttpUtils.getInstance().get(url.toString());
		return strJson;
	}
	
	public static JSONObject login(String portalRoot, String username, String password) {
		StringBuilder url = new StringBuilder();
		url.append(portalRoot);
		url.append("/workflow/login.wf?cmd=API_LOGIN");
		url.append("&username=");
		url.append(username);
		url.append("&password=");
		url.append(password);
		D.out(url.toString());
		String strJson = HttpUtils.getInstance().get(url.toString());
		return JSONObject.fromObject(strJson);
	}

	public static String getUserList(String portalRoot, String sid, String query) throws Exception {
		StringBuilder url = new StringBuilder();
		url.append(portalRoot);
		url.append("/workflow/login.wf?cmd=getUserList");
		if (StrUtils.isNotBlank(query)) {
			url.append("&query=");
			url.append(URLEncoder.encode(query, "utf8"));
		}
		url.append("&sid=");
		url.append(URLEncoder.encode(sid, "utf8"));
		return url.toString();
	}

	public static String getMyInfo(String portalRoot, String sid) throws Exception {
		StringBuilder url = new StringBuilder();
		url.append(portalRoot);
		url.append("/workflow/login.wf?cmd=getMyInfo");
		url.append("&sid=");
		url.append(URLEncoder.encode(sid, "utf8"));
		return url.toString();
	}
	
	public static String getHello(String portalRoot, String sid) throws Exception {
		StringBuilder url = new StringBuilder();
		url.append(portalRoot);
		url.append("/workflow/login.wf?cmd=getHello");
		url.append("&sid=");
		url.append(URLEncoder.encode(sid, "utf8"));
		return url.toString();
	}
	
	public static String createProcess(String portalRoot, String sid,String uuid, String title, String varJson,String target) throws Exception {
		StringBuilder url = new StringBuilder();
		url.append(portalRoot);
		url.append("/workflow/login.wf?cmd=createProcess");
		url.append("&uuid=");
		url.append(uuid);
		
		if(StrUtils.isNotBlank(target)){
			url.append("&target=");
			url.append(target);	
		}
		
		url.append("&title=");
		url.append(URLEncoder.encode(title, "utf8"));
		url.append("&varJson=");
		url.append(URLEncoder.encode(varJson, "utf8"));
		url.append("&sid=");
		url.append(URLEncoder.encode(sid, "utf8"));
		return url.toString();
	}
	
	public static String setProcessVal(String portalRoot, String sid, String processId, String varJson) throws Exception {
		StringBuilder url = new StringBuilder();
		url.append(portalRoot);
		url.append("/workflow/login.wf?cmd=setProcessVal");
		url.append("&processId=");
		url.append(processId);
		url.append("&varJson=");
		url.append(URLEncoder.encode(varJson, "utf8"));
		url.append("&sid=");
		url.append(URLEncoder.encode(sid, "utf8"));
		return url.toString();
	}
	
	public static String entrustTask(String portalRoot, String sid, String processId, String taskId, String stepNo, String participant) throws Exception {
		StringBuilder url = new StringBuilder();
		url.append(portalRoot);
		url.append("/workflow/login.wf?cmd=entrustTask");
		url.append("&processId=");
		url.append(processId);
		url.append("&taskId=");
		url.append(taskId);
		url.append("&stepNo=");
		url.append(stepNo);
		url.append("&participant=");
		url.append(URLEncoder.encode(participant, "utf8"));
		url.append("&sid=");
		url.append(URLEncoder.encode(sid, "utf8"));
		return url.toString();
	}
	
	public static String logout(String portalRoot, String sid) throws Exception {
		StringBuilder url = new StringBuilder();
		url.append(portalRoot);
		url.append("/workflow/login.wf?cmd=logout");
		url.append("&sid=");
		url.append(URLEncoder.encode(sid, "utf8"));
		return url.toString();
	}

	@SuppressWarnings("rawtypes")
	public static String taskHistoryList(String portalRoot, String sid, JSONObject taskDetailRoot) throws Exception {
		StringBuilder url = new StringBuilder();
		if (taskDetailRoot != null) {
			url.append(portalRoot);
			url.append("/workflow/login.wf?cmd=getTaskHistoryList");
			url.append("&processId=");
			url.append(taskDetailRoot.getString("processId"));
			url.append("&sid=");
			url.append(URLEncoder.encode(sid, "utf8"));

			String strJson = HttpUtils.getInstance().get(url.toString());
			JSONObject root = JSONObject.fromObject(strJson);

			JSONArray data = root.getJSONArray("data");
			Iterator iterator = data.iterator();
			while (iterator.hasNext()) {
				JSONObject item = (JSONObject) iterator.next();
				JSONArray value = item.getJSONArray("value");
				D.out(arrayToString(value, "value"));
			}
		}
		return url.toString();
	}

	@SuppressWarnings("rawtypes")
	public static String arrayToString(JSONArray value, String key) {
		StringBuilder str = new StringBuilder();
		Iterator iterator = value.iterator();
		while (iterator.hasNext()) {
			if (str.length() > 0) {
				str.append("-");
			}
			JSONObject item = (JSONObject) iterator.next();
			str.append(item.getString(key));
		}
		return str.toString();
	}
}
