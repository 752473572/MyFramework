package com.dcee.core.util.beanutils;

import com.thoughtworks.xstream.converters.ConversionException;
import java.util.Date;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.time.DateUtils;

public class DateConverter implements Converter {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object convert(Class type, Object value) {
		if (value == null) {
			return null;
		}
		if ((value instanceof Date)) {
			return value;
		}

		if ((value instanceof Long)) {
			Long longValue = (Long) value;
			return new Date(longValue.longValue());
		}
		try {
			return DateUtils.parseDate(value.toString(), new String[] { "yyyy-MM-dd HH:mm:ss.SSS",
					"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd" });
		} catch (Exception e) {
			throw new ConversionException(e);
		}
	}
}
