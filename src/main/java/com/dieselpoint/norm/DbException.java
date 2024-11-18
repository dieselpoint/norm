package com.dieselpoint.norm;

@SuppressWarnings("serial")

/**
 * Generic unchecked database exception. 
 */
public class DbException extends RuntimeException {
	
	private final String sql;
	
	public DbException(String msg) {
		super(msg);
        sql = null;
	}

	public DbException(Throwable t) {
		super(t);
        sql = null;
	}

	public DbException(Throwable t, String sql) {
		super(t);
        this.sql = sql;
	}

    public DbException(String msg, Throwable t) {
		super(msg, t);
        sql = null;
	}
	
	public String getSql() {
		return sql;
	}
	
}
