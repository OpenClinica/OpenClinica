package org.akaza.openclinica.service.rule;

import org.akaza.openclinica.patterns.ocobserver.OnStudyEventJDBCBeanChanged;
import org.springframework.context.ApplicationListener;

public class StudyEventBeanListener implements ApplicationListener<OnStudyEventJDBCBeanChanged> {

	@Override
	public void onApplicationEvent(OnStudyEventJDBCBeanChanged event) {
System.out.println("Listening to jdbc changes");		
	}

}
