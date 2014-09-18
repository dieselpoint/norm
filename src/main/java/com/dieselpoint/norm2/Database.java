package com.dieselpoint.norm2;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


public class Database {
	
	/*
	 * TODO
	 * docs
	 *   review datasources. document design pattern
	 *   all annotations
	 *   put test code in docs
	 *   sample one-liners
	 * put on maven central
	 */
	
	
	private DataSource ds;
	
	protected DataSource getDataSource() throws SQLException {
		HikariConfig config = new HikariConfig();
		config.setMaximumPoolSize(100);
		config.setDataSourceClassName(System.getProperty("norm.dataSourceClassName"));
		config.addDataSourceProperty("serverName", System.getProperty("norm.serverName"));
		config.addDataSourceProperty("databaseName", System.getProperty("norm.databaseName"));
		config.addDataSourceProperty("user", System.getProperty("norm.user"));
		config.addDataSourceProperty("password", System.getProperty("norm.password"));
		return new HikariDataSource(config);
	}
	
	public Query sql(String sql, Object... args) {
		return new Query(this).sql(sql, args);
	}
	
	public Query where(String where, Object... args) {
		return new Query(this).where(where, args);
	}
	
	public Query orderBy(String orderBy) {
		return new Query(this).orderBy(orderBy);
	}
	
	public Connection getConnection() {
		try {

			if (ds == null) {
				ds = getDataSource();
			}
			return ds.getConnection();

		} catch (Throwable t) {
			throw new DbException(t);
		}
	}

	public Query createTable(Class clazz) {
		return new Query(this).createTable(clazz);
	}

	public Query insert(Object row) {
		return new Query(this).insert(row);
	}

	public Query delete(Object row) {
		return new Query(this).delete(row);
	}
	
	public <T> List<T> results(Class<T> clazz) {
		return new Query(this).results(clazz);
	}

	public Query update(Object row) {
		return new Query(this).update(row);
	}
	
	public Query table(String table) {
		return new Query(this).table(table);
	}
	
	/**
	 * Start a database transaction. Perform database manipulations, then call
	 * transaction.commit() or .rollback() to complete the process.
	 * No need to close the transaction.
	 * @return a transaction object
	 */
	public Transaction startTransaction() {
		Transaction trans = new Transaction();
		trans.setConnection(getConnection());
		return trans;
	}

	public Query transaction(Transaction trans) {
		return new Query(this).transaction(trans);
	}
	
}
