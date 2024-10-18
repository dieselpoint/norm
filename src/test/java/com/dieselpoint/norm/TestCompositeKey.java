package com.dieselpoint.norm;

import com.dieselpoint.norm.sqlmakers.MySqlMaker;
import org.junit.Before;
import org.junit.Test;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestCompositeKey
{
	private Database db;

	@Before
	public void initDB(){
		Setup.setSysProperties();

		db = new Database();
		db.setSqlMaker(new MySqlMaker());
		db.sql("drop table if exists selecttest").execute();

		db.createTable(Row.class);

		Row row = new Row();
		row.id = 99;
		row.otherId = "id_2";
		row.name = "bob";
		db.insert(row);
	}

	@Test
	public void testPrimitive()
	{
		long myId = db.sql("select id from selecttest").first(Long.class);
		assertEquals(99,myId);
	}

	@Test
	public void testMap()
	{
		Map myMap = db.table("selecttest").first(LinkedHashMap.class);
		String str = myMap.toString();
		assertEquals("{id=99, otherId=id_2, name=bob}", str);
	}

	@Test
	public void testPOJO() {
		Row myRow = db.first(Row.class);
		String myRowStr = myRow.toString();
		assertEquals("99bob", myRowStr);
	}

	@Test
	public void testSecondInsertConflictingFirstKey() {
		Row row = new Row();
		row.id = 99;
		row.otherId = "id_3";
		row.name = "bob";
		db.insert(row);

		List<Row> myRow = db.results(Row.class);
		assertEquals(2, myRow.size());
		long row1FirstId = myRow.get(0).id;
		assertEquals(99, row1FirstId);
		String row1SecondId = myRow.get(0).otherId;
		assertEquals("id_2", row1SecondId);
		long row2FirstId = myRow.get(1).id;
		assertEquals(99, row2FirstId);
		String row2SecondId = myRow.get(1).otherId;
		assertEquals("id_3", row2SecondId);
	}

	@Test
	public void testUpdate() {
		Row row = new Row();
		row.id = 99;
		row.otherId = "id_2";
		row.name = "bob2";
		db.update(row);

		Row myRow = db.first(Row.class);
		assertEquals(99, myRow.id);
		assertEquals("id_2", myRow.otherId);
		assertEquals("bob2", myRow.name);
	}

	@Test
	public void testUpsertInsert() {
		Row row = new Row();
		row.id = 99;
		row.otherId = "id_3";
		row.name = "bob2";
		db.upsert(row);

		List<Row> myRow = db.results(Row.class);
		assertEquals(2, myRow.size());
		long row1FirstId = myRow.get(0).id;
		assertEquals(99, row1FirstId);
		String row1SecondId = myRow.get(0).otherId;
		assertEquals("id_2", row1SecondId);
		long row2FirstId = myRow.get(1).id;
		assertEquals(99, row2FirstId);
		String row2SecondId = myRow.get(1).otherId;
		assertEquals("id_3", row2SecondId);
	}

	@Test
	public void testUpsertUpdate() {
		Row row = new Row();
		row.id = 99;
		row.otherId = "id_2";
		row.name = "bob2";
		db.upsert(row);

		Row myRow = db.first(Row.class);
		assertEquals(99, myRow.id);
		assertEquals("id_2", myRow.otherId);
		assertEquals("bob2", myRow.name);
	}

	@Test
	public void testDelete() {
		Row row = new Row();
		row.id = 99;
		row.otherId = "id_2";
		db.delete(row);

		List<Row> myRow = db.results(Row.class);
		assertEquals(0, myRow.size());
	}

	@Table(name="selecttest")
	public static class Row {
		@Id
		public long id;

		@Id
		public String otherId;

		public String name;
		public String toString() {
			return id + name;
		}
	}

}
