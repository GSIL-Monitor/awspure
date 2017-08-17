package com.awspure.system.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.actionsoft.application.server.conf.AWFConfig;
import com.actionsoft.application.server.conf.DataBaseServerConf;
import com.actionsoft.awf.util.DBSql;

/**
 * @description 数据库操作工具类
 * */
public class SqlUtils {

	private PropertyUtils propertyUtils = null;

	private SqlUtils() {
		super();
		propertyUtils = PropertyUtils.getInstance();
	}

	private volatile static SqlUtils sqlUtils;

	public static SqlUtils getInstance() {
		if (sqlUtils == null) {
			synchronized (SqlUtils.class) {
				if (sqlUtils == null) {
					sqlUtils = new SqlUtils();
				}
			}
		}
		return sqlUtils;
	}

	public Connection getConnBySwitch(String switchSystem) {
		Connection conn = null;
		try {
			if (StrUtils.isNotBlank(switchSystem)) {
				D.out("Open Conn : " + switchSystem);
				String driver = propertyUtils.getProperty(switchSystem + ".driver");
				String url = propertyUtils.getProperty(switchSystem + ".url");
				String username = propertyUtils.getProperty(switchSystem + ".username");
				String password = propertyUtils.getProperty(switchSystem + ".password");

				if (StrUtils.isNotBlank(driver) && StrUtils.isNotBlank(url) && StrUtils.isNotBlank(username)
						&& StrUtils.isNotBlank(password)) {
					// 获取指定数据库
					Class.forName(driver);
					conn = DriverManager.getConnection(url, username, password);
				} else {
					conn = DBSql.open();
				}
			} else {
				conn = DBSql.open();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	// 通过系统判断是本地还是测试环境
	public Connection getDefaultDBC() {
		// 通过获得系统属性构造属性类 prop
		String systemName = propertyUtils.getOsName();// 获取操作系统名称
		D.out("Open Conn : DBSql.open()");
		if (systemName.contains("Windows")) {   
			return getConnBySwitch("local");
			//return DBSql.open();
		} else {
			return DBSql.open();
		}
	}

	public String getStringBySQL(Connection conn, String sql, String field) {
		Statement stmt = null;
		ResultSet rset = null;
		String result = null;
		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			if (rset != null) {
				if (rset.next()) {
					result = StrUtils.defaultIfEmpty(rset.getString(field));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
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
		return result;
	}

	public int getIntBySQL(Connection conn, String sql, String field) {
		Statement stmt = null;
		ResultSet rset = null;
		int result = 0;
		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			if (rset != null) {
				if (rset.next()) {
					result = rset.getInt(field);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
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
		return result;
	}

	public List<String> getListBySQL(Connection conn, String sql, String field) {
		List<String> resultList = new ArrayList<String>();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			if (rset != null) {
				while (rset.next()) {
					String value = StrUtils.defaultIfEmpty(rset.getString(field));
					resultList.add(value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
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

		return resultList;
	}

	public List<Integer> getListIntBySQL(Connection conn, String sql, String field) {
		List<Integer> resultList = new ArrayList<Integer>();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			if (rset != null) {
				while (rset.next()) {
					resultList.add(rset.getInt(field));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
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

		return resultList;
	}

	public List<Map<String, String>> getListMapBySql(Connection conn, String sql, String[] fields) {
		List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			if (rset != null) {
				while (rset.next()) {
					Map<String, String> row = new HashMap<String, String>();
					for (String field : fields) {
						String value = StrUtils.defaultIfEmpty(rset.getString(field));
						row.put(field, value);
					}
					resultList.add(row);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
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

		return resultList;
	}

	public List<Map<String, String>> getListMapBySql(Connection conn, String sql, List<String> fieldList) {
		List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			if (rset != null) {
				while (rset.next()) {
					Map<String, String> row = new HashMap<String, String>();
					for (String field : fieldList) {
						String value = StrUtils.defaultIfEmpty(rset.getString(field));
						row.put(field, value);
					}
					resultList.add(row);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
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

		return resultList;
	}

	public List<Map<Integer, String>> getListMapByIndex(Connection conn, String sql, int max) {
		List<Map<Integer, String>> resultList = new ArrayList<Map<Integer, String>>();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			if (rset != null) {
				while (rset.next()) {
					Map<Integer, String> row = new HashMap<Integer, String>();
					for (int i = 1; i <= max; i++) {
						String value = StrUtils.defaultIfEmpty(rset.getString(i));
						row.put(i, value);
					}
					resultList.add(row);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
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

		return resultList;
	}

	public Map<String, String> getMapBySql(Connection conn, String sql, String[] fields) {
		Map<String, String> result = new HashMap<String, String>();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			if (rset != null) {
				if (rset.next()) {
					for (String field : fields) {
						String value = StrUtils.defaultIfEmpty(rset.getString(field));
						result.put(field, value);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
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

		return result;
	}

	public Map<String, String> getMapBySql(Connection conn, String sql, List<String> fieldList) {
		Map<String, String> result = new HashMap<String, String>();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			if (rset != null) {
				if (rset.next()) {
					for (String field : fieldList) {
						String value = StrUtils.defaultIfEmpty(rset.getString(field));
						result.put(field, value);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
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

		return result;
	}

	public int execUpdateBySQL(Connection conn, String sql) {
		Statement stmt = null;
		int result = 0;
		try {
			stmt = conn.createStatement();
			result = stmt.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
					stmt = null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public String getPageSQL(String sql, int start, int rows) throws Exception {
		DataBaseServerConf conf = AWFConfig._databaseServerConf;
		StringBuffer sb = new StringBuffer();
		if (conf.isMySQL()) {
			sb.append(sql);
			sb.append(" limit ");
			sb.append(start);
			sb.append(",");
			sb.append(rows);
		} else if (conf.isOracle()) {
			sb.append("SELECT /*+ FIRST_ROWS */ * ");
			sb.append("FROM ( SELECT A.*, ROWNUM RN FROM (");
			sb.append(sql);
			sb.append(") A WHERE ROWNUM <=");
			sb.append(start + rows);
			sb.append(") WHERE RN >");
			sb.append(start);
		} else {
			throw new Exception("no matching database.");
		}
		return sb.toString();
	}

	public String getCountSql(String sql) {
		int index = sql.indexOf("from");
		return "select count(1) as rowCount " + sql.substring(index);
	}

	public void closeConn(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
				conn = null;
				D.out("Close Conn!");
			} else {
				D.out("Close Conn! But Conn is null .");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void closeConn(Connection conn, Statement stmt) {
		try {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
				D.out("Close Conn!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void closeConn(Connection conn, Statement stmt, ResultSet rset) {
		try {
			if (rset != null) {
				rset.close();
				rset = null;
			}
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
				D.out("Close Conn!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 批量更新, 普通的Statement
	 * 
	 * @param conn
	 * @param sqls
	 *            批量的SQL
	 * @return 每条SQL 成功执行数
	 */
	public int[] executeBatchByStatement(Connection conn, List<String> sqls) {
		int[] result = new int[0];
		Statement stm = null;
		try {
			conn.setAutoCommit(false);
			stm = conn.createStatement();
			if (sqls.size() > 0) {
				for (String sql : sqls) {
					stm.addBatch(sql);
				}
			}
			return stm.executeBatch();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				conn.setAutoCommit(true);
				if (stm != null) {
					stm.close();
					stm = null;
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 批量更新 PreparedStatement
	 * 
	 * @param conn
	 * @param sql
	 *            同一条SQL
	 * @param params
	 *            多条的参数
	 * @return 每条SQL 成功执行数
	 */
	public int[] executeBatchByPreparedStatement(Connection conn, String sql, List<Object[]> params) {

		int[] result = new int[0];
		PreparedStatement pstmt = null;
		try {
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(sql);
			if (null != params) {
				for (Object[] param : params) {
					for (int i = 0; i < param.length; i++) {
						pstmt.setObject(i + 1, param[i]);
					}
					pstmt.addBatch();
				}
			}
			return pstmt.executeBatch();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				conn.setAutoCommit(true);
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	private static int batchNum = 1000;// 批处理，每次提交个数

	/**
	 * 批量更新sql
	 * 
	 * @param sqlList
	 */
	public void executeBatch(Connection conn, List<String> sqlList) {
		List<String> tempList = new ArrayList<String>(batchNum);
		try {
			conn.setAutoCommit(false);
			PreparedStatement ps = conn.prepareStatement("");
			// conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			int num = 0;
			for (int i = 0; i < sqlList.size(); i++) {
				num++;
				tempList.add(sqlList.get(i));
				ps.addBatch(sqlList.get(i));
				if (num % batchNum == 0) {
					ps.executeBatch();
					ps.clearBatch();
					conn.commit();
					D.out("批量处理成功，记录数：" + num);
					tempList.clear();
				}
			}
			if (num % batchNum != 0) {
				ps.executeBatch();
				ps.clearBatch();
				conn.commit();
				D.out("批量处理成功，记录数：" + num);
				tempList.clear();
			}
		} catch (Exception e) {
			D.log("批量处理失败，失败记录集合：" + tempList.toString());
			D.log("回滚了2！" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}
}
