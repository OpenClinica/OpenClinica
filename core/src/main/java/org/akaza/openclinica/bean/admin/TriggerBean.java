package org.akaza.openclinica.bean.admin;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.login.UserAccountBean;

import java.util.Date;

public class TriggerBean extends EntityBean {

    private Date previousDate;
    private Date nextDate;
    private String fullName;
    private String groupName;
    private String description = "";
    private DatasetBean dataset = new DatasetBean();
    private UserAccountBean userAccount = new UserAccountBean();

    private String tab;
    private String cdisc;
    private String spss;
    private String exportFormat;
    private String contactEmail;
	private String periodToRun;
	private String datasetName;
	private String studyName;

    public String getPeriodToRun() {
		return periodToRun;
	}

	public void setPeriodToRun(String periodToRun) {
		this.periodToRun = periodToRun;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	public Date getPreviousDate() {
        return previousDate;
    }

    public void setPreviousDate(Date previousDate) {
        this.previousDate = previousDate;
    }

    public Date getNextDate() {
        return nextDate;
    }

    public void setNextDate(Date nextDate) {
        this.nextDate = nextDate;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTab() {
        return tab;
    }

    public void setTab(String tab) {
        this.tab = tab;
    }

    public String getCdisc() {
        return cdisc;
    }

    public void setCdisc(String cdisc) {
        this.cdisc = cdisc;
    }

    public String getSpss() {
        return spss;
    }

    public void setSpss(String spss) {
        this.spss = spss;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public DatasetBean getDataset() {
        return dataset;
    }

    public UserAccountBean getUserAccount() {
        return userAccount;
    }

    public void setDataset(DatasetBean dataset) {
        this.dataset = dataset;
    }

    public void setUserAccount(UserAccountBean userAccount) {
        this.userAccount = userAccount;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }
}
