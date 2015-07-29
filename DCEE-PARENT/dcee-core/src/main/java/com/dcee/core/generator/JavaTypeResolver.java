package com.dcee.core.generator;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.dcee.core.data.model.Column;

public class JavaTypeResolver {
	private static final String BYTE_NAME = Byte.class.getName();
	private static final String FLOAT_NAME = Float.class.getName();
	private static final String DOUBLE_NAME = Double.class.getName();
	private static final String SHORT_NAME = Short.class.getName();
	private static final String INTEGER_NAME = Integer.class.getName();
	private static final String BIGDECIMAL_NAME = BigDecimal.class.getName();
	private static final String DATE_NAME = Date.class.getName();
	private static final String TIME_NAME = Time.class.getName();
	private static final String TIMESTAMP_NAME = Timestamp.class.getName();
	private static final String STRING_NAME = String.class.getName();
	private static final String BOOLEAN_NAME = Boolean.class.getName();
	private static final String BYTES_NAME = "byte[]";
	private static final String LONG_NAME = Long.class.getName();
	private static final String OBJECT_NAME = Object.class.getName();

	private static final Map<Integer, String> typesMapping = new HashMap<Integer, String>();

	static {
		typesMapping.put(Integer.valueOf(-7), "BIT");
		typesMapping.put(Integer.valueOf(-6), "TINYINT");
		typesMapping.put(Integer.valueOf(5), "SMALLINT");
		typesMapping.put(Integer.valueOf(4), "INTEGER");
		typesMapping.put(Integer.valueOf(-5), "BIGINT");
		typesMapping.put(Integer.valueOf(6), "FLOAT");
		typesMapping.put(Integer.valueOf(7), "REAL");
		typesMapping.put(Integer.valueOf(8), "DOUBLE");
		typesMapping.put(Integer.valueOf(2), "NUMERIC");
		typesMapping.put(Integer.valueOf(3), "DECIMAL");
		typesMapping.put(Integer.valueOf(1), "CHAR");
		typesMapping.put(Integer.valueOf(12), "VARCHAR");
		typesMapping.put(Integer.valueOf(-1), "LONGVARCHAR");
		typesMapping.put(Integer.valueOf(91), "DATE");
		typesMapping.put(Integer.valueOf(92), "TIME");
		typesMapping.put(Integer.valueOf(93), "TIMESTAMP");
		typesMapping.put(Integer.valueOf(-2), "BINARY");
		typesMapping.put(Integer.valueOf(-3), "VARBINARY");
		typesMapping.put(Integer.valueOf(-4), "LONGVARBINARY");
		typesMapping.put(Integer.valueOf(0), "NULL");
		typesMapping.put(Integer.valueOf(1111), "OTHER");
		typesMapping.put(Integer.valueOf(2000), "JAVA_OBJECT");
		typesMapping.put(Integer.valueOf(2001), "DISTINCT");
		typesMapping.put(Integer.valueOf(2002), "STRUCT");
		typesMapping.put(Integer.valueOf(2003), "ARRAY");
		typesMapping.put(Integer.valueOf(2004), "BLOB");
		typesMapping.put(Integer.valueOf(2005), "CLOB");
		typesMapping.put(Integer.valueOf(2006), "REF");
		typesMapping.put(Integer.valueOf(70), "DATALINK");
		typesMapping.put(Integer.valueOf(16), "BOOLEAN");
	}

	public static String calculateJavaType(Column column, boolean forceBigDecimals) {
		int precision = column.getPrecision();
		int length = column.getLength();
		int type = column.getType();
		String answer = calculateJavaType(precision, length, type, forceBigDecimals);
		return answer;
	}

	public static String calculateJavaType(int precision, int length, int type, boolean forceBigDecimals) {
		String answer;
		switch (type) {
		case 0:
		case 70:
		case 1111:
		case 2000:
		case 2001:
		case 2002:
		case 2003:
		case 2006:
			answer = OBJECT_NAME;
			break;

		case -5:
			answer = LONG_NAME;
			break;

		case -4:
		case -3:
		case -2:
		case 2004:
			answer = BYTES_NAME;
			break;

		case -7:
		case 16:
			answer = BOOLEAN_NAME;
			break;

		case -1:
		case 1:
		case 12:
		case 2005:
			answer = STRING_NAME;
			break;

		case 91:
			answer = DATE_NAME;
			break;
		case 92:
			answer = TIME_NAME;
			break;
		case 93:
			answer = TIMESTAMP_NAME;
			break;

		case 6:
		case 8:
			answer = DOUBLE_NAME;
			break;

		case 4:
			answer = INTEGER_NAME;
			break;

		case 7:
			answer = FLOAT_NAME;
			break;

		case 5:
			answer = INTEGER_NAME;
			break;

		case -6:
			answer = BYTE_NAME;
			break;

		case 2:
		case 3:
			if ((precision > 0) || (length > 18) || (forceBigDecimals)) {
				answer = BIGDECIMAL_NAME;
			} else {
				if (length > 9) {
					answer = LONG_NAME;
				} else {
					if (length > 4) {
						answer = INTEGER_NAME;
					} else
						answer = SHORT_NAME;
				}
			}
			break;
		default:
			answer = null;
		}

		return answer;
	}

	public static String typesToString(Integer types) {
		return (String) typesMapping.get(types);
	}
}
