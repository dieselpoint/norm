package com.dieselpoint.norm;

public class Setup {
	
	public static void setSysProperties() {
		
		
		/* This is broken, per the Hikari docs. Must use jdbcUrl method instead.
		System.setProperty("norm.dataSourceClassName", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
		System.setProperty("norm.serverName", "localhost");
		System.setProperty("norm.databaseName", "mydb");
		 */

		System.setProperty("norm.jdbcUrl", "jdbc:mysql://localhost:3306/mydb?useSSL=false");
		System.setProperty("norm.user", "root");
		System.setProperty("norm.password", "rootpassword");
		
		/*
		System.setProperty("norm.dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
		System.setProperty("norm.user", "postgres");
		System.setProperty("norm.password", "postgres");
		*/

		/*
		System.setProperty("norm.jdbcUrl", "jdbc:h2:./h2test;database_to_upper=false");
		System.setProperty("norm.user", "root");
		System.setProperty("norm.password", "rootpassword");
		*/
		
		/*
		 * SampleCode doesn't yet work because the sqlite create table syntax is different. Need a new SQL maker.
		System.setProperty("norm.jdbcUrl", "jdbc:sqlite:sqlitetest.db");
		System.setProperty("norm.user", "root");
		System.setProperty("norm.password", "rootpassword");
		*/

		
	}
}
