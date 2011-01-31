/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2010-2011 Akaza Research

 * Development of this web service or portions thereof has been funded
 * by Federal Funds from the National Cancer Institute, 
 * National Institutes of Health, under Contract No. HHSN261200800001E.
 * In addition to the GNU LGPL license, this code is also available
 * from NCI CBIIT repositories under the terms of the caBIG Software License. 
 * For details see: https://cabig.nci.nih.gov/adopt/caBIGModelLicense
 */
package org.akaza.openclinica.ws.bean;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import java.util.Date;

public class RegisterSubjectBean {

    private Date dateOfBirth;
    private String gender;
    private String uniqueIdentifier;
    private String studyUniqueIdentifier;
    private String siteUniqueIdentifier; //NR?
    private Date enrollmentDate;
    private String studySubjectLabel;
    private String requestorId;
    private StudyBean studyBean;
    private UserAccountBean user; // auditing
    
    public RegisterSubjectBean(UserAccountBean user) {
        this.user = user;
        studyBean = new StudyBean();
    }
    
    public StudyBean getStudyBean() {
        return studyBean;
    }

    public void setStudyBean(StudyBean studyBean) {
        this.studyBean = studyBean;
    }

    public UserAccountBean getUser() {
        return user;
    }

    public void setUser(UserAccountBean user) {
        this.user = user;
    }
   
    public Date getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }
    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }
    public String getStudyUniqueIdentifier() {
        return studyUniqueIdentifier;
    }
    public void setStudyUniqueIdentifier(String studyUniqueIdentifier) {
        this.studyUniqueIdentifier = studyUniqueIdentifier;
    }
    public String getSiteUniqueIdentifier() {
        return siteUniqueIdentifier;
    }
    public void setSiteUniqueIdentifier(String siteUniqueIdentifier) {
        this.siteUniqueIdentifier = siteUniqueIdentifier;
    }
    public Date getEnrollmentDate() {
        return enrollmentDate;
    }
    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
    public String getStudySubjectLabel() {
        return studySubjectLabel;
    }
    public void setStudySubjectLabel(String studySubjectLabel) {
        this.studySubjectLabel = studySubjectLabel;
    }
    public String getRequestorId() {
        return requestorId;
    }
    public void setRequestorId(String requestorId) {
        this.requestorId = requestorId;
    }
    
    
}
