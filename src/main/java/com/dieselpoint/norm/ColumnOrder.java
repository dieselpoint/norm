package com.dieselpoint.norm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify the order of the columns. Is used in the create table sql.
 * ColumnOrder({"name","address", ...})
 * 
 * @author ccleve
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ColumnOrder {
	String [] value();
}

