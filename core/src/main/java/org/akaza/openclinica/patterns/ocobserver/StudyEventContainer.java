package org.akaza.openclinica.patterns.ocobserver;

import org.akaza.openclinica.domain.datamap.StudyEvent;

public class StudyEventContainer {
	private StudyEvent event = null;
	private StudyEventChangeDetails changeDetails = null;
	
	public StudyEventContainer(StudyEvent event, StudyEventChangeDetails changeDetails)
	{
		this.event = event;
		this.changeDetails = changeDetails;
	}

	public StudyEvent getEvent() {
		return event;
	}

	public void setEvent(StudyEvent event) {
		this.event = event;
	}

	public StudyEventChangeDetails getChangeDetails() {
		return changeDetails;
	}

	public void setChangeDetails(StudyEventChangeDetails changeDetails) {
		this.changeDetails = changeDetails;
	}

}
