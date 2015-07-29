package com.dcee.core.data.model;

import java.util.HashMap;
import java.util.Map;

public class DefaultDTO implements DTO {

	private static final long serialVersionUID = 1L;
	private UpdateFlag updateFlag = UpdateFlag.Unchanged;
	
	
	private Map<String, String> properties = new HashMap<String, String>();

	public UpdateFlag getUpdateFlag() {
		return updateFlag;
	}

	public void setUpdateFlag(UpdateFlag updateFlag) {
		this.updateFlag = updateFlag;
	}

	public String getProperty(String name) {
		return properties.get(name);
	}

	public void setProperty(String name, String value) {
		properties.put(name, value);
	}

	public void putAllProperty(Map<String, String> properties) {
		this.properties.putAll(properties);
	}

	public Map<String, String> getAllProperty() {
		return properties;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
}
