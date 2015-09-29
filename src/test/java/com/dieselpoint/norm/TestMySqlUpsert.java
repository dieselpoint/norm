package com.dieselpoint.norm;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;

import org.junit.Test;

import com.dieselpoint.norm.sqlmakers.MySqlMaker;

public class TestMySqlUpsert {
	
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
		
		String listStr = list.toString();
		if (!listStr.equals("[{name=Fred, id=1}]")) {
			fail();
		}
	}
	
	@Table(name="upserttest")
	public static class Row {
		@Column(unique=true)
		public long id;
		public String name; 
	}

}
