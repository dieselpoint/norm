package com.dieselpoint.norm;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Represents a database transaction. Create it using Entity.startTransation(),
 * pass it to various methods like insert(Transaction) and update(Transaction),
 * and then call commit() or rollback(). 
 * <p>
 * Some things to note: commit() and rollback() also call close() on the connection,
 * so this class cannot be reused after the transaction is committed or rolled back.
 * </p>
 * <p>
 * Also, the commit() and rollback() methods bury any SQLExceptions in an unchecked
 * RuntimeException. The reason is that such exceptions are rare and really are fatal,
 * and this scheme eliminates a ton of boilerplate exception-handling code in the client.
 * </p>
 * <p>
 * This is just a convenience class. If the implementation is too restrictive,
 * then you can manage your own transations by calling Entity.getConnection() and operate 
 * on the Connection directly.
 * </p>
 */
public class Transaction implements Closeable {
	private Connection con;
	
	// package-private
	void setConnection(Connection con) throws SQLException {
		this.con = con;
		con.setAutoCommit(false);
	}
	
	public void commit() {
		try {
			con.commit();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void rollback() {
		try {
			con.rollback();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public Connection getConnection() {
		return con;
	}

	@Override
	public void close() throws IOException {
		commit();
	}


}
