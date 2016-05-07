package com.dieselpoint.norm;

import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.concurrent.Callable;

/**
 * Created by Huang on 2016/5/7.
 */
public class TransactionTest {
    @Test
    public void test() {
        Setup.setSysProperties();

        final Database db = new Database();
        db.createTable(Row.class);

        db.transaction(new Runnable() {
            @Override
            public void run() {
                Row row = new Row();
                row.name = "tranaction test";
                db.insert(row);
                db.insert(row);
                db.update(row);
                db.delete(row);
            }
        });
    }

    @Table(name="test")
    public static class Row {
        @Column(unique=true)
        @Id
        @GeneratedValue
        public long id;
        public String name;
        public String toString() {
            return id + name;
        }
    }
}
