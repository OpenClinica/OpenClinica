package org.akaza.openclinica.web.pform.dto;

public class InputDTO extends BindAttributeDTO{
	private String ref;
	private LabelDTO label;
	private HintDTO hint;
	
	public String getRef() {
	  return ref;
  }
	
	public void setRef(String ref) {
	  this.ref = ref;
  }
	
	public LabelDTO getLabel() {
	  return label;
  }
	
	public void setLabel(LabelDTO label) {
	  this.label = label;
  }
	
	public HintDTO getHint() {
	  return hint;
  }
	
	public void setHint(HintDTO label) {
	  this.hint = label;
  }
	
	@Override
	public String toString() {
		String temp = "<input> ";
		if (label != null) {
			temp = temp + " " + label.toString();
		}
		if (hint!= null) {
			temp = temp + " " + hint.toString();
		}
		temp = temp + "</input>";
	  return temp;
	}
}