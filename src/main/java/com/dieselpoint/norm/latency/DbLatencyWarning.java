package com.dieselpoint.norm.latency;

import com.dieselpoint.norm.Transaction;

import java.util.Arrays;

/**
 * An exception-like class, that makes it easy to pass the messages, and stack trace associated
 * with a {@link com.dieselpoint.norm.Query} or {@link Transaction#commit()} database call that has
 * exceeded its latency threshold. LatencyAlerters can make a decision how to alert
 * the system administrators to the warning
 */
public class DbLatencyWarning {
    public final long maxAcceptableLatency;
    public final long actualLatency;
    public final String cause;
    public final String offendingStatement;

    protected DbLatencyWarning( long maxAcceptableLatency, long actualLatency, String cause ) {
        this.maxAcceptableLatency = maxAcceptableLatency;
        this.actualLatency = actualLatency;
        this.cause = cause;
        this.offendingStatement = getOffendingStatement();
    }

    public DbLatencyWarning(long maxAcceptableLatency, long actualLatency, String theNaughtySql, Object[] theNaughtyArgs ) {
        this( maxAcceptableLatency, actualLatency,
                        "SQL:" + theNaughtySql + ", SQL_Args:" + Arrays.deepToString(theNaughtyArgs) );
    }

    public DbLatencyWarning(long maxAcceptableLatency, long actualLatency, Transaction theNaughtyTransaction ) {
        this( maxAcceptableLatency, actualLatency, "Transaction commit exceeded threshold:" );
    }

    /**
     * @return the most recent call on the stack before any call to classes in the {@code com.dieselpoint.norm} package.
     * This ought to pinpoint the call that exceeded the latency threshold. Returns {@code "[Unknown]"}, when it can't
     * figure out the caller, i.e. will never return null
     */
    private String getOffendingStatement() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i=2; i<elements.length; i++) {
            StackTraceElement e = elements[i];
            // ignore everything in the com.dieselpoint.norm package
            if (e.getClassName().startsWith( "com.dieselpoint.norm.") == false)
                return e.toString();
        }
        return "[Unknown]";
    }

    public String toString() {
        if (maxAcceptableLatency == 0)
            return "Database Latency was: " + actualLatency + "ms, at " + offendingStatement + ". " + cause;
        return "Database Latency was: " + actualLatency + "ms, at " + offendingStatement + ", versus max acceptable: " + maxAcceptableLatency + "ms. Caused by " + cause;
    }
}
