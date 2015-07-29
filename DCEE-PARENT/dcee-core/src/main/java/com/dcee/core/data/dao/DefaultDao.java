package com.dcee.core.data.dao;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.dcee.core.data.exception.DataModelNotFoundException;
import com.dcee.core.data.model.DTO;
import com.dcee.core.data.model.DataModel;
import com.dcee.core.data.model.UpdateFlag;
import com.dcee.core.data.util.DataModelUtils;

public class DefaultDao implements Dao, InitializingBean {

	private static class CacheValue {

		Configuration configuration;

		SqlSessionTemplate template;

		boolean isChange(Configuration configuration) {
			if (configuration != null) {
				return this.configuration == null || !this.configuration.equals(configuration);
			}
			return false;
		}
	}

	@Resource
	private MybatisConfigManager configManager;

	@Resource
	private DaoModelManager daoModelManager;

	@Resource
	private DataSource dataSource;

	private Map<String, CacheValue> caches = new ConcurrentHashMap<String, CacheValue>();

	private JdbcTemplate jdbcTemplate;

	private SqlSessionTemplate getTemplate(String modelId) {
		Configuration configuration;
		try {
			configuration = configManager.getConfiguration(modelId);
		} catch (DataModelNotFoundException e) {
			throw new RuntimeException(e);
		}
		// 缓存
		CacheValue cacheValue = caches.get(modelId);
		if (cacheValue != null) {
			if (!cacheValue.isChange(configuration)) {
				return cacheValue.template;
			}
		} else {
			cacheValue = new CacheValue();
			caches.put(modelId, cacheValue);
		}

		SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
		String environment = SqlSessionFactoryBean.class.getSimpleName();
		Environment en = new Environment(environment, new SpringManagedTransactionFactory(), dataSource);
		configuration.setEnvironment(en);
		SqlSessionFactory sqlSessionFactory = builder.build(configuration);
		SqlSessionTemplate template = new SqlSessionTemplate(sqlSessionFactory);
		cacheValue.configuration = configuration;
		cacheValue.template = template;
		return template;
	}

	private DataModel findDataModel(String modelId) {
		try {
			return daoModelManager.findDataModel(modelId);
		} catch (DataModelNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private DataModel resolveDataModel(String modelId) {
		try {
			return daoModelManager.resolveDataModel(modelId);
		} catch (DataModelNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends DTO> T findById(String modelId, Serializable pk) {
		T result = (T) getTemplate(modelId).selectOne(modelId + "Mapper.findById", pk);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends DTO> T findById(Class<T> theClass, Serializable pk) {
		return (T) findById(theClass.getName(), pk);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends DTO> T find(String modelId, String where, List<Object> conditions) throws DataAccessException {
		Map<String, Object> parameters = buildCondition(where, conditions);
		return (T) getTemplate(modelId).selectOne(modelId + "Mapper.findByCondition", parameters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends DTO> T find(String modelId, String where, Object... conditions) throws DataAccessException {
		Map<String, Object> parameters = buildCondition(where, Arrays.asList(conditions));
		return (T) getTemplate(modelId).selectOne(modelId + "Mapper.findByCondition", parameters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> findAll(String modelId) {
		return (List<T>) getTemplate(modelId).selectList(modelId + "Mapper.findAll");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> findAll(String modelId, String where, List<Object> conditions) {
		Map<String, Object> parameters = buildCondition(where, conditions);
		return (List<T>) getTemplate(modelId).selectList(modelId + "Mapper.findAllByCondition", parameters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> findAll(String modelId, String where, Object... conditions) {
		Map<String, Object> parameters = buildCondition(where, Arrays.asList(conditions));
		return (List<T>) getTemplate(modelId).selectList(modelId + "Mapper.findAllByCondition", parameters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int count(String modelId) {
		return (Integer) getTemplate(modelId).selectOne(modelId + "Mapper.count");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int count(String modelId, String where, List<Object> conditions) {
		Map<String, Object> parameters = buildCondition(where, conditions);
		return (Integer) getTemplate(modelId).selectOne(modelId + "Mapper.countByCondition", parameters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void query(String modelId, RowCallbackHandler rch) {
		DataModel dataModel = findDataModel(modelId);
		String sql = dataModel.getSelectSQL();
		if (!StringUtils.hasText(sql)) {
			sql = String.format("select * from %s", dataModel.getTableName());
		}
		jdbcTemplate.query(sql, rch);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void query(String modelId, String where, List<Object> conditions, RowCallbackHandler rch) {
		DataModel dataModel = findDataModel(modelId);
		String sql = null;
		String selectSQL = dataModel.getSelectSQL();
		if (StringUtils.hasText(selectSQL)) {
			if (selectSQL.toLowerCase().indexOf("where") == -1) {
				sql = String.format("%s where %s", selectSQL, where);
			} else {
				sql = String.format("%s and %s", selectSQL, where);
			}
		} else {
			sql = String.format("select * from %s where %s", dataModel.getTableName(), where);
		}

		Object[] args = null;
		if (conditions == null) {
			args = new Object[0];
		} else {
			args = conditions.toArray();
		}
		jdbcTemplate.query(sql, args, rch);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends DTO> void query(String modelId, final BeanCallbackHandler<T> rch) {
		Assert.notNull(rch, "Argument BeanCallbackHandler must not be null");
		getTemplate(modelId).select(modelId + "Mapper.findAll", new ResultHandler() {

			@SuppressWarnings("unchecked")
			public void handleResult(ResultContext context) {
				rch.processRow((T) context.getResultObject());
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends DTO> void query(String modelId, String where, List<Object> conditions,
			final BeanCallbackHandler<T> rch) {
		Assert.notNull(rch, "Argument BeanCallbackHandler must not be null");
		Map<String, Object> parameters = buildCondition(where, conditions);
		getTemplate(modelId).select(modelId + "Mapper.findAllByCondition", parameters, new ResultHandler() {

			@SuppressWarnings("unchecked")
			public void handleResult(ResultContext context) {
				rch.processRow((T) context.getResultObject());
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isExists(String modelId, Serializable pk) {
		int num = (Integer) getTemplate(modelId).selectOne(modelId + "Mapper.isExists", pk);
		return num > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends DTO> T insert(T object) {
		String modelId = object.getClass().getName();
		DataModel dataModel = resolveDataModel(modelId);
		setPrimaryKey(dataModel, object);
		getTemplate(modelId).insert(modelId + "Mapper.insert", object);
		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends DTO> T insertSelective(T object) {
		String modelId = object.getClass().getName();
		DataModel dataModel = resolveDataModel(modelId);
		setPrimaryKey(dataModel, object);
		getTemplate(modelId).insert(modelId + "Mapper.insertSelective", object);
		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int removeById(String modelId, Serializable pk) {
		return getTemplate(modelId).delete(modelId + "Mapper.removeById", pk);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int removeByIds(String modelId, List<? extends Serializable> pks) {
		if (pks == null) {
			return 0;
		}

		int count = 0;
		for (Serializable pk : pks) {
			count += removeById(modelId, pk);
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int removeByIds(String modelId, Serializable... pks) {
		if (pks == null) {
			return 0;
		}

		int count = 0;
		for (Serializable pk : pks) {
			count += removeById(modelId, pk);
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int remove(Object object) {
		String modelId = object.getClass().getName();
		Serializable primaryKey = getPrimaryKey(object);
		Assert.notNull(primaryKey, "dao remove object[" + modelId + "] fail,primary key must not be null");
		return getTemplate(modelId).delete(modelId + "Mapper.remove", object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int remove(String modelId, String where, List<Object> conditions) {
		DataModel dataModel = findDataModel(modelId);
		String sql = String.format("delete from %s where %s", dataModel.getTableName(), where);
		Object[] args = null;
		if (conditions == null) {
			args = new Object[0];
		} else {
			args = conditions.toArray();
		}
		return jdbcTemplate.update(sql, args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int remove(String modelId, String where, Object... conditions) {
		return remove(modelId, where, Arrays.asList(conditions));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends DTO> int remove(List<T> objects) {
		if (objects == null) {
			return 0;
		}

		int count = 0;
		for (T t : objects) {
			count += remove(t);
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int update(Object object) {
		String modelId = object.getClass().getName();
		Serializable primaryKey = getPrimaryKey(object);
		Assert.notNull(primaryKey, "dao update object[" + modelId + "] fail,primary key must not be null");
		return getTemplate(modelId).update(modelId + "Mapper.update", object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int updateSelective(Object object) {
		String modelId = object.getClass().getName();
		Serializable primaryKey = getPrimaryKey(object);
		Assert.notNull(primaryKey, "dao updateSelective object[" + modelId + "] fail,primary key must not be null");
		return getTemplate(modelId).update(modelId + "Mapper.updateSelective", object);
	}

	private <T extends DTO> T handle(T dto, boolean selective, DaoEventListener<T> listener) {
		Assert.notNull(dto, "dto must not be null");
		Assert.notNull(dto.getUpdateFlag(), "dto.getUpdateFlag must not be null");
		if (dto.getUpdateFlag() == UpdateFlag.Removed) {
			// 删除逻辑：先删除从表，再删除主表
			boolean canDelete = true;
			if (listener != null) {
				canDelete = listener.beforeDelete(dto);
			}
			if (canDelete) {
				remove(dto);
				if (listener != null) {
					listener.afterDelete(dto);
				}
			}
		} else if (dto.getUpdateFlag() == UpdateFlag.Appended) {
			// 新增逻辑：先新增主表，设置从表外键再插入从表
			if (listener != null) {
				dto = listener.beforeInsert(dto, selective);
			}
			if (selective) {
				insertSelective(dto);
			} else {
				insert(dto);
			}
			if (listener != null) {
				dto = listener.afterInsert(dto, selective);
			}
		} else if (dto.getUpdateFlag() == UpdateFlag.Updated) {
			// 更新逻辑：先新增主表，设置从表外键再插入从表
			if (listener != null) {
				dto = listener.beforeUpdate(dto, selective);
			}
			if (selective) {
				updateSelective(dto);
			} else {
				update(dto);
			}
			if (listener != null) {
				dto = listener.afterUpdate(dto, selective);
			}
		} else if (dto.getUpdateFlag() == UpdateFlag.Unchanged) {
		}
		return dto;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DTO handle(DTO dto) {
		return handle(dto, false, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DTO handleSelective(DTO dto) {
		return handle(dto, true, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends DTO> List<T> handle(List<T> dtos) {
		Assert.notNull(dtos, "dtos must be null");
		for (DTO dto : dtos) {
			handle(dto);
		}
		return dtos;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends DTO> List<T> handleSelective(List<T> dtos) {
		Assert.notNull(dtos, "dtos must be null");
		for (DTO dto : dtos) {
			handleSelective(dto);
		}
		return dtos;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends DTO> T handle(T dto, DaoEventListener<T> listener) {
		return handle(dto, false, listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends DTO> T handleSelective(T dto, DaoEventListener<T> listener) {
		return handle(dto, true, listener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends DTO> List<T> handle(List<T> dtos, DaoEventListener<T> listener) {
		Assert.notNull(dtos, "dtos must be null");
		for (T dto : dtos) {
			handle(dto, listener);
		}
		return dtos;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends DTO> List<T> handleSelective(List<T> dtos, DaoEventListener<T> listener) {
		Assert.notNull(dtos, "dtos must be null");

		for (T dto : dtos) {
			handle(dto, listener);
		}
		return dtos;
	}

	private Serializable getPrimaryKey(Object dto) {
		return DataModelUtils.getPrimaryKey(daoModelManager, dto);
	}

	private void setPrimaryKey(DataModel dataModel, Object dto) {
	}

	protected void setForeignKey(DTO master, DTO detail) {

	}

	/**
	 * <pre>
	 * 将where的?替换#{param0}
	 * 将条件根据顺序存入key为param0...
	 * </pre>
	 * 
	 * @param where
	 * @param conditions
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> buildCondition(String where, List<Object> conditions) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		int index1 = 0;
		int index2 = where.indexOf("?");
		int num = 0;
		StringBuilder whereSQL = new StringBuilder();
		while (index2 != -1) {
			whereSQL.append(where.subSequence(index1, index2));
			if (conditions.get(num) instanceof List) {
				List<Object> list = (List<Object>) conditions.get(num);
				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						whereSQL.append(" #{param");
						whereSQL.append(num++);
						whereSQL.append("} ");
						if (i < list.size() - 1) {
							whereSQL.append(",");
						}
					}
				}
			} else {
				whereSQL.append(" #{param");
				whereSQL.append(num++);
				whereSQL.append("} ");
			}
			index1 = index2 + 1;
			index2 = where.indexOf("?", index1);
		}

		if (index1 < where.length()) {
			whereSQL.append(where.substring(index1));
		}
		parameters.put("where", whereSQL.toString());
		if (conditions != null) {
			for (int i = 0; i < conditions.size(); i++) {
				if (conditions.get(i) instanceof List) {
					List<Object> list = (List<Object>) conditions.get(i);
					if (list != null) {
						for (int j = 0; j < list.size(); j++) {
							parameters.put("param" + (i + j), list.get(j));
						}
					}
				} else {
					parameters.put("param" + i, conditions.get(i));
				}
			}
		}
		return parameters;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends DTO> RowMapper<T> getRowMapper(String modelId) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends DTO> List<T> loadDetails(DTO dto, String modelId, String foreignColumn) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends DTO> List<T> loadDetails(DTO dto, Class<T> clazz, String foreignColumn) {
		return loadDetails(dto, clazz.getName(), foreignColumn);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean deleteTreeDetails(String modelId, String column, Serializable parentId) {
		DataModel dataModel = findDataModel(modelId);
		String tableName = dataModel.getTableName();
		String where = String.format("%s = ?", column);
		List<DTO> childs = findAll(modelId, where, parentId);
		for (int i = 0, size = childs.size(); i < size; i++) {
			// 递归删除子孙记录
			DTO childDto = childs.get(i);
			Serializable childId = getPrimaryKey(childDto);
			deleteTreeDetails(modelId, column, childId);
		}

		// 删除子记录
		String sql = String.format("delete from %s where %s = ?", tableName, column);
		getJdbcTemplate().update(sql, parentId);

		// 删除当前
		removeById(modelId, parentId);
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends DTO> boolean deleteTreeDetails(Class<T> clazz, String column, Serializable parentId) {
		return deleteTreeDetails(clazz.getName(), column, parentId);
	}

}
