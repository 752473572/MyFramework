package com.dcee.core.generator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.ui.freemarker.SpringTemplateLoader;

import com.dcee.core.data.model.DataModel;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class Generator implements ApplicationContextAware {
	public static String PWP_ENCODING = "UTF-8";
	public static String TEMPLATE_DIR = "classpath:/conf/template";
	private static String PO_TEMPLATE_FILE = "po.ftl";
	private static String PK_TEMPLATE_FILE = "pk.ftl";
	private static String MAPPING_TEMPLATE_FILE = "mapping.ftl";
	private Configuration configuration;

	public String generateMapping(DataModel dataModel) throws IOException, TemplateException {
		TemplateModel templateModel = TemplateModel.createByDataModel(dataModel);

		return generate(MAPPING_TEMPLATE_FILE, templateModel);
	}

	public List<String> generateScript(DataModel dataModel) throws IOException, TemplateException {
		List<String> scripts = new ArrayList<String>();

		TemplateModel templateModel = TemplateModel.createByDataModel(dataModel);
		if (templateModel.getIsMultiplePK()) {
			String generatePk = generate(PK_TEMPLATE_FILE, templateModel);
			scripts.add(generatePk);
		}
		String generate = generate(PO_TEMPLATE_FILE, templateModel);
		scripts.add(generate);

		return scripts;
	}

	private String generate(String templatesFile, Object dataModel) throws IOException, TemplateException {
		Template template = this.configuration.getTemplate(templatesFile, PWP_ENCODING);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		OutputStreamWriter out = new OutputStreamWriter(os, PWP_ENCODING);
		template.process(dataModel, out);
		String script = os.toString();
		out.close();
		return script;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.configuration = new Configuration();
		SpringTemplateLoader stringTemplateLoader = new SpringTemplateLoader(applicationContext, TEMPLATE_DIR);

		this.configuration.setTemplateLoader(stringTemplateLoader);
		this.configuration.setObjectWrapper(new DefaultObjectWrapper());
	}
}
