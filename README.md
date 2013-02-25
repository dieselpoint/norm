#Norm

A simpler way to access a database. Norm stands for "Not Object-Relational Mapping", although it kind-of is.

###Overview

Norm is an extremely lightweight layer over JDBC. It gets rid of large amounts of boilerplate JDBC code
without all the overhead of [Hibernate](http://www.hibernate.org), [JPA](http://en.wikipedia.org/wiki/Java_Persistence_API)
 or other [ORM](http://en.wikipedia.org/wiki/Object-relational_mapping) solutions. It steals some ideas from
[ActiveJDBC](http://code.google.com/p/activejdbc/), which is a very nice system, but requires some very ugly 
instrumentation / byte code rewriting. [Lots of people think that complex ORMs are a bad idea.](http://stackoverflow.com/questions/398134/what-are-the-advantages-of-using-an-orm/398182)

Norm uses:
* No annotations
* No instrumentation
* No byte code games
* No reflection

###Why?

Sometimes the most important thing about writing software is knowing when to stop. A solution that gets
you 90% of the way is often good enough, because the other 90% isn't worth the hassle. In this case, 
Norm gives you a fast and convenient way to do select, insert, update and delete, and when you need
more, you just drop into straight SQL.

Norm returns results as a List of Entity objects, where each Entity is a small wrapper around a Map, and the Map is a 
record of name/value pairs. Names (columns) are Strings and values are Java primitives. This is a really nice structure because it 
maps directly to JSON.

###Sample Code

If you have a table named "account", create a class named "Account" to access it:

```Java
public class Account extends Entity {
}  
```
   
Account extends Entity, which contains all of the system's functionality. To access the database:

```Java
Account acct = new Account();
		
acct.put("accountId", 1);
acct.put("firstname", "Joe");
acct.put("lastname", "ThePlumber"); 
acct.insert();

acct.put("accountId", 2);
acct.put("firstname", "Joe");
acct.put("lastname", "Biden");
acct.insert();

acct.where("accountId=?", 1).delete();
System.out.println("rows deleted:" + acct.getRowsAffected());
		
acct.getRecord().clear();
acct.put("accountId", 2);
acct.put("lastname", "Superstar");
acct.update();
		
List<Account> list = acct.where("firstname=?", "Joe").orderBy("lastname").results();
for (Account a: list) {
    System.out.println(a.getString("firstname") + " " + a.getString("lastname"));
}
```

When you need more than this, just use straight SQL:

```Java
List<Entity> list1 = (new Entity()).sql(
    "select lastname, sum(amount) from account, transaction " + 
    "where account.accountId = transaction.accountId and date > ?", "2000-01-01").results();
```

Note that you don't have to subclass Entity; you can use it directly. Subclassing is useful for telling the system 
about the table name and the primary key and for adding table-specific methods. 

By default, the table name is the lowercased class name, and the primary key is tablename + "Id". Override `.getTableName()` and `.getPrimaryKeyName()` to 
specify custom names. 

A subclass doesn't have to correspond to a table. It could represent any result set.

Internally, an Entity stores the column/value pairs in a Map. The map is available using `.getRecord()`. 

###Transactions

If you need multiple database operations to succeed or fail as a unit, use a transaction.

```Java
Transaction trans = null;
		
try {
	MyEntity myEntity = new MyEntity();
	trans = myEntity.startTransaction();
		
	// do inserts, updates, deletes here
			
	myEntity.insert(trans);
	someOtherEntity.delete(trans);
		
	trans.commit();
		
} catch (Throwable t) {
	if (trans != null) {
		trans.rollback();
	}
} 

```
Transactions wrap SQLExceptions in unchecked RuntimeExceptions to eliminate a lot of
boilerplate error-checking code. If this isn't your style, then you can implement your
own transaction code by calling Entity.getConnection() directly and passing it around
to the methods that need it. Transaction is a [pretty simple class](https://github.com/dieselpoint/norm/blob/master/src/main/java/com/dieselpoint/norm/Transaction.java).


###Configuration

To use Norm in your project, add this Maven dependency (COMING SOON, NOT YET IN MAVEN CENTRAL):

```
<dependency>
    <groupId>com.dieselpoint</groupId>
    <artifactId>norm</artifactId>
    <version>0.5</version>
</dependency>
```  

To specify the database connection parameters, you can do this:

```Java
System.setProperty("norm.driver", "com.mysql.jdbc.Driver");
System.setProperty("norm.databasename", "mydb");
System.setProperty("norm.url", "jdbc:mysql://localhost/mydb");
System.setProperty("norm.user", "root");
System.setProperty("norm.password", "rootpassword");
```

This isn't very secure, though, because all classes in your app will have access to the password. For a more
secure method, override `Entity.getDataSource()` and supply your own DataSource using whatever method you 
prefer, then have all your actual entities classes subclass that one. For example:

```Java
class MyEntity extends Entity {

    @Override
    protected DataSource getDataSource() throws SQLException {
        // return your own DataSource from a pool or some place else
        return aDataSource;
    }
}

class Account extends MyEntity {
}
```

Take a look in the source code for `DataSourceFactory` for an example of how to do it.

That's about it. Post any bugs or feature requests to the issue tracker.






