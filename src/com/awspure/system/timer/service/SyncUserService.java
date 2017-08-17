package com.awspure.system.timer.service;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import com.awspure.system.common.entity.OrgUser;
import com.awspure.system.util.D;
import com.awspure.system.util.SqlUtils;
import com.awspure.system.util.StrUtils;

public class SyncUserService extends SyncServiceBase {
	private SyncUserService(){};
	private volatile static SyncUserService instance = null;
	
	/**
	 * @return 单例
	 */
	public static SyncUserService getInstance() {
		if(instance == null) {
			synchronized (SyncUserService.class) {
				if(instance == null) {
					instance = new SyncUserService();
				}
			}
		}
		return instance ;
	}
	
	private final String url = "http://172.20.107.240:8080/manage/html/SOAPServiceExt";
	private final String synUserAction = "syncEmployeeToAWS";
	private final String dataLocalName = "O_SYS_REFCUR";
	private final String orgPwd = "e10adc3949ba59abbe56e057f20f883e";
	private OMFactory fac = OMAbstractFactory.getOMFactory();
    private OMNamespace omNs;
    
    @SuppressWarnings("rawtypes")
	public List<OrgUser> getNewUserList(String fromDate, String thruDate) throws AxisFault{
    	ServiceClient sc = new ServiceClient();
        Options opts = new Options();
        opts.setTo(new EndpointReference(url));
        opts.setAction(synUserAction);
        sc.setOptions(opts);
                
        OMElement obj = setUserQuery(fromDate, thruDate);
        OMElement res = sc.sendReceive(obj);
        
        res = res.getFirstElement();
        Iterator iterator = res.getChildElements();
        List<OrgUser> userList = digui(iterator, new ArrayList<OrgUser>());
        return userList;
    }
    
    @SuppressWarnings("rawtypes")
	private List<OrgUser> digui(Iterator iterator, List<OrgUser> userList){
   	 while (iterator.hasNext()) {
   		 OMElement omcol = (OMElement) iterator.next();
   		 if(omcol.getLocalName().equals(dataLocalName)){
       		 Iterator rows = omcol.getChildElements();
       		while(rows.hasNext()){
      			OMElement row = (OMElement) rows.next();
      			Iterator contents = row.getChildElements();
      			OrgUser user = new OrgUser();
      			while(contents.hasNext()){
      				OMElement content = (OMElement) contents.next();
      				QName qName = new QName("name");
      				String name = content.getAttribute(qName).getAttributeValue();
      				String value = content.getText();
      				if("personCode".equals(name)){
      					user.setUserId(value);
      					user.setUserNo(value);
      					user.setPassWord(orgPwd);
      				}else if("firstName".equals(name)){
      					user.setUserName(value);
      				}else if("isMajor".equals(name)){
      					user.setIsMajor(value);
      				}else if("positionCodeName".equals(name)){
      					user.setRoleName(value);
      				}else if("partyCode".equals(name)){
      					user.setDepartmentNo(value);
      				}else if("postionNbr".equals(name)){
      					user.setPositionNo(value);
      				}else if("positionName".equals(name)){
      					user.setPositionName(value);
      				}else if("positionLevel".equals(name)){
      					user.setLevel(value);
      				}else if("statusId".equals(name)){
      					user.setStatus("1");
      				}else if("isPreside".equals(name)){
      					user.setIsManager(value.equals("N")?"0":"1");
      				}
      			}
      			userList.add(user);
      		 }
       		 break;
   		 }
   		 digui(omcol.getChildElements(), userList);
   	 }
   	 return userList;
    }
    
    /**
     * 查询
     * @param fromDate
     * @param thruDate
     * @return
     */
    private OMElement setUserQuery(String fromDate, String thruDate) {
        OMElement queryOm = null;
        try {
        	queryOm = fac.createOMElement(synUserAction, omNs);
            OMElement mapMap = fac.createOMElement("List", omNs);
            mapMap.addChild(createMapEntry(fac, omNs, "fromDate", fromDate));
            mapMap.addChild(createMapEntry(fac, omNs, "thruDate", thruDate));
            
            queryOm.addChild(mapMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryOm;
     }
    
    public String isExitUserByNo(Connection conn, String userNo){
    	SqlUtils sqlUtils = SqlUtils.getInstance();
    	String isExit = "";
    	Statement stmt = null;
    	try{
    		stmt = conn.createStatement();
    		String sql = "select id from orguser where userno='"+userNo+"'";
    		isExit = sqlUtils.getStringBySQL(conn, sql, "id");
    	}catch(Exception e){
    		D.err(e.getMessage());
    		e.printStackTrace();
    	}finally{
    		sqlUtils.closeConn(null, stmt);
    	}
    	return isExit;
    }
    
    public void insertUser(Connection conn, OrgUser user){
    	SqlUtils sqlUtils = SqlUtils.getInstance();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			// 创建获取序列
			int id = getSequenceId(sqlUtils, conn, "SYS_ORG");
			//获取部门id
			String deptId = sqlUtils.getStringBySQL(conn, "select id from orgdepartment where departmentno='"+user.getDepartmentNo()+"'", "id");
			
			String orderIndex = sqlUtils.getStringBySQL(conn, "select NVL(max(orderindex),0)+1 num from orguser where departmentid="+deptId, "num");
			//处理用户角色信息
			insertUserRole(conn, user.getRoleName());
			String roleId = sqlUtils.getStringBySQL(conn, "select id from orgrole where rolename='"+user.getRoleName()+"'", "id");
			
			// 组装插入sql 基础信息
			StringBuilder insertSQL = new StringBuilder();
			insertSQL.append("INSERT INTO orguser (");
			insertSQL.append("ID, USERID, PASSWORD, DEPARTMENTID, ROLEID, ORDERINDEX"
							+ ", DISENABLE, SESSIONTIME, USERNAME,LINENUMBER, LOGINCOUNTER, USERNO"
							+ ",ISIPLOGIN, USERIP, ISROVING, EXTEND1, EXTEND2,ISMANAGER"
							+ ",ISSINGLELOGIN, WORK_STATUS, SMID,PC_MAN, AWSPASS"
							+ ",EMAILPASS, LAYOUT_MODEL, POSITION_NO, POSITION_NAME, POSITION_LAYER) ");
			insertSQL.append(" values ");
			insertSQL.append("('" + id + "','" + user.getUserId() + "','"+user.getPassWord()+"',"+deptId+",'"+roleId+"',"+orderIndex 
					+ ",'0','120','" + user.getUserName() + "','18', '0', '"+ user.getUserNo()+"'"
					+ ",'0','','0',NULL,NULL" 
					+ ",'0','','','',''"
					+ ",'','aws-portlet-layout.7','"+user.getPositionNo()+"','"+user.getPositionName()+"','" + user.getLevel() + "')");
			int r1 = stmt.executeUpdate(insertSQL.toString());
			if(r1 == 0){
				super.saveErrMsg(sqlUtils, conn, user, "增加人员基础信息异常，错误信息【" + insertSQL.toString() +"】", "SyncUserService.insertUser()");
				D.out("增加人员基础信息失败："+insertSQL.toString());
			}
		} catch (Exception e) {
			super.saveErrMsg(sqlUtils, conn, user, "增加人员基础信息异常，错误信息【" + e.getMessage()+"】", "SyncUserService.insertUser()");
			D.out("增加人员基础信息异常!" + e.getMessage());
		} finally {
			sqlUtils.closeConn(null, stmt);
		}
    }
    
    public void updateUser(Connection conn, OrgUser user){
    	SqlUtils sqlUtils = SqlUtils.getInstance();
		try {
			//String deptId = sqlUtils.getStringBySQL(conn, "select id from orgdepartment where departmentno='"+user.getDepartmentNo()+"'", "id");
			StringBuilder updateSql = new StringBuilder();
			updateSql.append("UPDATE orguser SET ");
			/*updateSql.append("USERID='" + user.getUserId() + "'");
			updateSql.append(",USERNAME='" + user.getUserName() + "'");
			updateSql.append(",DEPARTMENTID='" + deptId + "'");
			updateSql.append(",POSITION_LAYER='" + user.getLevel() + "'");
			updateSql.append(",POSITION_NAME='" + user.getPositionName() + "'");*/
			updateSql.append("ISMANAGER='" + user.getIsManager() + "'");
			updateSql.append(" where USERNO ='" + user.getUserNo() + "'");
			int r1 = sqlUtils.execUpdateBySQL(conn, updateSql.toString());
			if(r1 == 0){
				D.out("update 0 条记录："+updateSql.toString());
			}
		} catch (Exception e) {
			super.saveErrMsg(sqlUtils, conn, user, "更新员工基本信息异常，错误信息【" + e.getMessage()+"】", "SyncUserService.updateUser()");
			D.out("更新员工基本信息异常!" + e.getMessage());
		}
    }
    
    /**
     * 插入用户角色
     * @param conn
     * @param roleName
     */
    public void insertUserRole(Connection conn, String roleName){
    	SqlUtils sqlUtils = SqlUtils.getInstance();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			// 创建获取序列
			int id = getSequenceId(sqlUtils, conn, "SYS_ORG");
			String isExit = sqlUtils.getStringBySQL(conn, "select id from orgrole where rolename='"+roleName+"'", "id");
			if(StrUtils.isBlank(isExit)){
				String orderIndex = sqlUtils.getStringBySQL(conn, "select max(orderindex)+1 cc from orgrole where GROUPNAME='用户'", "cc");
				//组装插入sql 基础信息
				StringBuilder insertSQL = new StringBuilder();
				insertSQL.append("INSERT INTO orgrole (");
				insertSQL.append("ID, ROLENAME, ORDERINDEX, GROUPNAME)");
				insertSQL.append(" values ");
				insertSQL.append("('" + id + "','" + roleName + "','"+orderIndex+"','用户')");
				int r1 = stmt.executeUpdate(insertSQL.toString());
				if(r1 == 0){
					super.saveErrMsg(sqlUtils, conn, roleName, "增加角色信息异常，错误信息【" + insertSQL.toString() +"】", "SyncUserService.insertUserRole()");
					D.out("增加角色信息失败："+insertSQL.toString());
				}
			}
		} catch (Exception e) {
			super.saveErrMsg(sqlUtils, conn, roleName, "增加角色信息异常，错误信息【" + e.getMessage()+"】", "SyncUserService.insertUserRole()");
			D.out("增加角色信息异常，错误信息："+e.getMessage());
		} finally {
			sqlUtils.closeConn(null, stmt);
		}
    }
    
    public void insertUserMap(Connection conn, OrgUser user){
    	SqlUtils sqlUtils = SqlUtils.getInstance();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			// 创建获取序列
			int id = getSequenceId(sqlUtils, conn, "SYS_ORG");
			//获取部门id
			String deptId = sqlUtils.getStringBySQL(conn, "select id from orgdepartment where departmentno='"+user.getDepartmentNo()+"'", "id");
			
			//获取用户id
			String userid = sqlUtils.getStringBySQL(conn, "select id from orguser where userno='"+user.getUserNo()+"'", "id");
			//是否已存在该兼岗
			//String isExit = sqlUtils.getStringBySQL(conn, "select id from orgusermap where mapid="+userid+" and departmentid="+deptId, "id");
			
			if(StrUtils.isBlank(userid)){
				super.saveErrMsg(sqlUtils, conn, user, "增加兼职信息异常，错误信息【主岗信息不存在】", "SyncUserService.insertUserMap()");
				D.out(user.toString()+"...主岗信息不存在");
			}else if(StrUtils.isNotBlank(deptId)){
				super.saveErrMsg(sqlUtils, conn, user, "增加兼职信息异常，错误信息【兼岗部门不存在】", "SyncUserService.insertUserMap()");
				D.out(user.toString()+"...兼岗部门不存在");
			}else{
				insertUserRole(conn, user.getRoleName());
				String roleId = sqlUtils.getStringBySQL(conn, "select id from orgrole where rolename='"+user.getRoleName()+"'", "id");
				if(StrUtils.isBlank(roleId)){
					super.saveErrMsg(sqlUtils, conn, user, "增加兼职信息异常，错误信息【该员工角色不存在】", "SyncUserService.insertUserMap()");
					D.out(user.toString()+"...该员工角色不存在");
					//roleId = StrUtils.isNotBlank(roleId)?roleId:"181";//如果
				}else{
					//组装插入sql 基础信息
					StringBuilder insertSQL = new StringBuilder();
					insertSQL.append("INSERT INTO orgusermap (");
					insertSQL.append("ID, MAPID, DEPARTMENTID, ROLEID, ISMANAGER, ISSHOW)");
					insertSQL.append(" values ");
					insertSQL.append("('" + id + "','" + userid + "','"+deptId+"','"+roleId+"','0','0')");
					int r1 = stmt.executeUpdate(insertSQL.toString());
					if(r1 == 0){
						super.saveErrMsg(sqlUtils, conn, user, "增加兼职信息异常，错误信息【" + insertSQL.toString()+"】", "SyncUserService.insertUserMap()");
						D.out("增加兼职信息失败："+insertSQL.toString());
					}
				}
			}
		} catch (Exception e) {
			super.saveErrMsg(sqlUtils, conn, user, "增加兼职信息异常，错误信息【" + e.getMessage()+"】", "SyncUserService.insertUserMap()");
			D.out("增加兼职信息异常，错误信息："+e.getMessage());
		} finally {
			sqlUtils.closeConn(null, stmt);
		}
    }
}
