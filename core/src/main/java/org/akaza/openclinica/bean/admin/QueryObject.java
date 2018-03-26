package org.akaza.openclinica.bean.admin;

import java.util.ArrayList;

public class QueryObject {
	
	private String sql;
	private ArrayList<SqlParameter> sqlParameters;
	
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public ArrayList<SqlParameter> getSqlParameters() {
		return sqlParameters;
	}
	public void setSqlParameters(ArrayList<SqlParameter> sqlParameter) {
		this.sqlParameters = sqlParameter;
	}
	
	@Override
	public String toString() {
		return "QueryObject [sql=" + sql + ", sqlParameters=" + sqlParameters + "]";
	}
	
	

}
