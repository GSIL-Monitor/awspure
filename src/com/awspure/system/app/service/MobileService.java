package com.awspure.system.app.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.actionsoft.awf.bo.cache.MetaDataCache;
import com.actionsoft.awf.bo.cache.MetaDataMapCache;
import com.actionsoft.awf.bo.model.MetaDataMapModel;
import com.actionsoft.awf.bo.model.MetaDataModel;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.awf.organization.model.UserModel;
import com.actionsoft.awf.util.DBSql;
import com.actionsoft.awf.util.Function;
import com.actionsoft.awf.workflow.execute.WorkflowException;
import com.actionsoft.awf.workflow.execute.engine.WorkflowEngine;
import com.actionsoft.sdk.local.level0.RuleAPI;
import com.awspure.system.app.entity.FormPBean;
import com.awspure.system.app.entity.FormPConst;
import com.awspure.system.app.entity.MetaBean;
import com.awspure.system.app.entity.SubBoBean;
import com.awspure.system.app.entity.TaskBean;
import com.awspure.system.app.entity.VersionBean;
import com.awspure.system.common.entity.Page;
import com.awspure.system.util.D;
import com.awspure.system.util.DateUtils;
import com.awspure.system.util.SqlUtils;
import com.awspure.system.util.StrUtils;

public class MobileService {

	private Connection conn = null;

	/*
	 * @param userId: 用户ID,getUID()
	 * 
	 * @param taskType: 0-全部，1-待办类，2-途知、传阅类
	 * 
	 * @param key : 查询条件 标题,申请人,申请部门,单号
	 * 
	 * @param params : queryString=;otherParam=;
	 */

	public Page<TaskBean> getTaskListPageBy(String userId, int taskType, String gxzx, String params, int start, int limit, boolean isHistory) {
		Page<TaskBean> taskPage = new Page<TaskBean>();
		List<TaskBean> taskList = new ArrayList<TaskBean>();
		SqlUtils sqlUtils = SqlUtils.getInstance();
		Map<String, String> param = trunPramsToMap(params);
		String queryString = param.get("queryString");

		// 构造SQL start
		StringBuilder bodySql = new StringBuilder();
		bodySql.append("from ");
		// 查询待办表还是历史表
		if (isHistory) {
			bodySql.append("wf_task_log");
		} else {
			bodySql.append("wf_task");
		}
		bodySql.append(" a ");
		if(StrUtils.isNotBlank(gxzx)){
			bodySql.append("inner join sysflowstep b on b.extendid ='"+gxzx+"' and b.id=a.wfsid ");
		}else{
			bodySql.append("inner join sysflowstep b on b.id=a.wfsid ");
		}
		/*
		 * 如果此处增加该条件,会屏蔽掉第一个节点
		 * 如果是待办,不显示第一次的填写节点,可以显示被驳回的填写节点
		 * */
		//if (!isHistory) {bodySql.append("and (b.stepno!=1 or a.owner<>a.target) ");}
		
		/*
		 * 增加节点扩展属性不在待办列表显示 开始 APP-LIST-OFF :待办列表不显示该节点 APP-DETAIL-OFF
		 * :任务详细页提示该任务需在PC端处理
		 */
		//bodySql.append("and b.extendId!='APP-LIST-OFF' ");
		// 增加节点扩展属性不在待办列表显示 结束
		bodySql.append("inner join wf_messagedata c on c.id=a.bind_id ");
		bodySql.append("inner join sysflow e on e.id=a.wfid ");
		bodySql.append("inner join orguser f on f.userid=c.create_user ");
		/* 增加流程启用禁用 */
		//bodySql.append("inner join bo_mobile_p g on g.lcbm=e.appid and (g.useyn='启用' or g.useyn is null) ");
		bodySql.append("where a.target='");
		bodySql.append(userId);
		bodySql.append("' ");
		bodySql.append("and (a.status=1 or a.status=3 or a.status=4 or a.status=11) ");
		if (queryString != null && !queryString.isEmpty()) {
			queryString = queryString.toLowerCase();
			bodySql.append("and (Lower(a.title) like '%");
			bodySql.append(queryString);
			bodySql.append("%' or Lower(c.create_user) like '%");
			bodySql.append(queryString);
			bodySql.append("%' or Lower(f.username) like '%");
			bodySql.append(queryString);
			bodySql.append("%' or Lower(c.file_from) like '%");
			bodySql.append(queryString);
			bodySql.append("%' or Lower(e.uuid) like '%");
			bodySql.append(queryString);
			bodySql.append("%' or Lower(e.flowname) like '%");
			bodySql.append(queryString);
			bodySql.append("%')");
		}

		String countSql = this.getCountSql(bodySql.toString());
		String dataSql = this.getDataSql(bodySql.toString(), isHistory);

		D.out("获得待办SQL:");
		D.out(dataSql);
		// 构造SQL end

		try {
			dataSql = sqlUtils.getPageSQL(dataSql, start, limit);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Connection conn = DBSql.open();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			// 获得总条数
			int total = DBSql.getInt(conn, countSql, "count");
			taskPage.setTotal(total);

			// 获得数据
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, dataSql);
			if (rset != null){
				while (rset.next()) {
					TaskBean taskBean = new TaskBean();
					taskBean.setId(rset.getInt("id"));
					taskBean.setBindId(rset.getInt("bind_id"));
					int status = rset.getInt("status");
					taskBean.setStatus(status);
					String stepName = getRsString2Json(rset, "stepname");
					taskBean.setStepName(stepName);
					taskBean.setTitle(getRsString2Json(rset, "title"));
					taskBean.setWfStyle(getRsString2Json(rset, "wf_style"));// processGroup
					String beginTime = DateUtils.dateToString(rset.getTimestamp("begintime"));
					taskBean.setBeginTime(beginTime);
					taskBean.setReadTask(rset.getInt("read_task"));
					String readTime = DateUtils.dateToString(rset.getTimestamp("readtime"));
					taskBean.setReadTime(readTime);
					taskBean.setPriority(rset.getInt("priority"));
					taskBean.setWfId(rset.getInt("wfid"));
					taskBean.setWfsId(rset.getInt("wfsid"));
					taskBean.setStepNo(StrUtils.defaultIfEmpty(rset.getString("stepno")));
					taskBean.setFlowName(getRsString2Json(rset, "flowname"));
					taskBean.setAppId(getRsString2Json(rset, "appid"));
					String createDate = DateUtils.dateToString(rset.getTimestamp("create_date"));
					taskBean.setCreateDate(createDate);
					taskBean.setPositionName(StrUtils.defaultIfEmpty(rset.getString("position_name")));
					taskBean.setWfEnd(rset.getInt("wf_end"));
					taskBean.setUuid(rset.getString("uuid"));
					taskBean.setModify(rset.getBoolean("report_ismodify"));
					taskBean.setIsGxzx(rset.getString("extendid"));
					// 添加数据集
					taskList.add(taskBean);
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, stmt, rset);
		}

		// 设置数据集
		taskPage.setDataList(taskList);
		return taskPage;
	}

	private String getCountSql(String bodySql) {
		return "select count(a.id) as count " + bodySql;
	}

	private String getDataSql(String bodySql, boolean isHistory) {
		StringBuilder dataSql = new StringBuilder();
		dataSql.append("select a.id,a.bind_id,a.owner,c.create_user,c.create_date,f.username,f.position_name,a.status,a.title,a.begintime,a.priority,a.wf_style,");
		dataSql.append("a.read_task,a.wfid,a.wfsid,a.readtime,b.stepno,b.stepname,b.bindurl,b.extendid,b.report_ismodify,c.title as wftitle,c.file_from,e.flowname,e.appid,c.wf_end,e.uuid ");
		dataSql.append(bodySql);
		if (!isHistory) {
			dataSql.append(" order by a.id desc");
		} else {
			dataSql.append(" order by a.endtime desc");
		}
		return dataSql.toString();
	}

	public Map<String, String> trunPramsToMap(String params) {
		Map<String, String> map = new HashMap<String, String>();
		if (params != null && params.length() > 0) {
			String[] param = params.trim().split(";");
			for (String s : param) {
				if (s.contains("=")) {
					String[] p = s.split("=");
					if (p.length == 2) {
						map.put(p[0], p[1]);
					} else {
						map.put(p[0], "");
					}
				}
			}
		}
		return map;
	}

	public Map<String, String> getBoValueByList(String boName, String bindId, List<String> result) {
		Map<String, String> map = new HashMap<String, String>();
		if (result != null && !result.isEmpty()) {
			Connection conn = getConn();
			Statement stmt = null;
			ResultSet rset = null;
			try {
				StringBuilder sql = new StringBuilder();
				for (String name : result) {
					if (sql.length() > 0) {
						sql.append(",");
					}
					sql.append(name);
				}
				sql.insert(0, "select ");
				sql.append(" from " + boName + " where bindid='" + bindId + "'");

				D.out("待办获取字段");
				D.out(sql.toString());

				stmt = conn.createStatement();
				rset = DBSql.executeQuery(conn, stmt, sql.toString());
				if (rset != null) {
					// 获取BO缓存
					MetaDataModel metaData = (MetaDataModel) MetaDataCache.getModel(boName);
					if (rset.next()) {
						for (String name : result) {
							if (name != null && !name.equals("")) {
								// 获取字段缓存
								MetaDataMapModel mmMap = (MetaDataMapModel) MetaDataMapCache.getModel(metaData.getId(), name);
								String val = this.getRsString(rset, name);
								String fieldType = mmMap.getFieldType();
								String displaySql = mmMap.getDisplaySetting();
								String displayType = mmMap.getDisplayType();
								if (displayType.equals("单选按纽组") || displayType.equals("列表")) {
									Map<String, String> valMap = convDisplaySql(displaySql);
									map.put(name, valMap.get(val));
								} else if (fieldType.equals("日期") && val.length() > 10) {
									/* 此处有空指针 1205解决 */
									map.put(name, val.substring(0, 10));
								} else {
									map.put(name, val);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				DBSql.close(null, stmt, rset);
			}
		} else {
			D.err("待办简要信息未设置!");
		}
		return map;
	}

	public Map<String, String> getBoValueByMapList(String boName, int bindId, List<MetaBean> metaBeanList) {
		Map<String, String> map = new HashMap<String, String>();
		if (metaBeanList != null && !metaBeanList.isEmpty()) {
			Connection conn = getConn();
			Statement stmt = null;
			ResultSet rset = null;
			try {
				StringBuilder sql = new StringBuilder();
				sql.append("select a.*,c.username create_user_name,b.create_date,b.file_from,c.position_name ");
				sql.append("from ");
				sql.append(boName);
				sql.append(" a ,wf_messagedata b,orguser c ");
				sql.append("where a.bindid=b.id and b.create_user=c.userid and a.bindid =");
				sql.append(bindId);

				D.out("主表数据:");
				D.out(sql.toString());

				stmt = conn.createStatement();
				rset = DBSql.executeQuery(conn, stmt, sql.toString());
				if (rset != null) {
					if (rset.next()) {
						/* 流程实例ID - 填充特殊字段 start */
						map.put(FormPConst.BIND_ID, rset.getString("BINDID"));
						/* BO主键 */
						map.put(FormPConst.ID, rset.getString("ID"));
						/* 申请人姓名 - 更改为create_user_name */
						map.put(FormPConst.CREATE_USER_NAME, rset.getString("create_user_name"));
						/* 申请部门 - 更改为file_from */
						String deptName = "";
						try {
							deptName = rset.getString("file_from");
							int index = deptName.indexOf("//");
							if (index != -1) {
								deptName = deptName.substring(index + 2);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						map.put(FormPConst.CREATE_DEPT_NAME, deptName);
						/* 申请日期 - 更改为create_date */
						map.put(FormPConst.CREATE_DATE, DateUtils.dateToString(rset.getTimestamp("create_date")));
						/* 申请人职位 20151205 position_name */
						map.put(FormPConst.POSITION_NAME, rset.getString("position_name"));
						/* 单据号 - 填充特殊字段 end */
						map.put(FormPConst.NO, getNoByResultSet(rset, boName));

						/* 填充配置字段的值 */
						for (MetaBean metaBean : metaBeanList) {
							String fname = metaBean.getFname();
							if (StrUtils.isNotBlank(fname)) {
								if (metaBean.getFieldType().equals("数值")) {
									BigDecimal num = rset.getBigDecimal(fname);
									map.put(fname, num.toString());
								} else if (metaBean.getFieldType().equals("日期")) {
									Timestamp time = rset.getTimestamp(fname);
									if (time != null) {
										if (metaBean.getDisplayType().equals("日期时间")) {
											map.put(fname, DateUtils.dateToString(time));
										} else {
											map.put(fname, DateUtils.dateToString(time, DateUtils.YYYYMMDD));
										}
									} else {
										map.put(fname, "");
									}
								} else {
									map.put(fname, rset.getString(fname));
								}

								/* 增加依赖字段0402 */
								String ylzdName = metaBean.getYlzd();
								if (ylzdName != null && !ylzdName.isEmpty()) {
									map.put(ylzdName, rset.getString(ylzdName));
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				DBSql.close(null, stmt, rset);
			}
		}
		return map;
	}

	public List<Map<String, String>> getSubBoValueByMapList(String boName, int bindId, List<MetaBean> metaBeanList) {
		List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
		if (metaBeanList != null && !metaBeanList.isEmpty()) {
			Connection conn = getConn();
			Statement stmt = null;
			ResultSet rset = null;
			try {
				StringBuilder sql = new StringBuilder();
				sql.append("select a.* ");
				sql.append("from ");
				sql.append(boName);
				sql.append(" a ");
				sql.append("where a.bindid =");
				sql.append(bindId);

				D.out("获取子表数据:");
				D.out(sql.toString());

				stmt = conn.createStatement();
				rset = DBSql.executeQuery(conn, stmt, sql.toString());
				if (rset != null) {
					if (rset.next()) {
						LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
						map.put("ID", rset.getString("ID"));
						map.put("BINDID", rset.getString("BINDID"));

						for (MetaBean metaBean : metaBeanList) {
							String fname = metaBean.getFname();
							map.put(fname, rset.getString(fname));

							Object obj = rset.getObject(fname);
							if (obj instanceof java.sql.Timestamp) {
								map.put(fname, DateUtils.dateToString(rset.getTimestamp(fname)));
							} else if (obj instanceof java.math.BigDecimal) {
								map.put(fname, String.valueOf(rset.getDouble(fname)));
							} else {
								map.put(fname, rset.getString(fname));
							}
						}
						mapList.add(map);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				DBSql.close(null, stmt, rset);
			}
		}
		return mapList;
	}

	public TaskBean getTaskBeanByType(String taskId, String type) {
		TaskBean tBean = new TaskBean();
		Connection conn = getConn();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			String boName = "wf_task_log";
			if (StrUtils.isNotBlank(type) && type.equals("0")) {// 类型0待办1已办
				boName = "wf_task";
			}

			StringBuilder sql = new StringBuilder();
			sql.append("select a.id,a.bind_id,a.target,a.status,c.extendId,");
			sql.append("a.title,a.wfid,a.wfsid,a.read_task,a.readtime,");
			sql.append("b.FLOWNAME,b.APPID,c.STEPNAME,c.STEPNO,d.ID subid,d.TITLE subtitle,");
			sql.append("b.uuid ");
			sql.append("from ");
			sql.append(boName);
			sql.append(" a ");
			sql.append("left join sysflow b on b.ID=a.WFID ");
			sql.append("left join sysflowstep c on c.id=a.WFSID ");
			sql.append("left join sysflowsub d on d.BEGINSTEPUUID=c.UUID ");
			sql.append("where a.id=");
			sql.append(taskId);

			D.out("获得任务详情:");
			D.out(sql.toString());

			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sql.toString());
			if (rset != null) {
				if (rset.next()) {
					int id = rset.getInt("id");
					int bindId = rset.getInt("bind_id");
					String target = rset.getString("target");
					int status = rset.getInt("status");
					String title = rset.getString("title");
					int wfId = rset.getInt("wfid");
					int wfsId = rset.getInt("wfsid");
					int readTask = rset.getInt("read_task");
					Date dateReadTime = rset.getDate("readtime");
					String readTime = DateUtils.dateToString(dateReadTime);
					String flowName = rset.getString("flowname");
					String appId = rset.getString("appid");
					String stepName = rset.getString("stepname");
					String stepNo = rset.getString("stepno");

					int subId = rset.getInt("subid");
					String subTitle = rset.getString("subtitle");
					String extendId = rset.getString("extendId");
					
					String uuid = rset.getString("uuid");

					tBean.setId(id);
					tBean.setBindId(bindId);
					tBean.setTarget(target);
					tBean.setStatus(status);
					tBean.setTitle(title);
					tBean.setWfId(wfId);
					tBean.setWfsId(wfsId);
					tBean.setReadTask(readTask);
					tBean.setReadTime(readTime);
					tBean.setFlowName(flowName);
					tBean.setAppId(appId);
					tBean.setStepName(stepName);
					tBean.setStepNo(stepNo);

					tBean.setSubId(subId);
					tBean.setSubTitle(subTitle);

					tBean.setExtendId(extendId);
					tBean.setUuid(uuid);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBSql.close(null, stmt, rset);
		}
		return tBean;
	}

	public FormPBean getFormPBeanByLcbm(String wLcbm) {
		FormPBean pBean = new FormPBean();
		Connection conn = getConn();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			String sql = "select * from BO_MOBILE_P where lcbm='" + wLcbm + "'";

			D.out("获取配置主表:");
			D.out(sql);
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sql);
			if (rset != null) {
				if (rset.next()) {
					String bindId = rset.getString("bindid");
					String lcbm = rset.getString("lcbm");
					String lcmc = rset.getString("lcmc");
					String boTitle = rset.getString("botitle");
					String boName = rset.getString("boname");
					String info1 = rset.getString("info1");
					String info2 = rset.getString("info2");
					String info3 = rset.getString("info3");
					String info4 = rset.getString("info4");
					String info5 = rset.getString("info5");
					String info6 = rset.getString("info6");
					String info1code = rset.getString("info1code");
					String info2code = rset.getString("info2code");
					String info3code = rset.getString("info3code");
					String info4code = rset.getString("info4code");
					String info5code = rset.getString("info5code");
					String info6code = rset.getString("info6code");

					pBean.setBindId(bindId);
					pBean.setLcmc(lcmc);
					pBean.setLcbm(lcbm);
					pBean.setBoName(boName);
					pBean.setBoTitle(boTitle);

					pBean.setInfo1(info1);
					pBean.setInfo2(info2);
					pBean.setInfo3(info3);
					pBean.setInfo4(info4);
					pBean.setInfo5(info5);
					pBean.setInfo6(info6);

					pBean.setInfo1code(info1code);
					pBean.setInfo2code(info2code);
					pBean.setInfo3code(info3code);
					pBean.setInfo4code(info4code);
					pBean.setInfo5code(info5code);
					pBean.setInfo6code(info6code);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBSql.close(null, stmt, rset);
		}
		return pBean;
	}

	@Deprecated
	public Map<String, MetaBean> getMapMetaBeanForMainBo(String boName) {
		Map<String, MetaBean> map = new HashMap<String, MetaBean>();
		Connection conn = getConn();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT b.id,b.field_type,b.display_type,b.display_sql,b.field_name,b.field_title ");
			sql.append("FROM SYS_BUSINESS_METADATA_MAP b,SYS_BUSINESS_METADATA a ");
			sql.append("WHERE b.metadata_id = a.ID and a.ENTITY_NAME = '");
			sql.append(boName);
			sql.append("'");
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sql.toString());
			if (rset != null) {
				while (rset.next()) {
					String id = rset.getString("id");
					String fname = rset.getString("field_name");
					String fieldType = rset.getString("field_type");
					String fieldTitle = rset.getString("field_title");
					String displayType = rset.getString("display_type");
					String displaySql = rset.getString("display_sql");

					MetaBean mb = new MetaBean();
					mb.setId(id);
					mb.setFname(fname);
					mb.setFieldType(fieldType);
					mb.setFtitle(fieldTitle);
					mb.setDisplayType(displayType);
					mb.setDisplaySql(displaySql);
					map.put(fname, mb);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBSql.close(null, stmt, rset);
		}
		return map;
	}

	@Deprecated
	public List<MetaBean> getListMetaBeanForSubBo(String boName, String bindId) {
		List<MetaBean> mbList = new ArrayList<MetaBean>();
		Connection conn = getConn();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT b.id,b.field_type,b.display_type,b.display_sql,b.field_name,b.field_title,b.map_index ");
			sql.append("FROM SYS_BUSINESS_METADATA_MAP b,SYS_BUSINESS_METADATA a ");
			sql.append("WHERE b.metadata_id = a.ID and a.ENTITY_NAME = '");
			sql.append(boName);
			sql.append("' ");
			sql.append("and b.field_name not in (select hidename from BO_MOBILE_SUBBOHIDE_S where subname = '");
			sql.append(boName);
			sql.append("' and bindid='");
			sql.append(bindId);
			sql.append("') order by b.map_index asc");

			D.out("获取子表字段数据结构:");
			D.out(sql.toString());
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sql.toString());
			if (rset != null) {
				while (rset.next()) {
					String id = rset.getString("id");
					String fname = rset.getString("field_name");
					String fieldType = rset.getString("field_type");
					String fieldTitle = rset.getString("field_title");
					String displayType = rset.getString("display_type");
					String displaySql = rset.getString("display_sql");
					int mapIndex = rset.getInt("map_index");

					MetaBean mb = new MetaBean();
					mb.setId(id);
					mb.setFname(fname);
					mb.setFieldType(fieldType);
					mb.setFtitle(fieldTitle);
					mb.setDisplayType(displayType);
					mb.setDisplaySql(displaySql);
					mb.setSn(mapIndex);

					mbList.add(mb);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBSql.close(null, stmt, rset);
		}

		return mbList;
	}

	public Map<String, MetaBean> getMapMetaBeanForSubBo(String boName, String bindId) {
		Map<String, MetaBean> map = new LinkedHashMap<String, MetaBean>();
		Connection conn = getConn();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT b.id,b.field_type,b.display_type,b.display_sql,b.field_name,b.field_title,b.map_index ");
			sql.append("FROM SYS_BUSINESS_METADATA_MAP b,SYS_BUSINESS_METADATA a ");
			sql.append("WHERE b.metadata_id = a.ID and a.ENTITY_NAME = '");
			sql.append(boName);
			sql.append("' ");
			sql.append("and b.field_name not in (select hidename from BO_MOBILE_SUBBOHIDE_S where subname = '");
			sql.append(boName);
			sql.append("' and bindid='");
			sql.append(bindId);
			sql.append("') order by b.map_index asc");

			D.out("获取子表字段数据结构:");
			D.out(sql.toString());
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sql.toString());
			if (rset != null) {
				while (rset.next()) {
					String id = rset.getString("id");
					String fname = rset.getString("field_name");
					String fieldType = rset.getString("field_type");
					String fieldTitle = rset.getString("field_title");
					String displayType = rset.getString("display_type");
					String displaySql = rset.getString("display_sql");
					int mapIndex = rset.getInt("map_index");

					MetaBean mb = new MetaBean();
					mb.setId(id);
					mb.setFname(fname);
					mb.setFieldType(fieldType);
					mb.setFtitle(fieldTitle);
					mb.setDisplayType(displayType);
					mb.setDisplaySql(displaySql);
					mb.setSn(mapIndex);

					map.put(fname, mb);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBSql.close(null, stmt, rset);
		}
		return map;
	}

	public List<SubBoBean> getListSubBoBean(String bindId) {
		List<SubBoBean> sbList = new ArrayList<SubBoBean>();
		Connection conn = getConn();
		Statement stmt = null;
		ResultSet rset = null;
		try {

			StringBuilder sql = new StringBuilder();
			sql.append("select id,bindid,subtitle,subname,info1,");
			sql.append("info1code,info2,info2code,sn ");
			sql.append("from bo_mobile_subbo_s where bindid=");
			sql.append(bindId);
			sql.append(" order by sn asc");

			D.out("获取子表设置:");
			D.out(sql.toString());
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sql.toString());

			if (rset != null) {
				while (rset.next()) {
					int id = rset.getInt("id");
					int tBindId = rset.getInt("bindid");
					String subTitle = rset.getString("subtitle");
					String subName = rset.getString("subname");
					String info1 = rset.getString("info1");
					String info1Code = rset.getString("info1code");
					String info2 = rset.getString("info2");
					String info2Code = rset.getString("info2code");
					int sn = rset.getInt("sn");
					SubBoBean sb = new SubBoBean();
					sb.setId(id);
					sb.setBindId(tBindId);
					sb.setSubTitle(subTitle);
					sb.setSubName(subName);
					sb.setInfo1(info1);
					sb.setInfo1Code(info1Code);
					sb.setInfo2(info2);
					sb.setInfo2Code(info2Code);
					sb.setSn(sn);

					sbList.add(sb);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBSql.close(null, stmt, rset);
		}

		return sbList;
	}

	public List<MetaBean> getListMetaBean(String bindId) {
		List<MetaBean> mbList = new ArrayList<MetaBean>();
		Connection conn = getConn();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			StringBuilder sql = new StringBuilder();
			
			// 附件下载时需要b.id
			sql.append("select b.id,c.ftitle,c.fname,b.field_type,");
			sql.append("b.display_type,b.display_sql,d.BONAME,d.BOTITLE,c.SN,c.YLZD ");
			sql.append("from BO_MOBILE_BO_S c ");
			sql.append("inner join BO_MOBILE_P d on d.bindid=c.bindid ");
			sql.append("inner join SYS_BUSINESS_METADATA a on d.boname=a.entity_name ");
			sql.append("left join SYS_BUSINESS_METADATA_MAP b on b.metadata_id=a.id and b.field_name=c.fname ");
			sql.append("where c.bindid=");
			sql.append(bindId);
			sql.append(" order by c.SN asc");

			D.out("主表分组和表结构数据:");
			D.out(sql.toString());
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sql.toString());

			if (rset != null) {
				while (rset.next()) {
					String id = rset.getString("id");
					String fname = rset.getString("fname");
					String ftitle = rset.getString("ftitle");
					String fieldType = rset.getString("field_type");
					String displayType = rset.getString("display_type");
					String displaySql = rset.getString("display_sql");

					String tBoName = rset.getString("boname");
					String tBoTitle = rset.getString("botitle");
					int sn = rset.getInt("sn");
					String ylzd = rset.getString("ylzd");

					MetaBean mb = new MetaBean();
					mb.setId(id);
					mb.setFname(fname);
					mb.setFtitle(ftitle);
					mb.setFieldType(fieldType);
					mb.setDisplayType(displayType);
					mb.setDisplaySql(displaySql);

					mb.setBoName(tBoName);
					mb.setBoTitle(tBoTitle);
					mb.setSn(sn);
					mb.setYlzd(ylzd);

					mbList.add(mb);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBSql.close(null, stmt, rset);
		}
		return mbList;
	}

	public Map<String, MetaBean> getMapMetaBean(String boName, String bindId) {
		Map<String, MetaBean> map = new HashMap<String, MetaBean>();
		Connection conn = getConn();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("select b.id,c.ftitle,c.fname,b.field_type,");
			sql.append("b.display_type,b.display_sql,d.BONAME,d.BOTITLE,c.SN,c.YLZD ");
			sql.append("from BO_MOBILE_BO_S c ");
			sql.append("inner join SYS_BUSINESS_METADATA_MAP b on b.field_name=c.fname ");
			sql.append("inner join SYS_BUSINESS_METADATA a on b.metadata_id=a.id ");
			sql.append("inner join BO_MOBILE_P d on d.bindid=c.bindid and d.boname=a.entity_name ");
			sql.append("where a.entity_name='");
			sql.append(boName);
			sql.append("' and c.bindid=");
			sql.append(bindId);

			D.out(sql.toString());
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sql.toString());

			if (rset != null) {
				while (rset.next()) {
					String id = rset.getString("id");
					String fname = rset.getString("fname");
					String ftitle = rset.getString("ftitle");
					String fieldType = rset.getString("field_type");
					String displayType = rset.getString("display_type");
					String displaySql = rset.getString("display_sql");

					String tBoName = rset.getString("boname");
					String tBoTitle = rset.getString("botitle");
					int sn = rset.getInt("sn");
					String ylzd = rset.getString("ylzd");

					MetaBean mb = new MetaBean();
					mb.setId(id);
					mb.setFname(fname);
					mb.setFtitle(ftitle);
					mb.setFieldType(fieldType);
					mb.setDisplayType(displayType);
					mb.setDisplaySql(displaySql);

					mb.setBoName(tBoName);
					mb.setBoTitle(tBoTitle);
					mb.setSn(sn);
					mb.setYlzd(ylzd);

					map.put(fname, mb);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBSql.close(null, stmt, rset);
		}
		return map;
	}

	public Connection getConn() {
		if (conn == null) {
			conn = DBSql.open();
		}
		return conn;
	}

	public void closeConn() {
		DBSql.close(conn, null, null);
	}

	public Statement getStatement() throws SQLException {
		return getConn().createStatement();
	}

	public String findTaskListBy(String userId, int taskType, String workflowGroupName, String workflowDefUUID, String key, String orderBy) {
		int workflowDefId = 0;
		UserContext user = null;
		try {
			userId = Function.getUID(userId);
			user = new UserContext(userId);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return "当前登录人不合法";
		}
		if ((workflowDefUUID != null) && (workflowDefUUID.trim().length() > 0)) {
			try {
				workflowDefId = WorkflowEngine.getInstance().getWorkflowDefId(workflowDefUUID);
			} catch (WorkflowException we) {
				we.printStackTrace();
			}
		}
		String sql = "select a.id,a.bind_id,a.owner,a.status,a.title,a.begintime,a.priority,a.wf_style,a.read_task,a.wfid,a.wfsid,a.readtime,b.stepname,b.bindurl,c.title as wftitle from wf_task a,SYSFLOWSTEP b,wf_messagedata c where a.wfsid=b.id and a.bind_id=c.id and a.target='"
				+ userId + "' ";
		if (taskType == 1)
			sql = sql + " and (a.status=1 or a.status=3 or a.status=4 or a.status=11) ";
		else if (taskType == 2) {
			sql = sql + " and (a.status=2 or a.status=9) ";
		}
		if ((workflowGroupName != null) && (workflowGroupName.trim().length() > 0)) {
			sql = sql + " and a.wf_style='" + workflowGroupName + "' ";
		}
		if (workflowDefId > 0) {
			sql = sql + " and a.wfid=" + workflowDefId + " ";
		}

		if (!key.equals("")) {
			sql = sql + " and (a.title like '%" + key + "%' or owner='" + key + "'";
			Pattern pattern = Pattern.compile("[0-9]{1,4}-[0-9]{1,2}-[0-9]{1,2}");
			Matcher matcher = pattern.matcher(key);

			while (matcher.find())
				try {
					String tmpFind = matcher.group();
					String fromDate = DBSql.convertLongDate(tmpFind + " 00:00:00");
					String endDate = DBSql.convertLongDate(tmpFind + " 23:59:59");
					sql = sql + " or (begintime<= " + endDate + " and begintime>=" + fromDate + ") ";
				} catch (Exception localException1) {
				}
			sql = sql + " or exists(select userid from orguser where owner=orguser.userid and orguser.disenable=0 and orguser.username='"
					+ key + "') ";
			sql = sql + " ) ";
		}
		String myOrderBy = "a.id";
		if (!orderBy.equals("")) {
			myOrderBy = "a." + orderBy;
		}
		sql = sql + " order by " + myOrderBy + " desc ";
		Connection conn = DBSql.open();
		Statement stmt = null;
		ResultSet rset = null;
		JSONObject worklists = new JSONObject();
		JSONArray worklistJsonArr = new JSONArray();
		try {
			stmt = conn.createStatement();
			rset = DBSql.executeQuery(conn, stmt, sql);
			int no = 1;
			if (rset != null)
				while (rset.next()) {
					JSONObject taskJson = new JSONObject();
					taskJson.put("no", no);
					taskJson.put("taskId", rset.getInt("id"));
					taskJson.put("processInstanceId", rset.getInt("bind_id"));
					taskJson.put("status", rset.getInt("status"));
					String owner = rset.getString("owner");
					taskJson.put("owner", owner);
					UserModel u = (UserModel) UserCache.getModel(owner);
					taskJson.put("ownerName", u == null ? "" : u.getUserName());
					taskJson.put("title", rset.getString("title"));
					taskJson.put("processGroup", rset.getString("wf_style"));
					taskJson.put("beginTime", rset.getTimestamp("begintime"));
					taskJson.put("isRead", rset.getBoolean("read_task"));
					taskJson.put("readTime", rset.getTimestamp("readtime"));
					taskJson.put("priority", rset.getInt("priority"));
					taskJson.put("processDefId", rset.getInt("wfid"));
					taskJson.put("activityDefId", rset.getInt("wfsid"));
					taskJson.put("stepname", rset.getString("stepname"));
					taskJson.put("wftitle", rset.getString("wftitle"));
					String bindUrl = rset.getString("bindurl");
					if (bindUrl == null) {
						bindUrl = "";
					} else {
						bindUrl = RuleAPI.getInstance().executeRuleScript(bindUrl, user, rset.getInt("bind_id"), rset.getInt("id"));
					}
					taskJson.put("bindUrl", bindUrl);
					worklistJsonArr.add(taskJson);
					no++;
				}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			DBSql.close(conn, stmt, rset);
		}
		try {
			worklists.put("tasks", worklistJsonArr);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return worklists.toString();
	}

	public String findStringBy(String boName, Map<String, Object> params, String rel) {
		String result = "";
		Statement stmt = null;
		Connection conn;
		try {
			conn = getConn();
			StringBuilder sql = new StringBuilder();
			sql.append("select " + rel);
			sql.append(" from " + boName + " where 1=1 ");
			for (String key : params.keySet()) {
				sql.append("and " + key + "='" + params.get(key) + "'");
			}

			D.out("findStringBy:" + sql.toString());
			stmt = conn.createStatement();
			ResultSet rest = stmt.executeQuery(sql.toString());
			if (rest != null) {
				if (rest.next()) {
					result = rest.getString(rel);
				}
			}
			rest.close();
			stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return result;
	}

	public List<Map<String, String>> findDataBy(String boName, Map<String, Object> params, List<String> result, String orderBy) {
		List<Map<String, String>> relList = new ArrayList<Map<String, String>>();
		Connection conn;
		try {
			conn = getConn();
			StringBuilder sql = new StringBuilder();
			sql.append("select ");
			for (String rel : result) {
				sql.append(rel + ",");
			}
			sql.delete(sql.length() - 1, sql.length());
			sql.append(" from " + boName + " where 1=1 ");
			for (String key : params.keySet()) {
				sql.append("and " + key + "='" + params.get(key) + "'");
			}
			if (orderBy != null && orderBy.length() > 0) {
				sql.append(" order by " + orderBy);
			}
			D.out(sql.toString());
			Statement stmt = conn.createStatement();
			ResultSet rest = stmt.executeQuery(sql.toString());
			if (rest != null) {
				while (rest.next()) {
					Map<String, String> rowMap = new LinkedHashMap<String, String>();
					for (String rel : result) {
						rowMap.put(rel, rest.getString(rel));
					}
					relList.add(rowMap);
				}
			}
			rest.close();
			stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return relList;
	}

	/*
	 * 转换列表
	 */
	private Map<String, String> convDisplaySql(String displaySql) {
		String[] vals = displaySql.split("\\|");
		Map<String, String> map = new HashMap<String, String>();
		for (String v : vals) {
			if (v.contains(":")) {
				String[] v_k = v.split(":");
				map.put(v_k[0], v_k[1]);
			} else {
				map.put(v, v);
			}
		}
		return map;
	}

	/* 空返回空串 */
	private String getRsString(ResultSet rest, String fieldName) {
		String result = "";
		try {
			result = rest.getString(fieldName);
			if (result == null || result.isEmpty()) {
				result = "";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/* 空返回空串 并 过滤 string2Json */
	private String getRsString2Json(ResultSet rest, String fieldName) {
		String result = "";
		try {
			result = rest.getString(fieldName);
			if (result == null || result.isEmpty()) {
				result = "";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return StrUtils.string2Json(result);
	}

	/* 返回流程单据编号 */
	@SuppressWarnings("unchecked")
	public String getNoByResultSet(ResultSet rset, String boName) {
		D.out("开始获得单据编号:" + boName);
		String fieldName = null;
		MetaDataModel mm = (MetaDataModel) MetaDataCache.getModel(boName);
		if (mm != null) {
			Map<Integer, MetaDataMapModel> data = MetaDataMapCache.getListOfMetaData(mm.getId());
			for (Integer key : data.keySet()) {
				MetaDataMapModel item = data.get(key);
				if (item.getFieldTitle().equals("单据编号")) {
					fieldName = item.getFieldName();
				}
			}
		}

		String imRealNo = "";
		if (StrUtils.isNotBlank(fieldName)) {
			try {
				imRealNo = rset.getString(fieldName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return imRealNo;
	}

	/*
	 * 查看是否还有子任务
	 */
	public int getCountByTaskId(int taskId) {
		SqlUtils sqlUtils = SqlUtils.getInstance();
		Connection conn = getConn();
		String childSql = "select count(1) cc from wf_task where status=11 and from_point=" + taskId;
		return sqlUtils.getIntBySQL(conn, childSql, "cc");

	}

	/*
	 * 查看版本信息
	 */
	public VersionBean getVersion(String t) {
		VersionBean vb = new VersionBean();
		String sql = "select id,v,url,remark from sys_app_version where id='"+t+"'";
		Connection conn = DBSql.open();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			if (rset != null) {
				if (rset.next()) {
					vb.setId(rset.getString("id"));
					vb.setV(rset.getString("v"));
					vb.setUrl(rset.getString("url"));
					vb.setRemark(rset.getString("remark"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) {
					conn.close();
					conn = null;
				}
				if (rset != null) {
					rset.close();
					rset = null;
				}
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return vb;
	}

}
