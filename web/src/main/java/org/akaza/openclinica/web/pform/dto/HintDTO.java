package org.akaza.openclinica.web.pform.dto;


public class HintDTO {
	private String ref;
	private String hint;
	
	public String getRef() {
	  return ref;
  }
	public void setRef(String ref) {
	  this.ref = ref;
  }
	public String getHint() {
	  return hint;
  }
	public void setHint(String hint) {
	  this.hint = hint;
  }
	
	@Override
	public String toString() {
		return "<hint>" +hint+ "</hint>";
	}
}
