package org.akaza.openclinica.web.pform.dto;


public class Item {
	private Label label;
	private String value;
	
	public Label getLabel() {
	  return label;
  }
	public void setLabel(Label label) {
	  this.label = label;
  }
	public String getValue() {
	  return value;
  }
	public void setValue(String value) {
	  this.value = value;
  }
	
	@Override
	public String toString() {
		String temp = "<item>";
		if (label != null) {
			temp = temp + " " + label.toString();
		}
		temp = temp + "<value>" +value+ "</value></item>";
	  return temp;
	}
}
