package org.akaza.openclinica.web.pform.dto;


public class ITextDTO {
	private TranslationDTO translation;

	public TranslationDTO getTranslation() {
	  return translation;
  }

	public void setTranslation(TranslationDTO translation) {
	  this.translation = translation;
  }
	
	@Override
	public String toString() {
		String temp = "<itext>";
		if (translation != null) {
			temp = temp + " " + translation.toString();
		}
		temp = temp + "</text>";
	  return temp;
	}
}
