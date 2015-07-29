package com.dcee.core.util.beanutils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.converters.BigDecimalConverter;

@SuppressWarnings({ "rawtypes", "unchecked" })
public final class ReflectUtil {
	public static String getFieldByName(Object orig, String fieldName, boolean pbIgnoreCase) {
		Field[] origFields = getDeclaredFieldsForClass(orig.getClass());
		String fieldNameToFind = fieldName;
		if (pbIgnoreCase) {
			fieldNameToFind = fieldName.toUpperCase();
		}
		Object objValue = null;

		for (int i = 0; i < origFields.length; i++) {
			Field origField = origFields[i];
			String name = origField.getName();
			if (pbIgnoreCase) {
				name = name.toUpperCase();
			}
			if (name.equals(fieldNameToFind)) {
				origField.setAccessible(true);
				try {
					objValue = origField.get(orig);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				origField.setAccessible(false);
				break;
			}
		}
		if (objValue != null) {
			return ConvertUtils.convert(objValue);
		}
		return null;
	}

	public static void setFieldByName(Object orig, String fieldName, String value, boolean pbIgnoreCase) {
		Field[] origFields = getDeclaredFieldsForClass(orig.getClass());
		String fieldNameToFind = fieldName;
		if (pbIgnoreCase) {
			fieldNameToFind = fieldName.toUpperCase();
		}
		boolean found = false;

		for (int i = 0; i < origFields.length; i++) {
			Field origField = origFields[i];
			String name = origField.getName();
			if (pbIgnoreCase) {
				name = name.toUpperCase();
			}
			if (name.equals(fieldNameToFind)) {
				origField.setAccessible(true);
				try {
					origField.set(orig, value);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				origField.setAccessible(false);
				found = true;
				break;
			}
		}
		if (!found) {
			throw new IllegalArgumentException("Field not found. fieldName ='" + fieldName + "'");
		}
	}

	public static Object invokeMethod(Object owner, String methodName, Object... args) {
		Class ownerClass = owner.getClass();
		Class[] argsClass = new Class[args.length];
		int i = 0;
		for (int j = args.length; i < j; i++) {
			argsClass[i] = args[i].getClass();
		}
		Method method = null;
		try {
			method = ownerClass.getMethod(methodName, argsClass);
			return method.invoke(owner, args);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static Object invokeStaticMethod(String className, String methodName, Object... args) {
		Class ownerClass = null;
		try {
			ownerClass = Class.forName(className);
			Class[] argsClass = new Class[args.length];
			int i = 0;
			for (int j = args.length; i < j; i++) {
				argsClass[i] = args[i].getClass();
			}
			Method method = ownerClass.getMethod(methodName, argsClass);
			return method.invoke(null, args);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static Object newInstance(String className, Object... args) {
		Class newoneClass = null;
		try {
			newoneClass = Class.forName(className);
			Class[] argsClass = new Class[args.length];
			int i = 0;
			for (int j = args.length; i < j; i++) {
				argsClass[i] = args[i].getClass();
			}
			Constructor cons = newoneClass.getConstructor(argsClass);
			return cons.newInstance(args);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static void copyNotNullProperties(Object objFrom, Object objTo) {
		copyAllPropertiesByName(objFrom, objTo, false);
	}

	public static void copyAllProperties(Object objFrom, Object objTo) {
		ConvertUtils.register(new DateConverter(), java.util.Date.class);
		ConvertUtils.register(new DateConverter(), java.sql.Date.class);
		ConvertUtils.register(new BigDecimalConverter(null), BigDecimal.class);
		copyAllPropertiesByName(objFrom, objTo, true);
	}

	private static void copyAllPropertiesByName(Object objFrom, Object objTo, boolean bIncludeNull) {
		if (bIncludeNull) {
			try {
				BeanUtils.copyProperties(objTo, objFrom);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		} else {
			copyProperties(objTo, objFrom, bIncludeNull);
		}
	}

	private static void copyProperties(Object dest, Object orig, boolean bIncludeNull) {
		if (dest == null) {
			throw new IllegalArgumentException("No destination bean specified");
		}
		if (orig == null) {
			throw new IllegalArgumentException("No origin bean specified");
		}

		if ((orig instanceof DynaBean)) {
			copyDynaBean(dest, (DynaBean) orig, bIncludeNull);
		} else if ((orig instanceof Map)) {
			copyBeanMap(dest, (Map) orig, bIncludeNull);
		} else {
			copyBeanArray(dest, orig, bIncludeNull);
		}
	}

	private static void copyBeanArray(Object dest, Object orig, boolean bIncludeNull) {
		PropertyDescriptor[] origDescriptors = PropertyUtils.getPropertyDescriptors(orig);
		for (int i = 0; i < origDescriptors.length; i++) {
			String name = origDescriptors[i].getName();
			if (!"class".equals(name)) {

				if ((PropertyUtils.isReadable(orig, name)) && (PropertyUtils.isWriteable(dest, name)))
					try {
						Object value = PropertyUtils.getSimpleProperty(orig, name);
						if ((bIncludeNull) || (value != null)) {
							BeanUtils.copyProperty(dest, name, value);
						}
					} catch (NoSuchMethodException ex) {
						throw new RuntimeException(ex);
					} catch (InvocationTargetException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
			}
		}
	}

	private static void copyBeanMap(Object dest, Map orig, boolean bIncludeNull) {
		Iterator names = orig.keySet().iterator();
		while (names.hasNext()) {
			String name = (String) names.next();
			if (PropertyUtils.isWriteable(dest, name)) {
				Object value = orig.get(name);
				if ((bIncludeNull) || (value != null)) {
					try {
						BeanUtils.copyProperty(dest, name, value);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					} catch (InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	private static void copyDynaBean(Object dest, DynaBean orig, boolean bIncludeNull) {
		DynaProperty[] origDescriptors = orig.getDynaClass().getDynaProperties();
		for (int i = 0; i < origDescriptors.length; i++) {
			String name = origDescriptors[i].getName();
			if (PropertyUtils.isWriteable(dest, name)) {
				Object value = orig.get(name);
				if ((bIncludeNull) || (value != null)) {
					try {
						BeanUtils.copyProperty(dest, name, value);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					} catch (InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	public static void setPropertyByName(Object objTo, String sFieldName, Object value, boolean bIgnoreCase) {
		if (bIgnoreCase) {
			sFieldName = findPropertyName(objTo.getClass(), sFieldName);
		}
		try {
			BeanUtils.copyProperty(objTo, sFieldName, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private static String findPropertyName(Class objClz, String sFieldName) {
		Field[] fields = getDeclaredFields(objClz);
		String sToRet = null;
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			String fieldName = field.getName();
			if (fieldName.equalsIgnoreCase(sFieldName)) {
				sToRet = fieldName;
				break;
			}
		}
		return sToRet;
	}

	private static Field[] getDeclaredFields(Class objClz) {
		ArrayList fields = new ArrayList();
		Class curClz = objClz;
		Collections.addAll(fields, curClz.getDeclaredFields());
		while (curClz.getSuperclass() != Object.class) {
			curClz = curClz.getSuperclass();
			Collections.addAll(fields, curClz.getDeclaredFields());
		}
		return (Field[]) fields.toArray(new Field[fields.size()]);
	}

	private static Field[] getDeclaredFieldsForClass(Class clz) {
		if (clz == Object.class) {
			return new Field[0];
		}
		ArrayList<Field> fieldlist = new ArrayList();
		fieldlist.addAll(Arrays.asList(clz.getDeclaredFields()));
		Field[] fieldsOfSuperClz = getDeclaredFieldsForClass(clz.getSuperclass());
		if (fieldsOfSuperClz != null) {
			fieldlist.addAll(Arrays.asList(fieldsOfSuperClz));
		}
		return (Field[]) fieldlist.toArray(new Field[0]);
	}

	public static <T> Class<T> getGenericTypeArgument(Class clazz) {
		return getGenericTypeArgument(clazz, 0);
	}

	public static Class getGenericTypeArgument(Class clazz, int index) {
		Type genType = clazz.getGenericSuperclass();

		if (!(genType instanceof ParameterizedType)) {
			return Object.class;
		}

		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

		if ((index >= params.length) || (index < 0)) {

			return Object.class;
		}
		if (!(params[index] instanceof Class)) {
			return Object.class;
		}

		return (Class) params[index];
	}
}
