package com.dieselpoint.norm.sqlmakers;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.dieselpoint.norm.ColumnOrder;
import com.dieselpoint.norm.DbException;
import com.dieselpoint.norm.serialize.DbSerializer;

/**
 * Provides means of reading and writing properties in a pojo.
 */
@SuppressWarnings("rawtypes")
public class StandardPojoInfo implements PojoInfo {
	
	/*
	 * annotations recognized: @ Id, @ GeneratedValue @ Transient @ Table @ Column @ DbSerializer @ Enumerated
	 */

	// these are public to make subclassing easier
	public LinkedHashMap<String, Property> propertyMap = new LinkedHashMap<String, Property>();
	public String table;
	public String primaryKeyName;
	public String generatedColumnName;
	
	public String insertSql;
	public int insertSqlArgCount;
	public String [] insertColumnNames;

	public String upsertSql;
	public int upsertSqlArgCount;
	public String [] upsertColumnNames;
	
	public String updateSql;
	public String[] updateColumnNames;
	public int updateSqlArgCount;
	
	public String selectColumns;

	public StandardPojoInfo(Class<?> clazz) {

		try {
			
			if (Map.class.isAssignableFrom(clazz)) {
				//leave properties empty
			} else {
				List<Property> props = populateProperties(clazz);
				
				ColumnOrder colOrder = clazz.getAnnotation(ColumnOrder.class);
				if (colOrder != null) {
					// reorder the properties
					String [] cols = colOrder.value();
					List<Property> reordered = new ArrayList<>();
					for (int i = 0; i < cols.length; i++) {
						for (Property prop: props) {
							if (prop.name.equals(cols[i])) {
								reordered.add(prop);
								break;
							}
						}
					}
					// props not in the cols list are ignored
					props = reordered;
				}
				
				for (Property prop: props) {
					propertyMap.put(prop.name, prop);
				}
			}
			
			Table annot = clazz.getAnnotation(Table.class);
			if (annot != null) {
				if (annot.schema() != null && !annot.schema().isEmpty()) {
					table = annot.schema() + "." + annot.name();
				}
				else {
					table = annot.name();
				}
			} else {
				table = clazz.getSimpleName();
			}

		} catch (Throwable t) {
			throw new DbException(t);
		}
	}
	
	
	
	private List<Property> populateProperties(Class<?> clazz) throws IntrospectionException, InstantiationException, IllegalAccessException {
		
		List<Property> props = new ArrayList<>();
		
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

				props.add(prop);
			}
		}

		BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
		PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
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

			props.add(prop);
		}
		
		return props;
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
			/* We default to STRING enum type. Can be overriden with @Enumerated annotation */
			prop.enumType = EnumType.STRING;
			if (ae.getAnnotation(Enumerated.class) != null) {
				prop.enumType = ae.getAnnotation(Enumerated.class).value();
			}
		}
		
		DbSerializer sc = ae.getAnnotation(DbSerializer.class);
		if (sc != null) {
			prop.serializer = sc.value().newInstance();
		}
		
	}

	
	public Object getValue(Object pojo, String name) {

		try {

			Property prop = propertyMap.get(name);
			if (prop == null) {
				throw new DbException("No such field: " + name);
			}
			
			Object value = null;
			
			if (prop.readMethod != null) {
				value = prop.readMethod.invoke(pojo);
				
			} else if (prop.field != null) {
				value = prop.field.get(pojo);
			}
			
			if (value != null) {
				if (prop.serializer != null) {
					value =  prop.serializer.serialize(value);
				
				} else if (prop.isEnumField) {
					// handle enums according to selected enum type
					if (prop.enumType == EnumType.ORDINAL) {
						value = ((Enum) value).ordinal();
					}
					// EnumType.STRING and others (if present in the future)
					else {
						value = value.toString();
					}					
				}	
			}

			return value;

		} catch (Throwable t) {
			throw new DbException(t);
		}
	}	

	public void putValue(Object pojo, String name, Object value) {
		putValue(pojo, name, value, false);
	}

	public void putValue(Object pojo, String name, Object value, boolean ignoreIfMissing) {

		Property prop = propertyMap.get(name);
		if (prop == null) {
			if (ignoreIfMissing) {
				return;
			} 
			throw new DbException("No such field: " + name);
		}

		if (value != null) {
			if (prop.serializer != null) {
				value = prop.serializer.deserialize((String) value, prop.dataType);

			} else if (prop.isEnumField) {
				value = getEnumConst(prop.enumClass, prop.enumType, value);
			}
		}

		if (prop.writeMethod != null) {
			try {
				prop.writeMethod.invoke(pojo, value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new DbException("Could not write value into pojo. Property: " + prop.name + " method: "
						+ prop.writeMethod.toString() + " value: " + value + " value class: " + value.getClass().toString(), e);
			}
			return;
		}

		if (prop.field != null) {
			try {
				prop.field.set(pojo, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new DbException("Could not set value into pojo. Field: " + prop.field.toString() + " value: " + value, e);
			}
			return;
		}

	}

	/**
	 * Convert a string to an enum const of the appropriate class.
	 */
	private <T extends Enum<T>> Object getEnumConst(Class<T> enumType, EnumType type, Object value) {
		String str = value.toString();
		if (type == EnumType.ORDINAL) {
			Integer ordinalValue = (Integer) value;
			if (ordinalValue < 0 || ordinalValue >= enumType.getEnumConstants().length) {
				throw new DbException("Invalid ordinal number " + ordinalValue + " for enum class " + enumType.getCanonicalName());
			}
			return enumType.getEnumConstants()[ordinalValue];
		}
		else {		
			for (T e: enumType.getEnumConstants()) {
				if (str.equals(e.toString())) {
					return e;
				}
			}
			throw new DbException("Enum value does not exist. value:" + str);
		}
	}



	@Override
	public Property getGeneratedColumnProperty() {
		return propertyMap.get(generatedColumnName);
	}



	@Override
	public Property getProperty(String name) {
		return propertyMap.get(name);
	}
	
	
	


	
	
}
