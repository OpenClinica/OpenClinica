package org.akaza.openclinica.service;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.domain.datamap.JobDetail;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

public interface ParticipantService {

	 abstract String createParticipant(SubjectTransferBean subjectTransfer,StudyBean currentStudy, String accessToken, String customerUuid, UserAccountBean userAccountBean, Locale locale) throws Exception;

	 List<StudySubjectBean> getStudySubject(StudyBean study);
	 StudyBean validateRequestAndReturnStudy(String studyOid, String siteOid,HttpServletRequest request);
	 UserAccountBean getUserAccount(HttpServletRequest request);
	void processBulkParticipants(StudyBean study, String studyOid, StudyBean siteStudy, String siteOid,
								 UserAccountBean user, String accessToken, String customerUuid, MultipartFile file,
								 JobDetail jobDetail, Locale locale, String uri, Map<String, Object> map);
}
