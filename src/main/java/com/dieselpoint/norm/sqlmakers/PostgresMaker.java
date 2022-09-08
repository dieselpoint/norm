package com.dieselpoint.norm.sqlmakers;

import javax.persistence.Column;

public class PostgresMaker extends StandardSqlMaker {

	@Override
	public String getCreateTableSql(Class<?> clazz) {
		
		StringBuilder buf = new StringBuilder();

		StandardPojoInfo pojoInfo = getPojoInfo(clazz);
		buf.append("create table ");
		buf.append(pojoInfo.table);
		buf.append(" (");
		
		boolean needsComma = false;
		for (Property prop : pojoInfo.propertyMap.values()) {
			
			if (needsComma) {
				buf.append(',');
			}
			needsComma = true;

			Column columnAnnot = prop.columnAnnotation;
			if (columnAnnot == null) {
	
				buf.append(prop.name);
				buf.append(" ");
				if (prop.isGenerated) {
					buf.append(" serial");
				} else {
					buf.append(getColType(prop.dataType, 255, 10, 2));
				}
				
			} else {
				if (columnAnnot.columnDefinition() == null) {
					
					// let the column def override everything
					buf.append(columnAnnot.columnDefinition());
					
				} else {

					buf.append(prop.name);
					buf.append(" ");
					if (prop.isGenerated) {
						buf.append(" serial");
					} else {
						buf.append(getColType(prop.dataType, columnAnnot.length(), columnAnnot.precision(), columnAnnot.scale()));
					}
					
					if (columnAnnot.unique()) {
						buf.append(" unique");
					}
					
					if (!columnAnnot.nullable()) {
						buf.append(" not null");
					}
				}
			}
		}
		
		if (pojoInfo.primaryKeyNames.size() > 0) {
			buf.append(", primary key (");
			for(int i = 0; i < pojoInfo.primaryKeyNames.size(); i++){
				if(i > 0){
					buf.append(",");
				}
				buf.append(pojoInfo.primaryKeyNames.get(i));
			}
			buf.append(")");
		}
		
		buf.append(")");
		
		return buf.toString();
	}


}
