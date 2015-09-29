package com.dieselpoint.norm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dieselpoint.norm.sqlmakers.PojoInfo;
import com.dieselpoint.norm.sqlmakers.SqlMaker;

/**
 * Holds all of the information in a query. Create a query
 * using Database.someQueryCreationMethod(), populate it using
 * a builder pattern, and execute it using either .execute() (to
 * update the database) or .results() (to get the results of a query.) 
 */
public class Query {

	private Object insertRow;
	
	private String sql;
	private String table;
	private String where;
	private String orderBy;

	private Object[] args;

	private int rowsAffected;

	private Database db;
	private SqlMaker sqlMaker;
	
	private Transaction transaction;

	public Query(Database db) {
		this.db = db;
		this.sqlMaker = db.getSqlMaker();
	}

	/**
	 * Add a where clause and some parameters to a query. Has no effect if
	 * the .sql() method is used.
	 * @param where Example: "name=?"
	 * @param args The parameter values to use in the where, example: "Bob"
	 */
	public Query where(String where, Object... args) {
		this.where = where;
		this.args = args;
		return this;
	}

	/**
	 * Create a query using straight SQL. Overrides any other methods
	 * like .where(), .orderBy(), etc.
	 * @param sql The SQL string to use, may include ? parameters.
	 * @param args The parameter values to use in the query.
	 */
	public Query sql(String sql, Object... args) {
		this.sql = sql;
		this.args = args;
		return this;
	}

	/**
	 * Create a query using straight SQL. Overrides any other methods
	 * like .where(), .orderBy(), etc.
	 * @param sql The SQL string to use, may include ? parameters.
	 * @param args The parameter values to use in the query.
	 */
	public Query sql(String sql, List<?> args) {
		this.sql = sql;
		this.args = args.toArray();
		return this;
	}

	/**
	 * Add an "orderBy" clause to a query.
	 */
	public Query orderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
	}

	/**
	 * Returns the first row in a query in a pojo, or null if the query returns no results. 
	 * Will return it in a Map if a class that implements Map is specified.
	 */
	public <T> T first(Class<T> clazz) {
		List<T> list = results(clazz);
		if (list.size() > 0) {
			return (T) list.get(0);
		} else {
			return null;
		}
	}

	
	/**
	 * Provides the results as a list of Map objects instead of a list of pojos.
	 */
	private List<Map> resultsMap(Class<Map<String, Object>> clazz) {

		List<Map> out = new ArrayList<Map>();
		Connection con = null;
		PreparedStatement state = null;

		try {
			if (sql == null) {
				sql = sqlMaker.getSelectSql(this, clazz);
			}

			Connection localCon;
			if (transaction == null) {
				localCon = db.getConnection();
				con = localCon; // con gets closed below if non-null
			} else {
				localCon = transaction.getConnection();
			}
			
			state = localCon.prepareStatement(sql);
			loadArgs(state);

			ResultSet rs = state.executeQuery();

			ResultSetMetaData meta = rs.getMetaData();
			int colCount = meta.getColumnCount();

			while (rs.next()) {
				Map<String, Object> map = clazz.newInstance();

				for (int i = 1; i <= colCount; i++) {
					String colName = meta.getColumnName(i);
					map.put(colName, rs.getObject(i));
				}
				out.add(map);
			}

		} catch (InstantiationException | IllegalAccessException | SQLException
				| IllegalArgumentException e) {
			throw new DbException(e);
		} finally {
			close(state);
			close(con);
		}

		return out;
	}	
	
	
	/**
	 * Execute a "select" query and return a list of results where each row
	 * is an instance of clazz. Returns an empty list if there are no results.
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> results(Class<T> clazz) {
		
		if (Map.class.isAssignableFrom(clazz)) {
			return (List<T>) resultsMap((Class<Map<String, Object>>) clazz);
		}

		List<T> out = new ArrayList<T>();
		Connection con = null;
		PreparedStatement state = null;

		try {
			if (sql == null) {
				sql = sqlMaker.getSelectSql(this, clazz);
			}

			Connection localCon;
			if (transaction == null) {
				localCon = db.getConnection();
				con = localCon; // con gets closed below if non-null
			} else {
				localCon = transaction.getConnection();
			}
			
			state = localCon.prepareStatement(sql);
			loadArgs(state);

			ResultSet rs = state.executeQuery();

			ResultSetMetaData meta = rs.getMetaData();
			int colCount = meta.getColumnCount();

			if (Util.isPrimitiveOrString(clazz)) {
				// if the receiver class is a primitive just grab the first column and assign it
				while (rs.next()) {
					Object colValue = rs.getObject(1);
					out.add((T) colValue);
				}
				
			} else {
				PojoInfo pojoInfo = sqlMaker.getPojoInfo(clazz);
				while (rs.next()) {
					T row = clazz.newInstance();

					for (int i = 1; i <= colCount; i++) {
						String colName = meta.getColumnName(i);
						Object colValue = rs.getObject(i);
						
						pojoInfo.putValue(row, colName, colValue);
					}
					out.add((T) row);
				}
			}

		} catch (InstantiationException | IllegalAccessException
				| SQLException	e) {
			throw new DbException(e);
		} finally {
			close(state);
			close(con);
		}

		return out;
	}

	private void loadArgs(PreparedStatement state) throws SQLException {
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				state.setObject(i + 1, args[i]);
			}
		}
	}
	
	private void close(AutoCloseable ac) {
		if (ac == null) {
			return;
		}
		try {
			ac.close();
		} catch (Exception e) {
			// bury it
		}
	}
	

	
	/**
	 * Insert a row into a table. The row pojo can have a @Table annotation to
	 * specify the table, or you can specify the table with the .table() method.
	 */
	public Query insert(Object row) {

		insertRow = row;

		sql = sqlMaker.getInsertSql(this, row);
		args = sqlMaker.getInsertArgs(this, row);

		execute();

		return this;
	}

	/**
	 * Upsert a row into a table.
	 * See http://en.wikipedia.org/wiki/Merge_%28SQL%29
	 */
	public Query upsert(Object row) {

		insertRow = row;

		sql = sqlMaker.getUpsertSql(this, row);
		args = sqlMaker.getUpsertArgs(this, row);

		execute();

		return this;
	}
	
	
	/**
	 * Update a row in a table. It will match an existing row based on the
	 * primary key.
	 */
	public Query update(Object row) {

		sql = sqlMaker.getUpdateSql(this, row);
		args = sqlMaker.getUpdateArgs(this, row);

		execute();
		return this;
	}

	/**
	 * Execute a sql command that does not return a result set. The sql should previously have
	 * been set with the sql(String) method. Returns this Query object. To see how the command did, call
	 * .rowsAffected().
	 */
	public Query execute() {
		
		Connection con = null;
		PreparedStatement state = null;

		try {
			
			Connection localCon;
			if (transaction == null) {
				localCon = db.getConnection();
				con = localCon; // con gets closed below if non-null
			} else {
				localCon = transaction.getConnection();
			}

			/*
			 * This is a hack to deal with an error in the Postgres driver.
			 * Postgres blindly appends "RETURNING *" to any query that includes
			 * Statement.RETURN_GENERATED_KEYS. This is a bug. See:
			 * http://www.postgresql.org/message-id/4BD196B4.3040607@smilehouse.com
			 * So, as a workaround, we only add that flag if the query contains
			 * "insert". Yuck.
			 */
			String lowerSql = sql.toLowerCase();
			if (lowerSql.contains("insert")) {
				state = localCon.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			} else {
				state = localCon.prepareStatement(sql);
			}
			
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					state.setObject(i + 1, args[i]);
				}
			}

			rowsAffected = state.executeUpdate();
			
			// Set auto generated primary key. The code assumes that the primary
			// key is the only auto generated key.
			if (insertRow != null) {
				ResultSet generatedKeys = state.getGeneratedKeys();
				if (generatedKeys.next()) {
					sqlMaker.populateGeneratedKey(generatedKeys, insertRow);
				}			
			}

		} catch (SQLException | IllegalArgumentException e) {
			throw new DbException(e);
		} finally {
			close(state);
			close(con);
		}

		return this;
	}

	/**
	 * Simple, primitive method for creating a table based on a pojo. 
	 */
	public Query createTable(Class<?> clazz) {
		sql = sqlMaker.getCreateTableSql(clazz);
		execute();
		return this;
	}

	
	/**
	 * Delete a row in a table. This method looks for an @Id annotation to find
	 * the row to delete by primary key, and looks for a @Table annotation to
	 * figure out which table to hit.
	 */
	public Query delete(Object row) {

		sql = sqlMaker.getDeleteSql(this, row);
		args = sqlMaker.getDeleteArgs(this, row);

		execute();
		return this;
	}

	/**
	 * Delete multiple rows in a table. Be sure to specify the 
	 * table with the .table() method and limit the rows to delete
	 * using the .where() method.
	 */
	public Query delete() {
		String table = getTable();
		if (table == null) {
			throw new DbException("You must specify a table name with the table() method.");
		}
		sql = "delete from " + table;
		if (where != null) {
			sql += " where " + where;
		}
		execute();
		return this;
	}
	
	/**
	 * Specify the table to operate on.
	 */
	public Query table(String table) {
		this.table = table;
		return this;
	}
	
	/**
	 * For queries that affect the database in some way, this method returns the 
	 * number of rows affected. Call it after you call .execute(), .update(), .delete(), etc.:
	 * .table("foo").where("bar=bah").delete().rowsAffected();
	 */
	public int getRowsAffected() {
		return rowsAffected;
	}

	/**
	 * Specify that this query should be a part of the specified transaction.
	 */
	public Query transaction(Transaction trans) {
		this.transaction = trans;
		return this;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public String getWhere() {
		return where;
	}

	public String getTable() {
		return table;
	}
	
}
