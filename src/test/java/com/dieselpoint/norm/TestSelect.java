package com.dieselpoint.norm;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
		
		Row firstRow = new Row(99, "bob");
		db.insert(firstRow);
		
		// primitive
		Long myId = db.sql("select id from selecttest").first(Long.class);
		assertEquals(new Long(99), myId);
		
		// map
		Map<String, Object> myMap = db.table("selecttest").first(LinkedHashMap.class);
		assertEquals(99L, myMap.get("id"));
		assertEquals("bob", myMap.get("name"));
		
		// pojo
		Row myRow = db.first(Row.class);
		assertEquals("99bob", myRow.toString());
		
		Row secondRow = new Row(100, "ant");
		db.insert(secondRow);
		
		List<HashMap> results = db.table("selecttest").orderBy("id", "desc").results(HashMap.class);
		assertEquals(100L, results.get(0).get("id"));
		assertEquals("ant", results.get(0).get("name"));
		assertEquals(99L, results.get(1).get("id"));
		assertEquals("bob", results.get(1).get("name"));
	}
	
	@Table(name="selecttest")
	public static class Row {
		@Column(unique=true)
		public long id;
		public String name; 
		public Row() {
		}
		public Row(long id, String name) {
			this.id = id;
			this.name = name;
		}
		public String toString() {
			return id + name;
		}
	}

}
