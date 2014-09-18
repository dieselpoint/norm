package com.dieselpoint.norm2;

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

	
}
