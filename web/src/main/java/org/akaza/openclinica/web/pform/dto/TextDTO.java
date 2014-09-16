package org.akaza.openclinica.web.pform.dto;


public class TextDTO {
	private String id;
	private String value;
	
	public String getId() {
	  return id;
  }
	public void setId(String id) {
	  this.id = id;
  }
	public String getValue() {
	  return value;
  }
	public void setValue(String value) {
	  this.value = value;
  }
	
	@Override
	public String toString() {
		String temp = " <text id= "+ id +"><value>" + value + "</value></text>";
		return temp;
	}
}
