package com.dieselpoint.norm2;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.dieselpoint.norm.DataSourceFactory;

public class Database {
	
	private DataSource ds;
	
	public Database() throws SQLException {
		ds = getDataSource();
	}
	
	protected DataSource getDataSource() throws SQLException {
		String driver = System.getProperty("norm.driver");
		String databaseName = System.getProperty("norm.databasename");
		String url = System.getProperty("norm.url");
		String user = System.getProperty("norm.user");
		String password = System.getProperty("norm.password");
		return DataSourceFactory.getDataSource(driver, databaseName, url, user, password);
	}
	
	public Query sql(String sql, Object... args) {
		return new Query(this).where(sql, args);
	}
	
	public Query where(String where, Object... args) {
		return new Query(this).where(where, args);
	}
	
	public Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

	public void createTable(Class clazz) {
		
		
		// TODO Auto-generated method stub
		
	}

	public void insert(Object row) {
		new Query(this).insert(row);
		
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}
