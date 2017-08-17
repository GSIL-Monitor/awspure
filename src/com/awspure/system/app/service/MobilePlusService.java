package com.awspure.system.app.service;

import java.sql.Connection;

import com.awspure.system.app.entity.TaskBean;
import com.awspure.system.util.SqlUtils;

public class MobilePlusService {

	/*
	 * 查看是否还有子任务
	 */
	public TaskBean getWaitingByTaskId(int taskId) {
		TaskBean tb = null;
		SqlUtils sqlUtils = SqlUtils.getInstance();
		Connection conn = sqlUtils.getDefaultDBC();
		try {
			if (conn != null) {
				tb = getWaitingByTaskId(conn, taskId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlUtils.closeConn(conn);
		}

		return tb;
	}

	public TaskBean getWaitingByTaskId(Connection conn, int taskId) {
		TaskBean tb = new TaskBean();
		tb.setId(taskId);
		tb.setStatus(1);
		tb.setWaitCount(0);

		SqlUtils sqlUtils = SqlUtils.getInstance();
		String taskSql = "select status from wf_task where id=" + taskId;
		int status = sqlUtils.getIntBySQL(conn, taskSql, "status");
		tb.setStatus(status);

		if (status == 11) {
			String childSql = "select count(1) cc from wf_task where status=11 and from_point=" + taskId;
			int cc = sqlUtils.getIntBySQL(conn, childSql, "cc");
			tb.setWaitCount(cc);
		}
		return tb;
	}
}
