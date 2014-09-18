package com.dieselpoint.norm2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

public class Test {
	
	static public void main(String [] args) throws SQLException, FileNotFoundException, IOException {
		
		setSysProps();
		
		Database db = new Database();
		
		/* test straight sql */
		db.sql("drop table if exists names").execute();
		
		/* test create table */
		db.createTable(Name.class);
		
		/* test inserts */
		Name john = new Name("John", "Doe");
		db.insert(john); 
		
		Name bill = new Name("Bill", "Smith");
		db.insert(bill);
		
		/* test where clause, also id and generated values */
		List<Name> list = db.where("firstName=?", "John").results(Name.class);
		dump("john only:", list);
		
		/* test delete single record */
		db.delete(john);
		List<Name> list1 = db.results(Name.class);
		dump("bill only:", list1);
		
		/* test update single record */
		bill.firstName = "Joe";
		int rowsAffected = db.update(bill).getRowsAffected();
		List<Name> list2 = db.results(Name.class);
		dump("bill is now joe, and rowsAffected=" + rowsAffected, list2);
		
		/* test delete with where clause */
		db.table("names").where("firstName=?", "Joe").delete();
		
		/* test using a map for results instead of a pojo */
		Map map = db.sql("select count(*) as count from names").first(HashMap.class);
		System.out.println("Num records (should be 0):" + map.get("count"));
		
		/* test transactions */
		db.insert(new Name("Fred", "Jones"));
		Transaction trans = db.startTransaction();
		db.transaction(trans).insert(new Name("Sam", "Williams"));
		db.transaction(trans).insert(new Name("George ", "Johnson"));
		trans.rollback();
		List<Name> list3 = db.results(Name.class);
		dump("fred only:", list3);
		
		db.sql("drop table names").execute();
	}
	
	
	private static void dump(String label, List<Name> list) {
		System.out.println(label);
		for (Name n: list) {
			System.out.println(n.toString());
		}
	}


	@Table(name="names")
	static public class Name {
		
		// must have 0-arg constructor
		public Name() {} 
		
		// can also have convenience constructor
		public Name(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}
		
		// primary key, generated on the server side
		@Id
		@GeneratedValue 
		public long id;
		
		// a public property without getter or setter
		public String firstName; 
		
		// a private property with getter and setter below
		private String lastName; 
		
		public String getLastName() {
			return lastName;
		}
		
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
		
		public String toString() {
			return id + " " + firstName + " " + lastName;
		}
	}
	
	private static void setSysProps() throws FileNotFoundException, IOException {
		Properties p = new Properties();
	    p.load(new FileInputStream("./etc/dbproperties.txt"));
	    for (String name : p.stringPropertyNames()) {
	        String value = p.getProperty(name);
	        System.setProperty(name, value);
	    }
	}


	/*
	 * to bypass the hikari code, use this in Database.getDataSource():
	com.mysql.jdbc.jdbc2.optional.MysqlDataSource ds = new com.mysql.jdbc.jdbc2.optional.MysqlDataSource();
	ds.setServerName(System.getProperty("norm.serverName"));
	ds.setDatabaseName(System.getProperty("norm.databaseName"));
	ds.setUser(System.getProperty("norm.user"));
	ds.setPassword(System.getProperty("norm.password"));
	return ds;
	*/
	
	
}
