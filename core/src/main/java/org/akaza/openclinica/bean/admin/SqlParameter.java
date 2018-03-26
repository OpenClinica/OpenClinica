package org.akaza.openclinica.bean.admin;

import java.sql.JDBCType;

public class SqlParameter {
	private String value;
	private JDBCType type;
	
	public SqlParameter(String value, JDBCType type) {
		super();
		this.value = value;
		this.type = type;
	}
	
	public SqlParameter(String value) {
		super();
		this.value = value;
		this.type = JDBCType.VARCHAR;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
	@Override
	public String toString() {
		return "SqlParameter [value=" + value + ", type=" + type + "]";
	}

	public JDBCType getType() {
		return type;
	}

	public void setType(JDBCType type) {
		this.type = type;
	}

}
