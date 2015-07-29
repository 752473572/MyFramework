package com.dcee.core.generator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.dcee.core.data.model.Column;
import com.dcee.core.util.beanutils.ReflectUtil;

public class TemplateColumnModel extends Column {
	
	private String javaType;
	private String jdbcType;

	public String getJdbcType() {
		return jdbcType;
	}

	public void setJdbcType(String jdbcType) {
		this.jdbcType = jdbcType;
	}

	public String getJavaFieldName() {
		return getProperty();
	}

	public void setJavaFieldName(String javaFieldName) {
		setProperty(javaFieldName);
	}

	public String getJavaType() {
		return javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	public static TemplateColumnModel createByColumn(Column column) {
		TemplateColumnModel model = new TemplateColumnModel();
		ReflectUtil.copyNotNullProperties(column, model);
		model.setProperty(getColumnToAlias(column.getName()));
		return model;
	}

	public static final String getColumnToAlias(String column) {
		String lowerCase = StringUtils.lowerCase(column);
		String[] sub = StringUtils.split(lowerCase, "_");
		if (sub.length == 1) {
			return lowerCase;
		}
		StringBuilder builder = new StringBuilder(50);
		int i = 0;
		for (int l = sub.length; i < l; i++) {
			builder.append(StringUtils.capitalize(sub[i]));
		}
		return StringUtils.uncapitalize(builder.toString());
	}

	public static List<TemplateColumnModel> createPKByColumnS(List<Column> columns) {
		List<TemplateColumnModel> pks = new ArrayList<TemplateColumnModel>();

		for (Column column : columns) {
			TemplateColumnModel model = createByColumn(column);
			pks.add(model);
		}

		return pks;
	}

	public boolean getIsPrimaryKey() {
		return isPrimaryKey();
	}
}
