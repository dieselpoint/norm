package com.dieselpoint.norm;

import java.util.Collection;

public class Util {

	public static String join(String[] strs) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < strs.length; i++) {
			if (i > 0) {
				buf.append(",");
			}
			buf.append(strs[i]);
		}
		return buf.toString();
	}

	public static String join(Collection<String> strs) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String col : strs) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(col);
		}
		return sb.toString();
	}

	public static String getQuestionMarks(int count) {
		StringBuilder sb = new StringBuilder(count * 2);
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append('?');
		}
		return sb.toString();
	}

	public static boolean isPrimitiveOrString(Class<?> c) {
		if (c.isPrimitive()) {
			return true;
		} else if (c == Byte.class || c == Short.class || c == Integer.class || c == Long.class || c == Float.class
				|| c == Double.class || c == Boolean.class || c == Character.class || c == String.class) {
			return true;
		} else {
			return false;
		}
	}

	public static Class<?> wrap(Class<?> type) {
		if (!type.isPrimitive()) {
			return type;
		}
		if (type == int.class) {
			return Integer.class;
		}
		if (type == long.class) {
			return Long.class;
		}
		if (type == boolean.class) {
			return Boolean.class;
		}
		if (type == byte.class) {
			return Byte.class;
		}
		if (type == char.class) {
			return Character.class;
		}
		if (type == double.class) {
			return Double.class;
		}
		if (type == float.class) {
			return Float.class;
		}
		if (type == short.class) {
			return Short.class;
		}
		if (type == void.class) {
			return Void.class;
		}
		throw new RuntimeException("Will never get here");
	}
}
