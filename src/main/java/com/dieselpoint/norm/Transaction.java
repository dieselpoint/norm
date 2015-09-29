package com.dieselpoint.norm;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;

/**
 * Represents a database transaction. Create it using Transaction trans =
 * Database.startTransation(), pass it to the query object using
 * .transaction(trans), and then call trans.commit() or trans.rollback().
 * <p>
 * Some things to note: commit() and rollback() also call close() on the
 * connection, so this class cannot be reused after the transaction is committed
 * or rolled back.
 * </p>
 * <p>
 * This is just a convenience class. If the implementation is too restrictive,
 * then you can manage your own transactions by calling Database.getConnection()
 * and operate on the Connection directly.
 * </p>
 */
public class Transaction implements Closeable {
	private Connection con;

	// package-private
	void setConnection(Connection con) {
		this.con = con;
		try {
			con.setAutoCommit(false);
		} catch (Throwable t) {
			throw new DbException(t);
		}
	}

	public void commit() {
		try {
			con.commit();
		} catch (Throwable t) {
			throw new DbException(t);
		} finally {
			try {
				con.close();
			} catch (Throwable t) {
				throw new DbException(t);
			}
		}
	}

	public void rollback() {
		try {
			con.rollback();
		} catch (Throwable t) {
			throw new DbException(t);
		} finally {
			try {
				con.close();
			} catch (Throwable t) {
				throw new DbException(t);
			}
		}
	}

	public Connection getConnection() {
		return con;
	}

	/**
	 * This simply calls .commit();
	 */
	@Override
	public void close() throws IOException {
		commit();
	}

}
