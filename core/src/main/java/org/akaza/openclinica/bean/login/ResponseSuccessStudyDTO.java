package org.akaza.openclinica.bean.login;

public class ResponseSuccessStudyDTO {

	private String message;
	private String uniqueStudyID;
	private String studyOid;
	private String schemaName;

	public String getUniqueStudyID() {
		return uniqueStudyID;
	}

	public void setUniqueStudyID(String uniqueStudyID) {
		this.uniqueStudyID = uniqueStudyID;
	}

	public String getStudyOid() {
		return studyOid;
	}

	public void setStudyOid(String studyOid) {
		this.studyOid = studyOid;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
}
