package com.dieselpoint.norm.latency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLatencyAlerter implements LatencyAlerter {
    private static final Logger logger = LoggerFactory.getLogger( Slf4jLatencyAlerter.class );
    private final Logger instanceLogger;

    public Slf4jLatencyAlerter() {
        this( logger );
    }

    public Slf4jLatencyAlerter( Logger theLoggerToUse ) {
        instanceLogger = theLoggerToUse;
    }

    @Override
    public void alertLatencyFailure( DbLatencyWarning warning ) {
        if (warning.maxAcceptableLatency == 0)
            instanceLogger.info( warning.toString() );
        else
            instanceLogger.warn( warning.toString() );
    }
}
