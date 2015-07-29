package com.dcee.core.data.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义字段绑定的DTO名称
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormFieldsFactory {

	String factoryType();
	
	String factoryParam()default"";
	
	
}
