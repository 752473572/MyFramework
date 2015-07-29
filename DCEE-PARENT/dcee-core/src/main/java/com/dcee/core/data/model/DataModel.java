package com.dcee.core.data.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.dcee.core.data.util.DataModelUtils;
import com.dcee.core.data.util.DatabaseMetaUtils;

public class DataModel implements Cloneable {

	public static final String SUFFIX = ".datamodel";

	public static final String EXTENSION = "datamodel";

	/**
	 * 模型Id
	 */
	private String id;

	/**
	 * 表名
	 */
	private String tableName;

	/**
	 * PO
	 */
	private String poClass;

	/**
	 * 是否动态模型
	 */
	private boolean dynamic;

	/**
	 * 普通字段
	 */
	private List<Column> columns = new ArrayList<Column>();

	/**
	 * pk字段
	 */
	private List<Column> pkColumns;

	/**
	 * 查询SQL
	 */
	private String selectSQL;

	/**
	 * 事件监听器
	 */
	private String eventListener;

	/**
	 * 主键属性名
	 */
	private String pkProperty;

	/**
	 * 主键属性类型
	 */
	private Class<?> pkClass;

	/**
	 * 父模型
	 */
	private String parent;

	/**
	 * 自定义表单格式化工厂类型集合
	 */
	private List<String> fmtTypes;

	/**
	 * 工厂类型
	 */
	private String factoryType;

	/**
	 * 工厂参数
	 */
	private String factoryParam;

	/**
	 * 模型中文名
	 */
	private String modelTitle;

	public String getFactoryType() {
		return factoryType;
	}

	public void setFactoryType(String factoryType) {
		this.factoryType = factoryType;
	}

	public String getFactoryParam() {
		return factoryParam;
	}

	public void setFactoryParam(String factoryParam) {
		this.factoryParam = factoryParam;
	}

	public List<String> getFmtTypes() {
		return fmtTypes;
	}

	public void setFmtTypes(List<String> fmtTypes) {
		this.fmtTypes = fmtTypes;
	}

	public DataModel(String id) {
		this.id = id;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getPoClass() {
		return poClass;
	}

	public void setPoClass(String poClass) {
		this.poClass = poClass;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		if (columns != null) {
			this.columns = columns;
		}
	}

	public List<Column> getPkColumns() {
		if (this.pkColumns == null || this.pkColumns.isEmpty()) {
			List<Column> pkColumns = new ArrayList<Column>();
			for (int i = 0, size = columns.size(); i < size; i++) {
				Column column = columns.get(i);
				if (column.isPrimaryKey()) {
					pkColumns.add(column);
				}
			}
			this.pkColumns = pkColumns;
		}
		return this.pkColumns;
	}

	public Column findColumn(String name) {
		for (Iterator<Column> iter = columns.iterator(); iter.hasNext();) {
			Column column = (Column) iter.next();
			if (column.getName().equalsIgnoreCase(name)) {
				return column;
			}
		}

		return null;
	}

	public void setPkColumns(List<Column> pkColumns) {
		this.pkColumns = pkColumns;
	}

	public boolean isDynamic() {
		return dynamic;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	public String getSelectSQL() {
		return selectSQL;
	}

	public void setSelectSQL(String selectSQL) {
		this.selectSQL = selectSQL;
	}

	public void setEventListener(String eventListener) {
		this.eventListener = eventListener;
	}

	public String getEventListener() {
		return eventListener;
	}

	public String getPkProperty() {
		return pkProperty;
	}

	public void setPkProperty(String pkProperty) {
		this.pkProperty = pkProperty;
	}

	public Class<?> getPkClass() {
		return pkClass;
	}

	public void setPkClass(Class<?> pkClass) {
		this.pkClass = pkClass;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getModelTitle() {
		return modelTitle;
	}

	public void setModelTitle(String modelTitle) {
		this.modelTitle = modelTitle;
	}

	/**
	 * 继承数据模型
	 * @param dataModel
	 */
	public void extendsFrom(DataModel dataModel) {
		if (dataModel == null) {
			return;
		}
		extendsProperty(dataModel);
		List<Column> superColumns = dataModel.getColumns();
		if (columns == null) {
			columns = new ArrayList<Column>();
		}
		if (superColumns != null) {
			Set<String> columnNames = new HashSet<String>();
			for (Column column : columns) {
				columnNames.add(column.getName());
			}
			for (Column column : superColumns) {
				if (!columnNames.contains(column.getName())) {
					Column clone = (Column) column.clone();
					clone.setExtendFlag(true);
					columns.add(clone);
				}
			}
		}

		getPkColumns();
		if (pkColumns == null || pkColumns.isEmpty()) {
			pkColumns = dataModel.getPkColumns();
			pkProperty = dataModel.getPkProperty();
			pkClass = dataModel.getPkClass();
		}
	}

	/**
	 * 继承简单属性，不继承列配置信息
	 * @param dataModel
	 */
	public void extendsProperty(DataModel dataModel) {
		if (dataModel == null) {
			return;
		}

		if (!StringUtils.hasText(tableName)) {
			setTableName(dataModel.getTableName());
		}
		
		if (!StringUtils.hasText(poClass)) {
			setPoClass(dataModel.getPoClass());
		}

		if (!StringUtils.hasText(selectSQL)) {
			setSelectSQL(dataModel.getSelectSQL());
		}

		if (!StringUtils.hasText(eventListener)) {
			setEventListener(dataModel.getEventListener());
		}
		if (!StringUtils.hasText(factoryType)) {
			setEventListener(dataModel.getFactoryType());
		}
		if (!StringUtils.hasText(factoryParam)) {
			setEventListener(dataModel.getFactoryParam());
		}
		
		if (pkColumns == null || pkColumns.isEmpty()) {
			pkColumns = dataModel.getPkColumns();
			pkProperty = dataModel.getPkProperty();
			pkClass = dataModel.getPkClass();
		}
	}

	/**
	 * 同步修改列类型
	 * 
	 * 动态数据模型允许不配置列类型
	 */
	public void synchronizedColumnType() {
		if (tableName == null) {
			return;
		}
		if (columns != null) {
			for (int i = 0, size = columns.size(); i < size; i++) {
				Column column = columns.get(i);
				if (column.getType() == 0) {
					int columnType = DatabaseMetaUtils.getColumnType(tableName,
							column.getName());
					column.setType(columnType);
				}

				if (column.getLength() == 0) {
					column.setLength(DatabaseMetaUtils.getColumnLength(
							tableName, column.getName()));
				}
			}
		}

		List<Column> pkColumns = getPkColumns();
		if (pkColumns != null && pkColumns.size() == 1) {
			Column column = pkColumns.get(0);
			setPkProperty(column.getProperty());
			setPkClass(DataModelUtils.getColumnClass(column.getType()));
		}
	}

	/**
	 * 检验数据模型
	 * @param dataModel
	 * @return
	 */
	public boolean validate() {
		if (tableName != null) {
			if (parent == null && getPkColumns().size() == 0) {
				throw new RuntimeException("数据模型-" + id + "没有配置主键");
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((tableName == null) ? 0 : tableName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataModel other = (DataModel) obj;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataModel [tableName=");
		builder.append(tableName);
		builder.append(", columns=");
		builder.append(columns);
		builder.append("]");
		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	public Object clone() {
		try {
			DataModel dataModel = (DataModel) super.clone();
			dataModel.setColumns((List<Column>) ((ArrayList<Column>) columns)
					.clone());
			return dataModel;
		} catch (CloneNotSupportedException e) {
			// this shouldn't happen, since we are Cloneable
			throw new InternalError();
		}
	}
}
