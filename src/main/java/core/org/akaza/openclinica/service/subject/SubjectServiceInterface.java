package core.org.akaza.openclinica.service.subject;

import core.org.akaza.openclinica.bean.managestudy.StudyBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.SubjectBean;

import java.util.Date;
import java.util.List;

public interface SubjectServiceInterface {

    public abstract String createSubject(SubjectBean subjectBean, StudyBean studyBean, Date enrollmentDate, String secondaryId);

    public List<StudySubjectBean> getStudySubject(StudyBean study);

}