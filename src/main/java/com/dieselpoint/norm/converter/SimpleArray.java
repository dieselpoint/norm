package com.dieselpoint.norm.converter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class SimpleArray implements java.sql.Array {
	
	private int baseType;
	private Object [] arr;
	
	public SimpleArray(int baseType, Object [] arr) {
		this.baseType = baseType;
		this.arr = arr;
	}

	@Override
	public String getBaseTypeName() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getBaseType() throws SQLException {
		return baseType;
	}

	@Override
	public Object getArray() throws SQLException {
		return arr;
	}

	@Override
	public Object getArray(Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getArray(long index, int count) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getArray(long index, int count, Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResultSet getResultSet(Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResultSet getResultSet(long index, int count) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResultSet getResultSet(long index, int count, Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void free() throws SQLException {
	}

}
