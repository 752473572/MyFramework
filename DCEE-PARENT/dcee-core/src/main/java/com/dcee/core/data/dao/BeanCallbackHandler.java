package com.dcee.core.data.dao;
public interface BeanCallbackHandler<T> {
	
	public void processRow(T row);
}