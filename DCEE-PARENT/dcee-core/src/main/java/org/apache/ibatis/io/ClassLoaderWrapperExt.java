package org.apache.ibatis.io;

import java.lang.reflect.Field;

import com.dcee.core.groovy.ScriptEngineManager;

public class ClassLoaderWrapperExt extends ClassLoaderWrapper {
	private ScriptEngineManager engine;

	Class<?> classForName(String name, ClassLoader[] classLoader) throws ClassNotFoundException {
		try {
			return super.classForName(name, classLoader);
		} catch (ClassNotFoundException e) {
		}
		return this.engine.getClass(name);
	}

	public static void updateResourcesClassLoader(ClassLoaderWrapperExt ext) {
		try {
			Field field = Resources.class.getDeclaredField("classLoaderWrapper");
			field.setAccessible(true);
			field.set(null, ext);
			field.setAccessible(false);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public ScriptEngineManager getEngine() {
		return this.engine;
	}

	public void setEngine(ScriptEngineManager engine) {
		this.engine = engine;
	}
}
