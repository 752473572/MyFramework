package com.dcee.core.data.util;

import java.util.List;

import com.dcee.core.data.model.Column;

public class ColumnsNode {

	private String table;

	private String modelTitle;

	private String parent;

	private String factoryType;

	private String factoryParam;

	private List<Column> columns;

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

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getParent() {
		return parent;
	}

	public String getModelTitle() {
		return modelTitle;
	}

	public void setModelTitle(String modelTitle) {
		this.modelTitle = modelTitle;
	}

}
