package com.dcee.core.data.dao;

import com.dcee.core.data.model.DTO;

public interface DaoEventListener<T extends DTO> {
	
	/**
	 * 调用insert、insertSelective之前
	 * @param object
	 * @return
	 */
	public T beforeInsert(T object,boolean selective);
	
	/**
	 * 调用insert、insertSelective之后
	 * @param object
	 * @return
	 */
	public T afterInsert(T object,boolean selective);
	
	/**
	 * 调用update、updateSelective之前
	 * @param object
	 * @return
	 */
	public T beforeUpdate(T object,boolean selective);
	
	/**
	 * 调用update、updateSelective之后
	 * @param object
	 * @return
	 */
	public T afterUpdate(T object,boolean selective);
	
	/**
	 * 调用delete之前
	 * @param object
	 * @return
	 */
	public boolean beforeDelete(T object);
	
	/**
	 * 调用delete之后调用
	 * @param pk
	 */
	public void afterDelete(T object);
	
	/**
	 * 调用selectById、select、selectList之后调用
	 * @param object
	 * @return
	 */
	public T afterSelect(T object);
	
	/**
	 * 调用query方法之后
	 * @param object
	 * @return
	 */
	public T afterQuery(T object);
}
