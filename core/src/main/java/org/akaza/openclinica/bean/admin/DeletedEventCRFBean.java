package org.akaza.openclinica.bean.admin;

import java.util.Date;

/*
 * @author S.M. Shamim 28/01/2008
 */
public class DeletedEventCRFBean {
    private int studyEventId;
    private String crfName;
    private String crfVersion;
    private String deletedBy;
    private Date deletedDate;
    private int deletedEventCrfId;
    
    public int getDeletedEventCrfId() {
		return deletedEventCrfId;
	}

	public void setDeletedEventCrfId(int deletedEventCrfId) {
		this.deletedEventCrfId = deletedEventCrfId;
	}

	public int getStudyEventId() {
        return studyEventId;
    }

    public void setStudyEventId(int studyEventId) {
        this.studyEventId = studyEventId;
    }

    public String getCrfName() {
        return crfName;
    }

    public void setCrfName(String crfName) {
        this.crfName = crfName;
    }

    public String getCrfVersion() {
        return crfVersion;
    }

    public void setCrfVersion(String crfVersion) {
        this.crfVersion = crfVersion;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Date getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(Date deletedDate) {
        this.deletedDate = deletedDate;
    }
}
