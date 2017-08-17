package com.awspure.system.timer;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.actionsoft.application.schedule.IJob;
import com.awspure.system.common.entity.OrgDepartment;
import com.awspure.system.timer.service.SyncDeptService;
import com.awspure.system.util.D;
import com.awspure.system.util.SqlUtils;
import com.awspure.system.util.StrUtils;

public class SynOrgDeptJob implements IJob{

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
		
	    SyncDeptService sDeptService = SyncDeptService.getInstance();
		SqlUtils sUtils = SqlUtils.getInstance();
		Connection conn = null;
		try{
			List<OrgDepartment> newDeptList = sDeptService.getNewDeptList(fromDate, thruDate);
			if(newDeptList != null && newDeptList.size() > 0){
				//conn = sUtils.getDefaultDBC();
				//conn = sUtils.getConnBySwitch("dev");
				conn = sUtils.getConnBySwitch("awslocal");
				for(OrgDepartment dept : newDeptList){
					String deptNo = dept.getDepartmentNo();
					String isExit = sDeptService.isExitDeptByNo(conn, deptNo);
					if(StrUtils.isNotBlank(isExit)){
						sDeptService.updateDept(conn, dept);
					}else{
						sDeptService.insertDept(conn, dept);
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
	    String fromDate = "2016-09-23";
	    String thruDate = "2016-08-23";
		
	    SyncDeptService sDeptService = SyncDeptService.getInstance();
		SqlUtils sUtils = SqlUtils.getInstance();
		Connection conn = null;
		try{
			List<OrgDepartment> newDeptList = sDeptService.getNewDeptList(fromDate, thruDate);
			if(newDeptList != null && newDeptList.size() > 0){
				//conn = sUtils.getDefaultDBC();
				conn = sUtils.getConnBySwitch("dev");
				//conn = sUtils.getConnBySwitch("awslocal");
				for(OrgDepartment dept : newDeptList){
					String deptNo = dept.getDepartmentNo();
					String isExit = sDeptService.isExitDeptByNo(conn, deptNo);
					if(StrUtils.isNotBlank(isExit)){
						sDeptService.updateDept(conn, dept);
					}else{
						sDeptService.insertDept(conn, dept);
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
}
