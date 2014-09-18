#Norm

Norm is a crazy simple way to access a JDBC database, usually in one line of code. It purges your code of the devil's spawn that is [Hibernate](http://www.hibernate.org), [JPA](http://en.wikipedia.org/wiki/Java_Persistence_API), [ORM](http://en.wikipedia.org/wiki/Object-relational_mapping), and other very bad no-good software. 

[Lots of people think that complex ORMs are a bad idea.](http://stackoverflow.com/questions/398134/what-are-the-advantages-of-using-an-orm/398182)

Let's get it on:

```Java
List<Person> people = db.where("name=?", "Bob").results(Person.class);
```

###Get Started

[Configure your system](#configuration).

[Start with this sample code](#sample code).

###Overview

Norm is an extremely lightweight layer over JDBC. It gets rid of large amounts of boilerplate JDBC code. It steals some ideas from [ActiveJDBC](http://code.google.com/p/activejdbc/), which is a very nice system, but requires some very ugly instrumentation / byte code rewriting. 

###Why?

Sometimes the most important thing about writing software is knowing when to stop. A solution that gets you 90% of the way is often good enough, because the other 90% isn't worth the hassle. In this case, Norm gives you a fast and convenient way to do select, insert, update and delete, and when you need more, you just drop into straight SQL.

Norm returns results as a list of [POJOs](http://en.wikipedia.org/wiki/Plain_Old_Java_Object) or as a `List` of `Map` objects, whichever you prefer.

POJOs are fabulous, truly fabulous:

* With the newer JVMs, populating them is really fast.
* You can use them for declaratory data validation among other things.
* Using [Jackson](https://github.com/FasterXML/jackson), you can serialize them to JSON.

which means that, yes, you can use the same class to fetch a record from a database and then create JSON from it.

###Sample Code

[There's a full example here.](https://github.com/dieselpoint/norm/blob/master/src/test/java/com/dieselpoint/norm/Test.java)

```Java
Database db = new Database();

Person joe = new Person();
joe.firstName = "Joe";
joe.lastName = "Biden";

db.insert(joe);

List<Person> people = db.where("lastname=?", "Biden").orderBy("lastName").results(Person.class);
```

The very simple Person class:

```Java
@Table(name="people")
class Person {
	public String firstName;
	public String lastName;
    // you can also use getters and setters
}
```
You can modify your database using .insert(), .update(), .delete(), and .sql().execute():

```Java
int rowsAffected = db.table("people").where("firstName=?", "Joe").delete();

// or just:
int rowsAffected = db.delete(joe);

// rowsAffected will equal the number of rows inserted, updated, or deleted

```

When you need more than this, just use straight SQL. This is the best way to do joins:

```Java
List<MyPojo> list1 = db.sql(
    "select lastname, sum(amount) from account, transaction " + 
    "where account.accountId = transaction.accountId " +
	"and date > ?", "2000-01-01")
	.results(MyPojo.class);
```

You can also use straight SQL to modify the database:

```Java
db.sql("drop table people").execute();
```

###Maps and Lists

Don't want to create a new POJO class for every freakin' query? No problem, just use a Map:

```Java
List<Map> list = db.sql("select * from people").results(HashMap.class);
```

HashMap, LinkedHashMap or any class that implements the Map interface will work.

###Annotations

Tell the system what to do with your POJOs by using a few annotations. Norm implements a subset of the `javax.persistence` annotations, including [@Table](http://docs.oracle.com/javaee/7/api/javax/persistence/Table.html), [@Id](http://docs.oracle.com/javaee/7/api/javax/persistence/Id.html), [@GeneratedValue](http://docs.oracle.com/javaee/7/api/javax/persistence/GeneratedValue.html) and [@Transient](http://docs.oracle.com/javaee/7/api/javax/persistence/Transient.html). (I'll probably add [@Column](http://docs.oracle.com/javaee/7/api/javax/persistence/Column.html) soon.)

```Java
@Table(name="people")
public class Person {
	@Id
	@GeneratedValue
	public long personId;

	public String Name;

	@Transient
	public String thisFieldGetsIgnored;
}

```

`@Table` specifies the table name. If it's not there, the table defaults to the class name.

`@Id` specifies the primary key. The system uses this to identify the record to delete or update.

`@GeneratedValue` indicates that the field is marked AUTO_INCREMENT and will be generated on the server. This prevents the field from being inserted, and it fills in the value in the POJO after an insert.

`@Transient` tells the system to ignore the field. It doesn't get persisted to the database.


###Transactions

If you need multiple database operations to succeed or fail as a unit, use a transaction. The basic scheme is to create a Transaction object, pass it to every query that needs it, and then .commit() or .rollback().

```Java
Transaction trans = db.startTransaction();
try {
	db.transaction(trans).insert(row1);
	db.transaction(trans).update(row2);
	trans.commit();
} catch (Throwable t) {
	trans.rollback();
} 

```
Transaction is a pretty simple class, so if it doesn't do what you need,  just subclass it and make it behave differently.


###Configuration
Here's the Maven dependency:

```
<dependency>
    <groupId>com.dieselpoint</groupId>
    <artifactId>norm</artifactId>
    <version>0.7</version>
</dependency>
```  

To specify the database connection parameters:

```Java
System.setProperty("norm.dataSourceClassName", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
System.setProperty("norm.serverName", "localhost");
System.setProperty("norm.databaseName", "mydb");
System.setProperty("norm.user", "root");
System.setProperty("norm.password", "rootpassword");
```
Internally, Norm uses the [Hikari](http://brettwooldridge.github.io/HikariCP/) connection pool. Hikari has a complete list of [DataSource class names](https://github.com/brettwooldridge/HikariCP#popular-datasource-class-names). Your database is bound to be on the list.

If you don't want to use system properties, or your DataSource needs some custom startup parameters, just subclass the [Database](https://github.com/dieselpoint/norm/blob/master/src/main/java/com/dieselpoint/norm/Database.java) class and override the .getDataSource() method. You can supply any DataSource you like.

And, of course, you need to include your JDBC driver as a dependency.

****

That's about it. Post any bugs or feature requests to the issue tracker.






