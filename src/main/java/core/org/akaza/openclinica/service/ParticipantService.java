package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.StudyParticipantDetailDTO;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudyBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import core.org.akaza.openclinica.domain.datamap.StudySubject;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;

public interface ParticipantService {

	 abstract String createParticipant(SubjectTransferBean subjectTransfer,StudyBean currentStudy, String accessToken, String customerUuid, UserAccountBean userAccountBean, Locale locale) throws Exception;

	 List<StudySubjectBean> getStudySubject(StudyBean study);
	 StudyBean validateRequestAndReturnStudy(String studyOid, String siteOid,HttpServletRequest request);
	 UserAccountBean getUserAccount(HttpServletRequest request);	
	
	StudyParticipantDetailDTO buildStudyParticipantDetailDTO(StudySubject studySubject);
}
