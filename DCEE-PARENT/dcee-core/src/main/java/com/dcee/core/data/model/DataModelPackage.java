package com.dcee.core.data.model;

/**
 * <pre>
 * 静态数据模型包配置
 *&lt;bean class="com.todaytech.pwp.core.data.model.DataModelPackage"&gt;
 * 	&lt;property name="id" value="acl"/&gt;
 * 	&lt;property name="packageName" value="com.todaytech.acl.model"/&gt;
 *&lt;/bean&gt;
 * </pre>
 */
public class DataModelPackage {
	
	private String id;
	
	private String packageName;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPackageName() {
		return packageName;
	}
	
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
}
