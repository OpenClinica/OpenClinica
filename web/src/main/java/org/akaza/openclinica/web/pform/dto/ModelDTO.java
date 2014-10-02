
package org.akaza.openclinica.web.pform.dto;

import java.util.List;

public class ModelDTO {
	private List<BindDTO> bind;
	private String instance = "initialvalueinmodeldto";
	private ITextDTO itext;
	
	public String getInstance() {
	  return instance;
  }
	
	public void setInstance(String instance) {
	  this.instance = instance;
  }
	
	public List<BindDTO> getBind() {
	  return bind;
  }
	
	public void setBind(List<BindDTO> bind) {
	  this.bind = bind;
  }
	
	public ITextDTO getItext() {
	  return itext;
  }
	
	public void setItext(ITextDTO itext) {
	  this.itext = itext;
  }
	
	public BindDTO getBindByNodeSet(String nodeSet) {
		if (bind != null) {
			for (int i = 0; i < bind.size(); i++) {
				if (bind.get(i).getNodeSet().equals(nodeSet)) {
					return bind.get(i);
				}
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		String temp = "<model> "+instance;
		if (itext != null) {
			temp = temp + " " + itext.toString();
		}
		if (bind.size() > 0) {
			for (int i = 0; i < bind.size(); i++) {
				temp = temp +" "+bind.get(i).toString();  
			}
		}
		temp = temp +"</model> ";
	  return temp ;
	}
}