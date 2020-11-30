package com.dieselpoint.norm;

public class Setup {

	public static void setSysProperties() {

		/*- This is broken, per the Hikari docs. Must use jdbcUrl method instead.
		System.setProperty("norm.dataSourceClassName", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
		System.setProperty("norm.serverName", "localhost");
		System.setProperty("norm.databaseName", "mydb");
		 */

		/*-
		 * MySQL JDBC incorrectly reports bigint SQL datatype as a BigInteger.
		 * https://stackoverflow.com/questions/65078455/mysql-jdbc-driver-incorrectly-reports-bigint-as-biginteger
		 * /
		System.setProperty("norm.jdbcUrl",
				"jdbc:mysql://localhost:3306/mydb?useSSL=false&allowPublicKeyRetrieval=true");
		System.setProperty("norm.user", "root");
		// System.setProperty("norm.password", "rootpassword");
		*/

		/*-
		System.setProperty("norm.dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
		System.setProperty("norm.user", "postgres");
		System.setProperty("norm.password", "postgres");
		*/

		/*-
		 * H2 handles mixed-case column names incorrectly.
		 */
		System.setProperty("norm.jdbcUrl", "jdbc:h2:./h2test;database_to_upper=false");
		System.setProperty("norm.user", "root");
		System.setProperty("norm.password", "rootpassword");

		/*-
		 * SampleCode doesn't yet work because the sqlite create table syntax is different. Need a new SQL maker.
		 * /
		System.setProperty("norm.jdbcUrl", "jdbc:sqlite:sqlitetest.db");
		System.setProperty("norm.user", "root");
		System.setProperty("norm.password", "rootpassword");
		*/

		/*-
		 * Does not run sample code because the "drop database if exists names" chokes on "exists"
		System.setProperty("norm.jdbcUrl", "jdbc:derby:mydb;create=true");
		System.setProperty("norm.user", "root");
		*/

	}
}
