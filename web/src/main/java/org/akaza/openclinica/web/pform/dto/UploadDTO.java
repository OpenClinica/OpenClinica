package org.akaza.openclinica.web.pform.dto;


public class UploadDTO extends BindAttributeDTO {
	private String ref;
	private String mediaType;
	private LabelDTO label;
	private HintDTO hint;
	
	public String getRef() {
	  return ref;
  }
	public void setRef(String ref) {
	  this.ref = ref;
  }
	public String getMediaType() {
	  return mediaType;
  }
	public void setMediaType(String mediaType) {
	  this.mediaType = mediaType;
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
	@Override
	public String toString() {
		String temp = "<upload mediatype=" +mediaType+ ">";
		if (label != null) {
			temp = temp + " " + label.toString();
		}
		if (hint != null) {
			temp = temp + " " + hint.toString();
		}
		temp = temp + "</upload>";
	  return temp;
	}
}
