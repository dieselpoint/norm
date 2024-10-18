package com.dieselpoint.norm.sqlmakers;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.junit.Before;
import org.junit.Test;

import com.dieselpoint.norm.Database;
import com.dieselpoint.norm.Query;

public class StandardSqlMakerTest {

    StandardSqlMaker sut;
    Database db;

    @Before
    public void setup() {
        sut = new StandardSqlMaker();
        db = mock(Database.class);

        when(db.getSqlMaker()).thenReturn(sut);
    }

    @Test
    public void getInsertSql() {
        Query query = new Query(db);

        TestTable testTable = new TestTable();
        testTable.setId(1);
        testTable.setName("test");

        String insertSql = sut.getInsertSql(query, testTable);

        assertEquals(insertSql, "insert into testTable (name) values (?)");
    }

    @Test
    public void getUpdateSql() {
        Query query = new Query(db);

        TestTable testTable = new TestTable();

        String updateSql = sut.getUpdateSql(query, testTable);

        assertEquals(updateSql, "update testTable set name=? where id=?");
    }

    @Test
    public void getSelectSql() {
        Query query = new Query(db);

        String selectSql = sut.getSelectSql(query, TestTable.class);

        assertEquals(selectSql, "select id,name from testTable");
    }

    @Test
    public void getCreateTableSql() {
        String createTableSql = sut.getCreateTableSql(TestTable.class);

        assertEquals(createTableSql, "create table testTable (id integer auto_increment,name varchar(255), primary key (id))");
    }

    @Test
    public void getDeleteSql() {
        TestTable testTable = new TestTable();

        String deleteSql = sut.getDeleteSql(new Query(db), testTable);

        assertEquals(deleteSql, "delete from testTable where id=?");
    }

    @Test
    public void getUpdateArgs() {
        Query query = new Query(db);

        TestTable testTable = new TestTable();
        testTable.setId(1);
        testTable.setName("test");

        Object[] updateArgs = sut.getUpdateArgs(query, testTable);

        assertEquals(updateArgs.length,2);

        assertArrayEquals(updateArgs, new Object[] {"test", 1});
    }

    @Test
    public void getInsertArgs() {
        Query query = new Query(db);

        TestTable testTable = new TestTable();
        testTable.setId(1);
        testTable.setName("test");

        Object[] insertArgs = sut.getUpdateArgs(query, testTable);

        assertEquals(insertArgs.length,2);

        assertArrayEquals(insertArgs, new Object[] {"test", 1});
    }

    @Test
    public void getDeleteArgs() {
        Query query = new Query(db);

        TestTable testTable = new TestTable();
        testTable.setId(1);
        testTable.setName("test");

        Object[] deleteArgs = sut.getDeleteArgs(query, testTable);

        assertEquals(deleteArgs.length,1);

        assertArrayEquals(deleteArgs, new Object[] { 1 });
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getUpsertArgs() {
        sut.getUpsertArgs(new Query(db), new TestTable());
    }

    @Table(name = "testTable")
    static class TestTable {
        private int id;
        private String name;

        @Id
        @GeneratedValue
        @Column(name = "id")
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Column(name = "name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}