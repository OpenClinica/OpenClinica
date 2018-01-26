package org.akaza.openclinica.bean.login;

public class ErrorObject {

private String resource;
private String field;
private String code;
public String getResource() {
	return resource;
}
public void setResource(String resource) {
	this.resource = resource;
}
public String getField() {
	return field;
}
public void setField(String field) {
	this.field = field;
}
public String getCode() {
	return code;
}
public void setCode(String code) {
	this.code = code;
}

	@Override
	public String toString() {
		return "Code:" + code + " resource:" + resource + " field:" + field;
	}
}
