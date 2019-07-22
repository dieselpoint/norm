package com.dieselpoint.norm.sqlmakers;



public interface PojoInfo {
	public Object getValue(Object pojo, String name);
	public void putValue(Object pojo, String name, Object value);
	public void putValue(Object pojo, String name, Object value, boolean ignoreIfMissing);
	public Property getGeneratedColumnProperty();
	public Property getProperty(String name);

}
