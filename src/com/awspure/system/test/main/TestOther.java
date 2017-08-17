package com.awspure.system.test.main;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import com.actionsoft.awf.organization.cache.UserCache;
import com.actionsoft.awf.organization.model.UserModel;
import com.awspure.system.util.D;
import com.awspure.system.util.SqlUtils;

/**
 * @description 测试类
 * */
public class TestOther {

	
	/**
	 * @param args
	 * @throws AxisFault 
	 */
	public static void main(String[] args){
		//testSqlUtils();
		Hashtable userModel = UserCache.getUserListOfRole(1);
		UserModel uModel = new UserModel();
	}
	
	public static void testSqlUtils() {
		SqlUtils sqlUtils = SqlUtils.getInstance();
		Connection conn = sqlUtils.getDefaultDBC();
		try {
			if (conn != null) {
				String sql = "select t1,t2,t3 from test_db";
				List<Map<String, String>> dataList = sqlUtils.getListMapBySql(conn, sql, new String[] { "t1", "t2", "t3" });
				for (Map<String, String> data : dataList) {
					D.out(data.get("t1"));
					D.out(data.get("t2"));
					D.out(data.get("t3"));
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sqlUtils.closeConn(conn);
		}
	}
}
