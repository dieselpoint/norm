package com.dieselpoint.norm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

/**
 * Contains a row in a result set, and has several methods for performing queries and updates.
 */
public class Entity<E> {
	
	/* TODO
	 * figure out why firstResult() needs a cast
	 * 
	 */
	

	private static ConcurrentHashMap<String, Meta> entities = new ConcurrentHashMap();

	private Meta meta;
	private LinkedHashMap<String, Object> record = new LinkedHashMap();
	private int rowsAffected;
	
	// these get populated temporarily during a query, then cleared
	private String sql;
	private String where;
	private Object[] args;
	private String orderBy;
	
	public Entity() {
		Meta meta = entities.get(this.getClass());
		if (meta == null) {
			meta = getMeta();
		}
		this.meta = meta;
	}
	
	protected Meta getMeta() {
		Meta meta = new Meta();
		try {
			meta.databaseName = getDatabaseName();
			meta.tableName = getTableName();
			meta.primaryKeyName = getPrimaryKeyName();
			meta.ds = getDataSource();
		} catch (SQLException e) {
			throw new RuntimeException(e); // fatal
		}
		return meta;
	}
	

	/**
	 * Return a DataSource for this entity. Gets called once when this class is initialized.
	 * @return a DataSource
	 * @throws SQLException
	 */
	protected DataSource getDataSource() throws SQLException {
		String driver = System.getProperty("norm.driver");
		String databaseName = System.getProperty("norm.databasename");
		String url = System.getProperty("norm.url");
		String user = System.getProperty("norm.user");
		String password = System.getProperty("norm.password");
		return DataSourceFactory.getDataSource(driver, databaseName, url, user, password);
	}

	public Entity where(String where, Object... args) {
		this.where = where;
		this.args = args;
		return this;
	}
	
	public Entity sql(String sql, Object... args) {
		this.sql = sql;
		this.args = args;
		return this;
	}
	
	public Entity orderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
	}
	
	
	public void insert() throws SQLException {
		
		List columns = new ArrayList(record.keySet());
		List values = new ArrayList(record.values());
		
		StringBuilder sql = new StringBuilder();
		sql.append("insert into ");
		sql.append(meta.tableName);
		sql.append(" (");
		appendCommaSep(sql, columns);
		sql.append(") values (");
		appendQuestionMarks(sql, values.size());
		sql.append(")");

		executeUpdate(sql.toString(), values);
	}

	
	
	public void update() throws SQLException {
		List columns = new ArrayList(record.keySet());
		List values = new ArrayList(record.values());
		
		StringBuilder sql = new StringBuilder();
		sql.append("update ");
		sql.append(meta.tableName);
		sql.append(" set ");

		int count = columns.size();
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				sql.append(',');
			}
			sql.append(columns.get(i));
			sql.append("=?");
		}

		executeUpdate(sql.toString(), values);
	}
	
	/**
	 * Deletes records in the table. This method uses the specified "where" clause
	 * to limit the records it deletes. If there is none, it uses the current
	 * primary key to delete a single record. If there is none, it throws an
	 * exception. 
	 * @throws SQLException
	 */
	public void delete() throws SQLException {
		
		Object pk = get(meta.primaryKeyName);
		
		StringBuilder sql = new StringBuilder();
		sql.append("delete from ");
		sql.append(meta.tableName);
		if (where != null) {
			sql.append(" where ");
			sql.append(where);
		} else if (pk != null) {
			sql.append(" where ");
			sql.append(meta.primaryKeyName);
			sql.append("=?");
			args = new Object[]{pk};
		} else {
			throw new SQLException("Delete statements require either a where clause or a value for the primary key in the current record. To delete all records in a table, call deleteAll()");
		}
		executeUpdate(sql.toString(), args);
	}
	
	/**
	 * Delete all records in the table.
	 * @throws SQLException
	 */
	public void deleteAll() throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("delete from ");
		sql.append(meta.tableName);
		executeUpdate(sql.toString(), null);
	}
	

	/**
	 * Returns the first record in a result set. If the result set
	 * is empty, returns null.
	 * @throws SQLException
	 */
	public E firstResult() throws SQLException {
		List<E> list = this.results();
		if (list.size() == 0) {
			return null;
		}
		return list.get(0);
	}
	
	
	private void appendCommaSep(StringBuilder sb, List strs) {
		int count = strs.size();
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(strs.get(i));
		}
	}
	
	private void appendQuestionMarks(StringBuilder sb, int size) {
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append('?');
		}
	}
	
	
	private void applyArgs(PreparedStatement state, Object args) throws SQLException {
		if (args != null) {
			if (args instanceof String) {
				state.setString(1, args.toString());
			} else if (args instanceof Object []) {
				Object [] arr = (Object[]) args;
				for (int i = 0; i < arr.length; i++) {
					state.setObject(i + 1, arr[i]);
				}
			} else if (args instanceof List) {
				List list = (List) args;
				for (int i = 0; i < list.size(); i++) {
					state.setObject(i + 1, ((List) args).get(i));
				}
			} else {
				throw new SQLException("args have an unrecognized type:" + args.toString());
			}
		}
	}
	
	
	/**
	 * Run an insert, update, or delete statement.
	 * @throws SQLException 
	 */
	public void executeUpdate(String sql, Object args) throws SQLException {
		
		Connection con = null;
		PreparedStatement state = null;
		//ResultSet generatedKeys = null;

		try {
			con = getConnection();
			state = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			applyArgs(state, args);

			rowsAffected = state.executeUpdate();
			
			// Set auto generated primary key. The code assumes that the primary
			// key is the only auto generated key.
			ResultSet generatedKeys = state.getGeneratedKeys();
			if (generatedKeys.next()) {
				put(meta.primaryKeyName, generatedKeys.getLong(1) + "");
			}
			

		} finally {
			if (state != null) {
				try {
					state.close();
				} catch (SQLException e) {
					// ok to ignore
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					// ok to ignore
				}
			}
		}
		clearQuery();
	}

	
	/**
	 * Returns a connection to the internal database. Can be useful for
	 * low-level operations. You must close the connections when you're
	 * finished with it.
	 * @throws SQLException 
	 */
	public Connection getConnection() throws SQLException {
		return meta.ds.getConnection();
	}

	
	/**
	 * Return the results of a query previously specified with a where() or sql() call.
	 * @return a list of records where each record is an instance of this class
	 * @throws SQLException
	 */
	public List<E> results() throws SQLException {

		ArrayList out = new ArrayList();
		
		String sql = this.sql;
		if (sql == null) {
			sql = "select * from "
					+ meta.tableName
					+ (where == null ? "" : " where "
							+ where
							+ (orderBy == null ? "" : " order by "
									+ orderBy));
		}

		Connection con = null;
		PreparedStatement state = null;

		try {
			con = getConnection();
			state = con.prepareStatement(sql);
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					state.setObject(i + 1, args[i]);
				}
			}

			ResultSet rs = state.executeQuery();

			ResultSetMetaData meta = rs.getMetaData();
			int colCount = meta.getColumnCount();

			while (rs.next()) {
				Entity ent = this.getClass().newInstance();

				Map map = ent.getMap();
				for (int i = 1; i <= colCount; i++) {
					String colName = meta.getColumnName(i);
					map.put(colName, rs.getString(i));
				}
				out.add(ent);
			}

		} catch (InstantiationException | IllegalAccessException e) {
			throw new SQLException(e);
		} finally {
			if (state != null) {
				try {
					state.close();
				} catch (SQLException e) {
					// ok to ignore
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					// ok to ignore
				}
			}
		}
		
		clearQuery();
		
		return out;
	}


	private void clearQuery() {
		sql = null;
		where = null;
		args = null;
		orderBy = null;
	}
	
	/**
	 * NOT CURRENTLY USED
	 * @return
	 */
	public String getDatabaseName() {
		return "default";
	}

	public String getTableName() {
		return this.getClass().getSimpleName().toLowerCase();
	}
	
	public String getPrimaryKeyName() {
		return getTableName() + "Id";
	}

	public Map getMap() {
		return record;
	}
	

	public Entity<E> put(String column, Object value) {
		record.put(column, value);
		return this;
	}
	
	/**
	 * Get the value of a column as an Object. The object will
	 * be a Java primitive, for example, String, Integer, etc.
	 * @param column column name
	 * @return an Object, or null if the column doesn't exist
	 */
	public Object get(String column) {
		return record.get(column);
	}

	/**
	 * Get the value of a column as a String.
	 * @param column column name
	 * @return a String, or null if the column doesn't exist
	 */
	public String getString(String column) {
		Object val = get(column);
		if (val != null) {
			return val.toString();
		}
		return null;
	}
	
	public String toString() {
		return record.toString();
	}
	
	/**
	 * Returns the number of rows affected by the last insert, update, or delete statement.
	 */
	public int getRowsAffected() {
		return rowsAffected;
	}
	
}
