package com.dieselpoint.norm.converter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class StringToIntListConverter implements AttributeConverter<List<Integer>, String> {

	@Override
	public String convertToDatabaseColumn(List<Integer> attribute) {
		if (attribute == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		int len = attribute.size();

		for (int i = 0; i < len; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(attribute.get(i).intValue());

		}
		return sb.toString();
	}

	@Override
	public List<Integer> convertToEntityAttribute(String in) {
		// deserialize string in the form "123,456" no spaces allowed
		List<Integer> list = new ArrayList<>();
		if (in == null || in.length() == 0) {
			return list;
		}

		int value = 0;
		for (int i = 0; i < in.length(); i++) {
			int digit = in.charAt(i) - '0';
			if (digit >= 0 && digit <= 9) {
				value = (value * 10) + digit;
			} else {
				// hit a comma
				list.add(value);
				value = 0;
			}
		}
		list.add(value);
		return list;
	}

}
