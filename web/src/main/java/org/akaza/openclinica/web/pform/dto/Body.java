package org.akaza.openclinica.web.pform.dto;

import java.util.List;

public class Body {
	private String cssClass = null;
	private String appearance = null;
	private List<Group> group;
	private List<Repeat> repeat;

	
	
	public List<Repeat> getRepeat() {
		return repeat;
	}
	public void setRepeat(List<Repeat> repeat) {
		this.repeat = repeat;
	}
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
}