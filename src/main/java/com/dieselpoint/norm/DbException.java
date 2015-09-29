package com.dieselpoint.norm;

@SuppressWarnings("serial")

/**
 * Generic unchecked database exception. 
 */
public class DbException extends RuntimeException {
	
	public DbException() {}
	
	public DbException(String msg) {
		super(msg);
	}

	public DbException(Throwable t) {
		super(t);
	}

	public DbException(String msg, Throwable t) {
		super(msg, t);
	}
	
}
