package es.ssib.otic.epona.openclinica.restful.dto;

import java.util.Date;

/**
 *  Created by SJM on 27/09/2016.
 */
public class CreateStudySubjectDto {

	private Integer subjectId;
	private Date dateOfBirth;
	private char gender;
	private String subjectUniqueIdentifier;

	private String studySubjectLabel;
	private String studySubjectSecondaryLabel;
	private String studySubjectOid;

	private String studyOid;

	public CreateStudySubjectDto() {
	}

	public Integer getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(
		Integer subjectId) {

		this.subjectId =
			subjectId;
	}

	public Date getDateOfBirth() {
		return
			this.dateOfBirth;
	}

	public void setDateOfBirth(
		Date dateOfBirth) {

		this.dateOfBirth =
			dateOfBirth;
	}
	
	public char getGender() {
		return
			this.gender;
	}

	public void setGender(
		char gender) {
		
		this.gender =
			gender;
	}

	public String getSubjectUniqueIdentifier() {
		return
			this.subjectUniqueIdentifier;
	}

	public void setSubjectUniqueIdentifier(
		String subjectUniqueIdentifier) {

		this.subjectUniqueIdentifier =
			subjectUniqueIdentifier;
	}

	public String getStudySubjectLabel() {
		return studySubjectLabel;
	}

	public void setStudySubjectLabel(
		String studySubjectLabel) {

		this.studySubjectLabel =
			studySubjectLabel;
	}

	public String getStudySubjectSecondaryLabel() {
		return studySubjectSecondaryLabel;
	}

	public void setStudySubjectSecondaryLabel(
		String studySubjectSecondaryLabel) {

		this.studySubjectSecondaryLabel =
			studySubjectSecondaryLabel;
	}

	public String getStudySubjectOid() {
		return
			this.studySubjectOid;
	}

	public void setStudySubjectOid(
		String studySubjectOid) {

		this.studySubjectOid =
			studySubjectOid;
	}

	public String getStudyOid() {
		return studyOid;
	}

	public void setStudyOid(
		String studyOid) {
		
		this.studyOid =
			studyOid;
	}
}

