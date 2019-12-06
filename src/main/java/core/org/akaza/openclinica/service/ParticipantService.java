package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.StudyParticipantDetailDTO;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudySubject;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;

public interface ParticipantService {

	 abstract String createParticipant(SubjectTransferBean subjectTransfer,Study currentStudy, String accessToken,String realm, String customerUuid, UserAccountBean userAccountBean, Locale locale) throws Exception;

	 List<StudySubjectBean> getStudySubject(Study study);
	 Study validateRequestAndReturnStudy(String studyOid, String siteOid,HttpServletRequest request);
	 UserAccountBean getUserAccount(HttpServletRequest request);	
	
	StudyParticipantDetailDTO buildStudyParticipantDetailDTO(StudySubject studySubject);
}
