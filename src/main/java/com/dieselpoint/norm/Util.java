package com.dieselpoint.norm;

import java.util.List;

public class Util {

	public static String join(String [] strs) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < strs.length; i++) {
			if (i > 0) {
				buf.append(",");
			}
			buf.append(strs[i]);
		}
		return buf.toString();
	}
	
	
	public static String join(List<String> strs) {
		StringBuilder buf = new StringBuilder();
		int len = strs.size();
		for (int i = 0; i < len; i++) {
			if (i > 0) {
				buf.append(",");
			}
			buf.append(strs.get(i));
		}
		return buf.toString();
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
	
	public static boolean isPrimitiveOrString(Class c) {
		if (c.isPrimitive()) {
			return true;
		} else if (c == Byte.class 
				|| c == Short.class 
				|| c == Integer.class
				|| c == Long.class 
				|| c == Float.class 
				|| c == Double.class
				|| c == Boolean.class 
				|| c == Character.class
				|| c == String.class) {
			return true;
		} else {
			return false;
		}
	}
}
