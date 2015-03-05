Add more unit tests. Test:

select
insert 
update
delete
upsert

with:

pojo
map
primitive
 


Release notes

v.0.8.1 includes a general refactoring of the code to support multiple flavors
of SQL. The primary motivation was to support an .upsert() command, which is supported
using different syntax in different databases.

Actually, that's not true. Almost all databases support the MERGE command. But not MySql.
Thanks, MySql. Let's all move to Postgres, shall we?

This release also includes:
  - Support for @Column 
  - A new, non-standard annotation, @DbSerializable.
  - Support for primitive classes (Integer, Long, String) as POJOs.
  - Better support for Enum fields. 
  
See the docs for more.



