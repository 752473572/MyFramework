package com.dcee.core.data.model;

/**
 * 数据库操作类型
 */
public enum UpdateFlag
{
  Unchanged,  Updated,  Appended,  Removed;
  
  private UpdateFlag() {}
}