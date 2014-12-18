package com.dieselpoint.norm;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Provides means of reading and writing properties in a pojo.
 */
public class PojoInfo {
	
	/*
	 * annotations recognized: @ Id, @ GeneratedValue @ transient @ table
	 */

	private LinkedHashMap<String, Property> propertyMap = new LinkedHashMap<String, Property>();
	private String table;
	private String primaryKeyName;
	private String generatedColumnName;
	//private String [] columns;
	//private String commaColumns;
	
	private String insertSql;
	private int insertSqlArgCount;
	private String [] insertColumnNames;
	private String selectColumns;
	private String updateSql;
	private String[] updateColumnNames;
	private int updateSqlArgCount;
	
	static class Property {
		String name;
		Method readMethod;
		Method writeMethod;
		Field field;
		Class<?> dataType;
		boolean isGenerated;
		boolean isPrimaryKey;
		boolean isEnumField;
		Class<Enum> enumClass;
	}

	public PojoInfo(Class<?> clazz) throws IntrospectionException {
	    
	    for (Field field: clazz.getFields()) {
	    	int modifiers = field.getModifiers();
	    	
	    	if (Modifier.isPublic(modifiers)) {
	    		
	    		if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
	    			continue;
	    		}
	    		
	    		if (field.getAnnotation(Transient.class) != null) {
	    			continue;
	    		}
	    		
	    		Property pair = new Property();
	            pair.name = field.getName();
	            pair.field = field;
	            pair.dataType = field.getType();
	    		
	    		if (field.getAnnotation(Id.class) != null) {
	    			pair.isPrimaryKey = true;
	    			primaryKeyName = field.getName();
	    		}
	    		
	    		if (field.getAnnotation(GeneratedValue.class) != null) {
	    			generatedColumnName = field.getName();
	    			pair.isGenerated = true;
	    		}
	            
	    		if (field.getType().isEnum()) {
	    			pair.isEnumField = true;
	    			pair.enumClass = (Class<Enum>) field.getType();
	    		}
	    		
	            propertyMap.put(pair.name, pair);
	    	}
	    }

		BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
	    PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
		for (PropertyDescriptor descriptor : descriptors) {
            String name = descriptor.getName();
            Property pair = new Property();
            pair.name = name;
            pair.readMethod = getMethod(descriptor.getReadMethod(), name, pair);
            pair.writeMethod = getMethod(descriptor.getWriteMethod(), name, pair);
            pair.dataType = descriptor.getPropertyType();
            propertyMap.put(name, pair);
		}
		
		Table annot = (Table) clazz.getAnnotation(Table.class);
		if (annot != null) {
			table = annot.name();
		} else {
			table = clazz.getSimpleName();
		}
		
		makeInsertSql();
		makeUpdateSql();
		makeSelectColumns();
	}
	
	/*
	private void makeColumns() {
		columns = new String [map.size()];
		map.keySet().toArray(columns);
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < columns.length; i++) {
			if (buf.length() > 0) {
				buf.append(',');
			}
			buf.append(columns[i]);
		}
		commaColumns = buf.toString();
	}
	*/

	private Method getMethod(Method meth, String propertyName, Property pair) {
		if (meth == null) {
			return null;
		}
		if (meth.getAnnotation(Transient.class) != null) {
			return null;
		}
		if (meth.getAnnotation(Id.class) != null) {
			this.primaryKeyName = propertyName;
			pair.isPrimaryKey = true;
		}
		if (meth.getAnnotation(GeneratedValue.class) != null) {
			this.generatedColumnName = propertyName;
			pair.isGenerated = true;
		}
		return meth;
	}

	public Object getValue(Object pojo, String name) throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Property prop = propertyMap.get(name);
		if (prop == null) {
			throw new NoSuchFieldException(name);
		}
		
		if (prop.readMethod != null) {
			return prop.readMethod.invoke(pojo);
		}
		
		if (prop.isEnumField) {
			// convert all enums to string
			Object o = prop.field.get(pojo);
			if (o == null) {
				return null;
			} else {
				return o.toString();
			}
		}
		
		if (prop.field != null) {
			return prop.field.get(pojo);
		}
		return null;
	}
	
	
	public void putValue(Object pojo, String name, Object value) throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Property pair = propertyMap.get(name);
		if (pair == null) {
			throw new NoSuchFieldException(name);
		}
		
		if (pair.writeMethod != null) {
			pair.writeMethod.invoke(pojo, value);
			return;
		}
		
		if (pair.field != null) {
			Object val = value;
			
			if (pair.isEnumField) {
				// convert to enum const
				val = getEnumConst(pair.enumClass, value.toString());
			}
			
			pair.field.set(pojo, val);
			return;
		}
	}

	private <T extends Enum<T>> Object getEnumConst(Class<T> enumType, String o) {
		return Enum.valueOf(enumType, o);
	}

	public String getTable() {
		return table;
	}

	public Collection<Property> getProperties() {
		return propertyMap.values();
	}
	
	public Property getProperty(String columnName) {
		return propertyMap.get(columnName);
	}

	public String getPrimaryKeyName() {
		return primaryKeyName;
	}

	//public String[] getColumns() {
	//	return columns;
	//}

	//public String getCommaColumns() {
	//	return commaColumns;
	//}

	public String getGeneratedColumnName() {
		return generatedColumnName;
	}

	private void makeInsertSql() {
		
		ArrayList<String> cols = new ArrayList<String>();
		for (Property prop: propertyMap.values()) {
			if (prop.isGenerated) {
				continue;
			}
			cols.add(prop.name);
		}
		insertColumnNames = cols.toArray(new String [cols.size()]);
		insertSqlArgCount = insertColumnNames.length;
		
		StringBuilder buf = new StringBuilder();
		buf.append("insert into ");
		buf.append(getTable());
		buf.append(" (");
		buf.append(Util.join(insertColumnNames)); // comma sep list?
		buf.append(") values (");
		buf.append(Util.getQuestionMarks(insertSqlArgCount));
		buf.append(")");
		
		insertSql = buf.toString();
	}
	
	private void makeUpdateSql() {
		
		ArrayList<String> cols = new ArrayList<String>();
		for (Property prop: propertyMap.values()) {
			
			if (prop.isPrimaryKey) {
				continue;
			}
			
			if (prop.isGenerated) {
				continue;
			}
			
			cols.add(prop.name);
		}
		updateColumnNames = cols.toArray(new String [cols.size()]);
		updateSqlArgCount = updateColumnNames.length + 1; // + 1 for the where arg
		
		StringBuilder buf = new StringBuilder();
		buf.append("update ");
		buf.append(getTable());
		buf.append(" set ");

		for (int i = 0; i < cols.size(); i++) {
			if (i > 0) {
				buf.append(',');
			}
			buf.append(cols.get(i) + "=?");
		}
		buf.append(" where " + primaryKeyName + "=?");
		
		updateSql = buf.toString();
	}


	public Object[] getInsertArgs(Object row) throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object [] args = new Object[insertSqlArgCount];
		for (int i = 0; i < insertSqlArgCount; i++) {
			args[i] = getValue(row, insertColumnNames[i]);
		}
		return args;
	}

	public Object[] getUpdateArgs(Object row) throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object [] args = new Object[updateSqlArgCount];
		for (int i = 0; i < updateSqlArgCount - 1; i++) {
			args[i] = getValue(row, updateColumnNames[i]);
		}
		// add the value for the where clause to the end
		Object pk = getValue(row, primaryKeyName);
		args[updateSqlArgCount - 1] = pk;
		return args;
	}
	
	
	public String getInsertSql() {
		return insertSql;
	}

	private void makeSelectColumns() {
		ArrayList<String> cols = new ArrayList<String>();
		for (Property prop: propertyMap.values()) {
			cols.add(prop.name);
		}
		selectColumns = Util.join(cols);
	}
	
	
	public String getSelectColumns() {
		return selectColumns;
	}

	public String getUpdateSql() {
		return updateSql;
	}

}
