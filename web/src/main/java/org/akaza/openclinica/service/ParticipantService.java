package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.StudyParticipantDetailDTO;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.domain.datamap.JobDetail;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface ParticipantService {

	 abstract String createParticipant(SubjectTransferBean subjectTransfer,StudyBean currentStudy, String accessToken, String customerUuid, UserAccountBean userAccountBean, Locale locale) throws Exception;

	 List<StudySubjectBean> getStudySubject(StudyBean study);
	 StudyBean validateRequestAndReturnStudy(String studyOid, String siteOid,HttpServletRequest request);
	 UserAccountBean getUserAccount(HttpServletRequest request);	
	
	StudyParticipantDetailDTO buildStudyParticipantDetailDTO(StudySubject studySubject);
}
