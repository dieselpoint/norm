package com.dieselpoint.norm;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Table;

import org.junit.Test;

public class TestSelect {
	
	@SuppressWarnings("unchecked")
	@Test
	public void test() {
	
		Setup.setSysProperties();
		
		Database db = new Database();
		
		db.sql("drop table if exists selecttest").execute();
		
		db.createTable(Row.class);
		
		Row row = new Row();
		row.id = 99;
		row.name = "bob";
		db.insert(row);
		
		// primitive
		Long myId = db.sql("select id from primitivetest").first(Long.class);
		assertEquals(new Long(99), myId);
		
		// map
		Map<String, Object> myMap = db.table("selecttest").first(LinkedHashMap.class);
		assertEquals(99L, myMap.get("id"));
		assertEquals("bob", myMap.get("name"));
		
		// pojo
		Row myRow = db.first(Row.class);
		assertEquals("99bob", myRow.toString());
		
	}
	
	@Table(name="selecttest")
	public static class Row {
		@Column(unique=true)
		public long id;
		public String name; 
		public String toString() {
			return id + name;
		}
	}

}
