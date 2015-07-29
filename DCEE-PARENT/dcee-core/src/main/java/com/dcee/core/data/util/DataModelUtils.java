package com.dcee.core.data.util;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;

import com.dcee.core.data.dao.DaoModelManager;
import com.dcee.core.data.exception.DataModelNotFoundException;
import com.dcee.core.data.model.Column;
import com.dcee.core.data.model.ColumntTitle;
import com.dcee.core.data.model.DataModel;
import com.dcee.core.data.model.DefaultDTO;
import com.dcee.core.data.model.FormFieldsFactory;
import com.dcee.core.data.model.Formatter;
import com.dcee.core.data.model.ModelTitle;
import com.dcee.core.xml.converter.ParameterConverter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.IntConverter;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class DataModelUtils {
	private static XStream xStream = new XStream(new DomDriver("UTF-8"));	

	/**
	 * java类型与SqlType类型转换
	 */
	private static final Map<Class<?>, Integer> columnTypeMapping = new HashMap<Class<?>, Integer>();

	/**
	 * SqlType类型转换java类型
	 */
	private static final Map<Integer, Class<?>> typesMapping = new HashMap<Integer, Class<?>>();

	static {
		registerSelfDefineConverter();
		xStream.alias("datamodel", DataModelNode.class);
		xStream.alias("columns", ColumnsNode.class);
		xStream.alias("column", Column.class);
		xStream.addImplicitCollection(ColumnsNode.class, "columns", Column.class);
		xStream.useAttributeFor(Column.class, "name");
		xStream.useAttributeFor(Column.class, "title");
		xStream.useAttributeFor(Column.class, "type");
		xStream.useAttributeFor(Column.class, "nullable");
		xStream.useAttributeFor(Column.class, "length");
		xStream.useAttributeFor(Column.class, "primaryKey");
		xStream.useAttributeFor(Column.class, "fmtValue");
		xStream.useAttributeFor(Column.class, "fmtType");
		xStream.useAttributeFor(Column.class, "sequence");
		xStream.useAttributeFor(ColumnsNode.class, "table");
		xStream.useAttributeFor(ColumnsNode.class, "modelTitle");
		xStream.useAttributeFor(ColumnsNode.class, "parent");
		xStream.useAttributeFor(ColumnsNode.class, "factoryType");
		xStream.useAttributeFor(ColumnsNode.class, "factoryParam");

		columnTypeMapping.put(short.class, Types.SMALLINT);
		columnTypeMapping.put(Short.class, Types.SMALLINT);
		columnTypeMapping.put(int.class, Types.INTEGER);
		columnTypeMapping.put(Integer.class, Types.INTEGER);
		columnTypeMapping.put(long.class, Types.BIGINT);
		columnTypeMapping.put(Long.class, Types.BIGINT);
		columnTypeMapping.put(float.class, Types.FLOAT);
		columnTypeMapping.put(Float.class, Types.FLOAT);
		columnTypeMapping.put(double.class, Types.DOUBLE);
		columnTypeMapping.put(Double.class, Types.DOUBLE);
		columnTypeMapping.put(BigDecimal.class, Types.NUMERIC);
		columnTypeMapping.put(BigDecimal.class, Types.DECIMAL);
		columnTypeMapping.put(char.class, Types.CHAR);
		columnTypeMapping.put(Character.class, Types.CHAR);
		columnTypeMapping.put(Date.class, Types.DATE);
		columnTypeMapping.put(Time.class, Types.TIME);
		columnTypeMapping.put(Timestamp.class, Types.TIMESTAMP);
		columnTypeMapping.put(byte[].class, Types.BLOB);
		columnTypeMapping.put(byte.class, Types.BINARY);
		columnTypeMapping.put(String.class, Types.VARCHAR);

		typesMapping.put(Types.TINYINT, Short.class);
		typesMapping.put(Types.SMALLINT, Short.class);
		typesMapping.put(Types.INTEGER, Integer.class);
		typesMapping.put(Types.BIGINT, Long.class);
		typesMapping.put(Types.FLOAT, Float.class);
		typesMapping.put(Types.DOUBLE, Double.class);
		typesMapping.put(Types.DECIMAL, BigDecimal.class);
		typesMapping.put(Types.CHAR, Character.class);
		typesMapping.put(Types.VARCHAR, String.class);
		typesMapping.put(Types.LONGVARCHAR, String.class);
		typesMapping.put(Types.DATE, Date.class);
		typesMapping.put(Types.TIME, Time.class);
		typesMapping.put(Types.TIMESTAMP, Timestamp.class);
		typesMapping.put(Types.BLOB, byte[].class);
		typesMapping.put(Types.CLOB, String.class);
		typesMapping.put(Types.BOOLEAN, Boolean.class);
	}

	private static void registerSelfDefineConverter() {
		xStream.registerConverter(new ParameterConverter(xStream.getMapper()), XStream.PRIORITY_VERY_HIGH);
		IntConverter intConverter = new IntConverter() {
			public Object fromString(String str) {
				if (!StringUtils.hasText(str)) {
					return 0;
				}
				return super.fromString(str);
			}
		};
		xStream.registerConverter(intConverter, XStream.PRIORITY_VERY_HIGH);
		StringConverter stringConverter = new StringConverter() {
			public Object fromString(String str) {
				Object s = super.fromString(str);
				return s == null ? s : ((String) s).trim();
			}
		};
		xStream.registerConverter(stringConverter, XStream.PRIORITY_VERY_HIGH);
	}

	/**
	 * 转换xml成DataModel
	 * 
	 * @param inputStream
	 * @return
	 */
	public static DataModel parserXml(String modelId, InputStream inputStream) {
		DataModelNode dataModelNode = (DataModelNode) xStream.fromXML(inputStream);
		dataModelNode.setId(modelId);
		return dataModelNode.toDataModel();
	}

	/**
	 * 转换DataModel成Xml
	 */
	public static String parserDataModel(DataModel dataModel) {
		List<Column> newColumns = new ArrayList<Column>();
		List<Column> columns = dataModel.getColumns();
		for (Column column : columns) {
			if (!column.isExtendFlag()) {
				// 忽略继承列
				newColumns.add(column);
			}
		}
		dataModel.setColumns(newColumns);
		String xml = xStream.toXML(dataModel);
		return xml;
	}

	/**
	 * 转换静态模型Class成DataModel
	 * 
	 * @param clazz
	 * @return
	 * @throws DataModelNotFoundException
	 */
	public static DataModel parserClass(Class<?> clazz) throws DataModelNotFoundException {
		DataModel dataModel = new DataModel(clazz.getName());
		Entity entity = clazz.getAnnotation(Entity.class);
		if (entity == null) {
			throw new DataModelNotFoundException(clazz.getName());
		}
		dataModel.setDynamic(false);
		dataModel.setPoClass(clazz.getName());
		Table table = clazz.getAnnotation(Table.class);
		if (table != null) {
			String tableName = table.name();
			if (StringUtils.hasText(tableName)) {
				dataModel.setTableName(tableName);
			}
		}
		ModelTitle modelTitleAnnotation = clazz.getAnnotation(ModelTitle.class);
		if (modelTitleAnnotation != null) {
			String modelTitle = modelTitleAnnotation.value();
			if (StringUtils.hasText(modelTitle)) {
				dataModel.setModelTitle(modelTitle);
			}
		}

		FormFieldsFactory formFieldsFactory = clazz.getAnnotation(FormFieldsFactory.class);
		if (formFieldsFactory != null) {
			String factoryType = formFieldsFactory.factoryType();
			String factoryParam = formFieldsFactory.factoryParam();
			if (StringUtils.hasText(factoryType)) {
				dataModel.setFactoryType(factoryType);
				dataModel.setFactoryParam(factoryParam);
			}
		}
		Field[] fields = clazz.getDeclaredFields();
		if (fields != null) {
			List<Column> columns = new ArrayList<Column>();
			for (Field field : fields) {
				Class<?> fieldType = field.getType();
				if (fieldType.isPrimitive() || fieldType == byte[].class || fieldType.getName().startsWith("java.")) {
					// 简单类型
					Column column = field2Column(dataModel, field);
					if (column != null) {
						columns.add(column);
					}
				} else {
					// 复杂类型
					Transient transientAnotation = field.getAnnotation(Transient.class);
					if (transientAnotation == null) {
						List<Column> field2Columns = field2Columns(dataModel, field);
						columns.addAll(field2Columns);
					}
				}
			}
			dataModel.setColumns(columns);
		}

		Class<?> superclass = clazz.getSuperclass();
		if (!superclass.equals(Object.class) && !superclass.equals(DefaultDTO.class)) {
			DataModel superDataModel = parserClass(superclass);
			dataModel.extendsFrom(superDataModel);
		}

		String filePath = "/conf/datamodel/" + clazz.getName().replace(".", "/") + DataModel.SUFFIX;
		InputStream dataModelConfig = DataModelUtils.class.getResourceAsStream(filePath);
		if (dataModelConfig != null) {
			try {
				DataModel parserXml = parserXml(clazz.getName(), dataModelConfig);
				dataModel.extendsFrom(parserXml);
			} finally {
				IOUtils.closeQuietly(dataModelConfig);
			}
		}
		dataModel.validate();
		return dataModel;
	}

	/**
	 * 复杂类型转换字段
	 * 
	 * @param dataModel
	 * 
	 * @param field
	 * @return
	 */
	private static List<Column> field2Columns(DataModel dataModel, Field field) {
		Class<?> clazz = field.getType();
		List<Column> columns = new ArrayList<Column>();
		Id id = field.getAnnotation(Id.class);
		EmbeddedId embeddedId = field.getAnnotation(EmbeddedId.class);
		boolean primaryKey = id != null || embeddedId != null;
		GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
		boolean sequence = generatedValue != null;
		if (clazz.isPrimitive() || clazz == byte[].class || clazz.getName().startsWith("java.")) {
			// 简单类型
			Column column = field2Column(dataModel, field);
			if (column != null) {
				columns.add(column);
			}
		} else {
			if (primaryKey && dataModel.getPkProperty() == null) {
				dataModel.setPkProperty(field.getName());
				dataModel.setPkClass(field.getType());
			}
			// 复杂类型
			Field[] fields = clazz.getDeclaredFields();
			if (fields != null) {
				for (Field temp : fields) {
					columns.addAll(field2Columns(dataModel, temp));
				}
			}
		}
		if (primaryKey || sequence) {
			for (Column column : columns) {
				column.setPrimaryKey(primaryKey);
				column.setSequence(sequence);
			}
		}
		return columns;
	}

	/**
	 * 转换字段
	 * 
	 * @param dataModel
	 * @param field
	 * @return
	 */
	private static Column field2Column(DataModel dataModel, Field field) {
		Transient transientAnotation = field.getAnnotation(Transient.class);
		if (transientAnotation != null) {
			return null;
		}
		javax.persistence.Column columnAnnotation = field.getAnnotation(javax.persistence.Column.class);
		Id id = field.getAnnotation(Id.class);
		EmbeddedId embeddedId = field.getAnnotation(EmbeddedId.class);

		if (columnAnnotation == null && id == null && embeddedId == null) {
			return null;
		}

		Column column = new Column();

		ColumntTitle columnComment = field.getAnnotation(ColumntTitle.class);
		if (columnComment != null) {
			column.setTitle(columnComment.value());
		} else {
			column.setTitle("");
		}

		if (id != null) {
			column.setPrimaryKey(true);
			if (dataModel.getPkProperty() == null) {
				dataModel.setPkProperty(field.getName());
				dataModel.setPkClass(field.getType());
			}
		}

		if (embeddedId != null) {
			column.setPrimaryKey(true);
			dataModel.setPkProperty(field.getName());
			dataModel.setPkClass(field.getType());
		}

		GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
		if (generatedValue != null) {
			column.setSequence(true);
		}
		Formatter formatter = field.getAnnotation(Formatter.class);

		if (formatter != null) {
			column.setFmtValue(formatter.fmtValue());
			column.setFmtType(formatter.fmtType());
		}
		if (columnAnnotation != null) {
			String name = columnAnnotation.name();
			if (StringUtils.hasText(name)) {
				column.setName(name);
			} else {
				column.setName(field.getName());
			}
		}
		column.setType(getType(field.getType()));
		column.setProperty(field.getName());
		return column;
	}

	public static int getType(Class<?> clazz) {
		if (columnTypeMapping.containsKey(clazz)) {
			return columnTypeMapping.get(clazz);
		}
		return 0;
	}

	public static Class<?> getColumnClass(int type) {
		return typesMapping.get(type);
	}

	/**
	 * 获取主键值
	 * 
	 * @param daoModelManager
	 * @param dto
	 * @return
	 */
	public static Serializable getPrimaryKey(DaoModelManager daoModelManager, Object dto) {
		String modelId = dto.getClass().getName();
		try {
			DataModel dataModel = daoModelManager.findDataModel(modelId);
			List<Column> pkColumns = dataModel.getPkColumns();
			if (pkColumns.isEmpty()) {
				throw new RuntimeException(modelId + "没有配置主键");
			}

			if (dataModel.getPkProperty() == null) {
				throw new RuntimeException(dataModel.getPoClass() + "主键没有属性");
			}

			if (dataModel.getPkClass() == null) {
				throw new RuntimeException(dataModel.getPoClass() + "主键没有类型");
			}

			return (Serializable) PropertyUtils.getProperty(dto, dataModel.getPkProperty());
		} catch (DataModelNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("获取" + modelId + "主键错误");
		} catch (InvocationTargetException e) {
			throw new RuntimeException("获取" + modelId + "主键错误");
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("获取" + modelId + "主键错误");
		}
	}

	public static boolean isEmptyProperty(DaoModelManager daoModelManager, Object dto) {
		String modelId = dto.getClass().getName();
		DataModel dataModel;
		try {
			dataModel = daoModelManager.findDataModel(modelId);
			List<Column> columns = dataModel.getColumns();
			boolean isEmpty = true;
			for (Column column : columns) {
				if (!column.isPrimaryKey()) {
					Object property = PropertyUtils.getProperty(dto, dataModel.getPkProperty());
					if (property != null) {
						isEmpty = false;
					}
				}
			}
			return isEmpty;
		} catch (DataModelNotFoundException e) {
			throw new RuntimeException("找不到数据模型", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("获取" + modelId + "属性错误", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("获取" + modelId + "属性错误", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("获取" + modelId + "属性错误", e);
		}
	}
}
