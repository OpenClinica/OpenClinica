package core.org.akaza.openclinica.service.subject;

import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.SubjectBean;
import core.org.akaza.openclinica.domain.datamap.Study;

import java.util.Date;
import java.util.List;

public interface SubjectServiceInterface {

    public abstract String createSubject(SubjectBean subjectBean, Study studyBean, Date enrollmentDate, String secondaryId);

    public List<StudySubjectBean> getStudySubject(Study study);

}