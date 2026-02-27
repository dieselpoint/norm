package com.dieselpoint.norm;

import com.dieselpoint.norm.sqlmakers.MySqlMaker;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class TestLocalDateTime
{
	static String DB_DRIVER_CLASS_NAME = "org.mariadb.jdbc.Driver";
	static String DB_SERVER_URL = "jdbc:mariadb://localhost:3306/test?allowPublicKeyRetrieval=true";
	static String DB_USERNAME = "test_user";
	static String DB_PASSWORD = "testUserPassword123!";

	private static Database db;

	@BeforeClass
	public static void setUp() throws SQLException
	{
		db = new Database();
		DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
		db.setDriverClassName(DB_DRIVER_CLASS_NAME);
		db.setJdbcUrl(DB_SERVER_URL);
		db.setUser(DB_USERNAME);
		db.setPassword(DB_PASSWORD);
		db.setSqlMaker(new MySqlMaker());
		db.sql("TRUNCATE test.foo").execute();
	}

	@Test
	public void testInsert() {
		Foo foo = new Foo();
		foo.startTime = LocalDateTime.of(1953, 1, 2, 3, 4, 5);
		db.insert(foo);
		Assert.assertTrue(true);
	}

	@Test
	public void testSelect() {
		List<Foo> results = db.results(Foo.class);
		Assert.assertEquals(1, results.size());
		Foo foo = results.get(0);
		Assert.assertEquals(1953, foo.startTime.getYear());
		Assert.assertEquals(1, foo.startTime.getMonthValue());
		Assert.assertEquals(2, foo.startTime.getDayOfMonth());
		Assert.assertEquals(3, foo.startTime.getHour());
		Assert.assertEquals(4, foo.startTime.getMinute());
		Assert.assertEquals(5, foo.startTime.getSecond());
	}

	@Table(schema = "test", name = "foo")
	public static class Foo
	{
		@Id
		@GeneratedValue
		public long id;
		@Column(name = "start_time")
		public LocalDateTime startTime;
	}

}
