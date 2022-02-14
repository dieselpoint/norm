package com.dieselpoint.norm.latency;

public class StdoutLatencyAlerter implements LatencyAlerter {

    public StdoutLatencyAlerter() {
    }

    @Override
    public void alertLatencyFailure( DbLatencyWarning warning ) {
        System.out.println( warning.toString() );
    }
}
