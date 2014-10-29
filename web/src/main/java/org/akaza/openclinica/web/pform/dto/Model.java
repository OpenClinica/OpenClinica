
package org.akaza.openclinica.web.pform.dto;

import java.util.List;

public class Model {
	private List<Bind> bind;
	private String instance = "initialvalueinmodeldto";
	
	public String getInstance() {
	  return instance;
  }
	
	public void setInstance(String instance) {
	  this.instance = instance;
  }
	
	public List<Bind> getBind() {
	  return bind;
  }
	
	public void setBind(List<Bind> bind) {
	  this.bind = bind;
  }
		
	public Bind getBindByNodeSet(String nodeSet) {
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
		if (bind.size() > 0) {
			for (int i = 0; i < bind.size(); i++) {
				temp = temp +" "+bind.get(i).toString();  
			}
		}
		temp = temp +"</model> ";
	  return temp ;
	}
}