package org.akaza.openclinica.ws.bean;

import org.akaza.openclinica.bean.login.UserAccountBean;

public class SubjectStudyDefinitionBean extends BaseStudyDefinitionBean{

    private String subjectOIDId;
    private String subjectLabel;

    public SubjectStudyDefinitionBean(String studyUniqueId, String siteUniqueId, 
    		UserAccountBean user, String subjectLabel) {
        super(studyUniqueId,  siteUniqueId,  user);
        this.setSubjectLabel(subjectLabel);
       
    }
    
    public SubjectStudyDefinitionBean(String studyUniqueId, UserAccountBean user, String subjectLabel) {
        super(studyUniqueId,    user);
        this.setSubjectLabel(subjectLabel);
      
    }
    

	/**
	 * @param subjectLabel the subjectLabel to set
	 */
	public void setSubjectLabel(String subjectLabel) {
		this.subjectLabel = subjectLabel;
	}

	/**
	 * @return the subjectLabel
	 */
	public String getSubjectLabel() {
		return subjectLabel;
	}

	/**
	 * @param subjectUniqueId the subjectUniqueId to set
	 */
	public void setSubjectOIDId(String subjectUniqueId) {
		this.subjectOIDId = subjectUniqueId;
	}

	/**
	 * @return the subjectUniqueId
	 */
	public String getSubjectOIDId() {
		return subjectOIDId;
	}

  


}

