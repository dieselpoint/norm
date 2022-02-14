package com.dieselpoint.norm.latency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Random;

/**
 * One of the dangers when reporting latency issues to external services, is that the reporting itself a) takes a
 * significant amount of time and may create Customer Experience issues, and b) you end up with millions of latency
 * alerts when a database goes bad. <p></p>This class implements a basic "exponential backoff with jitter" algorithm. Subclasses
 * can simply implement {@link BackoffLatencyAlerter#alertLatencyFailureAfterBackoffAndJitter(DbLatencyWarning, long)}
 * to take advantage of the exponential backoff facility.
 * <p>{@code var cwAlerter = CloudWatchAlerter( Duration.ofMillis( 500 ), Duration.ofMinutes( 10 ) ); } will initially
 * alert at 500ms intervals, then 1000ms (1 second), 2 seconds, 4 seconds .... 10 minutes.
 * <p>For more information, refer to
 * <a href="https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/">Exponential Backoff And Jitter</a>
 * by Amazon Web Services
 * <p>When implementing your alerter, remember to swallow errors. You don't want your platform slowing down/failing
 * because the monitoring service is failing. See the
 * {@link BackoffLatencyAlerter#alertLatencyFailureAfterBackoffAndJitter(DbLatencyWarning, long)} documentation for
 * further steps to ensure that monitoring doesn't accidentally become a significant overhead.
 */
@SuppressWarnings( "unused" )
public abstract class BackoffLatencyAlerter implements LatencyAlerter {
    private static final Logger logger = LoggerFactory.getLogger( BackoffLatencyAlerter.class );

    private final long minimumReportingLatencyMillis, maximumReportingIntervalMillis;
    private long nextReportTime;
    private double backoffs;
    private long alertsSwallowedWhileWaiting = 0;
    private final Random random = new Random();

    public BackoffLatencyAlerter( Duration minimumReportingInterval, Duration maximumReportingInterval ) {
        this.minimumReportingLatencyMillis = minimumReportingInterval.toMillis();
        this.maximumReportingIntervalMillis = maximumReportingInterval.toMillis();
        this.backoffs = 1;
        this.nextReportTime = System.currentTimeMillis();
    }

    private long calculateWaitTime() {
        backoffs += (alertsSwallowedWhileWaiting > 0) ? 1 : -0.25; // come back down much more slowly than we went up
        backoffs = backoffs < 1 ? 1 : backoffs;

        // timeToWait = (base * 2^n) +/- (jitter)
        long jitter = (minimumReportingLatencyMillis/2) - (random.nextLong() % minimumReportingLatencyMillis);
        long timeToWait = (minimumReportingLatencyMillis * (long)Math.pow(2, backoffs) ) + jitter;
        if (timeToWait > maximumReportingIntervalMillis) {
            // timeToWait = maximumReportingIntervalMillis; - NO, we still want the jitter included
            --backoffs;
        }
        return timeToWait;
    }

    @Override
    public synchronized void alertLatencyFailure( DbLatencyWarning warning ) {
        if (warning.maxAcceptableLatency != 0) {
            long myTime = System.currentTimeMillis();
            if (nextReportTime <= myTime) {
                if (alertLatencyFailureAfterBackoffAndJitter( warning, alertsSwallowedWhileWaiting ) == false)
                    ++alertsSwallowedWhileWaiting;
                nextReportTime = myTime + calculateWaitTime();
                alertsSwallowedWhileWaiting = 0;
            }
            else {
                logger.info( "Swallowed latency failure:" + warning );
                ++alertsSwallowedWhileWaiting;
            }
        }
    }

    /**
     * @param warning the latency warning
     * @param numberOfAlertsSwallowed the number of alerts that were swallowed during the exponential backoff period. This
     *                                might (or might not) be interesting to report alongside the current issue. It'll
     *                                definitely give you a sense of how bad things have gone!
     * @return true if notifying the remote service was successful, false otherwise. If false, then we'll automatically
     *          backoff calls to reporting in the same way as latency failures, to avoid a slowdown / issue on
     *          monitoring impacting the actual customer experience
     */
    public abstract boolean alertLatencyFailureAfterBackoffAndJitter( DbLatencyWarning warning, long numberOfAlertsSwallowed );
}
