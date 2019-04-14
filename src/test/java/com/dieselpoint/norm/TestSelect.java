package com.dieselpoint.norm;

import static org.junit.Assert.fail;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Table;

import org.junit.Ignore;
import org.junit.Test;

public class TestSelect {
	@Ignore
	@Test
	public void test() {
	
	//	Setup.setSysProperties();
		
		Database db = new Database();
		db.setJdbcUrl("jdbc:sqlite:/home/ghost/IdeaProjects/norm/norm/test.sqlite3");
		
		db.sql("drop table if exists selecttest").execute();
		
		db.createTable(Row.class);
		
		Row row = new Row();
		row.id = 99;
		row.name = "bob";
		db.insert(row);
		
		// primitive
		Long myId = db.sql("select id from selecttest").first(Long.class);
		if (myId != 99) {
			fail();
		}
		
		// map
		Map myMap = db.table("selecttest").first(LinkedHashMap.class);
		String str = myMap.toString();
		if (!str.equals("{id=99, name=bob}")) {
			fail();
		}
		
		// pojo
		Row myRow = db.first(Row.class);
		String myRowStr = myRow.toString();
		if (!myRowStr.equals("99bob")) {
			fail();
		}
		
	}

	@Test
	public void selectTest() {
		Database db = new Database();
		db.setJdbcUrl("jdbc:sqlite:/home/ghost/IdeaProjects/norm/norm/test.sqlite3");

		db.joinTable("rowtest", "rowtest_id = rowtest,id");
		db.where("name = ?", "nick");
		List<Row> results = db.results(Row.class);
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
