package com.dcee.core.data.model;

import com.dcee.core.data.util.DatabaseInfoReader;

public class Column implements Cloneable{

	/**
	 * 列名
	 */
	private String name;

	/**
	 * 实体属性名
	 */
	private String property;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 是否可空
	 */
	private boolean nullable;

	/**
	 * 数据类型
	 */
	private int type;

	/**
	 * 长度
	 */
	private int length;

	/**
	 * 小数点位数
	 */
	private int precision;

	/**
	 * 是否主键
	 */
	private boolean primaryKey;

	/**
	 * 是否使用序列号
	 */
	private boolean sequence;
	
	/**
	 * 继承标志
	 */
	private boolean extendFlag;
	
	/**
	 * 格式化groovy方法地址
	 */
	private String fmtValue;
	/**
	 * 格式化类型
	 */
	private String fmtType;
	
	/**
	 * 
	 * 是否是自定义列
	 */
	private boolean customFlag;
	
	
	
	public boolean isCustomFlag() {
		return customFlag;
	}

	public void setCustomFlag(boolean customFlag) {
		this.customFlag = customFlag;
	}

	public String getFmtValue() {
		return fmtValue;
	}

	public void setFmtValue(String fmtValue) {
		this.fmtValue = fmtValue;
	}

	public String getFmtType() {
		return fmtType;
	}

	public void setFmtType(String fmtType) {
		this.fmtType = fmtType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public void setSequence(boolean sequence) {
		this.sequence = sequence;
	}

	public boolean isSequence() {
		return sequence;
	}

	public String getProperty() {
		if (property == null && name != null) {
			property = DatabaseInfoReader.getColumnToAlias(name);
		}
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}
	
	public void setExtendFlag(boolean extendFlag) {
		this.extendFlag = extendFlag;
	}
	
	public boolean isExtendFlag() {
		return extendFlag;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Column other = (Column) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("Column [name=%s, title=%s]", name, title);
	}
	
	@Override
	public Object clone(){
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
}
