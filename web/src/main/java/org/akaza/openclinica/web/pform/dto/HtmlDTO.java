package org.akaza.openclinica.web.pform.dto;

public class HtmlDTO {
	
	private HeadDTO head;
	private BodyDTO body;
	
	public HtmlDTO() {}
	
	public HtmlDTO(HtmlDTO html) {
		setHead(html.getHead());
		setBody(html.getBody());
	}
	
	public HeadDTO getHead() {
	  return head;
  }
	
	public void setHead(HeadDTO head) {
	  this.head = head;
  }
	
	public BodyDTO getBody() {
	  return body;
  }
	
	public void setBody(BodyDTO body) {
	  this.body = body;
  }
	
	@Override
	public String toString() {
	  return "<html> "+body.toString()+" </html>";
	}
}