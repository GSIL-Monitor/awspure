package com.awspure.system.socket;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

import com.actionsoft.application.server.BaseSocketCommand;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.util.UtilCode;
import com.actionsoft.awf.util.UtilString;
import com.awspure.system.app.web.AppWeb;
import com.awspure.system.app.web.AppWebImpl;
import com.awspure.system.util.D;
import com.awspure.system.util.StrUtils;

public class AppSocketCommand implements BaseSocketCommand {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean executeCommand(UserContext uc, Socket socket, OutputStreamWriter out, Vector cmdVector, UtilString us, String socketCmd)
			throws Exception {

		// 返回参数
		boolean findCmd = true;

		if (socketCmd.equals("getHello")) {
			AppWeb web = new AppWebImpl(uc);
			out.write(web.getHello());
		} else if (socketCmd.equals("getTaskList")) {// 获取待办/已办的列表
			String type = UtilCode.decode(us.matchValue("_type[", "]type_"));// 类型0待办1已办
			String size = UtilCode.decode(us.matchValue("_size[", "]size_"));// 每页条数
			String page = UtilCode.decode(us.matchValue("_page[", "]page_"));// 第几页
			String gxzx = UtilCode.decode(us.matchValue("_gxzx[", "]gxzx_"));// 第几页
			String params = UtilCode.decode(us.matchValue("_params[", "]params_"));// 查询条件
			if (StrUtils.isBlank(page) || page.equals("0")) {
				page = "1";
			}
			if (StrUtils.isBlank(type)) {
				type = "1";
			}
			if (StrUtils.isBlank(size)) {
				size = "10";
			}
			AppWeb web = new AppWebImpl(uc);
			D.log("params="+params);
			out.write(web.getTaskList(Integer.parseInt(size), Integer.parseInt(page), Integer.parseInt(type), gxzx, params));
		} else if (socketCmd.equals("getTaskDetail")) {// 任务详情信息
			String type = UtilCode.decode(us.matchValue("_type[", "]type_"));// 类型0待办1已办
			String taskId = UtilCode.decode(us.matchValue("_taskId[", "]taskId_"));
			String taskTypeId = UtilCode.decode(us.matchValue("_taskTypeId[", "]taskTypeId_"));
			String stepNo = UtilCode.decode(us.matchValue("_stepNo[", "]stepNo_"));
			if (StrUtils.isBlank(type)) {
				type = "1";
			}
			AppWeb web = new AppWebImpl(uc);
			out.write(web.getTaskDetail(taskId, taskTypeId, stepNo, type));
		} else if (socketCmd.equals("executeTask")) {// 任务办理
			String processId = UtilCode.decode(us.matchValue("_processId[", "]processId_"));
			String taskId = UtilCode.decode(us.matchValue("_taskId[", "]taskId_"));
			String action = UtilCode.decode(us.matchValue("_action[", "]action_"));
			String comment = UtilCode.decode(us.matchValue("_comment[", "]comment_"));
			AppWeb web = new AppWebImpl(uc);
			out.write(web.doExecuteTask(action, comment, Integer.parseInt(processId), Integer.parseInt(taskId)));
		} else if (socketCmd.equals("getTaskHistoryList")) {// 任务历史审批意见列表
			String processId = UtilCode.decode(us.matchValue("_processId[", "]processId_"));
			AppWeb web = new AppWebImpl(uc);
			out.write(web.getTaskHistoryList(Integer.parseInt(processId)));
		} else if (socketCmd.equals("processTask")) {// 任务审批操作
			String processId = UtilCode.decode(us.matchValue("_processId[", "]processId_"));
			String taskId = UtilCode.decode(us.matchValue("_taskId[", "]taskId_"));
			String participants = UtilCode.decode(us.matchValue("_participants[", "]participants_"));
			AppWeb web = new AppWebImpl(uc);
			out.write(web.processTask(Integer.parseInt(processId), Integer.parseInt(taskId), participants));
		} else if (socketCmd.equals("getUserList")) {// 选择人
			String query = UtilCode.decode(us.matchValue("_query[", "]query_"));
			String size = UtilCode.decode(us.matchValue("_size[", "]size_"));// 每页条数
			String page = UtilCode.decode(us.matchValue("_page[", "]page_"));// 第几页
			if (StrUtils.isBlank(page) || page.equals("0")) {
				page = "1";
			}
			if (StrUtils.isBlank(size)) {
				size = "10";
			}
			AppWeb web = new AppWebImpl(uc);
			out.write(web.getUserList(Integer.parseInt(size), Integer.parseInt(page), query));
		} else if (socketCmd.equals("appendTask")) {// 发送加签
			String taskId = UtilCode.decode(us.matchValue("_taskId[", "]taskId_"));
			String processId = UtilCode.decode(us.matchValue("_processId[", "]processId_"));
			String participant = UtilCode.decode(us.matchValue("_participant[", "]participant_"));
			String comment = UtilCode.decode(us.matchValue("_comment[", "]comment_"));
			AppWeb web = new AppWebImpl(uc);
			out.write(web.appendTask(Integer.parseInt(taskId), Integer.parseInt(processId), participant, comment));
		} else if (socketCmd.equals("ccTask")) {// 直接转发
			String taskId = UtilCode.decode(us.matchValue("_taskId[", "]taskId_"));
			String processId = UtilCode.decode(us.matchValue("_processId[", "]processId_"));
			String participant = UtilCode.decode(us.matchValue("_participant[", "]participant_"));
			String comment = UtilCode.decode(us.matchValue("_comment[", "]comment_"));
			AppWeb web = new AppWebImpl(uc);
			out.write(web.ccTask(Integer.parseInt(taskId), Integer.parseInt(processId), participant, comment));
		} else if (socketCmd.equals("assignTask")) {// 处理加签任务
			String taskId = UtilCode.decode(us.matchValue("_taskId[", "]taskId_"));
			String processId = UtilCode.decode(us.matchValue("_processId[", "]processId_"));
			String comment = UtilCode.decode(us.matchValue("_comment[", "]comment_"));
			AppWeb web = new AppWebImpl(uc);
			out.write(web.assignTask(Integer.parseInt(taskId), Integer.parseInt(processId), comment));
		} else if (socketCmd.equals("getMyInfo")) {// 我的信息
			AppWeb web = new AppWebImpl(uc);
			out.write(web.getMyInfo());
		} else if (socketCmd.equals("logout")) {
			AppWeb web = new AppWebImpl(uc);
			out.write(web.logout());
		} else if (socketCmd.equals("API_LOGIN")) {
			String username = UtilCode.decode(us.matchValue("_username[", "]username_"));
			String password = UtilCode.decode(us.matchValue("_password[", "]password_"));
			AppWeb web = new AppWebImpl(uc);
			out.write(web.login(username, password));
		} else if (socketCmd.equals("API_LOGIN_NOPWD")) {
			String username = UtilCode.decode(us.matchValue("_username[", "]username_"));
			AppWeb web = new AppWebImpl(uc);
			out.write(web.loginNoPassword(username));
		} else if (socketCmd.equals("createProcess")) {//创建流程
			String uuid = UtilCode.decode(us.matchValue("_uuid[", "]uuid_"));
			String title = UtilCode.decode(us.matchValue("_title[", "]title_"));
			String varJson = UtilCode.decode(us.matchValue("_varJson[", "]varJson_"));
			String target = UtilCode.decode(us.matchValue("_target[", "]target_"));
			AppWeb web = new AppWebImpl(uc);
			out.write(web.createProcess(uuid, title, varJson, target));
		} else if (socketCmd.equals("setProcessVal")){//设置流程全局变量
			String processId = UtilCode.decode(us.matchValue("_processId[", "]processId_"));
			String varJson = UtilCode.decode(us.matchValue("_varJson[", "]varJson_"));
			AppWeb web = new AppWebImpl(uc);
			out.write(web.setProcessGlobalVariables(processId, varJson));
		} else if(socketCmd.equals("entrustTask")){//委托办理
			String processId = UtilCode.decode(us.matchValue("_processId[", "]processId_"));
			//String taskId = UtilCode.decode(us.matchValue("_taskId[", "]taskId_"));
			String participant = UtilCode.decode(us.matchValue("_participant[", "]participant_"));
			//String stepNo = UtilCode.decode(us.matchValue("_stepNo[", "]stepNo_"));
			AppWeb web = new AppWebImpl(uc);
			out.write(web.entrustTask(Integer.parseInt(processId), participant));
		} else if(socketCmd.equals("removeProcess")){//删除流程
			String processId = UtilCode.decode(us.matchValue("_processId[", "]processId_"));
			AppWeb web = new AppWebImpl(uc);
			out.write(web.removeProcess(Integer.parseInt(processId)));
		} else if(socketCmd.equals("getProcessVal")){//获取流程全局变量值
			String processId = UtilCode.decode(us.matchValue("_processId[", "]processId_"));
			String taskId = UtilCode.decode(us.matchValue("_taskId[", "]taskId_"));
			String key = UtilCode.decode(us.matchValue("_key[", "]key_"));
			AppWeb web = new AppWebImpl(uc);
			out.write(web.getProcessVal(Integer.parseInt(processId), Integer.parseInt(taskId), key));
		} else {
			findCmd = false;
		}

		// 如果执行了该类里面的方法,擦除AWS待办提示,返回纯净的JSON
		if (findCmd) {
			if (uc != null) {
				MessageQueue.getInstance().removeMessage(uc.getUID());
			}
		}

		return findCmd;
	}

}
