package com.dieselpoint.norm;

import com.dieselpoint.norm.latency.LatencyTimer;

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
	private Database db;
	private long maxLatency;

	Transaction() {
		this.maxLatency = -1;
	}

	// package-private
	Transaction( Database db, Connection con ) throws DbException {
		this.db = db;
		this.con = con;
		this.maxLatency = db.getMaxLatencyMillis();
		setConnection( con );
	}

	// package-private
	void setConnection(Connection con) throws DbException {
		this.con = con;
		try {
			con.setAutoCommit(false);
		} catch (Throwable t) {
			throw new DbException(t);
		}
	}

	public void commit() {
		try {
			LatencyTimer myLatencyTimer = new LatencyTimer( this );
			con.commit();
			myLatencyTimer.stop( this );
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

	public Database getDatabase() { return db; }

	/**
	 * sets the maximum acceptable latency for this transaction. Must be called before {@link #commit()}.
	 * <br>If latency of the query exceeds the threshold then the
	 * {@link com.dieselpoint.norm.latency.LatencyAlerter} that have been added to the
	 * {@link Database} will be called in order.
	 * @param millis maximum number of milliseconds that a query can take to execute before an alert will be generated
	 * @return {@code this}, to enable maxLatency to be chained, a la {@code trans.maxLatency("Ten People Transaction", 50).commit()}
	 */
	public Transaction maxLatency( long millis ) {
		this.maxLatency = millis;
		return this;
	}

	public long getMaxLatencyMillis() { return maxLatency; }

}
