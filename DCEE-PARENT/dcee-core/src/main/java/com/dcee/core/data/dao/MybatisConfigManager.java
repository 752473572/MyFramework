package com.dcee.core.data.dao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dcee.core.data.exception.DataModelNotFoundException;
import com.dcee.core.data.model.DataModel;
import com.dcee.core.generator.Generator;

import freemarker.template.TemplateException;

public class MybatisConfigManager {

	private static final Logger logger = LoggerFactory.getLogger(MybatisConfigManager.class);

	@Resource
	private DaoModelManager daoModelManager;

	@Resource
	private Generator generator;

	private class CacheValue {

		private long lastModifiedTime;

		private Configuration configuration;

		public boolean isChange(String modelId) throws DataModelNotFoundException {
			long lastModifiedWithParent = daoModelManager.getLastModifiedWithParent(modelId);
			if (lastModifiedWithParent != lastModifiedTime) {
				return true;
			}
			return false;

		}
	}

	/**
	 * 缓存
	 */
	private Map<String, CacheValue> cache = Collections.synchronizedMap(new HashMap<String, CacheValue>());

	public Configuration getConfiguration(String modelId) throws DataModelNotFoundException {
		modelId = daoModelManager.resolveModelId(modelId);
		String lock = MybatisConfigManager.class.getName() + modelId;
		synchronized (lock.intern()) {
			CacheValue cacheValue = cache.get(modelId);
			if (cacheValue != null) {
				if (!cacheValue.isChange(modelId)) {
					// 缓存
					return cacheValue.configuration;
				} else {
					cache.remove(modelId);
				}
			}

			cacheValue = new CacheValue();
			DataModel dataModel = daoModelManager.resolveDataModel(modelId);
			/*
			 * try { //强制更新dao Class<DTO> modelClass =
			 * daoModelManager.findModelClass(modelId); } catch
			 * (ClassNotFoundException e) { Assert.state(false); }
			 */

			Configuration configuration = new Configuration();
			String dataModel2Mapping = dataModel2Mapping(dataModel);
			logger.debug("DataModel[{}] Mapping:\n{}", modelId, dataModel2Mapping);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(dataModel2Mapping.getBytes());
			XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(inputStream, configuration, modelId,
					configuration.getSqlFragments());
			xmlMapperBuilder.parse();
			cacheValue.configuration = configuration;
			cacheValue.lastModifiedTime = daoModelManager.getLastModifiedWithParent(modelId);
			cache.put(modelId, cacheValue);

			return configuration;
		}
	}

	public String dataModel2Mapping(DataModel dataModel) {
		try {
			return generator.generateMapping(dataModel);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (TemplateException e) {
			throw new RuntimeException(e);
		}
	}

	public void setDaoModelManager(DaoModelManager daoModelManager) {
		this.daoModelManager = daoModelManager;
	}
}
