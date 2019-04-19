package com.dieselpoint.norm.sqlmakers;

import com.dieselpoint.norm.Database;
import com.dieselpoint.norm.Query;
import com.dieselpoint.norm.TestGeneratedId;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        assertEquals(insertSql, "insert into testTable (id,name) values (?,?)");
    }

    @Test
    public void getUpdateSql() {
        Query query = new Query(db);

        TestTable testTable = new TestTable();
        testTable.setId(1);
        testTable.setName("test");

        String updateSql = sut.getUpdateSql(query, testTable);

        assertEquals(updateSql, "update testTable set name=? where id=?");
    }

    @Test
    public void getSelectSql() {
        Query query = new Query(db);

        String selectSql = sut.getSelectSql(query, TestTable.class);

        assertEquals(selectSql, "select id,name from testTable");
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