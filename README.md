Norm
====

A simpler way to access a database. Norm stands for "Not Object-Relational Mapping", although it kind of is.

##Overview

Norm is an extremely lightweight layer over JDBC. It gets rid of large amounts of boilerplate JDBC code
without all the overhead of Hibernate, JPA or other ORM solutions. It steals some ideas from
ActiveJDBC, which is a very nice system, but doesn't require the very ugly instrumentation/byte code
rewriting that comes with it.

Norm uses:
*No annotations
*No instrumentation
*No byte code games
*No reflection

##Why?

Sometimes the most important thing about writing software is knowing when to stop. A solution that gets
you 90% of the way is often good enough, because the other 90% isn't worth the hassle. In this case, 
Norm gives you a fast and convenient way to do select, insert, update and delete, and when you need
more, you just drop into straight SQL.

Norm returns results as a List of Maps, where each Map is a record of name/value pairs. Names are Strings 
and values are Java primitives. This is a really nice structure because it maps directly to JSON and
similar data structures.

##Sample Code

If you have a table named "account", create a class name "Account" to access it:

```Java
public class Account extends Entity {
}  
```
   
Account extends Entity, which contains all of the system's functionality.

