package com.dcee.core.data.dao;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.ibatis.io.ClassLoaderWrapperExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.dcee.core.cache.FileCache;
import com.dcee.core.data.exception.DataModelNotFoundException;
import com.dcee.core.data.model.DTO;
import com.dcee.core.data.model.DataModel;
import com.dcee.core.data.model.DataModelPackage;
import com.dcee.core.data.util.DataModelUtils;
import com.dcee.core.generator.Generator;
import com.dcee.core.groovy.CompositeResourceConnector;
import com.dcee.core.groovy.GroovyResourceConnector;
import com.dcee.core.groovy.ScriptEngineManager;

import freemarker.template.TemplateException;

/**
 * 数据模型管理
 * 
 * @see com.todaytech.pwp.core.data.dao.MybatisConfigManager
 * @see com.todaytech.pwp.core.data.dao.DefaultDao
 */
public class DaoModelManager implements InitializingBean,
		GroovyResourceConnector,ApplicationContextAware {

	
	/**
	 * 日志
	 */
	private static final Logger logger = LoggerFactory.getLogger(DaoModelManager.class);

	/**
	 * 缓存对象
	 * 
	 */
	private class CacheValue {

		private DataModel dataModel;

		/**
		 * 最后模型配置文件修改时间
		 */
		private long lastModifiedTime = -1;

		/**
		 * 动态数据模型标志
		 */
		private boolean dynamic = false;

		/**
		 * 缓存是否变化
		 * 
		 * @param modelId
		 * @return
		 */
		private boolean isChange(String modelId) {
			if (!dynamic) {
				// 静态数据模型不变化
				return false;
			}

			FileObject modelFileObject = getDynamicModelConfig(modelId);
			long lastModifiedTime = dynamicConfigCache
					.getLastModifiedTime(modelFileObject);
			if (this.lastModifiedTime != lastModifiedTime) {
				return true;
			}
			return false;
		}

		/**
		 * 返回当前文件最后修改时间
		 * 
		 * @return 文件不存在跑异常异常，其他为文件修改时间
		 * @throws DataModelNotFoundException
		 */
		private long getLastModifiedTime() throws DataModelNotFoundException {
			if (!dynamic) {
				return this.lastModifiedTime;
			}

			FileObject modelFileObject = getDynamicModelConfig(dataModel
					.getId());
			long lastModifiedTime = dynamicConfigCache
					.getLastModifiedTime(modelFileObject);
			if (lastModifiedTime == FileCache.NOT_FOUND) {
				// 文件被删除变化
				throw new DataModelNotFoundException(dataModel.getId());
			}

			this.lastModifiedTime = lastModifiedTime;
			return lastModifiedTime;
		}
	}

	/**
	 * 静态包扫描路径 acl = com.todaytech.acl.model
	 * 
	 * @Entity
	 */
	private Map<String, String> staticPkgs;

	/**
	 * 发布区动态模型根目录
	 */
	private List<FileObject> dynamicModelDirs = new ArrayList<FileObject>();

	/**
	 * 缓存
	 */
	private Map<String, CacheValue> cache = Collections
			.synchronizedMap(new HashMap<String, CacheValue>());

	private Generator generator;

	private ScriptEngineManager scriptEngineManager;

	/**
	 * 缓存检查延时单位秒
	 */
	private int delay = 10;

	/**
	 * 动态数据模型文件缓存
	 */
	private FileCache<FileObject> dynamicConfigCache;
	
	

	private ApplicationContext applicationContext;

	/**
	 * <pre>
	 * 解析相对路径为完整路径
	 * 
	 * 如：acl.Account 解析成 com.todatytech.pwp.acl.model.Account
	 * </pre>
	 * 
	 * @param modelId
	 * @return
	 */
	public String resolveModelId(String modelId) {
		if (staticPkgs != null) {
			int index = modelId.indexOf(".");
			if (index != -1) {
				String prefix = modelId.substring(0, index);
				if (staticPkgs.containsKey(prefix)) {
					prefix = staticPkgs.get(prefix);
					if (prefix.endsWith(".")) {
						return prefix + modelId.substring(index + 1);
					} else {
						return prefix + modelId.substring(index);
					}
				}
			}
		}
		return modelId;
	}

	/**
	 * 加载当前数据模型
	 * 
	 * @param modelId
	 * @return
	 * @throws DataModelNotFoundException
	 */
	private CacheValue createDataModelCacheValue(String modelId)
			throws DataModelNotFoundException {
		CacheValue cacheValue = new CacheValue();
		DataModel dataModel = null;
		try {
			// 静态数据模型
			Class<?> clazz = Class.forName(modelId);
			cacheValue.dynamic = false;
			dataModel = DataModelUtils.parserClass(clazz);
			dataModel.synchronizedColumnType();
		} catch (ClassNotFoundException e) {
			// 动态数据模型
			String modelFile = modelId.replace(".", "/") + DataModel.SUFFIX;
			InputStream inputStream = null;
			try {
				FileObject modelFileObject = getDynamicModelConfig(modelId);
				if (modelFileObject == null || !modelFileObject.exists()) {
					throw new DataModelNotFoundException(modelId);
				}
				FileContent content = modelFileObject.getContent();
				cacheValue.lastModifiedTime = content.getLastModifiedTime();
				inputStream = content.getInputStream();
				dataModel = DataModelUtils.parserXml(modelId, inputStream);
				dataModel.setDynamic(true);
				dataModel.setPoClass(modelId);
				if (dataModel.getParent() != null) {
					DataModel parent = findDataModel(dataModel.getParent());
					dataModel.extendsProperty(parent);
				}
				dataModel.synchronizedColumnType();
			} catch (FileSystemException e1) {
				logger.error(modelFile + "读取异常:", e1);
				throw new RuntimeException(modelFile + "读取异常:"
						+ e1.getMessage());
			} finally {
				IOUtils.closeQuietly(inputStream);
			}
			cacheValue.dynamic = true;
		}
		cacheValue.dataModel = dataModel;
		cache.put(modelId, cacheValue);
		return cacheValue;
	}

	/**
	 * <pre>
	 * 解析完整数据模型，包括所有父类的结构
	 * 
	 * 不使用缓存，每次创建DataModel并根据父类创建完整结构
	 * </pre>
	 * 
	 * @param dataModel
	 * @return
	 * @throws DataModelNotFoundException
	 */
	public DataModel resolveDataModel(String modelId)
			throws DataModelNotFoundException {
		modelId = resolveModelId(modelId);
		DataModel dataModel = findDataModel(modelId);
		dataModel = (DataModel) dataModel.clone();
		if (dataModel.getParent() != null) {
			DataModel parentDataModel = resolveDataModel(dataModel.getParent());
			dataModel.extendsFrom(parentDataModel);
		}
		
		
		return dataModel;
	}
	
	

	/**
	 * <pre>
	 * 获取最后修改时间，从所有父类和自身的最后修改时间
	 * 
	 * 外部不使用DataModel作为缓存判断依据，使用此修改时间可以动态检测
	 * </pre>
	 * 
	 * @param modelId
	 * @return
	 * @throws DataModelNotFoundException
	 */
	public long getLastModifiedWithParent(String modelId)
			throws DataModelNotFoundException {
		modelId = resolveModelId(modelId);
		CacheValue cacheValue = findDataModelCache(modelId);
		DataModel dataModel = cacheValue.dataModel;
		long lastModifiedTime = cacheValue.getLastModifiedTime();
		if (dataModel.getParent() != null) {
			long parentLastModifiedTime = getLastModifiedWithParent(dataModel
					.getParent());
			if (parentLastModifiedTime > lastModifiedTime) {
				return parentLastModifiedTime;
			}
		}
		return lastModifiedTime;
	}

	/**
	 * <pre>
	 * 获取数据模型
	 * </pre>
	 * 
	 * @param modelId
	 *            路径支持相对
	 * @return
	 * @throws DataModelNotFoundException
	 */
	public DataModel findDataModel(String modelId)
			throws DataModelNotFoundException {
		return findDataModelCache(modelId).dataModel;
	}

	/**
	 * 查找数据模型缓存
	 * 
	 * @param modelId
	 * @return
	 * @throws DataModelNotFoundException
	 */
	private CacheValue findDataModelCache(String modelId)
			throws DataModelNotFoundException {
		modelId = resolveModelId(modelId);
		String lock = DaoModelManager.class.getName() + ":" + modelId;
		synchronized (lock.intern()) {
			CacheValue cacheValue = cache.get(modelId);
			if (cacheValue == null || cacheValue.isChange(modelId)) {
				cacheValue = createDataModelCacheValue(modelId);
				cache.put(modelId, cacheValue);
			}
			return cacheValue;
		}

	}

	/**
	 * 前台调用后台服务时候需要动态数据模型需要生成PO
	 * 
	 * @param modelId
	 * @return
	 * @throws ClassNotFoundException
	 * @throws DataModelNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public Class<DTO> findModelClass(String modelId)
			throws ClassNotFoundException, DataModelNotFoundException {
		modelId = resolveModelId(modelId);
		CacheValue cacheValue = findDataModelCache(modelId);
		Class<?> clazz = null;
		if (cacheValue.dynamic) {
			// 动态模型
			// generateDataModelClass(modelId);
			clazz = scriptEngineManager.getClass(modelId);
		} else {
			// 静态模型
			clazz = Class.forName(modelId);
		}
		if (!DTO.class.isAssignableFrom(clazz)) {
			String msg = String.format("数据模型%s未实现%s接口", modelId,
					DTO.class.getName());
			throw new IllegalArgumentException(msg);
		}
		return (Class<DTO>) clazz;
	}

	/**
	 * 创建POclass
	 * 
	 * @param modelId
	 */
	/*
	 * private void generateDataModelClass(String modelId) { modelId =
	 * resolveModelId(modelId); CacheValue cacheValue =
	 * findDataModelCache(modelId); DataModel dataModel = cacheValue.dataModel;
	 * 
	 * if (dataModel.getParent() != null) {
	 * generateDataModelClass(dataModel.getParent()); } if
	 * (!cacheValue.poGenerated) { createDataModelClass(dataModel);
	 * cacheValue.poGenerated = true; } }
	 */

	/**
	 * 创建POClass
	 * 
	 * @param dataModel
	 * @return
	 */
	private void createDataModelClass(DataModel dataModel,CompositeResourceConnector compositeResourceConnector) {
		try {
			List<String> scripts = generator.generateScript(dataModel);
			if (scripts.size() > 1) {
				// 复合主键
				logger.debug(scripts.get(0));
				logger.debug(scripts.get(1));
				compositeResourceConnector.addScript(dataModel.getPoClass() + "PK", scripts.get(0));
				compositeResourceConnector.addScript(dataModel.getPoClass(), scripts.get(1));
			} else {
				// 单主键
				logger.debug(scripts.get(0));
				compositeResourceConnector.addScript(dataModel.getPoClass(), scripts.get(0));
			}
		} catch (IOException e) {
			logger.error("读取类模板失败:", e);
			throw new RuntimeException("读取类模板失败");
		} catch (TemplateException e) {
			logger.error("由模板生成代码失败:", e);
			throw new RuntimeException("由模板生成代码失败");
		}
	}

	/**
	 * 设置发布区、扩展分区目录路径
	 * 
	 * @param releasePath
	 */
	public void setRelaseRoot(List<FileObject> roots) {
		Assert.notNull(roots, "setRelaseRoot must not be null");
		for (int i = 0; i < roots.size(); i++) {
			FileObject root = roots.get(i);
			try {
				FileObject dynamicDir = root.resolveFile("datamodel");
				if (dynamicDir == null || !dynamicDir.exists()) {
					continue;
				}
				dynamicModelDirs.add(dynamicDir);
			} catch (FileSystemException e) {
				logger.error(root.getName().getURI() + ":动态模型目录读取异常", e);
			}
		}
	}

	private FileObject getDynamicModelConfig(String modelId) {
		String modelFile = modelId.replace(".", "/") + DataModel.SUFFIX;
		try {
			for (int i = 0, size = dynamicModelDirs.size(); i < size; i++) {
				FileObject dynamicModelDir = dynamicModelDirs.get(i);
				FileObject modelFileObject = dynamicModelDir
						.resolveFile(modelFile);
				if (modelFileObject == null || !modelFileObject.exists()) {
					continue;
				}

				return modelFileObject;
			}
			return null;
		} catch (FileSystemException e1) {
			logger.error(modelFile + "读取异常:", e1);
			throw new RuntimeException(modelFile + "读取异常:" + e1.getMessage());
		}
	}

	public Map<String, String> getStaticPkgs() {
		return staticPkgs;
	}

	public void setStaticPkgs(Map<String, String> staticPkgs) {
		this.staticPkgs = staticPkgs;
	}

	public Generator getGenerator() {
		return generator;
	}

	public void setGenerator(Generator generator) {
		this.generator = generator;
	}

	public ScriptEngineManager getScriptEngineManager() {
		return scriptEngineManager;
	}

	public void setScriptEngineManager(ScriptEngineManager scriptEngineManager) {
		this.scriptEngineManager = scriptEngineManager;
	}

	public void setDelay(int delay) {
		this.delay = delay;
		if (dynamicConfigCache != null) {
			dynamicConfigCache.setDelay(delay);
		}
	}

	public int getDelay() {
		return delay;
	}

	public void afterPropertiesSet() throws Exception {

		dynamicConfigCache = new FileCache<FileObject>(delay);

		ClassLoaderWrapperExt ext = new ClassLoaderWrapperExt();
		ext.setEngine(scriptEngineManager);
		ClassLoaderWrapperExt.updateResourcesClassLoader(ext);
		
		Map<String, DataModelPackage> packages = applicationContext.getBeansOfType(DataModelPackage.class);
		if(packages != null){
			staticPkgs = new HashMap<String,String>();
			Set<Entry<String, DataModelPackage>> entrySet = packages.entrySet();
			for (Entry<String, DataModelPackage> entry : entrySet) {
				DataModelPackage value = entry.getValue();
				String id = value.getId();
				String packageName = value.getPackageName();
				if(!StringUtils.hasText(id)){
					logger.warn("ignore datamodel package {} , no id...",packageName);
					continue;
				}
				if(!StringUtils.hasText(packageName)){
					logger.warn("ignore datamodel package[id={}], packageName not config...",id);
					continue;
				}
				staticPkgs.put(id, packageName);
			}
		}
		
	}

	/**
	 * Groovy调用查找资源
	 * @throws ClassNotFoundException 
	 */
	public void updateClassFile(File classFile, String className,CompositeResourceConnector compositeResourceConnector)throws ClassNotFoundException {

		String modelId = className;
		try {
			CacheValue dataModel = findDataModelCache(modelId);

			if (!classFile.exists()
					|| classFile.lastModified() < dataModel.lastModifiedTime) {
				createDataModelClass(dataModel.dataModel,compositeResourceConnector);
			}

		} catch (DataModelNotFoundException e) {
			throw new ClassNotFoundException("datamodel " + className
					+ " not found.", e);
		}

	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
