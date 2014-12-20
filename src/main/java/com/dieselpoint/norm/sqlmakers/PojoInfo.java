package com.dieselpoint.norm.sqlmakers;

import java.sql.ResultSet;


public interface PojoInfo {
	public Object getValue(Object pojo, String name);
	public void putValue(Object pojo, String name, Object value);
	public void populateGeneratedKey(ResultSet generatedKeys, Object insertRow);
}
