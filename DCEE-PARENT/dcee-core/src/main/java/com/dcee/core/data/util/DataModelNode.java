package com.dcee.core.data.util;

import java.util.List;

import com.dcee.core.data.model.Column;
import com.dcee.core.data.model.DataModel;

public class DataModelNode {
	
	private String id;
	
	private String select;
	
	private String eventListener;
	
	private ColumnsNode columns;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getSelect() {
		return select;
	}

	public void setSelect(String select) {
		this.select = select;
	}

	public String getEventListener() {
		return eventListener;
	}

	public void setEventListener(String eventListener) {
		this.eventListener = eventListener;
	}

	public ColumnsNode getColumns() {
		return columns;
	}

	public void setColumns(ColumnsNode columns) {
		this.columns = columns;
	}

	public DataModel toDataModel() {
		DataModel dataModel = new DataModel(id);
		dataModel.setSelectSQL(select);
		dataModel.setEventListener(eventListener);
		dataModel.setPoClass(id);
		if(columns != null){
			if(columns.getParent() != null && columns.getTable() != null){
				Object [] args = new Object[]{id,columns.getParent(),columns.getTable()};
				String msg = String.format("数据模型[%s]配置错误：不能继承父模型的同时修改表,修改配置不继承父模型[%s]或者不指定表[%s]",args);
				throw new IllegalArgumentException(msg);
			}
			dataModel.setModelTitle(columns.getModelTitle());
			dataModel.setParent(columns.getParent());
			dataModel.setTableName(columns.getTable());
			dataModel.setFactoryType(columns.getFactoryType());
			dataModel.setFactoryParam(columns.getFactoryParam());
			dataModel.setColumns(columns.getColumns());
			List<Column> pkColumns = dataModel.getPkColumns();
			if(pkColumns != null && pkColumns.size() == 1){
				Column column = pkColumns.get(0);
				dataModel.setPkProperty(column.getProperty());
				dataModel.setPkClass(DataModelUtils.getColumnClass(column.getType()));
			}
			dataModel.validate();
		}
		return dataModel;
	}
}
