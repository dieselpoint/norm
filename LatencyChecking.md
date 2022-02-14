# Norm Latency Checking

Norm is awesome! Having spent years running heavyweight ORM solutions, norm is the perfect combination of a lighweight library that makes life super-easy, while still letting me get my hands really dirty.

The one feature that I feel strongly belongs to the 90% (see [README.md](README.md)) is latency checking. I've learned the hard way that actively managing the database is crucial and that (no matter how well intentioned your development team) performance will degrade over time. A few milliseconds a month, a couple of dodgy queries, and suddently your site is performing like a tortoise racing through treacle because the database can't clear the backlog before the next tranche of queries turns up.

The best solution I've found is to be super-opinionated about what constitutes acceptable database latency from day one of development; to use tooling that immediately reports (and ideally breaks your tests) when latency breaches reasonable boundaries in your dev/test environments; and carry that tooling into production to keep an eye on degredation over time.

So I've extended norm to add latency checking functionality. The code should be transparent, backward-compatible and introduces no additional dependencies. Now you can do:

```java
Database db = new Database();
...
db.setMaxLatency(30); // alert every time any call to the database takes >30 milliseconds

// report latency violations using an slf4j logger
db.addLatencyAlerter( new Slf4jLatencyAlerter() );

// also throw an exception when latency violations occur (only sensible in development/test!)
db.addLatencyAlerter( new ExceptionLatencyAlerter() ); 

// raise an alert if latency on these queries are >20ms
var people = db.where("lastname=?", "Sixpack").maxLatency(20).results(Person.class);
db.sql("select count(*) from people").maxLatency(20).first(Long.class);

// transactions work in exactly the same way
Transaction trans = db.startTransaction().maxLatency(30);
try {
	...
	trans.commit();
} catch (Throwable t) {
	trans.rollback();
} 
```

Should any of the queries / transaction commits exceed the max latency (whether global or for an individual transaction), then a message is logged showing the actual and expected time, along with the offending line of code.

One fun side-effect of this functionality is that setting maxLatency to 0ms, causes all SQL Statements to be logged, along with their elapsed execution time. By default maxLatency is set to -1: an ugly magic number but one that skips all the latency code.

It's also worth noting that setting the latency on a Query/Transaction supercedes the global latency setting for the Database. This is deliberate so you can (for example) set the global latency to 30ms and latency for a super-complex transaction to 50ms.

In keeping with existing norm capabilities, you can also set the global Database latency using environment variable `norm.maxLatency`

As you can see from the code above, you add `LatencyAlerters` to the Database instance. These are called in the order which they were added. There are four simple LatencyAlerters included, and it should be trivial to add more.

As an example of a LatencyAlerter, at [Divrsity](https://divrsity.team) we use HoneyBadger.io to raise an alert any time the database latency threshold is exceeded. Of course, you really don't want to further compromise customer experience due to the wait time for reporting (and you don't want a million alerts) so you'll want to implement some kind of backoff+jitter algorithm. A basic implementation is included: just subclass BackoffLatencyAlerter and implement the alertLatencyFailureAfterBackoffAndJitter() method. Then you can do:

```java
var hbAlerter = HoneyBadgerAlerter( Duration.ofMillis( 500 ), Duration.ofMinutes( 10 ) );
db.addLatencyAlerter( hbAlerter );
```

to report at most every 0.5 seconds, and at least once every 10 minutes.

N.B. Checking latency on Transaction rollback would be trivial, but I'd need to be convinced that it makes sense. If you're routinely rolling back transctions then you probably need to look at your logic (or just copy the missing two lines of code from the commit method to the rollback method).

The code has been running in production on for about a month or so, with a maxLatency of 25ms. We use AWS Aurora Postgres (which is properly awesome) and, so far, have seen just one (temporary) latency breach.

Mark(at)divrsity(dot)team

