package com.dieselpoint.norm.latency;

/**
 * Interface used to alert administrators to latency issues. Multiple LatencyAlerters can be added to a Database, using
 * the addLatencyAlerter. They will be called in order that they are added.<br>
 * Implementations are provided that log with Slf4j, that log to standard out and that throw an exception, but
 * it should be trivial to forward the latency alert to e.g. AWS Cloudwatch, PagerDuty, or honeybadger.io
 */
public interface LatencyAlerter {
    public void alertLatencyFailure( DbLatencyWarning warning );
}
