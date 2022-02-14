package com.dieselpoint.norm.latency;

import com.dieselpoint.norm.Database;
import com.dieselpoint.norm.Query;
import com.dieselpoint.norm.Transaction;

/**
 * Utility class that abstracts the starting / stopping of timers and checking whether sql duration was within threshold
 */
public class LatencyTimer {
    public final long startMillis;
    public long duration;
    public final long maxAcceptableLatency;
    public final Database db;

    public LatencyTimer( Query query ) {
        this.db = query.getDatabase();
        startMillis = System.currentTimeMillis();
        maxAcceptableLatency = query.getMaxLatencyMillis();
    }

    public LatencyTimer( Transaction transaction ) {
        this.db = transaction.getDatabase();
        startMillis = System.currentTimeMillis();
        maxAcceptableLatency = transaction.getMaxLatencyMillis();
    }

    /**
     * @return true if latency was within acceptable bounds, or maxAcceptableLatency is negative
     */
    private boolean stop() {
        if (maxAcceptableLatency < 0)
            return true;
        duration = System.currentTimeMillis() - startMillis;
        if (maxAcceptableLatency == 0)
            return false;
        return duration <= maxAcceptableLatency;
    }

    public boolean stop( String sql, Object[] args ) {
        if (stop() == false) {
            if (db != null) {
                db.alertLatency( new DbLatencyWarning( maxAcceptableLatency, duration, sql, args ) );
            }
        }
        return false;
    }

    public boolean stop( Transaction aTransaction ) {
        if (stop() == false) {
            if (db != null) {
                db.alertLatency( new DbLatencyWarning( maxAcceptableLatency, duration, aTransaction ) );
            }
        }
        return false;
    }


}
