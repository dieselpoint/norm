package com.dieselpoint.norm2;

@SuppressWarnings("serial")
public class DbException extends RuntimeException {
	
	public DbException() {}
	
	public DbException(String msg) {
		super(msg);
	}

	public DbException(Throwable t) {
		super(t);
	}

}
