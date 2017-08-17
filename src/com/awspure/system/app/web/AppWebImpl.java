package com.awspure.system.app.web;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.awf.bo.cache.MetaDataCache;
import com.actionsoft.awf.bo.model.MetaDataModel;
import com.actionsoft.awf.form.design.cache.SheetCache;
import com.actionsoft.awf.form.design.model.FormModel;
import com.actionsoft.awf.form.design.model.SheetModel;
import com.actionsoft.awf.form.execute.RuntimeFormManager;
import com.actionsoft.awf.form.execute.plugins.ext.AjaxDataDecode;
import com.actionsoft.awf.organization.cache.DepartmentCache;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.control.MessageQueue;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.DepartmentModel;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.session.dao.Session;
import com.actionsoft.awf.session.model.SessionModel;
import com.actionsoft.awf.util.DBSequence;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.Function;
import com.actionsoft.awf.util.UtilDate;
import com.actionsoft.awf.workflow.constant.ActivityDefinitionConst;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepBindReportCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepCache;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepOpinionCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepOpinionModel;
import com.actionsoft.awf.workflow.execute.PriorityType;
import com.actionsoft.awf.workflow.execute.SynType;
import com.actionsoft.awf.workflow.execute.WorkflowException;
import com.actionsoft.awf.workflow.execute.dao.ProcessRuntimeDaoFactory;
import com.actionsoft.awf.workflow.execute.engine.WorkflowEngine;
import com.actionsoft.awf.workflow.execute.engine.WorkflowTaskEngine;
import com.actionsoft.awf.workflow.execute.event.FormEventHandler;
import com.actionsoft.awf.workflow.execute.model.ProcessInstanceModel;
import com.actionsoft.awf.workflow.execute.model.TaskInstanceModel;
import com.actionsoft.awf.workflow.execute.model.UserTaskAuditMenuModel;
import com.actionsoft.awf.workflow.execute.model.UserTaskHistoryOpinionModel;
import com.actionsoft.htmlframework.htmlmodel.RepleaseKey;
import com.actionsoft.i18n.I18nRes;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.RuleAPI;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;
import com.actionsoft.sdk.local.level1.SessionAPI;
import com.awspure.system.app.entity.TaskBean;
import com.awspure.system.app.service.MobilePlusService;
import com.awspure.system.app.service.MobileService;
import com.awspure.system.common.entity.Page;
import com.awspure.system.util.D;
import com.awspure.system.util.DateUtils;
import com.awspure.system.util.SqlUtils;
import com.awspure.system.util.StrUtils;

public class AppWebImpl extends BaseActionsoftWeb implements AppWeb {

	private static final long serialVersionUID = 1L;

	public AppWebImpl(UserContext uc) {
		super(uc);
	}

	@Override
	public String getTaskList(int size, int page, int type, String gxzx, String params) {
		UserContext context = getContext();
		MobileService mobileService = new MobileService();
		int start = size * (page - 1);

		// 待办或者已办
		boolean isHistory = type == 1 ? true : false;

		JSONObject root = createErrorJson();
		JSONArray data = new JSONArray();

		try {
			// 获取数据分页
			Page<TaskBean> taskPage = mobileService.getTaskListPageBy(context.getUID(), 1, gxzx, params, start, size, isHistory);
			List<TaskBean> taskList = taskPage.getDataList();
			
			D.log("task size =" + taskList.size());
			if (taskList.isEmpty()) {
				root.put("page", page);
				root.put("total", 0);
				root.put("pageCount", 0);
				root.put("data", data);
			} else {
				int total = taskPage.getTotal();
				int pageCount = (int) Math.ceil(total / size);
				root.put("page", page);
				root.put("total", total);
				root.put("pageCount", pageCount);

				// 分页算法
				for (TaskBean taskBean : taskList) {
					JSONObject item = new JSONObject();
					item.put("taskId", taskBean.getId());
					item.put("isRead", taskBean.isRead());
					item.put("processId", taskBean.getBindId());
					item.put("taskTypeId", taskBean.getWfId());
					item.put("isEnd", taskBean.isEnd());
					// 已变更为流程标题
					item.put("taskTitle", taskBean.getTitle() + "-" + taskBean.getBindId());
					item.put("taskStatus", taskBean.getStatus());
					item.put("nodeId", taskBean.getStepNo());
					item.put("nodeName", taskBean.getStepName());
					String createDate = taskBean.getCreateDate();
					String positionName = taskBean.getPositionName();
					item.put("createDate", createDate);
					item.put("positionName", positionName);
					item.put("uuid", taskBean.getUuid());
					//2016-09-21增加表单是否可编辑
					item.put("isModify", taskBean.isModify());
					item.put("isGxzx", taskBean.getIsGxzx());
					
					data.add(item);
				}
				root.put("data", data);
			}

			root.put("status", 200);
			root.put("errmsg", "调用成功");

		} catch (Exception e) {
			e.printStackTrace();
			if (e != null)
				root.put("errmsg", e.getMessage());
		} finally {
			mobileService.closeConn();
		}
		return root.toString();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public String getTaskDetail(String taskId, String taskTypeId, String stepNo, String type) {
		MobileService mobileService = new MobileService();
		JSONObject root = createErrorJson();
		try {
			TaskBean taskBean = mobileService.getTaskBeanByType(taskId, type);

			root.put("taskName", taskBean.getTitle());
			root.put("processName", taskBean.getFlowName());
			root.put("taskId", taskId);
			root.put("processId", taskBean.getBindId());
			root.put("taskStatus", taskBean.getStatus());
			root.put("uuid", taskBean.getUuid());

			// 只能在PC端处理的标记
			root.put("onlyPc", false);
			root.put("onlyPcMsg", "");
			
			/*
			 * 增加节点扩展属性不在待办列表显示 开始 APP-LIST-OFF :待办列表不显示该节点 APP-DETAIL-OFF
			 * :任务详细页提示该任务需在PC端处理
			 */
			if (StrUtils.isNotBlank(taskBean.getExtendId()) && taskBean.getExtendId().equals("APP-DETAIL-OFF")) {
				root.put("onlyPc", true);
				root.put("onlyPcMsg", "该节点被设置为需到PC端处理");
			}
			// 增加节点扩展属性不在待办列表显示 结束

			// 包含子流程的节点只能在PC端处理
			if (taskBean.getSubId() > 0) {
				// 疑似包含子流程 - 需要验证是否按条件触发
				Vector subProcessProfiles = WorkflowTaskEngine.getInstance().getSubProcessProfilesOfRules(getContext(),
						taskBean.getBindId(), taskBean.getId());
				if (subProcessProfiles != null && !subProcessProfiles.isEmpty()) {
					D.out("检查子流程:" + subProcessProfiles.size());
					root.put("onlyPc", true);
					root.put("onlyPcMsg", "该节点包含子流程,需到PC端处理");
				}
			}

			// 加签未回复的无法处理 - 采用此参数控制
			if (taskBean.getStatus() == 4 || taskBean.getStatus() == 11) {
				// 判断为加签未回复
				int cc = mobileService.getCountByTaskId(taskBean.getId());
				if (cc > 0) {
					root.put("onlyPc", true);
					root.put("onlyPcMsg", "加签人员未回复,请稍后办理");
				} else if (taskBean.getStatus() == 4) {
					// 判断为等待
					root.put("onlyPc", true);
					root.put("onlyPcMsg", "该节点在等待中,请稍后办理");
				}
			}

			// 审核菜单
			JSONObject myMenu = new JSONObject();
			List<HashMap> menuList = getAuditMenu(getContext(), Integer.parseInt(taskTypeId), Integer.parseInt(taskId),
					Integer.parseInt(stepNo), false);
			if (menuList != null && menuList.size() > 0) {
				myMenu.put("tabName", "我的菜单");
				JSONArray actions = new JSONArray();
				for (HashMap menu : menuList) {
					JSONObject jac = new JSONObject();
					jac.put("key", menu.get("key"));
					jac.put("value", menu.get("value"));
					actions.add(jac);
				}
				myMenu.put("actions", actions);
			}
			root.put("myMenu", myMenu);

			//操作按钮
			JSONObject executeMenu = new JSONObject();
			List<HashMap> executeMenuList = getExecuteMenu(getContext(), Integer.parseInt(taskTypeId), Integer.parseInt(taskId),
					Integer.parseInt(stepNo), false);
			if (executeMenuList != null && executeMenuList.size() > 0) {
				executeMenu.put("tabName", "操作按钮");
				JSONArray toolBars = new JSONArray();
				for (HashMap menu : executeMenuList) {
					JSONObject jac = new JSONObject();
					jac.put("menuName", menu.get("menuName"));
					toolBars.add(jac);
				}
				executeMenu.put("toolBars", toolBars);
			}
			root.put("executeMenu", executeMenu);
			// 设置已读
			ProcessRuntimeDaoFactory.createTaskInstance().changeTaskReadTag(Integer.valueOf(taskId));

			root.put("status", 200);
			root.put("errmsg", "调用成功");
		} catch (Exception e) {
			e.printStackTrace();
			if (e != null)
				root.put("errmsg", e.getMessage());
		} finally {
			mobileService.closeConn();
		}
		return root.toString();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public String doExecuteTask(String action, String comment, int processInstanceId, int taskInstanceId) {
		UserContext me = getContext();
		Connection conn = DBSql.open();
		JSONObject root = createErrorJson();
		try {
			// 验证等待中的任务 - 共享connection
			MobilePlusService mpService = new MobilePlusService();
			TaskBean waiteTb = mpService.getWaitingByTaskId(conn, taskInstanceId);
			D.out("验证等待中的任务:" + waiteTb.getStatus());
			if (waiteTb != null) {
				if (waiteTb.getStatus() == 4 || waiteTb.getStatus() == 11) {
					// 判断为加签未回复
					if (waiteTb.getWaitCount() > 0) {
						root.put("status", 500);
						root.put("errmsg", "加签人员未回复,请稍后办理");
						return root.toString();
					} else if (waiteTb.getStatus() == 4) {
						// 判断为等待
						root.put("status", 500);
						root.put("errmsg", "该节点在等待中,请稍后办理");
						return root.toString();
					}
				}
			}

			WorkflowInstanceAPI wfApi = WorkflowInstanceAPI.getInstance();
			WorkflowTaskInstanceAPI wftApi = WorkflowTaskInstanceAPI.getInstance();
			ProcessInstanceModel processInstanceModel = ProcessRuntimeDaoFactory.createProcessInstance().getInstance(processInstanceId);
			if (processInstanceModel == null) {
				root.put("status", 500);
				root.put("errmsg", "流程实例不存在。");
				return root.toString();
			}
			WorkFlowStepModel workFlowNowStepModel = (WorkFlowStepModel) WorkFlowStepCache.getModelOfStepNo(processInstanceModel
					.getProcessDefinitionId(),
					processInstanceModel.getActivityDefinitionNo() == 0 ? 1 : processInstanceModel.getActivityDefinitionNo());
			
			// 抢先接收
			int pCount = DBSql.getInt(conn, "select count(*) as c from wf_task where bind_id=" + processInstanceId, "c");
			int taskCount = DBSql.getInt(conn, "select count(*) as c from wf_task where id=" + taskInstanceId, "c");
			if (pCount > 1) {
				//当前节点为并签和个人抢先接收
				if(taskCount == 1){
					if (workFlowNowStepModel._routePointType == 1 && workFlowNowStepModel._isSelfDispose) {
						D.out("存在会签情况,需要抢先接收[pCount=" + pCount + "][taskCount=" + taskCount + "]");
						WorkFlowStepModel workFlowStepModel = WorkFlowStepCache.getModelOfStepNo(processInstanceModel.getProcessDefinitionId(),
								processInstanceModel.getActivityDefinitionNo());
						ProcessRuntimeDaoFactory.createTaskInstance().removeOtherTaskByTask(processInstanceId, taskInstanceId);
						int from_point = DBSql.getInt(conn, "select from_point from wf_task where bind_id=" + processInstanceId
								+ " and target='" + me.getUID() + "'", "from_point");
						String delSql = "DELETE FROM wf_messagepoint WHERE PARENT_ID=" + processInstanceId + " AND wfs_ID="
								+ Integer.toString(workFlowStepModel._id) + " AND ID!=" + from_point;
						DBSql.executeUpdate(conn, delSql);
					} 
				}else { 
					root.put("status", 500);
					root.put("errmsg", "当前任务不存在或已被办理！");
					return root.toString();
				}
			}

			// 办理1获取审批可选列表
			String auditName = "";
			int auditType = -99;
			int auditId = 0;
			if (action != null && !action.equals("")) {
				String[] menu = action.split("/");
				auditName = menu[0];
				auditType = Integer.parseInt(menu[1]);
				auditId = Integer.parseInt(menu[2]);
			}
			D.out(">>>action=" + action);
			D.out(">>>auditName=" + auditName);
			D.out(">>>auditType=" + auditType);
			D.out(">>>auditId=" + auditId);

			FormModel formModel = (FormModel) WorkFlowStepBindReportCache.getWorkFlowStepDefaultForm(workFlowNowStepModel._id);
			SheetModel sheetModel = (SheetModel) SheetCache.getMastSheetModel(formModel.getId());
			MetaDataModel metaDataModel = (MetaDataModel) MetaDataCache.getModel(sheetModel.getMetaDataId());
			if (formModel == null || sheetModel == null || metaDataModel == null) {
				root.put("status", 500);
				root.put("errmsg", "表单不存在，此节点需到PC端处理。");
				return root.toString();
			}

			UserTaskAuditMenuModel model = new UserTaskAuditMenuModel();
			model.setAuditType(auditType);
			model.setAuditMenuName(auditName);
			model.setOpinion(AjaxDataDecode.getInstance().decode(comment));
			model.setTaskInstanceId(taskInstanceId);
			model.setId(auditId);
			RuntimeFormManager web = new RuntimeFormManager(me, processInstanceId, taskInstanceId, 1, 0, 0);
			if (!web.saveAuditDataNoPage(model).equals("true")) {
				root.put("status", 500);
				root.put("errmsg", "暂存审核菜单的选项和留言错误。");
				return root.toString();
			}
			D.out(">>>保存菜单ok");
			int businessObjectId = DBSql.getInt(conn, "select id from " + metaDataModel.getEntityName() + " where bindid="
					+ processInstanceModel.getId(), "id");
			// 触发VALIDATE事件
			boolean isOk = FormEventHandler.handleEvent(getContext(), ActivityDefinitionConst.TRIGGER_TASK_TRANSACTION_VALIDATE,
					processInstanceId, taskInstanceId, workFlowNowStepModel._flowId, workFlowNowStepModel._id,
					sheetModel != null ? sheetModel.getFormId() : 0, businessObjectId, new Hashtable(), metaDataModel == null ? null
							: metaDataModel.getEntityName());
			if (!isOk) {
				// 校验被阻止继续向下进行,并给出当前用户的一些提醒信息
				String alt = MessageQueue.getInstance().getMessage(getContext().getUID());
				root.put("status", 500);
				root.put("errmsg", alt + ",此节点需到PC端处理。");
				return root.toString();
			}
			if (auditType == -2 || auditType == -4) {
				root.put("status", 500);
				root.put("errmsg", "此节点需到PC端处理。");
				return root.toString();
			}
			if (auditType == -3) {// 终止流程
				WorkflowTaskEngine.getInstance().appendOpinionHistory(me, processInstanceId, taskInstanceId, model);
				WorkflowEngine.getInstance().closeProcessInstance(me, processInstanceId, taskInstanceId);
				root.put("status", 201);
				root.put("errmsg", "流程已经成功终止。");
				return root.toString();
			}

			D.out(">>>获得节点号");
			int stepNo = wftApi.getNextStepNo(me.getUID(), processInstanceId, taskInstanceId);
			D.out("stepNo:" + stepNo);
			String assignCode = WorkflowTaskEngine.getInstance().assignComplexProcessTaskInstance(me, processInstanceId, taskInstanceId);
			D.out("assignCode:" + assignCode);
			if (assignCode.equals("processEnd")) {// 最后节点,直接结束流程
				WorkflowTaskEngine.getInstance().closeProcessTaskInstance(me, processInstanceId, taskInstanceId);
				wfApi.closeProcessInstance(me.getUID(), processInstanceId, taskInstanceId);
				// 此处不是错误,是流程已经运行完毕
				root.put("status", 201);
				root.put("errmsg", "恭喜!当前流程已执行完成。");
			} else if (assignCode.equals("taskEnd")) {
				root.put("status", 200);
				root.put("errmsg", "当前任务已办理完毕。");
			} else if (assignCode.equals("assign")) {// 正常继续办理
				if ((stepNo == -1) || (stepNo == 9999)) {// 理应结束流程并归档---异常（容错处理）
					WorkflowTaskEngine.getInstance().appendOpinionHistory(me, processInstanceId, taskInstanceId, model);
					WorkflowEngine.getInstance().closeProcessInstance(me, processInstanceId, taskInstanceId);
					root.put("status", 201);
					root.put("errmsg", "流程已经成功终止。");
					return root.toString();
				}
				String participants = wftApi.getActivityParticipants(me.getUID(), processInstanceId, taskInstanceId, stepNo);
				D.out("审批人：participants:" + participants);

				/* 容错处理-当API无法获取时 */
				if (participants == null || participants.isEmpty()) {
					StringBuilder stepUserSql = new StringBuilder();
					stepUserSql.append("select stepuser,steptogetheruser from sysflowstep where flow_id=");
					stepUserSql.append(workFlowNowStepModel._flowId);
					stepUserSql.append(" and stepno=");
					stepUserSql.append(stepNo);

					D.err("re participants:" + stepUserSql.toString());
					String[] fields = new String[] { "stepuser", "steptogetheruser" };
					Map<String, String> data = SqlUtils.getInstance().getMapBySql(conn, stepUserSql.toString(), fields);
					// 同时获取审批人和候选人
					if (data != null && !data.isEmpty()) {
						StringBuilder reParticipants = new StringBuilder();

						String stepUser = data.get("stepuser");
						D.err("re stepUser:" + stepUser);
						if (StrUtils.isNotBlank(stepUser)) {
							if (stepUser.startsWith("@")) {
								reParticipants.append(RuleAPI.getInstance().executeRuleScript(stepUser, me, processInstanceId,
										taskInstanceId));
							} else {
								reParticipants.append(stepUser);
							}
						}

						String stepTogetherUser = data.get("steptogetheruser");
						D.err("re stepTogetherUser:" + stepTogetherUser);
						if (StrUtils.isNotBlank(stepTogetherUser)) {
							if (reParticipants.length() > 0) {
								reParticipants.append(" ");
							}
							if (stepTogetherUser.startsWith("@")) {
								reParticipants.append(RuleAPI.getInstance().executeRuleScript(stepTogetherUser, me, processInstanceId,
										taskInstanceId));
							} else {
								reParticipants.append(stepTogetherUser);
							}
						}
						participants = reParticipants.toString();
					}
					D.out("重新获得审批人：participants:" + participants);
				}

				// 获取下一个节点的相关设置
				WorkFlowStepModel workFlowNextStepModel = (WorkFlowStepModel) WorkFlowStepCache.getModelOfStepNo(
						processInstanceModel.getProcessDefinitionId(), stepNo);
				String taskTitle = "(" + workFlowNextStepModel._stepName + ")" + processInstanceModel.getTitle();

				// 获取是否禁止选择审批人
				root.put("approverEditable", true);
				String strRText = workFlowNextStepModel._routeText;
				if (StrUtils.isNotBlank(strRText)) {
					D.out("节点设置是否禁选:" + strRText);
					JSONObject routeText = JSONObject.fromObject(strRText);
					String multiSelect = routeText.getString("multiSelect");
					String msdcb = routeText.getString("multiSelectDisabledCheckBox");
					if (multiSelect.equals("0") && msdcb.equals("1")) {
						root.put("approverEditable", false);
					}
				}
				// 最终JSON
				root.put("status", 200);
				root.put("errmsg", "");
				root.put("taskTitle", taskTitle);
				JSONArray apja = new JSONArray();
				if (StrUtils.isNotBlank(participants)) {
					if (participants.contains(" ")) {
						String[] split = participants.split(" ");
						for (String user : split) {
							JSONObject ap = new JSONObject();
							ap.put("key", substrUserId(user));
							ap.put("value", user);
							apja.add(ap);
						}
					} else {
						JSONObject ap = new JSONObject();
						ap.put("key", substrUserId(participants));
						ap.put("value", participants);
						apja.add(ap);
					}
				}
				root.put("approverList", apja);
			}
		} catch (Exception e) {
			root.put("status", 500);
			root.put("errmsg", e.getMessage());
		} finally {
			DBSql.close(conn, null, null);
		}
		/* 此处MessageQueue.getInstance()有特殊消息,99路由未返回人 */
		String message = MessageQueue.getInstance().getMessage(me.getUID());
		if (message != null && !message.isEmpty()) {
			root.put("status", 500);
			root.put("errmsg", StrUtils.getFirstMessageByN(message));
		}
		/* 返回JSON数据 */
		return root.toString();
	}

	@Override
	public String getTaskHistoryList(int processInstanceId) {
		JSONObject root = createErrorJson();
		JSONArray jaData = new JSONArray();
		Statement stmt = null;
		Connection conn = null;
		MobileService dao = new MobileService();
		try {
			conn = dao.getConn();
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT * FROM (select wfs_id,a.auditdesc stepname,create_user,b.username,create_date,isagree,opinion ");
			sql.append("from wf_messageopinion a ");
			sql.append("left join orguser b on b.userid=a.create_user ");
			sql.append("where parent_id=");
			sql.append(processInstanceId);
			sql.append(" ORDER BY CREATE_DATE ASC) union all ");
			//sql.append("select a.wfsid,b.stepname,a.TARGET, c.USERNAME,if(a.read_task=0,a.BEGINTIME,a.readtime) create_date,");
			sql.append("select a.wfsid,b.stepname,a.TARGET, c.USERNAME,decode(a.read_task,0,a.BEGINTIME,a.readtime) create_date,");
			//sql.append("if(a.read_task=0,'未读','待审批') isagree,'' opinion ");
			sql.append("cast(decode(a.read_task,0,'未读','待审批') as NVARCHAR2(2000)) isagree,cast('' as NVARCHAR2(2000)) opinion ");
			sql.append("from wf_task a ");
			sql.append("left join sysflowstep b on b.id=a.WFSID ");
			sql.append("left join orguser c on c.USERID=a.TARGET ");
			sql.append("where a.status!=9 and a.bind_id=");
			sql.append(processInstanceId);
			D.out(sql.toString());

			stmt = conn.createStatement();
			ResultSet rest = stmt.executeQuery(sql.toString());
			if (rest != null) {
				int i = 1;
				while (rest.next()) {
					JSONObject jo = new JSONObject();
					jo.put("id", "history" + i);
					JSONArray jaVal = new JSONArray();

					JSONObject stpName = new JSONObject();
					stpName.put("key", "流程节点");
					stpName.put("value", getRsString2I18n(rest, "stepname"));
					jaVal.add(stpName);

					JSONObject username = new JSONObject();
					username.put("key", "审批人");
					username.put("value", getRsString(rest, "username") + "&nbsp;" + getRsString(rest, "create_user"));
					jaVal.add(username);

					JSONObject isagree = new JSONObject();
					String agreeValue = getRsString(rest, "isagree");
					isagree.put("key", "审批操作");
					isagree.put("value", agreeValue);
					jaVal.add(isagree);

					JSONObject cdate = new JSONObject();
					if (agreeValue.equals("未读")) {
						cdate.put("key", "到达时间");
					} else if (agreeValue.equals("待审批")) {
						cdate.put("key", "阅读时间");
					} else {
						cdate.put("key", "审批时间");
					}
					cdate.put("value", getRsDateString(rest, "create_date"));
					jaVal.add(cdate);

					JSONObject opinion = new JSONObject();
					opinion.put("key", "审批内容");
					String opinion1 = getRsString(rest, "opinion");
					try {
						opinion1 = RepleaseKey.replaceI18NTag(this.getContext().getLanguage(), opinion1);
					} catch (Exception e) {
						e.printStackTrace();
					}

					opinion1 = StrUtils.string2Json(StrUtils.formatOpinion(opinion1));
					opinion.put("value", opinion1);
					jaVal.add(opinion);

					jo.put("value", jaVal);
					jaData.add(jo);
					i++;
				}
			}

			root.put("status", 200);
			root.put("errmsg", "调用成功");
		} catch (Exception e1) {
			e1.printStackTrace();
			root.put("errmsg", "错误信息："+e1.getMessage());
		} finally {
			dao.closeConn();
		}
		root.put("data", jaData);
		return root.toString();
	}

	@Override
	public String processTask(int processInstanceId, int taskInstanceId, String participants) {
		UserContext user = getContext();
		JSONObject root = createErrorJson();

		// 正常处理
		int localDepartmentId = user.getDepartmentModel().getId();
		int hashNextNo = WorkflowTaskEngine.getInstance().getJumpStepNoOfRules(user, processInstanceId, taskInstanceId);
		ProcessInstanceModel instanceModel = ProcessRuntimeDaoFactory.createProcessInstance().getInstance(processInstanceId);
		TaskInstanceModel taskInstanceModel = ProcessRuntimeDaoFactory.createTaskInstance().getInstanceOfActive(taskInstanceId);
		try {
			int[] tid = null;
			try {
				if (StrUtils.isBlank(participants)) {
					root.put("status", 500);
					root.put("errmsg", "必须指定下个任务的办理人。");
					return root.toString();
				}
				if (instanceModel == null) {
					root.put("status", 500);
					root.put("errmsg", "该条流程已经被删除。");
					return root.toString();
				}
				if (taskInstanceModel == null) {
					root.put("status", 500);
					root.put("errmsg", "该任务已经结束。");
					return root.toString();
				}
				// 验证等待中的任务 - 独立connection
				MobilePlusService mpService = new MobilePlusService();
				TaskBean waiteTb = mpService.getWaitingByTaskId(taskInstanceId);
				if (waiteTb != null) {
					if (waiteTb.getStatus() == 4 || waiteTb.getStatus() == 11) {
						// 判断为加签未回复
						if (waiteTb.getWaitCount() > 0) {
							root.put("status", 500);
							root.put("errmsg", "加签人员未回复,请稍后办理");
							return root.toString();
						} else if (waiteTb.getStatus() == 4) {
							// 判断为等待
							root.put("status", 500);
							root.put("errmsg", "该节点在等待中,请稍后办理");
							return root.toString();
						}
					}
				}

				// 转换逗号分割为空格分割
				if (participants.contains(",")) {
					participants = participants.replaceAll(",", " ");
				}

				// 检查办理人是否存在
				String success = Function.checkAddress(participants);
				if (!success.equals("ok")) {
					root.put("status", 500);
					root.put("errmsg", "办理人参数错误。");
					return root.toString();
				}

				// 下一个节点的信息
				WorkFlowStepModel wfsModel = WorkFlowStepCache.getModelOfStepNo(instanceModel.getProcessDefinitionId(), hashNextNo);
				int stepLimitMore = wfsModel._stepLimitMore;// 下个节点最多办理人数
				int stepLimitLess = wfsModel._stepLimitLess;// 下个节点最少办理人数
				String[] participant = participants.split(" ");
				if (stepLimitLess > 0 && participant.length < stepLimitLess) {
					root.put("status", 500);
					root.put("errmsg", "该流程下一节点最少需要[" + stepLimitLess + "]人办理！");
					return root.toString();
				}
				if (stepLimitMore > 0 && participant.length > stepLimitMore) {
					root.put("status", 500);
					root.put("errmsg", "该流程下一节点最多只允许[" + stepLimitMore + "]人办理！");
					return root.toString();
				}
				int priority = 1;// 优先级
				// 获取下一个任务是串签还是并签
				int runStyle = wfsModel._routePointType;
				int status = taskInstanceModel.getStatus();
				String title = "(" + wfsModel._stepName + ")" + instanceModel.getTitle();
				int nextStepNo = hashNextNo;

				// 新程序
				boolean isSuccessfully = WorkflowTaskEngine.getInstance().closeProcessTaskInstance(user, processInstanceId, taskInstanceId);
				if (isSuccessfully) {// 成功
					ProcessRuntimeDaoFactory.createTaskInstance().removeOtherTaskByTask(processInstanceId, taskInstanceId);

					// 创建待办任务
					tid = WorkflowTaskEngine.getInstance().createProcessTaskInstance(user, processInstanceId, new SynType(runStyle),
							new PriorityType(priority), status, nextStepNo, participants, title, false, localDepartmentId);

					if (tid != null && tid.length > 0) {
						D.out("任务发送成功!");
						D.out(tid);
					} else {
						root.put("status", 500);
						root.put("errmsg", "任务创建失败,请联系管理员");
						return root.toString();
					}

				} else {// 不成功
					root.put("status", 500);
					root.put("errmsg", "任务不能结束,数据没有办理完毕!");
					return root.toString();
				}

			} catch (WorkflowException e) {
				root.put("status", 500);
				root.put("errmsg", e.getMessage());
				return root.toString();
			}

			// 任务成功
			root.put("status", 200);
			root.put("errmsg", "流程已经成功发送给" + participants);
		} catch (Exception e) {
			root.put("status", 500);
			root.put("errmsg", "出现系统错误ERROR:" + e.getMessage() + ",请联系管理员！");
		}
		return root.toString();
	}

	@Override
	public String getUserList(int size, int page, String query) {
		String userId = getContext().getUID();
		JSONObject root = createSuccessJson();
		JSONArray list = new JSONArray();
		root.put("size", size);
		root.put("page", page);
		int start = size * (page - 1);
		root.put("start", start);
		/* 按条件查询 */
		if (StrUtils.isNotBlank(query)) {
			List<UserModel> umList = queryUser(query, userId);
			int total = umList.size();
			int pageCount = (int) Math.ceil(total / size);
			root.put("total", total);
			root.put("pageCnt", pageCount);

			int index = 1;
			for (UserModel um : umList) {
				// 第一页特殊
				if (index > start || (index == start && page == 1)) {
					JSONObject item = createUserItem(um);
					list.add(item);
				}
				if (index >= (start + size)) {
					break;
				}
				index++;
			}
			root.put("data", list);
		} else {
			/* 普通分页 */
			List<UserModel> umList = queryUser(userId);
			int total = umList.size();
			int pageCount = (int) Math.ceil(total / size);
			root.put("total", total);
			root.put("pageCnt", pageCount);

			for (int i = 0; i < umList.size(); i++) {
				// 第一页特殊
				if (i > start || (i == start && page == 1)) {
					UserModel um = umList.get(i);
					JSONObject item = createUserItem(um);
					list.add(item);
				}
				if (i >= (start + size)) {
					break;
				}
			}
			root.put("data", list);
		}
		return root.toString();
	}

	@Override
	public String appendTask(int taskId, int processId, String participant, String comment) {
		long startTime = System.currentTimeMillis();
		JSONObject root = createErrorJson();
		StringBuilder msg = new StringBuilder();
		msg.append("appendTask:");
		msg.append(taskId);
		msg.append("-");
		msg.append(processId);
		msg.append("-");
		msg.append(participant);
		D.out(msg.toString());

		// 验证等待中的任务 - 独立connection
		MobilePlusService mpService = new MobilePlusService();
		TaskBean waiteTb = mpService.getWaitingByTaskId(taskId);
		D.out("验证等待中的任务:" + waiteTb.getStatus());
		if (waiteTb != null) {
			if (waiteTb.getStatus() == 4 || waiteTb.getStatus() == 11) {
				// 判断为加签未回复
				if (waiteTb.getWaitCount() > 0) {
					root.put("status", 500);
					root.put("errmsg", "加签人员未回复,请稍后办理");
					return root.toString();
				} else if (waiteTb.getStatus() == 4) {
					// 判断为等待
					root.put("status", 500);
					root.put("errmsg", "该节点在等待中,请稍后办理");
					return root.toString();
				}
			}
		}

		// 正常处理加签
		String ownerId = getContext().getUID();
		WorkflowTaskInstanceAPI wftApi = WorkflowTaskInstanceAPI.getInstance();

		StringBuilder sql = new StringBuilder();
		//sql.append("select concat(a.title,'___',b.STEPNAME) data ");
		sql.append("select a.title||'___'||b.STEPNAME data ");
		sql.append("from wf_task a left join sysflowstep b on b.ID=a.WFSID ");
		sql.append("where a.id=");
		sql.append(taskId);

		String data = DBSql.getString(sql.toString(), "data");
		String[] split = data.split("___");
		String title = "";
		if (split[0].startsWith("(加签)")) {
			title = split[0];
		} else {
			title = "(加签)" + split[0];
		}
		String stepName = split[1];
		try {
			// 加签留言
			if (StrUtils.isNotBlank(comment)) {
				UserTaskHistoryOpinionModel model = new UserTaskHistoryOpinionModel();
				model.setCreateUser(ownerId);
				model.setOpinion(comment + "<br><br><b><font color=red><I18N#发起加签></font></b>");
				model.setAuditMenuName("-");
				model.setProcessInstanceId(processId);
				model.setTaskInstanceId(new DBSequence().getSequence("SYS_WORKFLOWOPINION"));
				model.setAuditObject("<span style=font-size:14px><b><I18N#" + stepName + "></b></span>");
				ProcessRuntimeDaoFactory.createUserTaskHistoryOpinion().create(model);
			}

			D.out("加签备注" + (System.currentTimeMillis() - startTime) + "毫秒");

			// 转换逗号分割为空格分割
			if (participant.contains(",")) {
				participant = participant.replaceAll(",", " ");
			}

			// 发起加签
			int[] r = wftApi.appendProcessTaskInstance(ownerId, processId, taskId, participant, title);
			if (r.length > 0) {
				root.put("status", 200);
				root.put("errmsg", "调用成功");
			}
		} catch (AWSSDKException e) {
			e.printStackTrace();
		}

		D.out("加签完成" + (System.currentTimeMillis() - startTime) + "毫秒");
		return root.toString();
	}

	@Override
	public String ccTask(int taskId, int processId, String participant, String comment) {
		long startTime = System.currentTimeMillis();
		JSONObject root = createErrorJson();
		StringBuilder msg = new StringBuilder();
		msg.append("ccTask:");
		msg.append(taskId);
		msg.append("-");
		msg.append(processId);
		msg.append("-");
		msg.append(participant);
		D.out(msg.toString());

		String ownerId = getContext().getUID();
		WorkflowTaskInstanceAPI wftApi = WorkflowTaskInstanceAPI.getInstance();

		StringBuilder sql = new StringBuilder();
		//sql.append("select concat(a.title,'___',b.STEPNAME) data ");
		sql.append("select a.title||'___'||b.STEPNAME data ");
		sql.append("from wf_task a left join sysflowstep b on b.ID=a.WFSID ");
		sql.append("where a.id=");
		sql.append(taskId);

		String data = DBSql.getString(sql.toString(), "data");
		String[] split = data.split("___");
		String title = "(传阅)" + split[0];
		String stepName = split[1];
		try {
			// 传阅留言
			if (StrUtils.isNotBlank(comment)) {
				UserTaskHistoryOpinionModel model = new UserTaskHistoryOpinionModel();
				model.setCreateUser(ownerId);
				model.setOpinion(comment + "<br><br><b><font color=red><I18N#传阅></font></b>");
				model.setAuditMenuName("-");
				model.setProcessInstanceId(processId);
				model.setTaskInstanceId(new DBSequence().getSequence("SYS_WORKFLOWOPINION"));
				model.setAuditObject("<span style=font-size:14px><b><I18N#" + stepName + "></b></span>");
				ProcessRuntimeDaoFactory.createUserTaskHistoryOpinion().create(model);
			}

			D.out("传阅备注" + (System.currentTimeMillis() - startTime) + "毫秒");

			// 转换逗号分割为空格分割
			if (participant.contains(",")) {
				participant = participant.replaceAll(",", " ");
			}

			// 发起
			int[] r = wftApi.createCCProcessTaskInstance(ownerId, processId, taskId, participant, title);
			if (r.length > 0) {
				root.put("status", 200);
				root.put("errmsg", "调用成功");
			}
		} catch (AWSSDKException e) {
			e.printStackTrace();
		}

		D.out("传阅完成" + (System.currentTimeMillis() - startTime) + "毫秒");
		return root.toString();
	}
	
	@SuppressWarnings("unused")
	@Override
	public String entrustTask(int processId, String participants){
		JSONObject root = createErrorJson();
		ProcessInstanceModel model = ProcessRuntimeDaoFactory.createProcessInstance().getInstance(processId);
		int taskId = DBSql.getInt("SELECT ID FROM WF_TASK WHERE BIND_ID="+processId, "ID");
		int stepNo = DBSql.getInt("SELECT STEPNO FROM SYSFLOWSTEP WHERE id =(SELECT WFSID FROM WF_TASK WHERE BIND_ID="+processId+")", "STEPNO");
	    WorkFlowStepModel workFlowStepModel = WorkFlowStepCache.getModelOfStepNo(model.getProcessDefinitionId(), stepNo);
	    TaskInstanceModel taskModel = ProcessRuntimeDaoFactory.createTaskInstance().getInstanceOfActive(taskId);
	    String  success = Function.checkAddress(participants.trim());
		
	    if(model == null){
	    	root.put("errmsg", "流程已经被删除，可能是因为该表单没有填写信息导致认为垃圾数据而删除或该流程实例已经被其他人删除");
	    	root.put("status", 500);
		} else if ((taskModel == null) && (model.isStart())) {
			root.put("errmsg", "该任务已经结束");
	    	root.put("status", 500);
		} else if (!success.equals("ok")) {
			root.put("errmsg", "账户不合法");
			root.put("status", 500);
	    } else {
	    	int runStyle = 1;
	    	if (workFlowStepModel._routePointType == 0){
	    		runStyle = 0;
	    	}
	    	try {
	    		WorkflowTaskEngine.getInstance().closeProcessTaskInstance(getContext(), processId, taskId);
	    		WorkflowTaskEngine.getInstance().createProcessTaskInstance(getContext(), processId, 
	    				new SynType(runStyle), new PriorityType(1), taskModel.getStatus(), stepNo, participants, "(委托办理)"+taskModel.getTitle(), false, 
	    				getContext().getDepartmentModel().getId());
	    		root.put("status", 200);
	    		root.put("errmsg", "调用成功");
	    	} catch (WorkflowException we) {
	    		root.put("errmsg", "委托办理异常，错误信息："+we.getMessage());
	    		root.put("status", 500);
	    	}
	    }
		return root.toString();
	}
	
	public String removeProcess(int processId){
		JSONObject root = createSuccessJson();
		try {
			WorkflowInstanceAPI.getInstance().removeProcessInstance(processId);
		} catch (AWSSDKException e) {
			root.put("errmsg", "删除流程失败，错误信息："+e.getMessage());
    		root.put("status", 500);
			e.printStackTrace();
		}
		return root.toString();
	}
	
	@Override
	public String assignTask(int taskId, int processId, String comment) {
		JSONObject root = createErrorJson();
		StringBuilder msg = new StringBuilder();
		msg.append("assignTask:");
		msg.append(taskId);
		msg.append("-");
		msg.append(processId);
		msg.append("-");
		msg.append(comment);
		D.out(msg.toString());

		// 判断是否能回复 - 开始
		MobileService mobileService = new MobileService();
		int cc = mobileService.getCountByTaskId(taskId);
		if (cc > 0) {
			root.put("errmsg", "加签人员未回复,请稍后办理");
			return root.toString();
		}
		// 判断是否能回复 - 结束

		// 处理开始
		String userId = getContext().getUID();
		WorkflowTaskInstanceAPI wftApi = WorkflowTaskInstanceAPI.getInstance();

		try {

			// 保存留言
			if (StrUtils.isNotBlank(comment)) {
				UserTaskAuditMenuModel model = new UserTaskAuditMenuModel();
				model.setAuditType(0);
				model.setAuditMenuName("-");
				model.setOpinion(comment);
				model.setTaskInstanceId(taskId);
				model.setId(0);
				RuntimeFormManager web = new RuntimeFormManager(getContext(), processId, taskId, 1, 0, 0);
				String r0 = web.saveAuditDataNoPage(model);
				if (StrUtils.isNotBlank(r0) && r0.equals("true")) {
					// 加签通过
					boolean r = wftApi.closeAppendProcessTaskInstance(userId, processId, taskId);
					if (r) {
						root.put("status", 200);
						root.put("errmsg", "调用成功");
					}
				}
			} else {
				// 加签通过
				boolean r = wftApi.closeAppendProcessTaskInstance(userId, processId, taskId);
				if (r) {
					root.put("status", 200);
					root.put("errmsg", "调用成功");
				}
			}
		} catch (AWSSDKException e) {
			e.printStackTrace();
		}
		// 处理结束
		return root.toString();
	}

	public String loginNoPassword(String username){
		D.out(username + " Login!");
		JSONObject root = this.createErrorJson();
		if (StrUtils.isNotBlank(username) && StrUtils.isNotBlank(username)) {
			SessionAPI sApi = SessionAPI.getInstance();
			try {
				String sid = sApi.createSessionNoPassword(username, "cn", "127.0.0.1");
				root.put("sid", sid);
				root.put("status", 200);
				root.put("errmsg", "调用成功");
			} catch (AWSSDKException e) {
				if (e != null)
					root.put("errmsg", e.getMessage());
				e.printStackTrace();
			}
		} else {
			root.put("errmsg", "参数错误");
		}
		return root.toString();
	}
	
	@Override
	public String login(String username, String password) {
		D.out(username + " Login!");
		JSONObject root = this.createErrorJson();
		if (StrUtils.isNotBlank(username) && StrUtils.isNotBlank(username)) {
			SessionAPI sApi = SessionAPI.getInstance();
			try {
				String sid = sApi.createSession(username, password, "cn", "127.0.0.1");
				root.put("sid", sid);
				root.put("status", 200);
				root.put("errmsg", "调用成功");
			} catch (AWSSDKException e) {
				if (e != null)
					root.put("errmsg", e.getMessage());
				e.printStackTrace();
			}
		} else {
			root.put("errmsg", "参数错误");
		}
		return root.toString();
	}

	@Override
	public String logout() {
		JSONObject root = this.createErrorJson();
		String sid = getContext().getSessionId();
		D.out(getContext().getUID() + " Logout!");
		if (StrUtils.isNotBlank(sid) && StrUtils.isNotBlank(sid)) {
			SessionAPI sApi = SessionAPI.getInstance();
			boolean success = sApi.close(sid);
			if (success) {
				root.put("status", 200);
				root.put("errmsg", "调用成功");
			}
		}
		return root.toString();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public String getMyInfo() {
		JSONObject root = createSuccessJson();

		UserModel um = getContext().getUserModel();
		DepartmentModel dm = getContext().getDepartmentModel();
		root.put("userName", um.getUserName());
		root.put("userId", um.getUID());
		root.put("userNo", um.getUserNo());
		root.put("departmentName", dm.getDepartmentName());
		root.put("positionName", um.getPositionName());

		Hashtable h = new Session().getLogList(getContext().getUID());
		if (h != null) {
			SessionModel model = (SessionModel) h.get(new Integer(1));
			if (model != null) {
				String loginTime = UtilDate.datetimeFormat(new Date(model._startTime));
				String logoutTime = UtilDate.datetimeFormat(new Date(model._refreshTime));

				root.put("lastLogin", loginTime);
				root.put("lastLogout", logoutTime);
			}
		}

		StringBuilder photoDir = new StringBuilder();
		photoDir.append(AWFConfig._awfServerConf.getDocumentPath());
		photoDir.append("Photo/group");
		photoDir.append(um.getUID());
		photoDir.append("/file0/");
		photoDir.append(um.getUID());
		photoDir.append(".jpg");

		File photoFile = new File(photoDir.toString());
		if (photoFile.exists()) {
			String sid = getContext().getSessionId();
			StringBuilder photoUrl = new StringBuilder();
			photoUrl.append("/workflow/downfile.wf?flag1=");
			photoUrl.append(um.getUID());
			photoUrl.append("&flag2=0&sid=");
			photoUrl.append(sid);
			photoUrl.append("&rootDir=Photo&filename=");
			photoUrl.append(um.getUID());
			photoUrl.append(".jpg");
			photoUrl.append("");

			root.put("photoUrl", photoUrl.toString());
		} else {
			root.put("photoUrl", "");
		}

		return root.toString();
	}

	@Override
	public String getHello() {
		StringBuilder reslut = new StringBuilder();
		UserModel um = getContext().getUserModel();
		String hello = DateUtils.getHello();
		reslut.append(um.getUserName());
		reslut.append(" ");
		reslut.append(hello);
		return reslut.toString();
	}

	@SuppressWarnings("rawtypes")
	public List<HashMap> getExecuteMenu(UserContext me, int workflowId, int taskId, int stepNo, boolean isHiddenMenu){
		List<HashMap> menuList = new ArrayList<HashMap>();
		UserTaskAuditMenuModel model = (UserTaskAuditMenuModel) ProcessRuntimeDaoFactory.createUserTaskAuditMenu()
				.getInstanceOfTask(taskId);
		if (model == null) {
			model = new UserTaskAuditMenuModel();
		}
		WorkFlowStepModel stepModel = WorkFlowStepCache.getModelOfStepNo(workflowId, stepNo);
		if (stepModel == null) {
			stepModel = new WorkFlowStepModel();
		}
		
		/* 是否允许委托办理 */
		boolean isWeiTuo = stepModel._isStepTransmit;
		if(isWeiTuo){
			Map<String, String> map = new HashMap<String, String>();
			map.put("menuName", "转办");
			menuList.add((HashMap) map);
		}
		
		String[] menus = {"阅办","会签","加签","沟通"};
		String addParticipantsType = stepModel._addParticipantsType;
		if(StrUtils.isNotBlank(addParticipantsType)){
			for(String i : addParticipantsType.split(" ")){
				Map<String, String> map = new HashMap<String, String>();
				map.put("menuName", menus[Integer.parseInt(i)]);
				menuList.add((HashMap) map);
			}
		}
		return menuList;
	}
	
	@SuppressWarnings("rawtypes")
	public List<HashMap> getAuditMenu(UserContext me, int workflowId, int taskId, int stepNo, boolean isHiddenMenu) {
		List<HashMap> menuList = new ArrayList<HashMap>();
		UserTaskAuditMenuModel model = (UserTaskAuditMenuModel) ProcessRuntimeDaoFactory.createUserTaskAuditMenu()
				.getInstanceOfTask(taskId);
		if (model == null) {
			model = new UserTaskAuditMenuModel();
		}

		WorkFlowStepModel stepModel = WorkFlowStepCache.getModelOfStepNo(workflowId, stepNo);
		if (stepModel == null) {
			stepModel = new WorkFlowStepModel();
		}
		if (!stepModel._isAudit) {
			return menuList;// 未定义审核菜单
		} else {
			WorkFlowStepModel tmpStepModel = (WorkFlowStepModel) WorkFlowStepCache.getModelOfStepNo(workflowId, stepNo);
			Hashtable opinionList = WorkFlowStepOpinionCache.getListOfWorkFlowOpinion2(workflowId, tmpStepModel._id);
			if (opinionList.size() != 0 && !isHiddenMenu) {
				for (int i = 0; i < opinionList.size(); i++) {
					WorkFlowStepOpinionModel opinionModel = (WorkFlowStepOpinionModel) opinionList.get(new Integer(i));
					if (opinionModel._opinionType != -2 && opinionModel._opinionType != -4) {// 过滤手机端办理不了的办理按钮
						Map<String, String> map = new HashMap<String, String>();
						map.put("key", I18nRes.findValue(me.getLanguage(), opinionModel._opinionName.trim()));
						map.put("value", opinionModel._opinionName + "/" + opinionModel._opinionType + "/" + model.getId());
						menuList.add((HashMap) map);
					}
				}
			}
			
		}
		return menuList;
	}

	private String substrUserId(String userId) {
		if (userId.contains("<")) {
			return userId.split("<")[0];
		}
		return userId;
	}

	/* 空返回空串 */
	private String getRsString(ResultSet rest, String fieldName) {
		String result = "";
		try {
			result = rest.getString(fieldName);
			if (result == null || result.isEmpty()) {
				result = "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/* 过滤掉I18n标签<span style=font-size:14px><b><I18N#部门三审></b></span> */
	private String getRsString2I18n(ResultSet rest, String fieldName) {
		String result = "";
		try {
			result = rest.getString(fieldName);
			if (StrUtils.isNotBlank(result)) {
				int beginIndex = result.indexOf("<I18N#");
				int endIndex = result.indexOf("></b></span>");
				if (beginIndex != -1 && endIndex != -1) {
					result = result.substring(beginIndex + 6, endIndex);
				}
			} else {
				result = "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private String getRsDateString(ResultSet rest, String fieldName) {
		String result = "";
		try {
			Date date = rest.getTimestamp(fieldName);
			if (date != null) {
				result = DateUtils.dateToString(date);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<UserModel> queryUser(String query, String userId) {
		List<UserModel> umList = new ArrayList<UserModel>();
		Map<Integer, UserModel> userList = UserCache.getList();
		for (Integer key : userList.keySet()) {
			UserModel um = userList.get(key);
			if (userId.equals(um.getUID())) {
				continue;
			}
			// 模糊查询
			if (um.getUserName().contains(query) || um.getUID().contains(query)) {
				umList.add(um);
			}
		}
		return umList;
	}

	/**
	 * 将缓存中用户信息，由Map转换为List
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserModel> queryUser(String userId) {
		List<UserModel> umList = new ArrayList<UserModel>();
		Map<Integer, UserModel> userList = UserCache.getList();
		for (Integer key : userList.keySet()) {
			UserModel um = userList.get(key);
			if (userId.equals(um.getUID())) {
				continue;
			}
			umList.add(um);
		}
		return umList;
	}

	private JSONObject createUserItem(UserModel um) {
		JSONObject item = new JSONObject();
		item.put("id", um.getId());
		item.put("userName", um.getUserName());
		item.put("userId", um.getUID());
		DepartmentModel dm = (DepartmentModel) DepartmentCache.getModel(um.getDepartmentId());
		item.put("departmentName", dm.getDepartmentName());
		return item;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String createProcess(String uuid, String title, String varJson,String target) {
		String userId = getContext().getUID();
		JSONObject root = createErrorJson();
		WorkflowInstanceAPI wfApi = WorkflowInstanceAPI.getInstance();
		WorkflowTaskInstanceAPI wftApi = WorkflowTaskInstanceAPI.getInstance();
		try {
			// 创建流程
			int pId = wfApi.createProcessInstance(uuid, userId, title);
			root.put("processId", pId);
			// 保存变量
			if (StrUtils.isNotBlank(varJson)) {
				try {
					JSONObject varObject = JSONObject.fromObject(varJson);
					Hashtable<String, String> varTable = new Hashtable<String, String>();
					Iterator<String> iterator = varObject.keys();
					while (iterator.hasNext()) {
						String key = iterator.next();
						varTable.put(key, varObject.getString(key));
					}
					wfApi.assignVariables(pId, varTable);
					root.put("varCount", varTable.size());
				} catch (Exception e) {
					root.put("varCount", 0);
				}
			}
			// 创建任务
			int[] taskIds = null;
			if(StrUtils.isBlank(target)){
				taskIds = wftApi.createProcessTaskInstance(userId, pId, 1, userId, title);
			}else{
				taskIds = wftApi.createProcessTaskInstance(target, pId, 1, target, title);
			}
			if (taskIds != null && taskIds.length > 0) {
				root.put("taskId", taskIds[0]);
			}

			root.put("uuid", uuid);
			root.put("userId", userId);
			root.put("status", 200);
			root.put("errmsg", "调用成功");
		} catch (AWSSDKException e) {
			e.printStackTrace();
		}

		return root.toString();
	}
	
	/**
	 * 设置流程的全局变量
	 * @param processId 流程实例ID
	 * @param varJson 要设置的流程全局变量json串
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public String setProcessGlobalVariables(String processId, String varJson){
		JSONObject root = createSuccessJson();
		WorkflowInstanceAPI wfApi = WorkflowInstanceAPI.getInstance();
		// 保存变量
		if (StrUtils.isNotBlank(varJson)) {
			try {
				JSONObject varObject = JSONObject.fromObject(varJson);
				Iterator<String> iterator = varObject.keys();
				while (iterator.hasNext()) {
					String key = iterator.next();
					D.out(key +"="+ varObject.getString(key));
					wfApi.assignVariable(Integer.parseInt(processId), key, varObject.getString(key));
				}
			} catch (Exception e) {
				root.put("status", 500);
				root.put("errmsg", "设置流程全局变量失败，错误信息："+e.getMessage());
			}
		}
		return root.toString();
	}
	
	/**
	 * 获取流程全局变量
	 */
	public String getProcessVal(int processId, int taskId, String key){
		JSONObject root = createSuccessJson();
		WorkflowInstanceAPI wfApi = WorkflowInstanceAPI.getInstance();
		try {
			String value = wfApi.getVariable(this.getContext().getUID(), processId, taskId, key);
			root.put("value", value);
		} catch (AWSSDKException e) {
			root.put("status", 500);
			root.put("errmsg", "获取流程全局变量失败，错误信息："+e.getMessage());
			e.printStackTrace();
		}
		return root.toString();
	}
}
