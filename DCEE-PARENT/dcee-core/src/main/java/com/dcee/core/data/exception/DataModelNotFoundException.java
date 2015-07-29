package com.dcee.core.data.exception;

public class DataModelNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String modelId;
	
	public DataModelNotFoundException(String modelId){
		this.modelId = modelId;
	}
	
	public String getMessage() {
		return String.format("数据模型[%s]不存在", modelId);
	}
}
