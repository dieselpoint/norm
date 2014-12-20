package com.dieselpoint.norm.sqlmakers;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.LinkedHashMap;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.dieselpoint.norm.DbException;

/**
 * Provides means of reading and writing properties in a pojo.
 */
public class StandardPojoInfo implements PojoInfo {
	
	/*
	 * annotations recognized: @ Id, @ GeneratedValue @ Transient @ Table @ Column
	 */

	LinkedHashMap<String, Property> propertyMap = new LinkedHashMap<String, Property>();
	String table;
	String primaryKeyName;
	String generatedColumnName;
	
	String insertSql;
	int insertSqlArgCount;
	String [] insertColumnNames;
	
	String updateSql;
	String[] updateColumnNames;
	int updateSqlArgCount;
	
	String selectColumns;
	
	public static class Property {
		public String name;
		public Method readMethod;
		public Method writeMethod;
		public Field field;
		public Class<?> dataType;
		public boolean isGenerated;
		public boolean isPrimaryKey;
		public boolean isEnumField;
		public Class<Enum> enumClass;
	}

	public StandardPojoInfo(Class<?> clazz) {

		try {

			for (Field field : clazz.getFields()) {
				int modifiers = field.getModifiers();

				if (Modifier.isPublic(modifiers)) {

					if (Modifier.isStatic(modifiers)
							|| Modifier.isFinal(modifiers)) {
						continue;
					}

					if (field.getAnnotation(Transient.class) != null) {
						continue;
					}

					Property prop = new Property();
					prop.name = field.getName();
					prop.field = field;
					prop.dataType = field.getType();

					if (field.getAnnotation(Id.class) != null) {
						prop.isPrimaryKey = true;
						primaryKeyName = field.getName();
					}

					if (field.getAnnotation(GeneratedValue.class) != null) {
						generatedColumnName = field.getName();
						prop.isGenerated = true;
					}

					if (field.getType().isEnum()) {
						prop.isEnumField = true;
						prop.enumClass = (Class<Enum>) field.getType();
					}

					Column col = field.getAnnotation(Column.class);
					if (col != null) {
						String name = col.name().trim();
						if (name.length() > 0) {
							prop.name = name;
						}
					}

					propertyMap.put(prop.name, prop);
				}
			}

			BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
			PropertyDescriptor[] descriptors = beanInfo
					.getPropertyDescriptors();
			for (PropertyDescriptor descriptor : descriptors) {
				String name = descriptor.getName();
				Property pair = new Property();
				pair.name = name;
				pair.readMethod = getMethod(descriptor.getReadMethod(), name,
						pair);
				pair.writeMethod = getMethod(descriptor.getWriteMethod(), name,
						pair);
				pair.dataType = descriptor.getPropertyType();
				propertyMap.put(name, pair);
			}

			Table annot = (Table) clazz.getAnnotation(Table.class);
			if (annot != null) {
				table = annot.name();
			} else {
				table = clazz.getSimpleName();
			}

		} catch (Throwable t) {
			throw new DbException(t);
		}
	}

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

	public Object getValue(Object pojo, String name) {

		try {

			Property prop = propertyMap.get(name);
			if (prop == null) {
				throw new DbException("No such field: " + name);
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

		} catch (Throwable t) {
			throw new DbException(t);
		}

		return null;
	}	

	
	public void putValue(Object pojo, String name, Object value) {
		Property pair = propertyMap.get(name);
		if (pair == null) {
			throw new DbException("No such field: " + name);
		}

		try {

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
		} catch (Throwable t) {
			throw new DbException(t);
		}
	}

	
	private <T extends Enum<T>> Object getEnumConst(Class<T> enumType, String o) {
		return Enum.valueOf(enumType, o);
	}

	@Override
	public void populateGeneratedKey(ResultSet generatedKeys, Object insertRow) {

		try {

			//StandardPojoInfo pojoInfo = getPojoInfo(insertRow.getClass());
			Property prop = propertyMap.get(generatedColumnName);

			Object newKey;
			if (prop.dataType.isAssignableFrom(int.class)) {
				newKey = generatedKeys.getInt(1);
			} else {
				newKey = generatedKeys.getLong(1);
			}

			putValue(insertRow, generatedColumnName, newKey);

		} catch (Throwable t) {
			throw new DbException(t);
		}
	}
	
	
}
