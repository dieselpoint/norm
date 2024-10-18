package com.dieselpoint.norm;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestGeneratedId {

	static String DB_DRIVER_CLASS_NAME = "org.postgresql.ds.PGSimpleDataSource";
	static String DB_SERVER_NAME = "localhost";
	static String DB_DATABASE = "testdb";
	static String DB_USERNAME = "postgres";
	static String DB_PASSWORD = "rootpassword";

	private Database db;

	@Before
	public void setUp() {
		System.setProperty("norm.dataSourceClassName", DB_DRIVER_CLASS_NAME);
		System.setProperty("norm.serverName", DB_SERVER_NAME);
		System.setProperty("norm.databaseName", DB_DATABASE);
		System.setProperty("norm.user", DB_USERNAME);
		System.setProperty("norm.password", DB_PASSWORD);

		db = new Database();
	}

	@Test
	public void testCreate() {
		NormPojo np = new NormPojo();
		np.setId("MyID");
		np.setName("My name");
		db.generatedKeyReceiver(np, "id").insert(np);
		Assert.assertNotNull(np.getDatabaseId());
	}

	@Test
	public void testRetrieval() {
		List<NormPojo> npList = null;
		npList = db.results(NormPojo.class);
		Assert.assertNotNull(npList);
		Assert.assertTrue(npList.size() > 0);
	}

	@Table(name = "pojo")
	public static class NormPojo {

		/** Database record ID. */
		private Integer databaseId;

		/** Unique identifier of the object. */
		private String id;

		/** Human readable name. */
		private String name;

		/**
		 * @return the id
		 */
		@Column(name = "object_id", unique = true)
		public String getId() {
			return id;
		}

		/**
		 * @param id the id to set
		 */
		public void setId(String id) {
			this.id = id;
		}

		/**
		 * @return the name
		 */
		@Column(name = "name")
		public String getName() {
			return name;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		@Id
		@GeneratedValue
		@Column(name = "id")
		public Integer getDatabaseId() {
			return databaseId;
		}

		public void setDatabaseId(Integer databaseId) {
			this.databaseId = databaseId;
		}

	}

}
