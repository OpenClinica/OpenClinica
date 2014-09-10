package org.akaza.openclinica.patterns.ocobserver;

public class StudyEventChangeDetails {
	Boolean statusChanged = false;
	Boolean startDateChanged = false;
	
	public StudyEventChangeDetails()
	{
	}
	
	public StudyEventChangeDetails(Boolean statusChanged,Boolean startDateChanged)
	{
		this.statusChanged = statusChanged;
		this.startDateChanged = startDateChanged;
	}

	public Boolean getStatusChanged() {
		return statusChanged;
	}

	public void setStatusChanged(Boolean statusChanged) {
		this.statusChanged = statusChanged;
	}

	public Boolean getStartDateChanged() {
		return startDateChanged;
	}

	public void setStartDateChanged(Boolean startDateChanged) {
		this.startDateChanged = startDateChanged;
	}

	
	
}
