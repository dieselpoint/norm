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
		
	}
}
