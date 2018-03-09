/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2008 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.submit.crfdata;

import java.util.ArrayList;
import java.util.List;

import org.akaza.openclinica.domain.datamap.StudySubject;

/**
 * OpenClinica subject attributes have been included in addition to ODM
 * SubjectData attributes
 * 
 * @author ywang (Nov, 2008)
 */

public class ExportSubjectDataBean extends SubjectDataBean {
    private String studySubjectId;
    private String uniqueIdentifier;
    private String status;
    private String secondaryId;
    private Integer yearOfBirth;
    private String dateOfBirth;
    private String subjectGender;
    private StudySubject studySubject;

    private String enrollmentDate = "";
    
    private List<ExportStudyEventDataBean> exportStudyEventData;
    private List<SubjectGroupDataBean> subjectGroupData;

    public ExportSubjectDataBean() {
        super();
        this.exportStudyEventData = new ArrayList<ExportStudyEventDataBean>();
        this.subjectGroupData = new ArrayList<SubjectGroupDataBean>();
    }

    public void setStudySubjectId(String studySubjectId) {
        this.studySubjectId = studySubjectId;
    }

    public String getStudySubjectId() {
        return this.studySubjectId;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public String getUniqueIdentifier() {
        return this.uniqueIdentifier;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public void setSecondaryId(String secondaryId) {
        this.secondaryId = secondaryId;
    }

    public String getSecondaryId() {
        return this.secondaryId;
    }

    public void setYearOfBirth(Integer yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public Integer getYearOfBirth() {
        return this.yearOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDateOfBirth() {
        return this.dateOfBirth;
    }

    public void setSubjectGender(String gender) {
        this.subjectGender = gender;
    }

    public String getSubjectGender() {
        return this.subjectGender;
    }

    public List<ExportStudyEventDataBean> getExportStudyEventData() {
        return exportStudyEventData;
    }

    public void setExportStudyEventData(List<ExportStudyEventDataBean> studyEventData) {
        this.exportStudyEventData = studyEventData;
    }

    public void setSubjectGroupData(List<SubjectGroupDataBean> subjectGroupData) {
        this.subjectGroupData = subjectGroupData;
    }

    public List<SubjectGroupDataBean> getSubjectGroupData() {
        return this.subjectGroupData;
    }

    public String getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(String enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public StudySubject getStudySubject() {
        return studySubject;
    }

    public void setStudySubject(StudySubject studySubject) {
        this.studySubject = studySubject;
    }

}
