package org.akaza.openclinica.web.pform.dto;

import java.util.List;


public class SelectDTO extends BindAttributeDTO{
	private String ref;
	private LabelDTO label;
	private HintDTO hint;
	private List<ItemDTO> item;
	
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
	public void setHint(HintDTO hint) {
	  this.hint = hint;
  }
	public List<ItemDTO> getItem() {
	  return item;
  }
	public void setItem(List<ItemDTO> item) {
	  this.item = item;
  }
	
	@Override
	public String toString() {
	  String temp = "<select>";
	  if (label != null) {
	  	temp = temp + " " + label.toString();
	  }
	  if (hint != null) {
	  	temp = temp + " " + hint.toString();
	  }
	  if (item != null){
	  	for (int i = 0; i < item.size(); i++) {
	  		temp = temp + " " + item.get(i).toString();
	  	}
	  }
	  temp = temp + "</select>";
		return temp;
	}
}
