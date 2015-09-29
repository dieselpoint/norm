package com.dieselpoint.norm.serialize;


/**
 * Serializes a class to and from a string.
 * Implementations must have a zero-arg constructor and must
 * be thread-safe.
 */
public interface DbSerializable {
	
	public String serialize(Object in);
	public Object deserialize(String in, Class<?> targetClass);

}
