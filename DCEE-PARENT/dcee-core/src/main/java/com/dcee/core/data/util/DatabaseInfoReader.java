package com.dcee.core.data.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dcee.core.data.model.Column;

public class DatabaseInfoReader {
	public static final String LINE = "_";
	protected static final String ORACLE_PRODUCTNAME = "Oracle";
	protected static final String DB2_PRODUCTNAME = "DB2";
	protected static final String COLUMN_NAME = "COLUMN_NAME";
	protected static final String COLUMN_TYPE = "DATA_TYPE";
	protected static final String COLUMN_SIZE = "COLUMN_SIZE";
	protected static final String COLUMN_REMARKS = "REMARKS";
	private static final Logger logger = LoggerFactory.getLogger(DatabaseInfoReader.class);

	private DataSource dataSource;

	public List<String> getTablesList() {
		return getTablesList(dataSource);
	}

	public List<String> getTablesList(DataSource otherDataSource) {
		return getTablesList(otherDataSource, new String[] { "TABLE" });
	}

	public List<String> getTablesList(DataSource otherDataSource, String[] tableType) {
		List<String> tableList = new ArrayList<String>();
		ResultSet tablesRs = null;
		Connection connection = null;
		try {
			connection = otherDataSource.getConnection();
			DatabaseMetaData metaData = connection.getMetaData();
			String schema = getSchema(metaData);
			tablesRs = metaData.getTables(null, schema, null, tableType);
			while (tablesRs.next()) {
				tableList.add(tablesRs.getString("TABLE_NAME"));
			}
		} catch (SQLException e) {
			logger.error("读取所有表信息失败!");
			throw new RuntimeException("读取所有表信息失败!", e);
		} finally {
			closeRSAndConnection(tablesRs, connection);
		}
		return tableList;
	}

	protected void closeRSAndConnection(ResultSet rs, Connection connection) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	protected String getSchema(DatabaseMetaData dmd) throws SQLException {
		String dbProductName = dmd.getDatabaseProductName();
		if ((dbProductName.startsWith("Oracle")) || (dbProductName.startsWith("DB2"))) {
			return dmd.getUserName().toUpperCase();
		}
		return null;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public List<Column> getTableInfo(String tableName) {
		return getTableInfo(tableName, dataSource);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Column> getTableInfo(String tableName, DataSource otherDataSource) {
		DatabaseMetaData metaData = null;
		Connection connection;
		try {
			connection = otherDataSource.getConnection();
			metaData = connection.getMetaData();
		} catch (SQLException e) {
			logger.error("初始化metaData失败!");
			throw new RuntimeException("初始化metaData失败!", e);
		}

		ResultSet tables = null;
		try {
			tables = metaData.getTables(null, getSchema(metaData), tableName.toUpperCase(), new String[] { "TABLE" });

			if (tables.next()) {
				tableName = tableName.toUpperCase();
			}
		} catch (SQLException e) {
			logger.error("读取{}表名失败!", tableName);
			throw new RuntimeException("读取" + tableName + "表名失败!", e);
		} finally {
			closeRSAndConnection(tables, null);
		}

		List<String> keys = new ArrayList<String>();
		ResultSet primaryKeys = null;
		try {
			primaryKeys = metaData.getPrimaryKeys(null, getSchema(metaData), tableName);

			while (primaryKeys.next()) {
				String columnName = primaryKeys.getString("COLUMN_NAME");
				keys.add(columnName.toLowerCase());
			}
		} catch (SQLException e) {
			logger.error("读取{}主键失败!", tableName);
			throw new RuntimeException("读取" + tableName + "主键失败!", e);
		} finally {
			closeRSAndConnection(primaryKeys, null);
		}

		ResultSet columns = null;
		Object columnsInfos = new ArrayList<String>();
		try {
			columns = metaData.getColumns(null, getSchema(metaData), tableName, "%");

			while (columns.next()) {
				Column columnsInfo = new Column();

				String columnName = columns.getString("COLUMN_NAME").toLowerCase();

				columnsInfo.setName(columnName);
				String property = getColumnToAlias(columnName);
				columnsInfo.setProperty(property);

				int dataType = columns.getInt("DATA_TYPE");
				columnsInfo.setType(dataType);

				int columnSize = columns.getInt("COLUMN_SIZE");
				columnsInfo.setLength(columnSize);

				String remarks = columns.getString("REMARKS");
				if (remarks != null) {
					remarks = remarks.replaceAll("\n", "").replaceAll("\r", "");
				} else {
					remarks = property;
				}
				columnsInfo.setTitle(remarks);

				if (keys.contains(columnName)) {
					columnsInfo.setPrimaryKey(true);
				}
				((List) columnsInfos).add(columnsInfo);
			}
		} catch (SQLException e) {
			logger.error("读取{}列信息失败!", tableName);
			throw new RuntimeException("读取" + tableName + "列信息失败!", e);
		} finally {
			closeRSAndConnection(columns, connection);
		}

		return (List<Column>) columnsInfos;
	}

	public static final String getColumnToAlias(String column) {
		String[] sub = StringUtils.split(StringUtils.lowerCase(column), "_");
		if (sub.length == 1) {
			return column;
		}
		StringBuilder builder = new StringBuilder(50);
		int i = 0;
		for (int l = sub.length; i < l; i++) {
			builder.append(StringUtils.capitalize(sub[i]));
		}
		return StringUtils.uncapitalize(builder.toString());
	}
}
