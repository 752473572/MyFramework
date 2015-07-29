package com.dcee.core.data.model;

import java.io.Serializable;
import java.util.Map;

public interface DTO extends Serializable{
	
	public UpdateFlag getUpdateFlag();
	
	public void setUpdateFlag(UpdateFlag updateFlag);
	
	public String getProperty(String name);
	
	public void setProperty(String name,String value);
	
	public void putAllProperty(Map<String, String> properties);
	
	public Map<String, String> getAllProperty();
}