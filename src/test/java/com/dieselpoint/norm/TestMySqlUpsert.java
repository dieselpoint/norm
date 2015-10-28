package com.dieselpoint.norm;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;

import org.junit.Test;

import com.dieselpoint.norm.sqlmakers.MySqlMaker;

public class TestMySqlUpsert {
	
	@SuppressWarnings("rawtypes")
	@Test
	public void test() {
	
		Setup.setSysProperties();
		
		Database db = new Database();
		db.setSqlMaker(new MySqlMaker());
		
		db.sql("drop table if exists upserttest").execute();
		
		db.createTable(Row.class);
		
		Row row = new Row();
		row.id = 1;
		row.name = "bob";
		db.upsert(row);
		
		row.name = "Fred";
		db.upsert(row);
		
		List<HashMap> list = db.table("upserttest").results(HashMap.class);
		assertEquals(1L, list.get(0).get("id"));
		assertEquals("Fred", list.get(0).get("name"));
		
	}
	
	@Table(name="upserttest")
	public static class Row {
		@Column(unique=true)
		public long id;
		public String name; 
	}

}
