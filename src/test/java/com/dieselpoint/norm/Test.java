package com.dieselpoint.norm;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

public class Test {

	public static void main(String[] args) throws SQLException {
		
		System.setProperty("norm.driver", "com.mysql.jdbc.Driver");
		System.setProperty("norm.databasename", "mydb");
		System.setProperty("norm.url", "jdbc:mysql://localhost/mydb");
		System.setProperty("norm.user", "root");
		System.setProperty("norm.password", "rootpassword");
		
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
		for (Account acct1: list) {
			System.out.println(acct1.toString());
		}
		
		List<Entity> list1 = (new Entity()).sql("select lastname, sum(amount) from account, transaction where account.accountId = transaction.accountId and date > ?", "2000-01-01").results();
		for (Entity acct1: list1) {
			System.out.println(acct1.toString());
		}
		
		
	}

	class MyEntity extends Entity {

		@Override
		protected DataSource getDataSource() throws SQLException {
			// return your own DataSource from a pool or someplace else
			return null;
		}
	}
	
}
