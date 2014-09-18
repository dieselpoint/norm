package com.dieselpoint.norm;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dieselpoint.norm.PojoInfo.Property;

public class Query {

	private PojoInfo pojoInfo;
	private Object insertRow;
	
	private String sql;
	private String table;
	private String where;
	private String orderBy;

	private Object[] args;

	private int rowsAffected;

	private Database db;
	
	private Transaction transaction;

	public Query(Database db) {
		this.db = db;
	}

	public Query where(String where, Object... args) {
		this.where = where;
		this.args = args;
		return this;
	}

	public Query sql(String sql, Object... args) {
		this.sql = sql;
		this.args = args;
		return this;
	}

	public Query sql(String sql, List args) {
		this.sql = sql;
		this.args = args.toArray();
		return this;
	}

	public Query orderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
	}

	public <T> T first(Class<T> clazz) {
		List list = results(clazz);
		if (list.size() > 0) {
			return (T) list.get(0);
		} else {
			return null;
		}
	}

	
	/**
	 * Provides the results as a list of Map objects instead of a list of pojos.
	 */
	private List<Map> resultsMap(Class<Map> clazz) {

		List<Map> out = new ArrayList();
		Connection con = null;
		PreparedStatement state = null;

		try {
			assembleSql();

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
				Map map = clazz.newInstance();

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
	 * Execute a select query and return a list of results where each row
	 * is an instance of clazz.
	 */
	public <T> List<T> results(Class<T> clazz) {
		
		if (Map.class.isAssignableFrom(clazz)) {
			return (List<T>) resultsMap((Class<Map>) clazz);
		}

		List<T> out = new ArrayList();
		Connection con = null;
		PreparedStatement state = null;

		try {
			pojoInfo = PojoCache.getPojoInfo(clazz);
			assembleSql();

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
				Object row = clazz.newInstance();

				for (int i = 1; i <= colCount; i++) {
					String colName = meta.getColumnName(i);
					pojoInfo.putValue(row, colName, rs.getObject(i));
				}
				out.add((T) row);
			}

		} catch (InstantiationException | IllegalAccessException
				| InvocationTargetException | SQLException
				| IntrospectionException | NoSuchFieldException
				| IllegalArgumentException e) {
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
	

	private void assembleSql() {
		if (sql != null) {
			return;
		}
		
		String columns = "*";
		if (pojoInfo != null) {
			columns = pojoInfo.getSelectColumns();
		}
		
		StringBuilder out = new StringBuilder();
		out.append("select ");
		out.append(columns);
		out.append(" from ");
		out.append(getTable());
		if (where != null) {
			out.append(" where ");
			out.append(where);
		}
		if (orderBy != null) {
			out.append(" orderBy ");
			out.append(orderBy);
		}
		sql = out.toString();

	}

	private String getTable() {
		if (table == null && pojoInfo != null) {
			return pojoInfo.getTable();
		}
		return table;
	}

	public Query insert(Object row) {
		
		try {
			insertRow = row;
			pojoInfo = PojoCache.getPojoInfo(row.getClass());
			
			sql = pojoInfo.getInsertSql();
			args = pojoInfo.getInsertArgs(row);
			
			execute();
			
		} catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | IntrospectionException e) {
			throw new DbException(e);
		}

		return this;
	}

	/**
	 * Updates all fields in a row in a table using the primary key to find the row.
	 */
	public Query update(Object row) {
		
		try {
			pojoInfo = PojoCache.getPojoInfo(row.getClass());
			String primaryKey = pojoInfo.getPrimaryKeyName();
			
			if (primaryKey == null) {
				throw new DbException("No primary key specified in the row. Use the @Id annotation.");
			}
			
			sql = pojoInfo.getUpdateSql();
			args = pojoInfo.getUpdateArgs(row);
			
			execute();
			
		} catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | IntrospectionException e) {
			throw new DbException(e);
		}

		return this;
	}

	/**
	 * Add an arg to the list.
	 * /
	private void addArg(Object val) {
		if (args == null) {
			args = new Object[1];
			args[0] = val;
			return;
		}
		
		Object [] newArgs = new Object[args.length + 1];
		System.arraycopy(args, 0, newArgs, 0, args.length);
		newArgs[args.length] = val;
		
		args = newArgs;
	}
	*/


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

			state = localCon.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
					long newKey = generatedKeys.getLong(1);
					PojoInfo pi = PojoCache.getPojoInfo(insertRow.getClass());
					pi.putValue(insertRow, pojoInfo.getGeneratedColumnName(), newKey);
				}			
			}

		} catch (IllegalAccessException
				| InvocationTargetException | SQLException
				| NoSuchFieldException
				| IllegalArgumentException | IntrospectionException e) {
			throw new DbException(e);
		} finally {
			close(state);
			close(con);
		}

		return this;
	}

	/**
	 * Creates a simple table without indexes or complex data types. Probably
	 * not suitable for production use.
	 */
	public Query createTable(Class clazz) {
		try {
			StringBuilder buf = new StringBuilder();

			pojoInfo = PojoCache.getPojoInfo(clazz);
			buf.append("create table ");
			buf.append(pojoInfo.getTable());
			buf.append(" (");
			
			boolean needsComma = false;
			for (Property prop : pojoInfo.getProperties()) {
				
				if (needsComma) {
					buf.append(',');
				}
				needsComma = true;
				
				String colType = "varchar(512)";
				Class dataType = prop.dataType;
				
				if (dataType.equals(Integer.class) || dataType.equals(int.class)) {
					colType = "integer";
				} else if (dataType.equals(Long.class) || dataType.equals(long.class)) {
					colType = "bigint";
				} else if (dataType.equals(Double.class) || dataType.equals(double.class)) {
					colType = "double";
				} else if (dataType.equals(Float.class) || dataType.equals(float.class)) {
					colType = "float";
				}
				
				buf.append(prop.name);
				buf.append(" ");
				buf.append(colType);
				
				if (prop.isGenerated) {
					buf.append(" auto_increment");
				}
				
			}
			
			if (pojoInfo.getPrimaryKeyName() != null) {
				buf.append(", primary key (");
				buf.append(pojoInfo.getPrimaryKeyName());
				buf.append(")");
			}
			
			buf.append(")");

			sql = buf.toString();
			execute();
			return this;

		} catch (IntrospectionException e) {
			throw new DbException(e);
		}
	}

	public Query delete(Object row) {
		try {
			pojoInfo = PojoCache.getPojoInfo(row.getClass());
			
			String table = getTable();
			if (table == null) {
				throw new DbException("You must specify a table name");
			}
			
			String primaryKeyName = pojoInfo.getPrimaryKeyName();
			Object primaryKeyValue = pojoInfo.getValue(row, primaryKeyName);
			args = new Object[1];
			args[0] = primaryKeyValue;
			
			sql = "delete from " + table + " where " + primaryKeyName + "=?";
			
			execute();
			
		} catch (IntrospectionException | NoSuchFieldException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new DbException(e);
		}
		return this;
	}

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
	
	public Query table(String table) {
		this.table = table;
		return this;
	}
	
	public int getRowsAffected() {
		return rowsAffected;
	}

	public Query transaction(Transaction trans) {
		this.transaction = trans;
		return this;
	}
	
}
