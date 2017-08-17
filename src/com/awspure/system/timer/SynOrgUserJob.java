package com.awspure.system.timer;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.actionsoft.application.schedule.IJob;
import com.awspure.system.timer.service.SyncUserService;
import com.awspure.system.util.D;
import com.awspure.system.util.SqlUtils;
import com.awspure.system.util.StrUtils;
import com.awspure.system.common.entity.OrgUser;

public class SynOrgUserJob implements IJob {

	@SuppressWarnings("static-access")
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.add(calendar.DAY_OF_YEAR, -1);
	    String fromDate = dateFormat.format(calendar.getTime());
	    String thruDate = dateFormat.format(date);
	    
		SyncUserService sUserService = SyncUserService.getInstance();
		SqlUtils sUtils = SqlUtils.getInstance();
		Connection conn = null;
		try{
			List<OrgUser> newUserList = sUserService.getNewUserList(fromDate, thruDate);
			List<OrgUser> userMapList = new ArrayList<OrgUser>();//兼职信息集合
			if(newUserList != null && newUserList.size() > 0){
				conn = sUtils.getDefaultDBC();
				for(OrgUser user : newUserList){
					String userNo = user.getUserNo();
					String isMajor = user.getIsMajor();// N 兼职  Y 主岗
					if("Y".equals(isMajor)){
						String isExit = sUserService.isExitUserByNo(conn, userNo);
						if(StrUtils.isNotBlank(isExit)){
							sUserService.updateUser(conn, user);
 						}else{
							sUserService.insertUser(conn, user);
						}
					}else{
						userMapList.add(user);
					}
				}
				/*-- 处理兼职 --*/
				if(userMapList != null && userMapList.size() > 0){
					for(OrgUser user : userMapList){
						sUserService.insertUserMap(conn, user);
					}
				}
			}
		}catch(Exception e){
			D.err(e.getMessage());
			e.printStackTrace();
		}finally{
			sUtils.closeConn(conn);
		}
	}
	
	public static void initUser(){
		SyncUserService sUserService = SyncUserService.getInstance();
		SqlUtils sUtils = SqlUtils.getInstance();
		Connection conn = null;
		String fromDate = "2016-08-20";
		String thruDate = "2016-09-23";
		try{
			List<OrgUser> newUserList = sUserService.getNewUserList(fromDate, thruDate);
			List<OrgUser> userMapList = new ArrayList<OrgUser>();//兼职信息集合
			if(newUserList != null && newUserList.size() > 0){
				//conn = sUtils.getDefaultDBC();
				conn = sUtils.getConnBySwitch("dev");
				for(OrgUser user : newUserList){
					String userNo = user.getUserNo();
					String isMajor = user.getIsMajor();// N 兼职  Y 主岗
					if("Y".equals(isMajor)){
						String isExit = sUserService.isExitUserByNo(conn, userNo);
						if(StrUtils.isNotBlank(isExit)){
							sUserService.updateUser(conn, user);
							//D.out(user.toString()+"///已有");
 						}else{
							sUserService.insertUser(conn, user);
						}
					}else{
						userMapList.add(user);
					}
				}
				/*-- 处理兼职 --*/
				if(userMapList != null && userMapList.size() > 0){
					for(OrgUser user : userMapList){
						sUserService.insertUserMap(conn, user);
					}
				}
			}
		}catch(Exception e){
			D.err(e.getMessage());
			e.printStackTrace();
		}finally{
			sUtils.closeConn(conn);
		}
	}
	
	public static void main(String[] args) {
		initUser();
		//setRoleDefaultSecurity();
	}
	
	public static void setRoleDefaultSecurity(){
		SqlUtils sUtils = SqlUtils.getInstance();
		Connection conn = null;
		try{
			conn = sUtils.getConnBySwitch("dev");
			String sql = "SELECT id FROM ORGROLE where id not in (select roleid from sys_rolesecurity where securitygroupid='1668')";
			List<String> roleList = sUtils.getListBySQL(conn, sql, "id");
			for(String roleId : roleList){
				sUtils.execUpdateBySQL(conn, "update SYSSEQUENCE set sequencevalue = sequencevalue+sequencestep where SEQUENCENAME = 'SYS_SECURITY'");
				String idSql = "select SEQUENCEVALUE from SYSSEQUENCE where SEQUENCENAME = 'SYS_SECURITY'";
				int id = sUtils.getIntBySQL(conn, idSql, "SEQUENCEVALUE");
				
				StringBuilder insertSb = new StringBuilder();
				insertSb.append("insert into sys_rolesecurity ");
				insertSb.append("(id,roleid,securitygroupid) ");
				insertSb.append(" values ");
				insertSb.append("("+id+","+roleId+",1668) ");
				System.out.println(insertSb.toString());
				int rel = sUtils.execUpdateBySQL(conn, insertSb.toString());
				if(rel > 0){
					System.out.println("insert success!");
				}
			}
		} catch (Exception e){
			//D.err(e.getMessage());
			e.printStackTrace();
		}finally{
			sUtils.closeConn(conn);
		}
	}
	
}
