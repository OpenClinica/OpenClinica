/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2009 Akaza Research 
 */
package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.login.UserAccountBean;

import java.util.Date;

/**
 * @author Krikor Krumlian
 */
public class SubjectTransferBean extends EntityBean {

    private static final long serialVersionUID = 2270466335721404526L;
    private StudyBean study;
    private String personId;
    private String studySubjectId;
    private Date dateOfBirth;
    private String yearOfBirth;
    private char gender;
    private String studyOid;
    private String studyUniqueIdentifier;
    private Date dateReceived;
    private Date enrollmentDate;
    private String secondaryId;
    private String siteIdentifier;
    private StudyBean siteStudy;

    UserAccountBean owner;

    public SubjectTransferBean() {
        // TODO Auto-generated constructor stub
    }

    public SubjectTransferBean(String personId, String studySubjectId, Date dateOfBirth, char gender, String studyOid) {
        super();
        this.personId = personId;
        this.studySubjectId = studySubjectId;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.studyOid = studyOid;
        this.dateReceived = new Date();
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getStudySubjectId() {
        return studySubjectId;
    }

    public void setStudySubjectId(String studySubjectId) {
        this.studySubjectId = studySubjectId;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public char getGender() {
        return gender;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    public String getStudyOid() {
        return studyOid;
    }

    public void setStudyOid(String studyOid) {
        this.studyOid = studyOid;
    }

    public Date getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(Date dateReceived) {
        this.dateReceived = dateReceived;
    }

    public UserAccountBean getOwner() {
        return owner;
    }

    public void setOwner(UserAccountBean owner) {
        this.owner = owner;
    }

    public Date getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(String yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public String getStudyUniqueIdentifier() {
        return studyUniqueIdentifier;
    }

    public void setStudyUniqueIdentifier(String studyUniqueIdentifier) {
        this.studyUniqueIdentifier = studyUniqueIdentifier;
    }

    public String getSecondaryId() {
        return secondaryId;
    }

    public void setSecondaryId(String secondaryId) {
        this.secondaryId = secondaryId;
    }

    public String getSiteIdentifier() {
        return siteIdentifier;
    }

    public void setSiteIdentifier(String siteIdentifier) {
        this.siteIdentifier = siteIdentifier;
    }

    public StudyBean getStudy() {
        return study;
    }

    public void setStudy(StudyBean study) {
        this.study = study;
    }

	public StudyBean getSiteStudy() {
		return siteStudy;
	}

	public void setSiteStudy(StudyBean siteStudy) {
		this.siteStudy = siteStudy;
	}

}