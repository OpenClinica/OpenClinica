package core.org.akaza.openclinica.web.restful.data.bean;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.domain.datamap.Study;

public class BaseStudyDefinitionBean {

    private String studyUniqueId;
    private String siteUniqueId;
    private UserAccountBean user;
    private Study study;

    public BaseStudyDefinitionBean(String studyUniqueId, String siteUniqueId, UserAccountBean user) {
        super();
        this.studyUniqueId = studyUniqueId;
        this.siteUniqueId = siteUniqueId;
        this.user = user;
    }
    
    public BaseStudyDefinitionBean(String studyUniqueId, UserAccountBean user) {
        super();
        this.studyUniqueId = studyUniqueId;
       
        this.user = user;
    }

    public String getStudyUniqueId() {
        return studyUniqueId;
    }

    public void setStudyUniqueId(String studyUniqueId) {
        this.studyUniqueId = studyUniqueId;
    }

    public String getSiteUniqueId() {
        return siteUniqueId;
    }

    public void setSiteUniqueId(String siteUniqueId) {
        this.siteUniqueId = siteUniqueId;
    }

    public UserAccountBean getUser() {
        return user;
    }

    public void setUser(UserAccountBean user) {
        this.user = user;
    }

	/**
	 * @param study the study to set
	 */
	public void setStudy(Study study) {
		this.study = study;
	}

	/**
	 * @return the study
	 */
	public Study getStudy() {
		return study;
	}

}

