package org.akaza.openclinica.bean.login;

import java.util.ArrayList;
import java.util.Date;

public class ResponseStudyParticipantsBulkDTO {

	private int	uploadCount;
	private int	failureCount;
	private String  message;
	private String createdBy;
	private String createdAt;

	
	ArrayList<ResponseFailureStudyParticipantSingleDTO> failedParticipants;
	ArrayList<ResponseSuccessStudyParticipantDTO> participants;
	
	public int getUploadCount() {
		return uploadCount;
	}
	public void setUploadCount(int uploadCount) {
		this.uploadCount = uploadCount;
	}
	public int getFailureCount() {
		return failureCount;
	}
	public void setFailureCount(int failureCount) {
		this.failureCount = failureCount;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public ArrayList<ResponseFailureStudyParticipantSingleDTO> getFailedParticipants() {
		if(failedParticipants == null) {
			failedParticipants = new ArrayList();
		}
		return failedParticipants;
	}
	public void setFailedParticipants(ArrayList<ResponseFailureStudyParticipantSingleDTO> failedParticipants) {
		this.failedParticipants = failedParticipants;
	}
	public ArrayList<ResponseSuccessStudyParticipantDTO> getParticipants() {
		if(participants == null) {
			participants = new ArrayList();
		}
		return participants;
	}
	public void setParticipants(ArrayList<ResponseSuccessStudyParticipantDTO> participants) {
		this.participants = participants;
	}

}
