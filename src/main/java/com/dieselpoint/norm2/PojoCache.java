package com.dieselpoint.norm2;

import java.beans.IntrospectionException;
import java.util.concurrent.ConcurrentHashMap;

public class PojoCache {
	
	private static ConcurrentHashMap<Class, PojoInfo> map = new ConcurrentHashMap();
	
	public static PojoInfo getPojoInfo(Class clazz) throws IntrospectionException {
		PojoInfo pi = map.get(clazz);
		if (pi == null) {
			pi = new PojoInfo(clazz);
			map.put(clazz, pi);
		}
		return pi;
	}

}
