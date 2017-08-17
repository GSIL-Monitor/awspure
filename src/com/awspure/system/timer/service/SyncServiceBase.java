package com.awspure.system.timer.service;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.poi.hssf.record.formula.functions.T;
import com.awspure.system.util.SqlUtils;

public class SyncServiceBase {
	/**
	 * 創建匹配節點
	 * @param fac
	 * @param omNs
	 * @param key
	 * @param val
	 * @return
	 */
   protected OMElement createMapEntry(OMFactory fac,OMNamespace omNs, String key, String val) {
	   OMElement mapEntry = fac.createOMElement(key, omNs);
	   mapEntry.setText(val);
       return mapEntry;
    }
   /**
    * 根据sequencename获取id
    * @param sqlUtils
    * @param conn
    * @param sequencename
    * @return id
    */
   protected int getSequenceId(SqlUtils sqlUtils, Connection conn, String sequencename){
	   sqlUtils.execUpdateBySQL(conn, "update SYSSEQUENCE set sequencevalue = sequencevalue+sequencestep where SEQUENCENAME = '"+sequencename+"'");
	   String idSql = "select SEQUENCEVALUE from SYSSEQUENCE where SEQUENCENAME = '"+sequencename+"'";
	   return sqlUtils.getIntBySQL(conn, idSql, "SEQUENCEVALUE");
   }
   
   @SuppressWarnings("hiding")
   protected <T> void saveErrMsg(SqlUtils sqlUtils, Connection conn, T t, String errmsg,String functionName){
	   SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	   StringBuilder insertSql = new StringBuilder();
	   insertSql.append("insert into SYN_ERR_MSG ");
	   insertSql.append("(NOWTIME, ERRMSG, UPDATE_DATA, FUNCTION_NAME, update_date)");
	   insertSql.append(" values (");
	   insertSql.append("'"+String.valueOf(System.currentTimeMillis())+"',");
	   insertSql.append("'"+errmsg+"',");
	   insertSql.append("'"+t.toString()+"',");
	   insertSql.append("'"+functionName+"',");
	   insertSql.append("'"+df.format(new Date())+"')");
	   sqlUtils.execUpdateBySQL(conn, insertSql.toString());
   }
}
