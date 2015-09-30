package org.akaza.openclinica.bean.login;

import java.util.ArrayList;

public class EventDefinitionDTO {

	private String name;
	private String description;
	private String category;
	private String type;
	private String repeating;
	private ArrayList<ErrorObject> errors;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getRepeating() {
		return repeating;
	}

	public void setRepeating(String repeating) {
		this.repeating = repeating;
	}

	public ArrayList<ErrorObject> getErrors() {
		return errors;
	}

	public void setErrors(ArrayList<ErrorObject> errors) {
		this.errors = errors;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
