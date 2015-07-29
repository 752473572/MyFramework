package com.dcee.core.data.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

public class DatabaseMetaUtils implements InitializingBean{
	private static final Map<String,Integer> types = Collections.synchronizedMap(new HashMap<String,Integer>());
	private static final Map<String,Integer> lengths = Collections.synchronizedMap(new HashMap<String,Integer>());
	private static final Logger logger = LoggerFactory.getLogger(DatabaseMetaUtils.class);
	private static DataSource ds;
	private DataSource dataSource;
	
	public static int getColumnType(String tableName,String columnName){
		String cacheKey = tableName.toUpperCase()+":"+columnName.toUpperCase();
		Integer integer = types.get(cacheKey);
		if(integer == null){
			reloadTableColumn(tableName);
			integer = types.get(cacheKey);
			if(integer == null){
				logger.error("Table[{}] column [{}] not found",tableName,columnName);
				return 0;
			}
		}
		return integer;
	}

	public static int getColumnLength(String tableName, String columnName) {
		String cacheKey = tableName.toUpperCase()+":"+columnName.toUpperCase();
		Integer integer = lengths.get(cacheKey);
		if(integer == null){
			reloadTableColumn(tableName);
			integer = lengths.get(cacheKey);
			if(integer == null){
				logger.error("Table[{}] column [{}] not found",tableName,columnName);
				return 0;
			}
		}
		return integer;
	}
	
	private static void reloadTableColumn(String tableName){
		Connection connection = DataSourceUtils.getConnection(ds);
		ResultSet rs = null;
		try{
			DatabaseMetaData dbmd = connection.getMetaData();
		    rs = dbmd.getColumns(null, dbmd.getUserName().toUpperCase(), tableName.toUpperCase(), null);
		    while (rs.next()) {
		         String columnName = rs.getString("COLUMN_NAME").toUpperCase();
		         int dataType = Integer.parseInt(rs.getString("DATA_TYPE"));
		         int length = Integer.parseInt(rs.getString("COLUMN_SIZE"));
		         String key = tableName.toUpperCase()+":"+columnName.toUpperCase();
		         types.put(key, dataType);
		         lengths.put(key, length);
		    }
		} catch (SQLException e) {
			logger.error("load table ["+tableName+"] column exception",e);
			throw new RuntimeException(e);
		}finally{
			JdbcUtils.closeResultSet(rs);
			DataSourceUtils.releaseConnection(connection, ds);
		}
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void afterPropertiesSet() throws Exception {
		DatabaseMetaUtils.ds = dataSource;
	}
}
