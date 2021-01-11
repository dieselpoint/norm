package com.dieselpoint.norm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BigIntBug {

	public static void main(String[] args) throws SQLException {

		Connection conn = DriverManager
				.getConnection("jdbc:mysql://localhost:3306/mydb?useSSL=false&allowPublicKeyRetrieval=true&user=root");

		Statement state = conn.createStatement();
		state.executeUpdate("drop table if exists foo");
		state.executeUpdate("create table foo (id bigint auto_increment, name varchar(255), primary key(id))");
		state.executeUpdate("insert into foo (name) values ('bob')", Statement.RETURN_GENERATED_KEYS);

		ResultSet rs = state.getGeneratedKeys();
		rs.next();

		Object obj = rs.getObject(1);

		System.out.println(obj);
		System.out.println(obj.getClass());

		conn.close();

	}

}
