package org.akaza.openclinica.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;

public interface StudyEventService {

	ResponseEntity<Object> scheduleEvent(HttpServletRequest request, String studyOID, String siteOID,String studyEventOID,String participantId,String sampleOrdinalStr, String startDate,String endDate);
	
}
