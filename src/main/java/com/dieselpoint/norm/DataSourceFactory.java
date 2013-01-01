package com.dieselpoint.norm;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

public class DataSourceFactory {

	public static DataSource getDataSource(String driver, String dbName, String url,
			String user, String password) throws SQLException {

		// TODO use BoneCP or C3PO instead
		
		if ("com.mysql.jdbc.Driver".equalsIgnoreCase(driver)) {
			MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();
			ds.setDatabaseName(dbName);
			ds.setUrl(url);
			ds.setUser(user);
			ds.setPassword(password);
			return ds;
			
		} else {
			throw new SQLException("driver not found:" + driver);
		}
	}

}
