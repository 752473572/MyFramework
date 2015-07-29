package com.dcee.core.cache;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCache<T> {
	public static long NOT_FOUND = -1L;
	private int delay = 10;
	private Map<String, FileCache<T>.CacheValue> cache = Collections.synchronizedMap(new HashMap<String, FileCache<T>.CacheValue>());
	private static final Logger logger = LoggerFactory.getLogger(FileCache.class);

	public FileCache() {
	}

	private class CacheValue {
		private long lastReadTime = System.currentTimeMillis();
		private long lastModifiedTime = -1L;
		private T file;

		private CacheValue() {
			
		}
	}

	public FileCache(int delay) {
		this.delay = delay;
	}

	public long getLastModifiedTime(T file) {
		String cacheKey = getCacheKey(file);
		if (cacheKey == null) {
			return NOT_FOUND;
		}
		FileCache<T>.CacheValue cacheValue = (CacheValue) this.cache.get(cacheKey);
		if (cacheValue == null) {
			logger.debug("创建文件缓存[{}]", cacheKey);
			this.cache.put(cacheKey, new CacheValue());
			cacheValue = updateCache(cacheKey, file);
			return cacheValue.lastModifiedTime;
		}
		if (System.currentTimeMillis() - cacheValue.lastReadTime > this.delay * 1000) {
			logger.debug("读取文件缓存[{}]", cacheKey);
			cacheValue = updateCache(cacheKey, file);
			return cacheValue.lastModifiedTime;
		}
		return cacheValue.lastModifiedTime;
	}

	private FileCache<T>.CacheValue updateCache(String cacheKey, T file) {
		FileCache<T>.CacheValue cacheValue = (CacheValue) this.cache.get(cacheKey);
		cacheValue.lastReadTime = System.currentTimeMillis();
		cacheValue.file = file;
		cacheValue.lastModifiedTime = readLastModifiedTime(file);
		return cacheValue;
	}

	public long readLastModifiedTime(T object) {
		if (object == null) {
			return NOT_FOUND;
		}
		if ((object instanceof File)) {
			File file = (File) object;
			if (file.exists()) {
				return file.lastModified();
			}
			return NOT_FOUND;
		}
		if ((object instanceof FileObject)) {
			FileObject fileObject = (FileObject) object;
			try {
				return fileObject.getContent().getLastModifiedTime();
			} catch (FileSystemException e) {
				logger.error("readLastModifiedTime", e);
				return NOT_FOUND;
			}
		}
		return NOT_FOUND;
	}

	protected String getCacheKey(T object) {
		if ((object instanceof File)) {
			File file = (File) object;
			return file.getAbsolutePath();
		}
		if ((object instanceof FileObject)) {
			FileObject fileObject = (FileObject) object;
			return fileObject.getName().getURI();
		}
		return null;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getDelay() {
		return this.delay;
	}
}
