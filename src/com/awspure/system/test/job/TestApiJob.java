package com.awspure.system.test.job;

import java.util.Hashtable;
import java.util.Vector;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.application.schedule.IJob;
import com.actionsoft.sdk.AWSSDKException;
import com.actionsoft.sdk.local.level0.WorkflowInstanceAPI;
import com.awspure.system.util.D;

public class TestApiJob implements IJob {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		D.out("TestApiJob 开始执行...");
		executeAction(context);
		D.out("TestApiJob 执行结束!!!");
	}

	private void executeAction(JobExecutionContext context) {
		testWfApi();
	}

	private static void testWfApi() {
		try {
			Hashtable<String, Vector<Hashtable<String, String>>> dataSet = new Hashtable<String, Vector<Hashtable<String, String>>>();
			Vector<Hashtable<String, String>> mainBo = new Vector<Hashtable<String, String>>();// 主表
			Hashtable<String, String> recordData = new Hashtable<String, String>();
			recordData.put("NAME", "测试Name");
			recordData.put("TITLE", "测试Title");
			mainBo.add(recordData);// 主表一行记录

			dataSet.put("BO_PURE_TEST_P", mainBo);
			WorkflowInstanceAPI wfApi = WorkflowInstanceAPI.getInstance();
			int pId = wfApi.createInstances("01c2a5efc57595e84745280d447da51e", "admin", "aws-test", "示例流程-测试-管理员",
					dataSet);
			D.out("返回流程实例ID:" + pId);
		} catch (AWSSDKException e) {
			e.printStackTrace(System.err);
		}
	}

}
