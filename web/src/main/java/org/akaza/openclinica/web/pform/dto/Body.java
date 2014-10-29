package org.akaza.openclinica.web.pform.dto;

import java.util.List;

public class Body {
	private String cssClass = null;
	private String appearance = null;
	private List<Group> group;

	public List<Group> getGroup() {
	  return group;
  }

	public void setGroup(List<Group> group) {
	  this.group = group;
  }
	
	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public String getAppearance() {
		return appearance;
	}

	public void setAppearance(String appearance) {
		this.appearance = appearance;
	}
	@Override
	public String toString() {
		String temp = "<body> ";
		if (group != null) {
			for (int i = 0; i < group.size(); i++) {
				temp = temp + " " + group.get(i).toString();
			}
		}		
		temp = temp + " </body>";
	  return temp;
	}
}