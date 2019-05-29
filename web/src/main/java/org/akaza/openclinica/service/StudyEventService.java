package org.akaza.openclinica.service;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.bean.login.RestReponseDTO;
import org.akaza.openclinica.bean.login.UserAccountBean;


public interface StudyEventService {

	RestReponseDTO scheduleStudyEvent(HttpServletRequest request, String studyOID, String siteOID,String studyEventOID,String participantId, String startDate,String endDate);

	RestReponseDTO scheduleStudyEvent(UserAccountBean ub, String studyOID, String siteOID, String studyEventOID,
			String participantId,  String startDate, String endDate);
	
}
