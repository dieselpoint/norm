package com.dieselpoint.norm.sqlmakers;



public interface PojoInfo {
	public Object getValue(Object pojo, String name);
	public void putValue(Object pojo, String name, Object value);
	public Property getGeneratedColumnProperty();

}
