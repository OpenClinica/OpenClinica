package org.akaza.openclinica.web.pform.dto;


public class BindAttributeDTO {
	private String type;
	private boolean readOnly;
	private boolean required;
	private String constraint;
	private String constraintMsg;
	
	public String getType() {
	  return type;
  }
	
	public void setType(String type) {
	  this.type = type;
  }
	
	public boolean isReadOnly() {
	  return readOnly;
  }
	
	public void setReadOnly(boolean readOnly) {
	  this.readOnly = readOnly;
  }
	
	public boolean isRequired() {
	  return required;
  }
	
	public void setRequired(boolean required) {
	  this.required = required;
  }
	
	public String getConstraint() {
	  return constraint;
  }
	
	public void setConstraint(String constraint) {
	  this.constraint = constraint;
  }
	
	public String getConstraintMsg() {
	  return constraintMsg;
  }
	
	public void setConstraintMsg(String constraintMsg) {
	  this.constraintMsg = constraintMsg;
  }
}
