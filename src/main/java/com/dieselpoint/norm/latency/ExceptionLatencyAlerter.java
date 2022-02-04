package com.dieselpoint.norm.latency;

import com.dieselpoint.norm.DbException;

/**
 * For use in development/testing environment, throws an Exception when the latency has exceeded the threshold.
 * For early warning of latency issues, it's good practise to prevent tests from passing when latency exceeds the threshold
 */
public class ExceptionLatencyAlerter implements LatencyAlerter {

    public ExceptionLatencyAlerter() {
    }

    @Override
    public void alertLatencyFailure( DbLatencyWarning warning ) {
        if (warning.maxAcceptableLatency > 0)
            throw new DbException( warning.toString() );
    }
}