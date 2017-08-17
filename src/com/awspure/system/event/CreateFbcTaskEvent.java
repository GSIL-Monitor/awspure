package com.awspure.system.event;

import java.net.URLEncoder;
import net.sf.json.JSONObject;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.workflow.design.cache.WorkFlowStepCache;
import com.actionsoft.awf.workflow.design.model.WorkFlowStepModel;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.local.level0.WorkflowTaskInstanceAPI;
import com.awspure.system.util.D;
import com.awspure.system.util.HttpUtils;
import com.awspure.system.util.PropertyUtils;

public class CreateFbcTaskEvent extends WorkFlowStepRTClassA {

	public CreateFbcTaskEvent() {}
	
	public CreateFbcTaskEvent(UserContext uc) {
		super(uc);
		setVersion("1.0.0");
		setDescription("这是一个测试，用户判断下个节点是否到任务池，如果是则创建fbc任务");
	}
	
	@Override
	public boolean execute() {
		final String hostUrl = PropertyUtils.getInstance().getProperty("fbc.host");
		int processInstanceId = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(this.PARAMETER_TASK_ID).toInt();
		int tatkTypeId = getParameter(this.PARAMETER_WORKFLOW_ID).toInt();
		int stepNo = getParameter(this.PARAMETER_WORKFLOW_STEP_ID).toInt();
		/*String uuid = DBSql.getString("SELECT UUID FROM SYSFLOW WHERE id="+tatkTypeId, "UUID");*/
		UserContext me = getUserContext();
		try {
			int nextStepNo = WorkflowTaskInstanceAPI.getInstance().getNextStepNo(me.getUID(), processInstanceId, taskId);
			WorkFlowStepModel workFlowNowStepModel = (WorkFlowStepModel) WorkFlowStepCache.getModelOfStepNo(tatkTypeId, nextStepNo);
			String isGxzx = workFlowNowStepModel._extendId;
			if("Y".equals(isGxzx)){
				String posts = workFlowNowStepModel._stepName.split(":")[1];
				StringBuilder url = new StringBuilder();
				url.append(hostUrl);
				url.append("?processId=");
				url.append(processInstanceId);
				url.append("&stepNo=");
				url.append(stepNo);
				url.append("&taskId=");
				url.append(taskId);
				url.append("&posts=");
				url.append(URLEncoder.encode(posts, "utf8"));
				D.log(url.toString());
				String strJson = HttpUtils.getInstance().get(url.toString());
				JSONObject root = JSONObject.fromObject(strJson);
				String rel = root.getString("resFlag");
				if("success".equals(rel)){
					return true;
				}else{
					return false;
				}
			}else{
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}