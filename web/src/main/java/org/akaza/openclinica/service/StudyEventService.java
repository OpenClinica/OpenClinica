package org.akaza.openclinica.service;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.bean.login.RestReponseDTO;


public interface StudyEventService {

	RestReponseDTO scheduleStudyEvent(HttpServletRequest request, String studyOID, String siteOID,String studyEventOID,String participantId,String sampleOrdinalStr, String startDate,String endDate);
	
}
