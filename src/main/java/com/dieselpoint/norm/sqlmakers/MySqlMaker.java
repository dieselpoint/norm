package com.dieselpoint.norm.sqlmakers;

import com.dieselpoint.norm.Query;


public class MySqlMaker extends StandardSqlMaker {

	@Override
	public String getUpsertSql(Query query, Object row) {
		
		
		// INSERT INTO table (a,b,c) VALUES (1,2,3) ON DUPLICATE KEY UPDATE c=c+1;
		//start here
		
		

		// TODO Auto-generated method stub
		return super.getUpsertSql(query, row);
		
		
	}

	@Override
	public Object[] getUpsertArgs(Query query, Object row) {
		
		
		
		
		// TODO Auto-generated method stub
		return super.getUpsertArgs(query, row);
		
		
	}

	
}
