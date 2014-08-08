package com.dieselpoint.norm2;

import java.sql.SQLException;
import java.util.List;

import com.dieselpoint.norm2.annotations.DbTable;

public class Test {
	
	static public void main(String [] args) throws SQLException {
		
		Database db = new Database();
		
		db.createTable(Name.class);
		
		Name name = new Name();
		name.firstName = "John";
		name.lastName = "Doe";
		db.insert(name);
		
		name = new Name();
		name.firstName = "Bill";
		name.lastName = "Smith";
		db.insert(name);
		
		List<Name> list = db.where("firstName=?", "John").results(Name.class);
		for (Name n: list) {
			System.out.println(n.toString());
		}
		
		db.sql("drop table names").execute();
		
	}

	@DbTable("names")
	static public class Name {
		public String firstName;
		public String lastName;
		public String toString() {
			return firstName + " " + lastName;
		}
	}
	
}
