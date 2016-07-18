package org.akaza.openclinica.bean.login;

public class ResponseSuccessEventDefDTO {

	private String name;
	private String message;
	private String eventDefOid;

	public String getEventDefOid() {
		return eventDefOid;
	}

	public void setEventDefOid(String eventDefOid) {
		this.eventDefOid = eventDefOid;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
