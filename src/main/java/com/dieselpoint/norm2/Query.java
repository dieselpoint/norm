package com.dieselpoint.norm2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.dieselpoint.norm2.annotations.DbTable;

public class Query {

	private String sql;
	private String table;
	private String columns;
	private String where;
	private String orderBy;
	
	private Object[] args;
	
	private int rowsAffected;
	
	private Database db;

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
	
	public <T> List<T> results(Class<T> clazz) throws SQLException {
		
		populateParams(clazz);
		assembleSql();
		
		List<T> out = new ArrayList();
		Connection con = null;
		PreparedStatement state = null;

		try {
			con = db.getConnection();
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
				Object row = clazz.newInstance();
				for (int i = 1; i <= colCount; i++) {
					String colName = meta.getColumnName(i);
					
					// TODO use BeanUtils or something else to set the property
					// look at jackson copy value functionality
					// omit missing fields (in case done with *)
				}
				out.add((T) row);
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
		
		return out;
	}

	

	private void populateParams(Class clazz) {
		if (sql != null) {
			return; // sql overrides everything
		}
		
		if (table == null) {
			DbTable annot = (DbTable) clazz.getAnnotation(DbTable.class);
			if (annot != null) {
				table = annot.value();
			}
		}
		
		// TODO the rest
	}

	private void assembleSql() {
		// TODO Auto-generated method stub
		
	}

	public Query insert(Object row) {
		// TODO Auto-generated method stub
		
		// somehow deal with returning rows affected
		rowsAffected = 1; // example
		return this;
	}

	public Query execute() {
		// TODO Auto-generated method stub
		
		return this;
	}

	
	
}
