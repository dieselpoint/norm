package com.dieselpoint.norm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

public class SampleCode {
	
	static public void main(String [] args) throws SQLException, FileNotFoundException, IOException {
		
		Setup.setSysProperties();
		
		Database db = new Database();
		
		// db.setSqlMaker(new PostgresMaker()); // set this to match your sql flavor		
		
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
		List<Name> list1 = db.orderBy("lastName").results(Name.class);
		dump("bill only:", list1);
		
		/* test update single record */
		bill.firstName = "Joe";
		int rowsAffected = db.update(bill).getRowsAffected();
		List<Name> list2 = db.results(Name.class);
		dump("bill is now joe, and rowsAffected=" + rowsAffected, list2);
		
		/* test using a map for results instead of a pojo */
		Map map = db.sql("select count(*) as count from names").first(HashMap.class);
		System.out.println("Num records (should be 1):" + map.get("count"));
		
		/* test using a primitive for results instead of a pojo */
		Long count = db.sql("select count(*) as count from names").first(Long.class);
		System.out.println("Num records (should be 1):" + count);
		
		/* test delete with where clause */
		db.table("names").where("firstName=?", "Joe").delete();

		/* make sure the delete happened */
		count = db.sql("select count(*) as count from names").first(Long.class);
		System.out.println("Num records (should be 0):" + count);
		
		/* test transactions */
		db.insert(new Name("Fred", "Jones"));
		Transaction trans = db.startTransaction();
		db.transaction(trans).insert(new Name("Sam", "Williams"));
		db.transaction(trans).insert(new Name("George ", "Johnson"));
		trans.rollback();
		List<Name> list3 = db.results(Name.class);
		dump("fred only:", list3);
		
		//db.sql("drop table names").execute();
	}

	
	
	public static void dump(String label, List<Name> list) {
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
		@Column(name="firstname")  // must do this for Postgres
		public String firstName; 
		
		// a private property with getter and setter below
		private String lastName; 
		
		@Column(name="lastname")  // must do this for Postgres
		public String getLastName() {
			return lastName;
		}
		
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		@Transient
		public String ignoreMe;
		
		// ignore static fields
		public static String ignoreThisToo;
		
		public String toString() {
			return id + " " + firstName + " " + lastName;
		}
	}
	
}
