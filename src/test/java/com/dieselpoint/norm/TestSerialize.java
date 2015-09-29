package com.dieselpoint.norm;

import java.util.List;

import org.junit.Test;

import com.dieselpoint.norm.serialize.DbSerializable;
import com.dieselpoint.norm.serialize.DbSerializer;

public class TestSerialize {

	@Test
	public void test() {

	}

	class MyPojo {
		@DbSerializer(MySerializer.class)
		public List<String> myList;
	}

	class MySerializer implements DbSerializable {

		@Override
		public String serialize(Object in) {
			return in.toString();
		}

		@Override
		public Object deserialize(String in, Class<?> targetClass) {
			Object out = null; // convert the string back to a list here
			return out;
		}
	}

}
