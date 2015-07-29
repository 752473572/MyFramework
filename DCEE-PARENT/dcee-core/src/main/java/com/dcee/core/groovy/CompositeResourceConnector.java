package com.dcee.core.groovy;

import groovy.util.ResourceConnector;
import groovy.util.ResourceException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dcee.core.data.dao.DaoModelManager;

public class CompositeResourceConnector
  implements ResourceConnector
{
  private static final Logger logger = LoggerFactory.getLogger(DaoModelManager.class);
  private String dataModelAbsPath;
  private File dataModelSrc;
  private Collection<GroovyResourceConnector> connectors;
  
  public String getDataModelAbsPath()
  {
    return this.dataModelAbsPath;
  }
  
  public void setDataModelAbsPath(File dataModelSrc)
  {
    this.dataModelSrc = dataModelSrc;
    try
    {
      this.dataModelAbsPath = dataModelSrc.getAbsoluteFile().toURL().getPath();
    }
    catch (MalformedURLException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public CompositeResourceConnector(Collection<GroovyResourceConnector> connectors)
  {
    this.connectors = connectors;
  }
  
  public URLConnection getResourceConnection(String name)
    throws ResourceException
  {
    System.out.println("check:" + name);
    if ((name.startsWith("java")) || (name.startsWith("groovy"))) {
      throw new ResourceException("No resource for " + name + " was found");
    }
    String relativePath = name;
    if (name.startsWith(this.dataModelAbsPath)) {
      relativePath = name.substring(this.dataModelAbsPath.length());
    }
    String path = relativePath.substring(0, relativePath.lastIndexOf("."));
    String modelId = path.replace(File.separatorChar, '.').replace('/', '.');
    
    File classFile = new File(this.dataModelSrc, relativePath);
    for (GroovyResourceConnector c : this.connectors) {
      try
      {
        c.updateClassFile(classFile, modelId, this);
        return classFile.toURI().toURL().openConnection();
      }
      catch (ClassNotFoundException e) {}catch (MalformedURLException e)
      {
        throw new ResourceException("No resource for " + name + " was found", e);
      }
      catch (IOException e)
      {
        throw new ResourceException("No resource for " + name + " was found", e);
      }
    }
    throw new ResourceException("No resource for " + name + " was found");
  }
  
  public void addScript(String className, String source)
  {
    File file = new File(this.dataModelSrc, className.replace('.', File.separatorChar) + ".Groovy");
    
    File dir = file.getParentFile();
    if (!dir.exists()) {
      dir.mkdirs();
    }
    FileOutputStream output = null;
    try
    {
      output = new FileOutputStream(file);
      IOUtils.write(source, output);
    }
    catch (IOException e)
    {
      throw new RuntimeException("addScript [" + className + "] IOException", e);
    }
    finally
    {
      IOUtils.closeQuietly(output);
    }
  }
}
