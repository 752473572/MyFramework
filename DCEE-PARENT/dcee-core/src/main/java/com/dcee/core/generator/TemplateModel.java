package com.dcee.core.generator;

import java.util.ArrayList;
import java.util.List;

import com.dcee.core.data.model.Column;
import com.dcee.core.data.model.DataModel;

public class TemplateModel {
	private DataModel dataModel;
	private String packageDeclaration;
	private boolean isSinglePK;
	private boolean isMultiplePK;
	private boolean usedSequence;
	private String className;
	private TemplateColumnModel pk;
	private List<TemplateColumnModel> pks;
	private List<TemplateColumnModel> columns = new ArrayList<TemplateColumnModel>();
	private List<TemplateColumnModel> allColumns = new ArrayList<TemplateColumnModel>();

	public List<TemplateColumnModel> getPks() {
		return this.pks;
	}

	public void setPks(List<TemplateColumnModel> pks) {
		this.pks = pks;
	}

	public String getPackageDeclaration() {
		return this.packageDeclaration;
	}

	public void setPackageDeclaration(String packageDeclaration) {
		this.packageDeclaration = packageDeclaration;
	}

	public boolean getIsSinglePK() {
		return this.isSinglePK;
	}

	public void setSinglePK(boolean isSinglePK) {
		this.isSinglePK = isSinglePK;
	}

	public boolean getIsMultiplePK() {
		return this.isMultiplePK;
	}

	public void setMultiplePK(boolean isMultiplePK) {
		this.isMultiplePK = isMultiplePK;
	}

	public boolean isUsedSequence() {
		return this.usedSequence;
	}

	public void setUsedSequence(boolean usedSequence) {
		this.usedSequence = usedSequence;
	}

	public String getTableName() {
		return this.dataModel.getTableName();
	}

	public String getParent() {
		return this.dataModel.getParent();
	}

	public String getClassName() {
		return this.className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public TemplateColumnModel getPk() {
		return this.pk;
	}

	public void setPk(TemplateColumnModel pk) {
		this.pk = pk;
	}

	public List<TemplateColumnModel> getColumns() {
		return this.columns;
	}

	public void setColumns(List<TemplateColumnModel> columns) {
		this.columns = columns;
	}

	public String getPoClass() {
		return this.dataModel.getPoClass();
	}

	public String getSelectSQL() {
		return this.dataModel.getSelectSQL();
	}

	public List<TemplateColumnModel> getAllColumns() {
		return this.allColumns;
	}

	public static TemplateModel createByDataModel(DataModel dataModel) {
		TemplateModel templateModel = new TemplateModel();

		templateModel.dataModel = dataModel;

		String poClass = dataModel.getPoClass();
		int lastIndex = poClass.lastIndexOf(".");
		if (lastIndex != -1) {
			templateModel.packageDeclaration = poClass.substring(0, lastIndex);
		}
		templateModel.className = poClass.substring(lastIndex + 1);

		List<Column> pkColumns = dataModel.getPkColumns();
		templateModel.pks = TemplateColumnModel.createPKByColumnS(pkColumns);
		templateModel.isSinglePK = (pkColumns.size() == 1);
		templateModel.isMultiplePK = (pkColumns.size() > 1);
		if (pkColumns.size() == 1) {
			templateModel.isSinglePK = true;
			Column pkColumn = (Column) pkColumns.get(0);
			templateModel.usedSequence = pkColumn.isSequence();
			templateModel.pk = TemplateColumnModel.createByColumn(pkColumn);
		} else if (pkColumns.size() > 1) {
			templateModel.isMultiplePK = true;
			TemplateColumnModel pkColumn = new TemplateColumnModel();
			pkColumn.setJavaType(templateModel.className + "PK");
			pkColumn.setJavaFieldName("id");
			pkColumn.setTitle("ID");
			templateModel.pk = pkColumn;
		}
		List<Column> dataModelcolumns = dataModel.getColumns();
		for (Column column : dataModelcolumns) {
			if (!column.isPrimaryKey()) {
				templateModel.columns.add(TemplateColumnModel.createByColumn(column));
			}
		}
		templateModel.allColumns.addAll(templateModel.pks);
		templateModel.allColumns.addAll(templateModel.columns);

		return templateModel;
	}
}
