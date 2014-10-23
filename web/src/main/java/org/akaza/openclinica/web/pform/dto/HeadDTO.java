package org.akaza.openclinica.web.pform.dto;

public class HeadDTO {
	
	private String title;
	private ModelDTO model;

	public String getTitle() {
	  return title;
  }

	public void setTitle(String title) {
	  this.title = title;
  }
	
	public ModelDTO getModel() {
	  return model;
  }

	public void setModel(ModelDTO model) {
	  this.model = model;
  }
	
	@Override
	public String toString() {
	  return "<head> <title>"+title+"</title> "+model.toString()+ " </head>";
	}
}