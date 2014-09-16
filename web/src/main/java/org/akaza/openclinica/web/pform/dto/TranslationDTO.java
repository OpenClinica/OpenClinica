package org.akaza.openclinica.web.pform.dto;

import java.util.List;


public class TranslationDTO {
	private String lang;
	private List<TextDTO> text;
	
	public String getLang() {
	  return lang;
  }
	
	public void setLang(String lang) {
	  this.lang = lang;
  }
	
	public List<TextDTO> getText() {
	  return text;
  }
	
	public void setText(List<TextDTO> text) {
	  this.text = text;
  }
	
	public TextDTO getTextById(String id) {
		if (text != null) {
			for (int i = 0; i < text.size(); i++) {
				if (text.get(i).getId().equals(id)) {
					return text.get(i);
				}
			}
			return null;
		}else {
			return null;
		}
	}
	
	@Override
	public String toString() {
	  String temp = "<translation lang=" +lang+ ">";
	  if (text != null) {
	  	temp = temp + " " + text.toString(); 
	  }
	  temp = temp + " </translation>";
		return temp;
	}
}
