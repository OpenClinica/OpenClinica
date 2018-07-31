package org.akaza.openclinica.service.participant;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.exception.OpenClinicaException;

public interface ParticipantService {

	 public abstract String createParticipant(SubjectTransferBean subjectTransfer,StudyBean currentStudy) throws OpenClinicaException;

	 public List<StudySubjectBean> getStudySubject(StudyBean study);
	 public StudyBean validateRequestAndReturnStudy(String studyOid, String siteOid,HttpServletRequest request);
	 public UserAccountBean getUserAccount(HttpServletRequest request);
}
