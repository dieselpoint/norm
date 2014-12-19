package com.dieselpoint.norm.sqlmakers;

import java.sql.ResultSet;

import com.dieselpoint.norm.Query;



public interface SqlMaker {
	
	public String getInsertSql(Query query, Object row);
	public Object[] getInsertArgs(Query query, Object row);

	public String getUpdateSql(Query query, Object row);
	public Object[] getUpdateArgs(Query query, Object row);
	
	public String getDeleteSql(Query query, Object row);
	public Object[] getDeleteArgs(Query query, Object row);

	public String getSelectSql(Query query, Class rowClass);
	public String getCreateTableSql(Class<?> clazz);
	
	public void putValue(Object pojo, String name, Object value);
	
	public void populateGeneratedKey(ResultSet generatedKeys, Object insertRow);
	
}
