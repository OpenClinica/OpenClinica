/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
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
		
	
}
