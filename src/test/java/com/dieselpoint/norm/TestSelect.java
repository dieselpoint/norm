package com.dieselpoint.norm;

import static org.junit.Assert.fail;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

import org.junit.Ignore;
import org.junit.Test;

public class TestSelect {
	@Test
	public void selectTest() {
		Database db = new Database();
		db.setJdbcUrl("jdbc:sqlite:/home/ghost/IdeaProjects/norm/norm/test.sqlite3");

		List<Person> rows =
				db.select("person.id, name")
					.table("person")
						.innerJoin("name")
						.on("person.name_id = name.id")
					.where("name.name = ? or name.name = ?", "nick", "Nick")
					.results(Person.class);

		System.out.println();
	}
	
	@Table(name="person")
	public static class Person {
		@Id
		public long id;

		@OneToOne()
		@JoinColumn
		public String name;
	}

	@Table(name="name")
	public static class Name {
		@Id
		public long id;
		public String firstName;
	}
}
