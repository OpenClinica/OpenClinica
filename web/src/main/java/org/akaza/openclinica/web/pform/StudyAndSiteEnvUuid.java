package org.akaza.openclinica.web.pform;


import org.akaza.openclinica.domain.user.UserAccount;

public class StudyAndSiteEnvUuid {
    String studyEnvUuid;
    String siteEnvUuid;
    UserAccount currentUser;

    public String getStudyEnvUuid() {
        return studyEnvUuid;
    }

    public void setStudyEnvUuid(String studyEnvUuid) {
        this.studyEnvUuid = studyEnvUuid;
    }

    public String getSiteEnvUuid() {
        return siteEnvUuid;
    }

    public void setSiteEnvUuid(String siteEnvUuid) {
        this.siteEnvUuid = siteEnvUuid;
    }

    public UserAccount getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserAccount currentUser) {
        this.currentUser = currentUser;
    }
}