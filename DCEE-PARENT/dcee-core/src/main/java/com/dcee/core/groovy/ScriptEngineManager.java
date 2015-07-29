package com.dcee.core.groovy;

import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import java.io.File;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ScriptEngineManager
  implements InitializingBean, ApplicationContextAware
{
  private static final Logger logger = LoggerFactory.getLogger(ScriptEngineManager.class);
  private static final int RECOMPILATION_INTERVAL_SECOND = 10;
  private GroovyScriptEngine groovyScriptEngine;
  private ApplicationContext applicationContext;
  
  public void afterPropertiesSet()
    throws Exception
  {
    Map<String, GroovyResourceConnector> connectors = this.applicationContext.getBeansOfType(GroovyResourceConnector.class);
    
    CompositeResourceConnector compositeResourceConnector = new CompositeResourceConnector(connectors.values());
    
    File tempDir = new File(System.getProperty("java.io.tmpdir"));
    File dataModelSrc = new File(tempDir, "datamodel");
    if (!dataModelSrc.exists())
    {
      dataModelSrc.mkdirs();
      logger.debug("��������������������:{}", dataModelSrc.getAbsolutePath());
    }
    dataModelSrc.deleteOnExit();
    
    compositeResourceConnector.setDataModelAbsPath(dataModelSrc);
    this.groovyScriptEngine = new GroovyScriptEngine(compositeResourceConnector);
  }
  
  public Class<?> getClass(String className)
    throws ClassNotFoundException
  {
    try
    {
      className = className.replace('.', File.separatorChar) + ".Groovy";
      return this.groovyScriptEngine.loadScriptByName(className);
    }
    catch (ResourceException e)
    {
      throw new ClassNotFoundException(className, e);
    }
    catch (ScriptException e)
    {
      throw new ClassNotFoundException(className, e);
    }
  }
  
  public ClassLoader getClassLoader()
  {
    return this.groovyScriptEngine.getGroovyClassLoader();
  }
  
  public void setApplicationContext(ApplicationContext applicationContext)
    throws BeansException
  {
    this.applicationContext = applicationContext;
  }
}
