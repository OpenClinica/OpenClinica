package org.akaza.openclinica.web.pform.dto;

import java.util.List;

public abstract class UserControl {

	private String ref;
	private String mediatype;
	private String appearance;
	private Label label = null;
	private Hint hint = null;
	private List<Item> item;
	
	public String getRef() {
	  return ref;
  }
	public void setRef(String ref) {
	  this.ref = ref;
  }

	public String getAppearance() {
		return appearance;
	}
	public void setAppearance(String appearance) {
		this.appearance = appearance;
	}
	public String getMediatype() {
		return mediatype;
	}
	public void setMediatype(String mediatype) {
		this.mediatype = mediatype;
	}

	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label) {
		this.label = label;
	}
	public Hint getHint() {
		return hint;
	}
	public void setHint(Hint hint) {
		this.hint = hint;
	}
	public List<Item> getItem() {
		  return item;
	}
	public void setItem(List<Item> item) {
	  this.item = item;
	}
}
