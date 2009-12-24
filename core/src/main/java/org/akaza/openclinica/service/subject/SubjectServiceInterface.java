package org.akaza.openclinica.service.subject;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.submit.SubjectBean;

import java.util.Date;

public interface SubjectServiceInterface {

    public abstract String createSubject(SubjectBean subjectBean, StudyBean studyBean, Date enrollmentDate, String secondaryId);

    public abstract boolean validate(SubjectTransferBean subjectTransferBean);

}