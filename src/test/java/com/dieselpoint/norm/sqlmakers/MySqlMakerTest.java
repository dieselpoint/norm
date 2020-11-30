package com.dieselpoint.norm.sqlmakers;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.dieselpoint.norm.Database;
import com.dieselpoint.norm.Query;

public class MySqlMakerTest {
    MySqlMaker sut;
    Database db;

    @Before
    public void setup() {
        sut = new MySqlMaker();
        db = mock(Database.class);

        when(db.getSqlMaker()).thenReturn(sut);
    }

    @Test
    public void getUpsertSql() {
        Query query = new Query(db);

        StandardSqlMakerTest.TestTable testTable = new StandardSqlMakerTest.TestTable();
        testTable.setId(2);
        testTable.setName("test");

        String updateSql = sut.getUpsertSql(query, testTable);

        assertEquals(updateSql, "insert into testTable (name) values (?) on duplicate key update name=?");
    }

    @Test
    public void getUpsertArgs() {
        Query query = new Query(db);

        StandardSqlMakerTest.TestTable testTable = new StandardSqlMakerTest.TestTable();
        testTable.setId(2);
        testTable.setName("test");

        Object[] upsertArgs = sut.getUpsertArgs(query, testTable);

        assertEquals(upsertArgs.length, 2);
        assertArrayEquals(upsertArgs, new Object[] { "test", "test" });
    }

    @Test
    public void makeUpsertSql() {
        StandardSqlMakerTest.TestTable testTable = new StandardSqlMakerTest.TestTable();
        testTable.setId(2);
        testTable.setName("test");

        StandardPojoInfo pojoInfo = sut.getPojoInfo(StandardSqlMakerTest.TestTable.class);
        sut.makeUpsertSql(pojoInfo);

        assertEquals(pojoInfo.upsertSql, "insert into testTable (name) values (?) on duplicate key update name=?");
    }
}