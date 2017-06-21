package es.ssib.otic.epona.openclinica.restful.dto;

import org.akaza.openclinica.domain.datamap.StudySubject;

import java.util.Date;

/**
 *  Created by SJM on 27/09/2016.
 */
public class StudySubjectDto {

	private int studySubjectId;
	private String studySubjectLabel;
	private String studySubjectSecondaryLabel;
	private Date studySubjectEnrollment;
	private String ocOid;

	private int studyId;
	private String studyName;

	private int subjectId;
	private String subjectUid;

	public StudySubjectDto() {
	}

	public StudySubjectDto(
		StudySubject studySubject) {

		if (studySubject == null) {
			return;
		}

		this.studySubjectId =
			studySubject.
				getStudySubjectId();
		this.studySubjectLabel =
			studySubject.
				getLabel();
		this.studySubjectSecondaryLabel =
			studySubject.
				getSecondaryLabel();
		this.studySubjectEnrollment =
			studySubject.
				getEnrollmentDate();
		this.ocOid =
			studySubject.
				getOcOid();

		if (studySubject.getStudy() != null) {
			this.studyId =
				studySubject.
					getStudy().
					getStudyId();
			this.studyName =
				studySubject.
					getStudy().
					getName();
		}

		if (studySubject.getSubject() != null) {
			this.subjectId =
				studySubject.
					getSubject().
					getSubjectId();
			this.subjectUid =
				studySubject.
					getSubject().
					getUniqueIdentifier();
		}
	}
	
	public int getStudySubjectId() {
		return studySubjectId;
	}

	public void setStudySubjectId(int studySubjectId) {
		this.studySubjectId = studySubjectId;
	}

	public String getStudySubjectLabel() {
		return studySubjectLabel;
	}

	public void setStudySubjectLabel(String studySubjectLabel) {
		this.studySubjectLabel = studySubjectLabel;
	}

	public String getStudySubjectSecondaryLabel() {
		return studySubjectSecondaryLabel;
	}

	public void setStudySubjectSecondaryLabel(String studySubjectSecondaryLabel) {
		this.studySubjectSecondaryLabel = studySubjectSecondaryLabel;
	}

	public Date getStudySubjectEnrollment() {
		return studySubjectEnrollment;
	}

	public void setStudySubjectEnrollment(Date studySubjectEnrollment) {
		this.studySubjectEnrollment = studySubjectEnrollment;
	}

	public String getOcOid() {
		return ocOid;
	}

	public void setOcOid(String ocOid) {
		this.ocOid = ocOid;
	}

	public int getStudyId() {
		return studyId;
	}

	public void setStudyId(int studyId) {
		this.studyId = studyId;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	public int getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(int subjectId) {
		this.subjectId = subjectId;
	}

	public String getSubjectUid() {
		return subjectUid;
	}

	public void setSubjectUid(String subjectUid) {
		this.subjectUid = subjectUid;
	}
}

