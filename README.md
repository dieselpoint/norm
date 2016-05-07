#Norm

Norm is a simple way to access a JDBC database, usually in one line of code. It purges your code of the complex mess that is [Hibernate](http://www.hibernate.org), [JPA](http://en.wikipedia.org/wiki/Java_Persistence_API), and [ORM](http://en.wikipedia.org/wiki/Object-relational_mapping). 

[Lots of people think that complex ORMs are a bad idea.](http://stackoverflow.com/questions/398134/what-are-the-advantages-of-using-an-orm/398182)

Here it is:

```Java
List<Person> people = db.where("name=?", "Bob").results(Person.class);
```

###Get Started

[Configure your system](#configuration).

[Start with this sample code](#sample-code).

###Overview

Norm is an extremely lightweight layer over JDBC. It gets rid of large amounts of boilerplate JDBC code. It steals some ideas from [ActiveJDBC](hhttp://javalite.io/), which is a very nice system, but requires some very ugly instrumentation / byte code rewriting. 

###Why?

Sometimes the most important thing about writing software is knowing when to stop. A solution that gets you 90% of the way is often good enough, because the other 90% isn't worth the hassle. In this case, Norm gives you a fast and convenient way to do select, insert, update and delete, and when you need more, you just drop into straight SQL.

Norm returns results as a list of [POJOs](http://en.wikipedia.org/wiki/Plain_Old_Java_Object) or as a `List` of `Map` objects, whichever you prefer.

POJOs are fabulous, truly fabulous:

* Populating them is really fast with the newer JVMs.
* You can use them for declaratory data validation.
* Using [Jackson](https://github.com/FasterXML/jackson), you can serialize them to JSON.

which means that, yes, you can use the same class to fetch a record from a database and then create JSON from it.

###Sample Code

[There's a full example here.](https://github.com/dieselpoint/norm/blob/master/src/test/java/com/dieselpoint/norm/SampleCode.java)

```Java
Database db = new Database();

Person joe = new Person();
joe.firstName = "Joe";
joe.lastName = "Biden";

db.insert(joe);

List<Person> people = db.where("lastname=?", "Biden").orderBy("lastName").results(Person.class);
```

The `Person` class:

```Java
@Table(name="people")
class Person {
	public String firstName;
	public String lastName;
    // you can also use getters and setters
}
```
You can modify your database using .insert(), .upsert(), .update(), .delete(), and .sql().execute():

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

Don't want to create a new POJO class for every query? No problem, just use a Map:

```Java
List<Map> list = db.sql("select * from people").results(HashMap.class);
```

HashMap, LinkedHashMap or any class that implements the Map interface will work.

Note that you must specify full sql, or at a minimum a table name, because the system won't be able to guess the table name from the Map class. Unless you've annotated it to that effect.

###Primitives

A single column result set can come back in the form of a list of primitives, or even as a single primitive. 

```Java
Long count = db.sql("select count(*) from people").results(Long.class);
```

It's sometimes really useful to get a result in the form of a `List<String>`.

Note that you have to specify the full sql when doing primitives because the system won't be able to guess the column or tables names from the primitive class.


###Annotations

Tell the system what to do with your POJOs by using a few annotations. Norm implements a subset of the `javax.persistence` annotations, including [@Table](http://docs.oracle.com/javaee/7/api/javax/persistence/Table.html), [@Id](http://docs.oracle.com/javaee/7/api/javax/persistence/Id.html), [@GeneratedValue](http://docs.oracle.com/javaee/7/api/javax/persistence/GeneratedValue.html), [@Transient](http://docs.oracle.com/javaee/7/api/javax/persistence/Transient.html), [@Column](http://docs.oracle.com/javaee/7/api/javax/persistence/Column.html) and [@Enumerated](http://docs.oracle.com/javaee/7/api/javax/persistence/Enumerated.html).

```Java
@Table(name="people")
public class Person {
	@Id
	@GeneratedValue
	public long personId;

	public String Name;

	@Column(name = "theColumnName")
	public String renameThis;

	@Transient
	public String thisFieldGetsIgnored;
}

```

`@Table` specifies the table name. If it's not there, the table defaults to the class name.

`@Id` specifies the primary key. The system uses this to identify the record to delete or update.

`@GeneratedValue` indicates that the field is marked AUTO_INCREMENT and will be generated on the server. This prevents the field from being inserted, and it fills in the value in the POJO after an insert.

`@Transient` tells the system to ignore the field. It doesn't get persisted to the database. (Note that this is `javax.persistence.Transient`, not `java.beans.Transient`. Different annotations.)

`@Column` implements a subset of `javax.persistence.Column`. `Column.name` will attach a property to a database column of a different name. `Column.unique`, `.nullable`, `.length`, `.precision`, and `.scale` apply when you call `Database.createTable()`;

`@Enumerated` specifies type of enumeration to be stored in the database. By default `EnumType.STRING` is used for string representation. One can select `EnumType.ORDINAL` for integer representation.


Column-level annotations can go on either a public property or on a public getter for the property. Annotations on setters will be ignored.


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

```Java
// if fails will auto rollback
db.transaction(new Runnable() {
        @Override
        public void run() {
            db.insert(row);
            db.update(row);
            db.delete(row);
        }
});
```

Transaction is a pretty simple class, so if it doesn't do what you need,  just subclass it and make it behave differently.

###Custom Serialization

Sometimes it's hard to map a property in your POJO to a database field. For example, suppose your POJO property is List<String>, but you want your database field to be a plain, comma-separated string. You need a way to tell the system how to convert your list to the proper format.

Do it like this:

```Java
class MyPojo {
	@DbSerializer(MySerializer.class)
	public List<String> myList;
}

class MySerializer implements DbSerializable {

	@Override
	public String serialize(Object in) {
		return in.toString();
	}

	@Override
	public Object deserialize(String in, Class<?> targetClass) {
		Object out = // convert the string back to a list here
		return out;
	}
}

```
You can sometimes achieve the same purpose by using appropriate getters and setters on your POJO. Mark the ones that Norm should ignore with @Transient.


###Pluggable SQL Flavors

You can specify the particular flavor of SQL for your database with `Database.setSqlMaker()`. By default, the `StandardSqLMaker` will handle most needs. As of version 0.8.1, there is also a `MySqlMaker` class that will handle MySql-style upserts. To implement your own flavor, subclass `StandardSqlMaker` and possibly `StandardPojoInfo` and do what you need.

```Java
Database db = new Database();
db.setSqlMaker(new MySqlMaker());
```
Some database-specific notes:

MySQL: Should work out of the box.

Postgres: Inexplicably, Postgres converts all column names to lowercase when you create a table, and 
forces you to use double quotes around column names if you want mixed or upper case. The workaround
is to add an @Column(name="somelowercasename") annotation to the fields in your pojo.

###Configuration
Here's the Maven dependency:

```
<dependency>
    <groupId>com.dieselpoint</groupId>
    <artifactId>norm</artifactId>
    <version>0.8.1-rc2</version>
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

In particular, you might want to override .getDataSource() to set the maximum number of connections that the connection pool opens. By default, it's set to 100, but that is really excessive if you have a large number of servers connecting to a single database. Hikari opens all the connections on startup and leaves them open. This can be a burden on your database server.

###Dependencies
Norm needs javax.persistence, but that's just for annotations.

It also has a dependency on HikariCP for connection pooling, but that's entirely optional. If you don't want it, add an `<exclude>`  to the Norm dependency in your project's pom. Then subclass Database and override the getDataSource() method. 

Finally, you'll need to include your JDBC driver as a dependency. Here's a sample for MySQL:

```
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.32</version>
</dependency>
```  

****

That's about it. Post any bugs or feature requests to the issue tracker. Post any support requests to Stack Overflow.






