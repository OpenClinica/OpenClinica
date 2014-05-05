package org.akaza.openclinica.patterns.ocobserver;

import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.springframework.context.ApplicationEvent;
/**
 * The StudyEvent is propagated here on change/update to studyeventdao..
 * @author jnyayapathi
 *
 */
public abstract class OnStudyEventChanged extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private  StudyEvent studyEvent;
	public OnStudyEventChanged(StudyEvent source) {
		super(source);
		this.setStudyEvent(source);
		
		
	}
	public StudyEvent getStudyEvent() {
		return studyEvent;
	}
	public void setStudyEvent(StudyEvent studyEvent) {
		this.studyEvent = studyEvent;
	}

}
