package com.dieselpoint.norm.sqlmakers;

import com.dieselpoint.norm.Query;


public class MySqlMaker extends StandardSqlMaker {

	@Override
	public String getUpsertSql(Query query, Object row) {
		StandardPojoInfo pojoInfo = getPojoInfo(row.getClass());
		return pojoInfo.upsertSql;
	}

	@Override
	public Object[] getUpsertArgs(Query query, Object row) {
		
		// same args as insert, but we need to duplicate the values
		Object [] args = super.getInsertArgs(query, row);
		
		int count = args.length;
		
		Object [] upsertArgs = new Object[count * 2];
		System.arraycopy(args, 0, upsertArgs, 0, count);
		System.arraycopy(args, 0, upsertArgs, count, count);
		
		return upsertArgs;
	}
	

	@Override
	public void makeUpsertSql(StandardPojoInfo pojoInfo) {

		// INSERT INTO table (a,b,c) VALUES (1,2,3) ON DUPLICATE KEY UPDATE c=c+1;
		
		// mostly the same as the makeInsertSql code
		// it uses the same column names and argcount

		StringBuilder buf = new StringBuilder();
		buf.append(pojoInfo.insertSql);
		buf.append(" on duplicate key update ");
		
		boolean first = true;
		for (String colName: pojoInfo.insertColumnNames) {
			if (first) {
				first = false;
			} else {
				buf.append(',');
			}
			buf.append(colName);
			buf.append("=?");
		}
		
		pojoInfo.upsertSql = buf.toString();
	}

	
}
