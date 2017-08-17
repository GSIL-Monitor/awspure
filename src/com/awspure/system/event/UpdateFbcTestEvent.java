package com.awspure.system.event;

import java.sql.Connection;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.awspure.system.util.SqlUtils;

public class UpdateFbcTestEvent extends WorkFlowStepRTClassA {

	public UpdateFbcTestEvent(UserContext uc) {
		super(uc);
		setVersion("1.0.0");
		setDescription("这是一个测试，用户判断下个节点是否到任务池，如果是则创建fbc任务");
	}
	
	@Override
	public boolean execute() {
		int processInstanceId = getParameter(this.PARAMETER_INSTANCE_ID).toInt();
		int taskId = getParameter(this.PARAMETER_TASK_ID).toInt();
		int workFlowId = getParameter(this.PARAMETER_WORKFLOW_ID).toInt();
		int stepId = getParameter(this.PARAMETER_WORKFLOW_STEP_ID).toInt();
		System.out.println("processInstanceId="+processInstanceId);
		System.out.println("taskId="+taskId);
		System.out.println("workFlowId="+workFlowId);
		System.out.println("stepId="+stepId);
		
		SqlUtils sqlUtils = SqlUtils.getInstance();
		Connection conn = sqlUtils.getConnBySwitch("fbc");
		sqlUtils.getDefaultDBC();
		
		
		WorkflowInstanceAPI wfApi = WorkflowInstanceAPI.getInstance();
		
		try {
			String zz = wfApi.getVariable(this.getUserContext().getUID(), processInstanceId, taskId, "ZZ");
		} catch (AWSSDKException e) {
			e.printStackTrace();
		}
		
		
		
		return false;
	}
}
