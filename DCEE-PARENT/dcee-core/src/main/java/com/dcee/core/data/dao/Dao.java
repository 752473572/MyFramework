package com.dcee.core.data.dao;

import java.io.Serializable;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import com.dcee.core.data.model.DTO;

public interface Dao {
	
	/**
	 * 根据id查询数据
	 * @param modelId
	 * @param pk
	 * @return 如果记录不存在，返回null
	 */
	public <T extends DTO> T findById(String modelId,Serializable pk) throws DataAccessException;
	
	/**
	 * 根据id查询数据
	 * @param modelId
	 * @param pk
	 * @return 如果记录不存在，返回null
	 */
	public <T extends DTO> T findById(Class<T> theClass,Serializable pk) throws DataAccessException;
	
	/**
	 * 根据条件查询数据
	 * @param modelId
	 * @param where
	 * @param conditions
	 * @throws 
	 * @return 没有满足条件记录返回null，多条记录情况抛异常
	 */
	public <T extends DTO> T find(String modelId,String where,List<Object> conditions) throws DataAccessException;
	
	/**
	 * 根据条件查询数据
	 * @param modelId
	 * @param where
	 * @param conditions
	 * @throws 
	 * @return 没有满足条件记录返回null，多条记录情况抛异常
	 */
	public <T extends DTO> T find(String modelId, String where, Object ...conditions);
	/**
	 * 查询所有数据
	 * @param modelId
	 * @return
	 */
	public <T> List<T> findAll(String modelId);
	
	/**
	 * 根据条件查询数据集合
	 * @param modelId
	 * @param where
	 * @param conditions
	 * @return
	 */
	public <T> List<T> findAll(String modelId,String where,List<Object> conditions);
	/**
	 * 根据条件查询数据集合
	 * @param modelId
	 * @param where
	 * @param conditions
	 * @return
	 */
	public <T> List<T> findAll(String modelId,String where,Object ...conditions);
	
	/**
	 * 计算数据总条数
	 * @param modelId
	 * @return
	 */
	public int count(String modelId);
	
	/**
	 * 根据条件计算总条数
	 * @param modelId
	 * @param where
	 * @param conditions
	 * @return
	 */
	public int count(String modelId,String where,List<Object> conditions);
	
	/**
	 * 查询数据并回调处理记录
	 * @param modelId
	 * @param rch
	 */
	public void query(String modelId,RowCallbackHandler rch);
	
	/**
	 * 根据条件查询数据并回调处理记录
	 * @param modelId
	 * @param rch
	 */
	public void query(String modelId,String where,List<Object> conditions,RowCallbackHandler rch);
	
	/**
	 * 查询数据并回调处理记录
	 * @param modelId
	 * @param rch
	 */
	public <T extends DTO> void query(String modelId,BeanCallbackHandler<T> rch);
	
	/**
	 * 查询数据并回调处理记录
	 * @param modelId
	 * @param where
	 * @param conditions
	 * @param rch
	 */
	public <T extends DTO> void query(String modelId,String where,List<Object> conditions,BeanCallbackHandler<T> rch);
	
	
	/**
	 * 记录是否已经存在
	 * @param modelId
	 * @param pk
	 * @return
	 */
	public boolean isExists(String modelId,Serializable pk);
	
	/**
	 * 新增数据
	 * @param object
	 * @return
	 */
	public <T extends DTO> T insert(T object);
	
	/**
	 * 新增数据（忽略null字段）
	 * @param object
	 * @return
	 */
	public <T extends DTO> T insertSelective(T object);
	
	
	/**
	 * 删除数据
	 * @param modelId
	 * @param pk
	 */
	public int removeById(String modelId,Serializable pk);
	
	/**
	 * 批量删除删除数据
	 * @param modelId
	 * @param pk
	 */
	public int removeByIds(String modelId,Serializable ...pks);
	
	/**
	 * 批量删除数据
	 * @param modelId
	 * @param pk
	 */
	public int removeByIds(String modelId,List<? extends Serializable> pks);
	
	/**
	 * 删除数据
	 * @param object
	 * @return
	 */
	public int remove(Object object);
	
	/**
	 * 根据条件删除数据
	 * @param where 如name=?
	 * @param conditions 如"张三"
	 * @return
	 */
	public int remove(String modelId,String where,List<Object> conditions);
	
	/**
	 * 根据条件删除数据
	 * @param where 如name=?
	 * @param conditions 如"张三"
	 * @return
	 */
	public int remove(String modelId,String where,Object ...conditions);
	
	/**
	 * 批量删除数据
	 * @param objects
	 * @return
	 */
	public <T extends DTO> int remove(List<T> objects);
	
	/**
	 * 更新数据
	 * @param object
	 * @return
	 */
	public int update(Object object);
	
	/**
	 * 更新数据（忽略null）
	 * @param object
	 * @return
	 */
	public int updateSelective(Object object);
	
	/**
	 * 处理主从表数据
	 * @param dto
	 * @return
	 */
	public DTO handle(DTO dto);
	
	/**
	 * 处理主从表数据
	 * @param dto
	 * @return
	 */
	public DTO handleSelective(DTO dto);
	
	/**
	 * 处理主从表数据
	 * @param dto
	 * @return
	 */
	public <T extends DTO> List<T> handle(List<T> dto);
	
	/**
	 * 处理主从表数据
	 * @param dto
	 * @return
	 */
	public <T extends DTO> List<T> handleSelective(List<T> dto);
	
	/**
	 * 处理主从表数据
	 * @param dto
	 * @return
	 */
	public <T extends DTO> T handle(T dto,DaoEventListener<T> listener);
	
	/**
	 * 处理主从表数据
	 * @param dto
	 * @return
	 */
	public <T extends DTO> T handleSelective(T dto,DaoEventListener<T> listener);
	
	/**
	 * 处理主从表数据
	 * @param dto
	 * @return
	 */
	public <T extends DTO> List<T> handle(List<T> dto,DaoEventListener<T> listener);
	
	/**
	 * 处理主从表数据
	 * @param dto
	 * @return
	 */
	public <T extends DTO> List<T> handleSelective(List<T> dto,DaoEventListener<T> listener);

    /**
     * 
    * @Title: getRowMapper 
    * @Description:获取行映射
    * @param dto
    * @return    设定文件 
    * @throws
     */
	public   <T extends DTO>  RowMapper<T> getRowMapper(String modelId);
	
	/**
	 * 获取JdbcTemplate
	 * @return
	 */
    public JdbcTemplate getJdbcTemplate();
    
    
    /**
     * <pre>
     * 加载子表数据至dto.details
     * 
     * Order order = dao.findById("com.demo.dto.Order",1);
     * List&lt;OrderDetails&gt; orderDetails = dao.loadDetails(order,"com.demo.dto.OrderDetails","order_id");
     * </pre>
     * @param dto 主表数据
     * @param modelId 从表模型id
     * @param foreignColumn 从表中对应主表的外键字段
     * @return
     */
    public <T extends DTO> List<T> loadDetails(DTO dto,String modelId,String foreignColumn);
    
    /**
     * <pre>
     * 加载子表数据至dto.details
     * 
     * Order order = dao.findById("com.demo.dto.Order",1);
     * List&lt;OrderDetails&gt; orderDetails = dao.loadDetails(order,OrderDetails.class,"order_id");
     * </pre>
     * 
     * @param dto 主表数据
     * @param clazz 从表模型
     * @param foreignColumn 从表中对应主表的外键字段
     * @return
     */
    public <T extends DTO> List<T> loadDetails(DTO dto,Class<T> clazz,String foreignColumn);
    
    
    /**
     * <pre>
     * 级联删除树形结构数据
     * 
     * dao.deleteTreeDetails("com.demo.dto.Org","p_id","1");
     * </pre>
     * @param modelId
     * @param column
     * @param parentId
     * @return
     */
    public boolean deleteTreeDetails(String modelId,String column,Serializable parentId);
    
    /**
     * <pre>
     * 级联删除树形结构数据
     * 
     * dao.deleteTreeDetails(com.demo.dto.Org.class,"p_id","1");
     * </pre>
     * @param clazz
     * @param column
     * @param parentId
     * @return
     */
    public <T extends DTO> boolean deleteTreeDetails(Class<T> clazz,String column,Serializable parentId);
}