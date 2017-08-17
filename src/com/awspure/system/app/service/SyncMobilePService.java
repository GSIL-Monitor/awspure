package com.awspure.system.app.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.awspure.system.util.D;
import com.awspure.system.util.SqlUtils;

public class SyncMobilePService {

	public static void main(String[] args) {
		execAction();
	}

	public static void execAction() {
		SqlUtils sqlUtils = SqlUtils.getInstance();
		Connection conn = sqlUtils.getDefaultDBC();
		try {
			if (conn != null) {
				// 全局变量
				Map<String, Integer> globalMap = new HashMap<String, Integer>();
				globalMap.put("BO_MOBILE_BO_S", 0);
				globalMap.put("BO_MOBILE_SUBBO_S", 0);
				globalMap.put("BO_MOBILE_SUBBOHIDE_S", 0);

				// 清除设置的数据
				D.out("delete from WF_MESSAGEDATA where id < 200;");
				D.out("delete from BO_MOBILE_P;");
				D.out("delete from BO_MOBILE_BO_S;");
				D.out("delete from BO_MOBILE_SUBBO_S;");
				D.out("delete from BO_MOBILE_SUBBOHIDE_S;");

				// 开始传输数据
				int page = 3;// 页码
				int size = 50;// 每页行数
				for (int i = 0; i < page; i++) {
					outSqlByPage(conn, (i * size), size, globalMap);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlUtils.closeConn(conn);
		}
	}

	public static void outSqlByPage(Connection conn, int start, int end, Map<String, Integer> globalMap) {
		SqlUtils sqlUtils = SqlUtils.getInstance();
		// 手机模版设置流程id
		int wfId = 23524;

		// 插入主表
		StringBuilder i1Sql = new StringBuilder();

		// 插入流程表
		StringBuilder wfSql = new StringBuilder();

		// 查询数据BO_MOBILE_P
		String sql = "select * from BO_MOBILE_P limit " + start + "," + end;
		List<String> fieldList = getPFieldList();
		List<Map<String, String>> dataList = sqlUtils.getListMapBySql(conn, sql, fieldList);

		int rowIndex = start;// 新ID
		Map<String, Integer> bindMap = new LinkedHashMap<String, Integer>();
		for (Map<String, String> data : dataList) {
			rowIndex++;// 新ID
			if (i1Sql.length() > 0) {
				i1Sql.append(",");
			}

			// 构造每行数据
			i1Sql.append("(");
			for (String field : fieldList) {
				i1Sql.append("'");
				if (field.equals("ID")) {
					i1Sql.append(rowIndex);
				} else if (field.equals("BINDID")) {
					// 新旧ID映射
					bindMap.put(data.get(field), rowIndex);
					i1Sql.append(rowIndex);
				} else {
					i1Sql.append(data.get(field));
				}

				i1Sql.append("'");
				i1Sql.append(",");
			}
			i1Sql.deleteCharAt(i1Sql.length() - 1);
			i1Sql.append(")");

			// 构造流程实例SQL
			if (wfSql.length() > 0) {
				wfSql.append(",");
			}
			wfSql.append("(");
			wfSql.append(rowIndex);
			wfSql.append(",");
			wfSql.append(wfId);
			wfSql.append(",1,0,0,'系统同步手机模版','admin',now(),'系统','手机模版");
			wfSql.append(data.get("LCMC"));
			wfSql.append("',0,0,1)");
		}

		i1Sql.insert(0, "INSERT INTO BO_MOBILE_P VALUES ");
		i1Sql.append(";");
		D.out(i1Sql.toString());

		wfSql.insert(0, " values ");
		wfSql.insert(0, "(ID,WF_ID,WFS_NO,WF_START,WF_END,WF_STYLE,CREATE_USER,CREATE_DATE,FILE_FROM,TITLE,WF_EXCEPTION,WF_OVERTIME,ORGNO)");
		wfSql.insert(0, "insert into WF_MESSAGEDATA ");
		wfSql.append(";");
		D.out(wfSql.toString());

		// 查看映射
		StringBuilder bindids = new StringBuilder();
		for (String key : bindMap.keySet()) {
			if (bindids.length() > 0) {
				bindids.append(",");
			}
			bindids.append(key);
		}
		bindids.insert(0, "(");
		bindids.append(")");

		// 查询数据BO_MOBILE_BO_S
		outSqlByBoName(conn, "BO_MOBILE_BO_S", bindids.toString(), bindMap, globalMap);
		// 查询数据BO_MOBILE_SUBBO_S
		outSqlByBoName(conn, "BO_MOBILE_SUBBO_S", bindids.toString(), bindMap, globalMap);
		// 查询数据BO_MOBILE_SUBBOHIDE_S
		outSqlByBoName(conn, "BO_MOBILE_SUBBOHIDE_S", bindids.toString(), bindMap, globalMap);
	}

	public static void outSqlByBoName(Connection conn, String boName, String bindids, Map<String, Integer> bindMap, Map<String, Integer> globalMap) {
		SqlUtils sqlUtils = SqlUtils.getInstance();
		// 查询数据
		String sql = "select * from " + boName + " where bindid in " + bindids;
		List<String> fieldList = getFieldListByBoName(boName);
		List<Map<String, String>> dataList = sqlUtils.getListMapBySql(conn, sql, fieldList);

		// 构造SQL
		StringBuilder i1Sql = new StringBuilder();

		// 获取新的ID
		int rowIndex = globalMap.get(boName);// 新ID
		for (Map<String, String> data : dataList) {
			rowIndex++;// 新ID
			if (i1Sql.length() > 0) {
				i1Sql.append(",");
			}

			// 构造每行数据
			i1Sql.append("(");
			for (String field : fieldList) {
				i1Sql.append("'");
				if (field.equals("ID")) {
					i1Sql.append(rowIndex);
				} else if (field.equals("BINDID")) {
					// 新旧ID映射-获取
					i1Sql.append(bindMap.get(data.get(field)));
				} else {
					i1Sql.append(data.get(field));
				}

				i1Sql.append("'");
				i1Sql.append(",");
			}
			i1Sql.deleteCharAt(i1Sql.length() - 1);
			i1Sql.append(")");
		}

		// 保存序列
		globalMap.put(boName, rowIndex);

		// 补全SQL
		i1Sql.insert(0, "INSERT INTO " + boName + " VALUES ");
		i1Sql.append(";");
		D.out(i1Sql.toString());
	}

	public static List<String> getFieldListByBoName(String boName) {
		List<String> fieldList = null;
		if (boName.equals("BO_MOBILE_BO_S")) {
			fieldList = getSFieldList();
		} else if (boName.equals("BO_MOBILE_SUBBO_S")) {
			fieldList = getSUBSFieldList();
		} else if (boName.equals("BO_MOBILE_SUBBOHIDE_S")) {
			fieldList = getSUBHIDESFieldList();
		}
		return fieldList;
	}

	public static List<String> getSUBHIDESFieldList() {
		List<String> fieldList = new ArrayList<String>();
		fieldList.add("ID");
		fieldList.add("ORGNO");
		fieldList.add("BINDID");
		fieldList.add("UPDATEDATE");
		fieldList.add("UPDATEUSER");
		fieldList.add("CREATEDATE");
		fieldList.add("CREATEUSER");
		fieldList.add("WORKFLOWID");
		fieldList.add("WORKFLOWSTEPID");
		fieldList.add("ISEND");
		fieldList.add("SUBTITLE");
		fieldList.add("SUBNAME");
		fieldList.add("HIDETITLE");
		fieldList.add("HIDENAME");
		return fieldList;
	}

	public static List<String> getSUBSFieldList() {
		List<String> fieldList = new ArrayList<String>();
		fieldList.add("ID");
		fieldList.add("ORGNO");
		fieldList.add("BINDID");
		fieldList.add("UPDATEDATE");
		fieldList.add("UPDATEUSER");
		fieldList.add("CREATEDATE");
		fieldList.add("CREATEUSER");
		fieldList.add("WORKFLOWID");
		fieldList.add("WORKFLOWSTEPID");
		fieldList.add("ISEND");
		fieldList.add("SUBTITLE");
		fieldList.add("SUBNAME");
		fieldList.add("INFO1");
		fieldList.add("INFO1CODE");
		fieldList.add("INFO2");
		fieldList.add("INFO2CODE");
		fieldList.add("SN");
		return fieldList;
	}

	public static List<String> getSFieldList() {
		List<String> fieldList = new ArrayList<String>();
		fieldList.add("ID");
		fieldList.add("ORGNO");
		fieldList.add("BINDID");
		fieldList.add("UPDATEDATE");
		fieldList.add("UPDATEUSER");
		fieldList.add("CREATEDATE");
		fieldList.add("CREATEUSER");
		fieldList.add("WORKFLOWID");
		fieldList.add("WORKFLOWSTEPID");
		fieldList.add("ISEND");
		fieldList.add("FTITLE");
		fieldList.add("FNAME");
		fieldList.add("SN");
		fieldList.add("YLZD");
		return fieldList;
	}

	public static List<String> getPFieldList() {
		List<String> fieldList = new ArrayList<String>();
		fieldList.add("ID");
		fieldList.add("ORGNO");
		fieldList.add("BINDID");
		fieldList.add("UPDATEDATE");
		fieldList.add("UPDATEUSER");
		fieldList.add("CREATEDATE");
		fieldList.add("CREATEUSER");
		fieldList.add("WORKFLOWID");
		fieldList.add("WORKFLOWSTEPID");
		fieldList.add("ISEND");
		fieldList.add("LCBM");
		fieldList.add("LCMC");
		fieldList.add("BOTITLE");
		fieldList.add("BONAME");
		fieldList.add("INFO1");
		fieldList.add("INFO2");
		fieldList.add("INFO3");
		fieldList.add("INFO4");
		fieldList.add("TAB");
		fieldList.add("INFO1CODE");
		fieldList.add("INFO2CODE");
		fieldList.add("INFO3CODE");
		fieldList.add("INFO4CODE");
		fieldList.add("INFO5");
		fieldList.add("INFO6");
		fieldList.add("INFO5CODE");
		fieldList.add("INFO6CODE");
		fieldList.add("USEYN");
		return fieldList;
	}

}
