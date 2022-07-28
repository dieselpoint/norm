package com.dieselpoint.norm.sqlmakers;

import com.dieselpoint.norm.DbException;
import com.dieselpoint.norm.Query;
import com.dieselpoint.norm.Util;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Produces ANSI-standard SQL. Extend this class to handle different flavors of
 * sql.
 */
public class StandardSqlMaker implements SqlMaker {

	private static final ConcurrentHashMap<Class<?>, StandardPojoInfo> map = new ConcurrentHashMap<>();

	public StandardPojoInfo getPojoInfo(Class<?> rowClass) {
		StandardPojoInfo pi = map.get(rowClass);
		if (pi == null) {
			pi = new StandardPojoInfo(rowClass);
			map.put(rowClass, pi);

			makeInsertSql(pi);
			makeUpsertSql(pi);
			makeUpdateSql(pi);
			makeSelectColumns(pi);
		}
		return pi;
	}

	@Override
	public String getInsertSql(Query query, Object row) {
		StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
		return String.format(pojoInfo.insertSql, Objects.requireNonNullElse(query.getTable(), pojoInfo.table));
	}

	@Override
	public Object[] getInsertArgs(Query query, Object row) {
		StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
		Object[] args = new Object[pojoInfo.insertSqlArgCount];
		for (int i = 0; i < pojoInfo.insertSqlArgCount; i++) {
			args[i] = pojoInfo.getValue(row, pojoInfo.insertColumnNames[i]);
		}
		return args;
	}

	@Override
	public String getUpdateSql(Query query, Object row) {
		StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
		if (pojoInfo.primaryKeyName == null) {
			throw new DbException("No primary key specified in the row. Use the @Id annotation.");
		}
		return String.format(pojoInfo.updateSql, Objects.requireNonNullElse(query.getTable(), pojoInfo.table));
	}

	@Override
	public Object[] getUpdateArgs(Query query, Object row) {
		StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());

		Object[] args = new Object[pojoInfo.updateSqlArgCount];
		for (int i = 0; i < pojoInfo.updateSqlArgCount - 1; i++) {
			args[i] = pojoInfo.getValue(row, pojoInfo.updateColumnNames[i]);
		}
		// add the value for the where clause to the end
		Object pk = pojoInfo.getValue(row, pojoInfo.primaryKeyName);
		args[pojoInfo.updateSqlArgCount - 1] = pk;
		return args;
	}

	public void makeUpdateSql(StandardPojoInfo pojoInfo) {

		ArrayList<String> cols = new ArrayList<>();
		for (Property prop : pojoInfo.propertyMap.values()) {

			if (prop.isPrimaryKey) {
				continue;
			}

			if (prop.isGenerated) {
				continue;
			}

			cols.add(prop.name);
		}
		pojoInfo.updateColumnNames = cols.toArray(new String[cols.size()]);
		pojoInfo.updateSqlArgCount = pojoInfo.updateColumnNames.length + 1; // + 1 for the where arg

		StringBuilder buf = new StringBuilder();
		buf.append("update %s set ");

		for (int i = 0; i < cols.size(); i++) {
			if (i > 0) {
				buf.append(',');
			}
			buf.append(cols.get(i)).append("=?");
		}
		buf.append(" where ").append(pojoInfo.primaryKeyName).append("=?");

		pojoInfo.updateSql = buf.toString();
	}

	public void makeInsertSql(StandardPojoInfo pojoInfo) {
		ArrayList<String> cols = new ArrayList<>();
		for (Property prop : pojoInfo.propertyMap.values()) {
			if (prop.isGenerated) {
				continue;
			}
			cols.add(prop.name);
		}
		pojoInfo.insertColumnNames = cols.toArray(new String[cols.size()]);
		pojoInfo.insertSqlArgCount = pojoInfo.insertColumnNames.length;

		pojoInfo.insertSql = "insert into %s (" + Util.join(pojoInfo.insertColumnNames) + // comma sep list?
				") values (" + Util.getQuestionMarks(pojoInfo.insertSqlArgCount) + ")";
	}

	public void makeUpsertSql(StandardPojoInfo pojoInfo) {
	}

	private void makeSelectColumns(StandardPojoInfo pojoInfo) {
		if (pojoInfo.propertyMap.isEmpty()) {
			// this applies if the rowClass is a Map
			pojoInfo.selectColumns = "*";
		} else {
			ArrayList<String> cols = new ArrayList<>();
			for (Property prop : pojoInfo.propertyMap.values()) {
				cols.add(prop.name);
			}
			pojoInfo.selectColumns = Util.join(cols);
		}
	}

	@Override
	public String getSelectSql(Query query, Class<?> rowClass) {

		// unlike insert and update, this needs to be done dynamically
		// and can't be precalculated because of the where and order by

		StandardPojoInfo pojoInfo = getPojoInfo(rowClass);
		String columns = pojoInfo.selectColumns;

		String where = query.getWhere();
		String table = query.getTable();
		if (table == null) {
			table = pojoInfo.table;
		}
		String orderBy = query.getOrderBy();

		StringBuilder out = new StringBuilder();
		out.append("select ");
		out.append(columns);
		out.append(" from ");
		out.append(table);
		if (where != null) {
			out.append(" where ");
			out.append(where);
		}
		if (orderBy != null) {
			out.append(" order by ");
			out.append(orderBy);
		}
		return out.toString();
	}

	@Override
	public String getCreateTableSql(Class<?> clazz) {

		StringBuilder buf = new StringBuilder();

		StandardPojoInfo pojoInfo = getPojoInfo(clazz);
		buf.append("create table ");
		buf.append(pojoInfo.table);
		buf.append(" (");

		boolean needsComma = false;
		for (Property prop : pojoInfo.propertyMap.values()) {

			if (needsComma) {
				buf.append(',');
			}
			needsComma = true;

			Column columnAnnot = prop.columnAnnotation;
			if (columnAnnot == null) {

				buf.append(prop.name);
				buf.append(" ");
				buf.append(getColType(prop.dataType, 255, 10, 2));
				if (prop.isGenerated) {
					buf.append(" auto_increment");
				}

			} else {
				if (columnAnnot.columnDefinition() == null) {

					// let the column def override everything
					buf.append(columnAnnot.columnDefinition());

				} else {

					buf.append(prop.name);
					buf.append(" ");
					buf.append(getColType(prop.dataType, columnAnnot.length(), columnAnnot.precision(),
							columnAnnot.scale()));
					if (prop.isGenerated) {
						buf.append(" auto_increment");
					}

					if (columnAnnot.unique()) {
						buf.append(" unique");
					}

					if (!columnAnnot.nullable()) {
						buf.append(" not null");
					}
				}
			}
		}

		if (pojoInfo.primaryKeyName != null) {
			buf.append(", primary key (");
			buf.append(pojoInfo.primaryKeyName);
			buf.append(")");
		}

		buf.append(")");

		return buf.toString();
	}

	protected String getColType(Class<?> dataType, int length, int precision, int scale) {
		String colType;

		if (dataType.equals(Integer.class) || dataType.equals(int.class)) {
			colType = "integer";

		} else if (dataType.equals(Long.class) || dataType.equals(long.class)) {
			colType = "bigint";

		} else if (dataType.equals(Double.class) || dataType.equals(double.class)) {
			colType = "double";

		} else if (dataType.equals(Float.class) || dataType.equals(float.class)) {
			colType = "float";

		} else if (dataType.equals(BigDecimal.class)) {
			colType = "decimal(" + precision + "," + scale + ")";

		} else if (dataType.equals(java.util.Date.class)) {
			colType = "datetime";

		} else {
			colType = "varchar(" + length + ")";
		}
		return colType;
	}

	public Object convertValue(Object value, String columnTypeName) {
		return value;
	}

	@Override
	public String getDeleteSql(Query query, Object row) {

		StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());

		String table = query.getTable();
		if (table == null) {
			table = pojoInfo.table;
			if (table == null) {
				throw new DbException("You must specify a table name");
			}
		}

		String primaryKeyName = pojoInfo.primaryKeyName;

		return "delete from " + table + " where " + primaryKeyName + "=?";
	}

	@Override
	public Object[] getDeleteArgs(Query query, Object row) {
		StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
		Object primaryKeyValue = pojoInfo.getValue(row, pojoInfo.primaryKeyName);
		Object[] args = new Object[1];
		args[0] = primaryKeyValue;
		return args;
	}

	@Override
	public String getUpsertSql(Query query, Object row) {
		String msg = "There's no standard upsert implementation. There is one in the MySql driver, though,"
				+ "so if you're using MySql, call Database.setSqlMaker(new MySqlMaker()); Or roll your own.";
		throw new UnsupportedOperationException(msg);
	}

	@Override
	public Object[] getUpsertArgs(Query query, Object row) {
		throw new UnsupportedOperationException();
	}

}