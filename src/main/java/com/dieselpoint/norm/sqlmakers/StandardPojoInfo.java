package com.dieselpoint.norm.sqlmakers;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.dieselpoint.norm.DbException;
import com.dieselpoint.norm.serialize.Serializer;
import com.dieselpoint.norm.serialize.SerializerClass;

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

	String upsertSql;
	int upsertSqlArgCount;
	String [] upsertColumnNames;
	
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
		public Column columnAnnotation;
		public Serializer serializer;
	}

	public StandardPojoInfo(Class<?> clazz) {

		try {
			
			if (Map.class.isAssignableFrom(clazz)) {
				//leave properties empty
			} else {
				populateProperties(clazz);
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
	
	
	
	private void populateProperties(Class<?> clazz) throws IntrospectionException, InstantiationException, IllegalAccessException {
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

				applyAnnotations(prop, field);

				propertyMap.put(prop.name, prop);
			}
		}

		BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
		PropertyDescriptor[] descriptors = beanInfo
				.getPropertyDescriptors();
		for (PropertyDescriptor descriptor : descriptors) {

			Method readMethod = descriptor.getReadMethod();
			if (readMethod == null) {
				continue;
			}
			if (readMethod.getAnnotation(Transient.class) != null) {
				continue;
			}
			
			Property prop = new Property();
			prop.name = descriptor.getName();
			prop.readMethod = readMethod;
			prop.writeMethod = descriptor.getWriteMethod();
			prop.dataType = descriptor.getPropertyType();

			applyAnnotations(prop, prop.readMethod);
			
			propertyMap.put(prop.name, prop);
		}
	}


	/**
	 * Apply the annotations on the field or getter method to the property.
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private void applyAnnotations(Property prop, AnnotatedElement ae) throws InstantiationException, IllegalAccessException {
		
		Column col = ae.getAnnotation(Column.class);
		if (col != null) {
			String name = col.name().trim();
			if (name.length() > 0) {
				prop.name = name;
			}
			prop.columnAnnotation = col;
		}
		
		if (ae.getAnnotation(Id.class) != null) {
			prop.isPrimaryKey = true;
			primaryKeyName = prop.name;
		}

		if (ae.getAnnotation(GeneratedValue.class) != null) {
			generatedColumnName = prop.name;
			prop.isGenerated = true;
		}

		if (prop.dataType.isEnum()) {
			prop.isEnumField = true;
			prop.enumClass = (Class<Enum>) prop.dataType;
		}
		
		SerializerClass sc = ae.getAnnotation(SerializerClass.class);
		if (sc != null) {
			prop.serializer = sc.value().newInstance();
		}
		
	}


/*
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
*/
	
	
	public Object getValue(Object pojo, String name) {

		try {

			Property prop = propertyMap.get(name);
			if (prop == null) {
				throw new DbException("No such field: " + name);
			}
			
			Object value = null;
			
			if (prop.readMethod != null) {
				value = prop.readMethod.invoke(pojo);
			
			} else if (prop.isEnumField) {
				// convert all enums to string
				Object o = prop.field.get(pojo);
				if (o != null) {
					value = o.toString();
				}
				
			} else if (prop.field != null) {
				value = prop.field.get(pojo);
			}
			
			if (prop.serializer != null) {
				value =  prop.serializer.serialize(value);
			}

			return value;

		} catch (Throwable t) {
			throw new DbException(t);
		}
	}	

	
	public void putValue(Object pojo, String name, Object value) {
		
		Property prop = propertyMap.get(name);
		if (prop == null) {
			throw new DbException("No such field: " + name);
		}

		try {

			if (prop.serializer != null) {
				value = prop.serializer.deserialize((String) value, prop.dataType);
			}
			
			if (prop.writeMethod != null) {
				prop.writeMethod.invoke(pojo, value);
				return;
			}

			if (prop.field != null) {
				Object val = value;

				if (prop.isEnumField) {
					// convert to enum const
					val = getEnumConst(prop.enumClass, value.toString());
				}

				prop.field.set(pojo, val);
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
