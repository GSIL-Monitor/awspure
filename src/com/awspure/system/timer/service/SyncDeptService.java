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
import com.awspure.system.common.entity.OrgDepartment;
import com.awspure.system.util.D;
import com.awspure.system.util.SqlUtils;
import com.awspure.system.util.StrUtils;


public class SyncDeptService extends SyncServiceBase {

	private SyncDeptService() {}
	
	private volatile static SyncDeptService instance = null;
	
	/**
	 * @return 单例
	 */
	public static SyncDeptService getInstance() {
		if(instance == null) {
			synchronized (SyncDeptService.class) {
				if(instance == null) {
					instance = new SyncDeptService();
				}
			}
		}
		return instance ;
	}
	
	private final String url = "http://172.20.107.240:8080/manage/html/SOAPServiceExt";
	private final String synUserAction = "syncCompanyToAWS";
	private final String dataLocalName = "O_SYS_REFCUR";
	private OMFactory fac = OMAbstractFactory.getOMFactory();
    private OMNamespace omNs;
	
    @SuppressWarnings("rawtypes")
	public List<OrgDepartment> getNewDeptList(String fromDate, String thruDate) throws AxisFault{
    	ServiceClient sc = new ServiceClient();
        Options opts = new Options();
        opts.setTo(new EndpointReference(url));
        opts.setAction(synUserAction);
        sc.setOptions(opts);
                
        OMElement obj = setDeptQuery(fromDate, thruDate);
        OMElement res = sc.sendReceive(obj);     
        
        res = res.getFirstElement();
        Iterator iterator = res.getChildElements();
        List<OrgDepartment> deptList = digui(iterator, new ArrayList<OrgDepartment>());
        return deptList;
    }
    
    @SuppressWarnings("rawtypes")
	private List<OrgDepartment> digui(Iterator iterator, List<OrgDepartment> deptList){
   	 while (iterator.hasNext()) {
   		 OMElement omcol = (OMElement) iterator.next();
   		 if(omcol.getLocalName().equals(dataLocalName)){
       		 Iterator rows = omcol.getChildElements();
       		while(rows.hasNext()){
      			OMElement row = (OMElement) rows.next();
      			Iterator contents = row.getChildElements();
      			OrgDepartment dept = new OrgDepartment();
      			while(contents.hasNext()){
      				OMElement content = (OMElement) contents.next();
      				QName qName = new QName("name");
      				String name = content.getAttribute(qName).getAttributeValue();
      				String value = content.getText();
      				if("groupName".equals(name)){
      					dept.setDepartmentName(value);
      				}else if("partyCode".equals(name)){
      					dept.setDepartmentNo(value);
      				}else if("parentCode".equals(name)){
      					dept.setParentDepartmentNo(value);
      				}
      			}
      			deptList.add(dept);
      		 }
       		 break;
   		 }
   		 digui(omcol.getChildElements(), deptList);
   	 }
   	 return deptList;
    }
    
    /**
     * 查询
     * @param fromDate
     * @param thruDate
     * @return
     */
    private OMElement setDeptQuery(String fromDate, String thruDate) {
        OMElement queryOm = null;
        try {
        	queryOm = fac.createOMElement(synUserAction, omNs);
            OMElement mapMap = fac.createOMElement("map-Map", omNs);
            queryOm.addChild(mapMap);
            mapMap.addChild(createMapEntry(fac, omNs, "fromDate", fromDate));
            mapMap.addChild(createMapEntry(fac, omNs, "thruDate", thruDate));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryOm;
     }
    
    public String isExitDeptByNo(Connection conn, String deptNo){
    	SqlUtils sqlUtils = SqlUtils.getInstance();
    	String isExit = "";
    	Statement stmt = null;
    	try{
    		stmt = conn.createStatement();
    		String sql = "select id from orgdepartment where departmentno='"+deptNo+"'";
    		isExit = sqlUtils.getStringBySQL(conn, sql, "id");
    	}catch(Exception e){
    		D.err(e.getMessage());
    		e.printStackTrace();
    	}finally{
    		sqlUtils.closeConn(null, stmt);
    	}
    	return isExit;
    }
    
    /**
     * 插入新部门
     * @param conn
     * @param dept
     */
	public void insertDept(Connection conn, OrgDepartment dept) {
		SqlUtils sqlUtils = SqlUtils.getInstance();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			// 查询父级部门ID
			String pp = null;
			if (StrUtils.isNotBlank(dept.getParentDepartmentNo()) && !dept.getParentDepartmentNo().equals("0")) {
				StringBuilder ppSql = new StringBuilder();
				ppSql.append("select (a.id||'_'||TO_NUMBER(layer+1,0)||'_'||");
				ppSql.append("(select NVL(max(b.orderindex)+1,1) from orgdepartment b where b.parentdepartmentid=a.id)");
				ppSql.append(") pp ");
				ppSql.append("from orgdepartment a where a.departmentno = '"+dept.getParentDepartmentNo()+"'");
				pp = sqlUtils.getStringBySQL(conn,ppSql.toString(), "pp");
			}else{
				String cc = sqlUtils.getStringBySQL(conn,
						"select NVL(max(orderindex)+1,1) cc from orgdepartment where parentdepartmentid=0", "cc");
				/*String cc = sqlUtils.getStringBySQL(conn,
						"select IFNULL(max(orderindex)+1,1) cc from orgdepartment where parentdepartmentid=0", "cc");*/
				pp = "0_1_" + cc;
			}
			
			if (StrUtils.isNotBlank(pp)) {
				// 创建获取序列
				int id = getSequenceId(sqlUtils, conn, "SYS_ORG");
				// 插入SQL
				String[] ppSplit = pp.split("_");
				String parentId = ppSplit[0];
				String layer = ppSplit[1];
				String orderIndex = ppSplit[2];
				
				StringBuilder insertSQL = new StringBuilder();
				insertSQL.append("insert into orgdepartment ");
				insertSQL.append("(id,departmentname,companyid,LOGINCOUNTER,departmentno,");
				insertSQL.append("parentdepartmentid,layer,orderindex)");
				insertSQL.append(" values ");
				insertSQL.append("(");
				insertSQL.append(id);
				insertSQL.append(",'");
				insertSQL.append(dept.getDepartmentName());
				insertSQL.append("',1,0,'");
				insertSQL.append(dept.getDepartmentNo());
				insertSQL.append("',");
				insertSQL.append(parentId);
				insertSQL.append(",");
				insertSQL.append(layer);
				insertSQL.append(",");
				insertSQL.append(orderIndex);
				insertSQL.append(")");
				D.out(insertSQL.toString());  
				int r1 = stmt.executeUpdate(insertSQL.toString());
				D.out("增加部门信息成功!" + r1);
			} else {
				super.saveErrMsg(sqlUtils, conn, dept, "增加部门信息成功，错误信息【父部门编码不存在】", "SyncDeptService.insertDept()");
				D.out(dept.getDepartmentNo() + "父部门编码不存在");
			}
		} catch (Exception e) {
			super.saveErrMsg(sqlUtils, conn, dept, "增加部门信息成功，错误信息【"+e.getMessage()+"】", "SyncDeptService.insertDept()");
			D.out("增加部门信息异常!" + e.getMessage());
		} finally {
			sqlUtils.closeConn(null, stmt);
		}
	}
	
	/**
	 * 更新部门
	 * @param conn
	 * @param dept
	 */
	public void updateDept(Connection conn, OrgDepartment dept){
		SqlUtils sqlUtils = SqlUtils.getInstance();
		String deptNo = dept.getDepartmentNo();
		Statement stmt = null;
		StringBuilder updateSQL = new StringBuilder();
		try {
			stmt = conn.createStatement();
			// 查询父级部门ID
			String pp = null;
			String boId = null;
			String oldParentNo = null;
			if(StrUtils.isNotBlank(deptNo)){
				boId = sqlUtils.getStringBySQL(conn, "select id from orgdepartment where departmentno='"+deptNo+"'", "id");
				oldParentNo = sqlUtils.getStringBySQL(conn, "select departmentno from orgdepartment where id=(select parentdepartmentid from orgdepartment where departmentno='"+deptNo+"')", "departmentno");
				oldParentNo = StrUtils.isBlank(oldParentNo)?"0":oldParentNo;
			}
			
			if(StrUtils.isNotBlank(oldParentNo) && oldParentNo.equals(dept.getParentDepartmentNo())){
				pp = sqlUtils.getStringBySQL(conn, "select (parentdepartmentid||'_'||layer||'_'||orderindex) pp from orgdepartment where id="+boId, "pp");
				//pp = sqlUtils.getStringBySQL(conn, "select concat(parentdepartmentid,'_',layer,'_',orderindex) pp from orgdepartment where id="+boId, "pp");
			}else{
				if (StrUtils.isNotBlank(dept.getParentDepartmentNo()) && !dept.getParentDepartmentNo().equals("0")) {
					StringBuilder ppSql = new StringBuilder();
					ppSql.append("select (a.id||'_'||TO_NUMBER(layer+1,0)||'_'||");
					ppSql.append("(select NVL(max(b.orderindex)+1,1) from orgdepartment b where b.parentdepartmentid=a.id)");
					ppSql.append(") pp ");
					ppSql.append("from orgdepartment a where a.departmentno = '");
					ppSql.append(dept.getParentDepartmentNo());
					ppSql.append("'");
					pp = sqlUtils.getStringBySQL(conn,ppSql.toString(), "pp");
				}else{
					String cc = sqlUtils.getStringBySQL(conn,
							"select NVL(max(orderindex)+1,1) cc from orgdepartment where parentdepartmentid=0", "cc");
					pp = "0_1_" + cc;
				}
			}
			
			if (StrUtils.isNotBlank(pp) && StrUtils.isNotBlank(boId)) {
				// SQL
				String[] ppSplit = pp.split("_");
				String parentId = ppSplit[0];
				String layer = ppSplit[1];
				String orderIndex = ppSplit[2];
				
				updateSQL.append("update orgdepartment ");
				updateSQL.append("set parentdepartmentid="+parentId);
				updateSQL.append(", layer="+layer);
				updateSQL.append(", orderindex="+orderIndex);
				updateSQL.append(", departmentname='"+dept.getDepartmentName()+"' ");
				updateSQL.append(" where id="+boId);
				D.out(updateSQL.toString());
				int r1 = stmt.executeUpdate(updateSQL.toString());
				D.out("变更部门信息成功!" + r1);
			} else {
				super.saveErrMsg(sqlUtils, conn, dept, "变更部门信息异常，错误信息【父部门编码不存在】", "SyncDeptService.updateDept()");
				D.out(dept.getDepartmentNo() + "父部门编码不存在");
			}
		} catch (Exception e) {
			super.saveErrMsg(sqlUtils, conn, dept, "变更部门信息异常，错误信息【"+ e.getMessage()+"】，错误sql="+updateSQL.toString(), "SyncDeptService.updateDept()");
			D.out("变更部门信息异常!" + e.getMessage());
		} finally {
			sqlUtils.closeConn(null, stmt);
		}
	}
	
}
