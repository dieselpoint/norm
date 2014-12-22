package com.dieselpoint.norm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;

import com.dieselpoint.norm.sqlmakers.MySqlMaker;

public class TestMySqlUpsert {
	
	static public void main(String [] args) throws SQLException, FileNotFoundException, IOException {
		
		Test.setSysProperties();
		
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
		
		System.out.println(list);
	}
	
	@Table(name="upserttest")
	public static class Row {
		@Column(unique=true)
		public long id;
		public String name; 
	}

}
