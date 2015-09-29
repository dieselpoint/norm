package com.dieselpoint.norm;

public class Setup {
	
	public static void setSysProperties() {
		
		System.setProperty("norm.serverName", "localhost");
		System.setProperty("norm.databaseName", "mydb");
		
		System.setProperty("norm.dataSourceClassName", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
		System.setProperty("norm.user", "root");
		System.setProperty("norm.password", "rootpassword");
		
		/*
		System.setProperty("norm.dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
		System.setProperty("norm.user", "postgres");
		System.setProperty("norm.password", "postgres");
		*/
		
	}
}
